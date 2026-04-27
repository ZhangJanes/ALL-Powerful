package com.allpowerful.backend.photo;

import com.allpowerful.backend.common.ApiResponse;
import com.allpowerful.backend.common.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
public class PhotoController {
    private final PhotoService photoService;

    @GetMapping("/libraries")
    public ApiResponse<List<PhotoDtos.LibraryResponse>> libraries() {
        return ApiResponse.ok(photoService.listLibraries(CurrentUser.id()));
    }

    @PostMapping("/libraries")
    public ApiResponse<PhotoDtos.LibraryResponse> createLibrary(@Valid @RequestBody PhotoDtos.LibraryRequest req) {
        return ApiResponse.ok(photoService.createLibrary(CurrentUser.id(), req));
    }

    @PostMapping("/libraries/{libraryId}/verify")
    public ApiResponse<Boolean> verify(@PathVariable Long libraryId, @Valid @RequestBody PhotoDtos.VerifyRequest req) {
        return ApiResponse.ok(photoService.verifyLibrary(CurrentUser.id(), libraryId, req.password()));
    }

    @GetMapping("/libraries/{libraryId}")
    public ApiResponse<List<PhotoDtos.PhotoResponse>> libraryPhotos(@PathVariable Long libraryId) {
        return ApiResponse.ok(photoService.listLibraryPhotos(CurrentUser.id(), libraryId));
    }

    @GetMapping("/public")
    public ApiResponse<List<PhotoDtos.PhotoResponse>> publicPhotos() {
        return ApiResponse.ok(photoService.listPublicPhotos(CurrentUser.id()));
    }

    @PostMapping("/upload")
    public ApiResponse<List<PhotoDtos.PhotoResponse>> upload(@Valid @RequestBody PhotoDtos.UploadRequest req) {
        return ApiResponse.ok(photoService.upload(CurrentUser.id(), req));
    }
}
