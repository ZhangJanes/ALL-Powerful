package com.allpowerful.backend.idea;

import com.allpowerful.backend.common.AppException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IdeaService {
    private final IdeaRepository ideaRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<IdeaDtos.IdeaResponse> list(Long userId) {
        return ideaRepository.findByUserIdAndDeletedAtIsNullOrderByUpdatedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<IdeaDtos.IdeaResponse> trash(Long userId) {
        return ideaRepository.findByUserIdAndDeletedAtIsNotNullOrderByDeletedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public IdeaDtos.IdeaResponse create(Long userId, IdeaDtos.IdeaRequest req) {
        Idea entity = new Idea();
        patch(entity, userId, req);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        ideaRepository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public IdeaDtos.IdeaResponse update(Long userId, Long id, IdeaDtos.IdeaRequest req) {
        Idea entity = mustOwn(userId, id);
        patch(entity, userId, req);
        entity.setUpdatedAt(LocalDateTime.now());
        ideaRepository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Idea entity = mustOwn(userId, id);
        entity.setDeletedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        ideaRepository.save(entity);
    }

    @Transactional
    public void restore(Long userId, Long id) {
        Idea entity = mustOwn(userId, id);
        entity.setDeletedAt(null);
        entity.setUpdatedAt(LocalDateTime.now());
        ideaRepository.save(entity);
    }

    @Transactional
    public void deletePermanent(Long userId, Long id) {
        Idea entity = mustOwn(userId, id);
        ideaRepository.delete(entity);
    }

    @Transactional
    public IdeaDtos.IdeaResponse toggleStar(Long userId, Long id) {
        Idea entity = mustOwn(userId, id);
        entity.setStarred(!Boolean.TRUE.equals(entity.getStarred()));
        entity.setUpdatedAt(LocalDateTime.now());
        ideaRepository.save(entity);
        return toResponse(entity);
    }

    private Idea mustOwn(Long userId, Long id) {
        Idea entity = ideaRepository.findByIdAndUserId(id, userId).orElseThrow(() -> new AppException("灵感不存在"));
        if (!entity.getUserId().equals(userId)) throw new AppException("无权限");
        return entity;
    }

    private void patch(Idea entity, Long userId, IdeaDtos.IdeaRequest req) {
        entity.setUserId(userId);
        entity.setTitle(req.title().trim());
        entity.setBody(req.body());
        entity.setCategory(req.category().trim());
        entity.setStarred(req.starred());
        entity.setTagsJson(writeJson(req.tags() == null ? List.of() : req.tags()));
        entity.setImageUrlsJson(writeJson(req.imageUrls() == null ? List.of() : req.imageUrls()));
    }

    private IdeaDtos.IdeaResponse toResponse(Idea x) {
        return new IdeaDtos.IdeaResponse(
                x.getId(),
                x.getTitle(),
                x.getBody(),
                x.getCategory(),
                readJsonArray(x.getTagsJson()),
                readJsonArray(x.getImageUrlsJson()),
                x.getStarred(),
                x.getUpdatedAt()
        );
    }

    private String writeJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (Exception e) {
            throw new AppException("JSON 序列化失败");
        }
    }

    private List<String> readJsonArray(String value) {
        if (value == null || value.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(value, new TypeReference<>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
