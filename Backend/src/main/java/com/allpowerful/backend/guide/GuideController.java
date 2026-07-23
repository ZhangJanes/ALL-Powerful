package com.allpowerful.backend.guide;

import com.allpowerful.backend.common.ApiResponse;
import com.allpowerful.backend.common.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guides")
@RequiredArgsConstructor
public class GuideController {
    private final GuideService guideService;

    @GetMapping("/categories")
    public ApiResponse<List<GuideDtos.CategoryResponse>> categories() {
        return ApiResponse.ok(guideService.categories());
    }

    @PostMapping("/categories")
    public ApiResponse<GuideDtos.CategoryResponse> createCategory(@Valid @RequestBody GuideDtos.CategoryRequest req) {
        return ApiResponse.ok(guideService.createCategory(req));
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<GuideDtos.CategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody GuideDtos.CategoryRequest req) {
        return ApiResponse.ok(guideService.updateCategory(id, req));
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        guideService.deleteCategory(id);
        return ApiResponse.ok(null, "已删除");
    }

    @GetMapping("/articles")
    public ApiResponse<List<GuideDtos.ArticleResponse>> articles(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword
    ) {
        if ((category == null || category.isBlank()) && (keyword == null || keyword.isBlank())) {
            return ApiResponse.ok(List.of());
        }
        return ApiResponse.ok(guideService.articles(CurrentUser.id(), category, keyword));
    }

    @GetMapping("/articles/{id}")
    public ApiResponse<GuideDtos.ArticleResponse> article(@PathVariable Long id) {
        return ApiResponse.ok(guideService.article(CurrentUser.id(), id));
    }

    @GetMapping("/categories/{category}/articles/{slug}")
    public ApiResponse<GuideDtos.ArticleResponse> articleBySlug(@PathVariable String category, @PathVariable String slug) {
        return ApiResponse.ok(guideService.articleBySlug(CurrentUser.id(), category, slug));
    }

    @PostMapping("/articles")
    public ApiResponse<GuideDtos.ArticleResponse> createArticle(@Valid @RequestBody GuideDtos.ArticleRequest req) {
        return ApiResponse.ok(guideService.createArticle(CurrentUser.id(), req));
    }

    @PutMapping("/articles/{id}")
    public ApiResponse<GuideDtos.ArticleResponse> updateArticle(@PathVariable Long id, @Valid @RequestBody GuideDtos.ArticleRequest req) {
        return ApiResponse.ok(guideService.updateArticle(CurrentUser.id(), id, req));
    }

    @DeleteMapping("/articles/{id}")
    public ApiResponse<Void> deleteArticle(@PathVariable Long id) {
        guideService.deleteArticle(id);
        return ApiResponse.ok(null, "已删除");
    }

    @GetMapping("/favorites")
    public ApiResponse<List<GuideDtos.FavoriteResponse>> favorites() {
        return ApiResponse.ok(guideService.favorites(CurrentUser.id()));
    }

    @PostMapping("/articles/{id}/favorite")
    public ApiResponse<Void> favorite(@PathVariable Long id) {
        guideService.favorite(CurrentUser.id(), id);
        return ApiResponse.ok(null, "已收藏");
    }

    @DeleteMapping("/articles/{id}/favorite")
    public ApiResponse<Void> unfavorite(@PathVariable Long id) {
        guideService.unfavorite(CurrentUser.id(), id);
        return ApiResponse.ok(null, "已取消收藏");
    }

    @PostMapping("/articles/{id}/link-memo")
    public ApiResponse<GuideDtos.LinkMemoResponse> linkMemo(@PathVariable Long id, @RequestBody(required = false) GuideDtos.LinkMemoRequest req) {
        return ApiResponse.ok(guideService.linkMemo(CurrentUser.id(), id, req));
    }
}
