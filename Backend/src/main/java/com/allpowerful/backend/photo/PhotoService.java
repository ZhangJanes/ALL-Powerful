package com.allpowerful.backend.photo;

import com.allpowerful.backend.common.AppException;
import com.allpowerful.backend.common.DomainEventPublisher;
import com.allpowerful.backend.config.KafkaTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhotoService {
    private final PhotoLibraryRepository photoLibraryRepository;
    private final PhotoRepository photoRepository;
    private final PasswordEncoder passwordEncoder;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional(readOnly = true)
    public List<PhotoDtos.LibraryResponse> listLibraries(Long userId) {
        return photoLibraryRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toLibraryResp)
                .toList();
    }

    @Transactional
    public PhotoDtos.LibraryResponse createLibrary(Long userId, PhotoDtos.LibraryRequest req) {
        if (photoLibraryRepository.existsByUserIdAndName(userId, req.name())) throw new AppException("库名称已存在");
        PhotoLibrary lib = new PhotoLibrary();
        lib.setUserId(userId);
        lib.setName(req.name());
        lib.setVisibility(req.visibility());
        if ("private".equals(req.visibility())) {
            if (req.password() == null || req.password().isBlank()) throw new AppException("私有库必须设置密码");
            lib.setPasswordHash(passwordEncoder.encode(req.password()));
        }
        lib.setCreatedAt(LocalDateTime.now());
        lib.setUpdatedAt(LocalDateTime.now());
        photoLibraryRepository.save(lib);
        return toLibraryResp(lib);
    }

    @Transactional(readOnly = true)
    public boolean verifyLibrary(Long userId, Long libraryId, String password) {
        PhotoLibrary lib = photoLibraryRepository.findByIdAndUserId(libraryId, userId).orElseThrow(() -> new AppException("库不存在"));
        if ("public".equals(lib.getVisibility())) return true;
        return password != null && passwordEncoder.matches(password, lib.getPasswordHash());
    }

    @Transactional(readOnly = true)
    public List<PhotoDtos.PhotoResponse> listLibraryPhotos(Long userId, Long libraryId) {
        PhotoLibrary lib = photoLibraryRepository.findByIdAndUserId(libraryId, userId).orElseThrow(() -> new AppException("库不存在"));
        return photoRepository.findByUserIdAndLibraryIdOrderByCreatedAtDesc(userId, lib.getId()).stream().map(this::toPhotoResp).toList();
    }

    @Transactional(readOnly = true)
    public List<PhotoDtos.PhotoResponse> listPublicPhotos(Long userId) {
        List<Long> libIds = photoLibraryRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(x -> "public".equals(x.getVisibility()))
                .map(PhotoLibrary::getId)
                .toList();
        if (libIds.isEmpty()) return List.of();
        return photoRepository.findByUserIdAndLibraryIdInOrderByCreatedAtDesc(userId, libIds).stream().map(this::toPhotoResp).toList();
    }

    @Transactional
    public List<PhotoDtos.PhotoResponse> upload(Long userId, PhotoDtos.UploadRequest req) {
        PhotoLibrary lib = photoLibraryRepository.findByIdAndUserId(req.libraryId(), userId).orElseThrow(() -> new AppException("库不存在"));
        if ("private".equals(lib.getVisibility()) && (req.password() == null || !passwordEncoder.matches(req.password(), lib.getPasswordHash()))) {
            throw new AppException("私有库密码错误");
        }
        if (req.items().size() > 99) throw new AppException("单次最多上传 99 张");
        List<Photo> entities = req.items().stream().map(i -> {
            Photo p = new Photo();
            p.setUserId(userId);
            p.setLibraryId(lib.getId());
            p.setName(i.name());
            p.setCategory(i.category());
            p.setLocked("private".equals(lib.getVisibility()));
            p.setObjectKey(i.objectKey());
            p.setAtDate(i.atDate());
            p.setCreatedAt(LocalDateTime.now());
            return p;
        }).toList();
        List<PhotoDtos.PhotoResponse> saved = photoRepository.saveAll(entities).stream().map(this::toPhotoResp).toList();
        domainEventPublisher.publish(KafkaTopics.PHOTO_UPLOADED, "{\"libraryId\":" + lib.getId() + ",\"count\":" + saved.size() + "}");
        return saved;
    }

    private PhotoDtos.LibraryResponse toLibraryResp(PhotoLibrary lib) {
        return new PhotoDtos.LibraryResponse(lib.getId(), lib.getName(), lib.getVisibility(), lib.getCreatedAt());
    }

    private PhotoDtos.PhotoResponse toPhotoResp(Photo photo) {
        return new PhotoDtos.PhotoResponse(
                photo.getId(),
                photo.getLibraryId(),
                photo.getName(),
                photo.getCategory(),
                photo.getLocked(),
                photo.getObjectKey(),
                photo.getAtDate()
        );
    }
}
