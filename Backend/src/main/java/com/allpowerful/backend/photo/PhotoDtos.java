package com.allpowerful.backend.photo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PhotoDtos {
    public record LibraryRequest(@NotBlank String name, @NotBlank String visibility, String password) {}
    public record VerifyRequest(@NotBlank String password) {}
    public record UploadItem(@NotBlank String name, @NotBlank String category, String objectKey, @NotNull LocalDate atDate) {}
    public record UploadRequest(@NotNull Long libraryId, String password, @NotNull List<UploadItem> items) {}

    public record LibraryResponse(Long id, String name, String visibility, LocalDateTime createdAt) {}
    public record PhotoResponse(Long id, Long libraryId, String name, String category, Boolean locked, String objectKey, LocalDate atDate) {}
}
