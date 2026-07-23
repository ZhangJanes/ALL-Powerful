package com.allpowerful.backend.guide;

import com.allpowerful.backend.common.AppException;
import com.allpowerful.backend.memo.Memo;
import com.allpowerful.backend.memo.MemoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuideService {
    private final GuideCategoryRepository categoryRepository;
    private final GuideArticleRepository articleRepository;
    private final GuideFavoriteRepository favoriteRepository;
    private final MemoRepository memoRepository;

    @Transactional(readOnly = true)
    public List<GuideDtos.CategoryResponse> categories() {
        List<GuideCategory> rows = categoryRepository.findByEnabledTrueOrderBySortOrderAscIdAsc();
        Map<Long, Integer> counts = articleRepository.findAll().stream()
                .collect(Collectors.groupingBy(GuideArticle::getCategoryId, Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
        return rows.stream().map(x -> toCategoryResponse(x, counts.getOrDefault(x.getId(), 0))).toList();
    }

    @Transactional(readOnly = true)
    public List<GuideDtos.ArticleResponse> articles(Long userId, String categoryCode, String keyword) {
        Map<Long, GuideCategory> catMap = categoryRepository.findAll().stream().collect(Collectors.toMap(GuideCategory::getId, Function.identity()));
        List<GuideArticle> rows;
        if (keyword != null && !keyword.isBlank()) {
            rows = articleRepository.findByTitleContainingIgnoreCaseOrderByUpdatedAtDesc(keyword.trim());
        } else {
            GuideCategory category = categoryRepository.findByCode(categoryCode).orElseThrow(() -> new AppException("分类不存在"));
            rows = articleRepository.findByCategoryIdOrderByUpdatedAtDesc(category.getId());
        }
        return rows.stream().map(x -> toArticleResponse(userId, x, catMap.get(x.getCategoryId()))).toList();
    }

    @Transactional(readOnly = true)
    public GuideDtos.ArticleResponse article(Long userId, Long id) {
        GuideArticle article = articleRepository.findById(id).orElseThrow(() -> new AppException("指南不存在"));
        GuideCategory category = categoryRepository.findById(article.getCategoryId()).orElseThrow(() -> new AppException("分类不存在"));
        return toArticleResponse(userId, article, category);
    }

    @Transactional(readOnly = true)
    public GuideDtos.ArticleResponse articleBySlug(Long userId, String categoryCode, String slug) {
        GuideCategory category = categoryRepository.findByCode(categoryCode).orElseThrow(() -> new AppException("分类不存在"));
        GuideArticle article = articleRepository.findBySlug(slug).orElseThrow(() -> new AppException("指南不存在"));
        if (!article.getCategoryId().equals(category.getId())) throw new AppException("指南分类不匹配");
        return toArticleResponse(userId, article, category);
    }

    @Transactional(readOnly = true)
    public List<GuideDtos.FavoriteResponse> favorites(Long userId) {
        Map<Long, GuideArticle> map = articleRepository.findAll().stream().collect(Collectors.toMap(GuideArticle::getId, Function.identity()));
        Map<Long, GuideCategory> catMap = categoryRepository.findAll().stream().collect(Collectors.toMap(GuideCategory::getId, Function.identity()));
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(f -> map.get(f.getArticleId()))
                .filter(x -> x != null)
                .map(x -> new GuideDtos.FavoriteResponse(x.getId(), x.getTitle(), catMap.get(x.getCategoryId()) == null ? "" : catMap.get(x.getCategoryId()).getCode()))
                .toList();
    }

    @Transactional
    public void favorite(Long userId, Long articleId) {
        articleRepository.findById(articleId).orElseThrow(() -> new AppException("指南不存在"));
        if (favoriteRepository.findByUserIdAndArticleId(userId, articleId).isPresent()) return;
        GuideFavorite f = new GuideFavorite();
        f.setUserId(userId);
        f.setArticleId(articleId);
        f.setCreatedAt(LocalDateTime.now());
        favoriteRepository.save(f);
    }

    @Transactional
    public void unfavorite(Long userId, Long articleId) {
        GuideFavorite f = favoriteRepository.findByUserIdAndArticleId(userId, articleId).orElseThrow(() -> new AppException("收藏不存在"));
        favoriteRepository.delete(f);
    }

    @Transactional
    public GuideDtos.CategoryResponse createCategory(GuideDtos.CategoryRequest req) {
        if (categoryRepository.findByCode(req.code()).isPresent()) throw new AppException("code 已存在");
        GuideCategory x = new GuideCategory();
        patchCategory(x, req);
        x.setCreatedAt(LocalDateTime.now());
        x.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(x);
        return toCategoryResponse(x, 0);
    }

    @Transactional
    public GuideDtos.CategoryResponse updateCategory(Long id, GuideDtos.CategoryRequest req) {
        GuideCategory x = categoryRepository.findById(id).orElseThrow(() -> new AppException("分类不存在"));
        patchCategory(x, req);
        x.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(x);
        int count = articleRepository.findByCategoryIdOrderByUpdatedAtDesc(x.getId()).size();
        return toCategoryResponse(x, count);
    }

    @Transactional
    public void deleteCategory(Long id) {
        GuideCategory category = categoryRepository.findById(id).orElseThrow(() -> new AppException("分类不存在"));
        categoryRepository.delete(category);
    }

    @Transactional
    public GuideDtos.ArticleResponse createArticle(Long userId, GuideDtos.ArticleRequest req) {
        GuideCategory category = categoryRepository.findById(req.categoryId()).orElseThrow(() -> new AppException("分类不存在"));
        GuideArticle x = new GuideArticle();
        patchArticle(x, req);
        x.setCreatedAt(LocalDateTime.now());
        x.setUpdatedAt(LocalDateTime.now());
        articleRepository.save(x);
        return toArticleResponse(userId, x, category);
    }

    @Transactional
    public GuideDtos.ArticleResponse updateArticle(Long userId, Long id, GuideDtos.ArticleRequest req) {
        GuideCategory category = categoryRepository.findById(req.categoryId()).orElseThrow(() -> new AppException("分类不存在"));
        GuideArticle x = articleRepository.findById(id).orElseThrow(() -> new AppException("指南不存在"));
        patchArticle(x, req);
        x.setUpdatedAt(LocalDateTime.now());
        articleRepository.save(x);
        return toArticleResponse(userId, x, category);
    }

    @Transactional
    public void deleteArticle(Long id) {
        GuideArticle article = articleRepository.findById(id).orElseThrow(() -> new AppException("指南不存在"));
        articleRepository.delete(article);
    }

    @Transactional
    public GuideDtos.LinkMemoResponse linkMemo(Long userId, Long articleId, GuideDtos.LinkMemoRequest req) {
        GuideArticle article = articleRepository.findById(articleId).orElseThrow(() -> new AppException("指南不存在"));
        Memo memo = new Memo();
        memo.setUserId(userId);
        memo.setTitle("办事指南：" + article.getTitle());
        String content = """
                办理条件：
                %s

                所需材料：
                %s

                办理流程：
                %s

                备注：
                %s
                """.formatted(
                nullToDash(article.getConditionsText()),
                nullToDash(article.getMaterialsText()),
                nullToDash(article.getProcessText()),
                req == null ? "" : nullToDash(req.extraNote())
        );
        memo.setContent(content.trim());
        memo.setCategory("办事");
        memo.setPinned(false);
        memo.setRemindRepeat(null);
        if (req != null && req.remindAt() != null && !req.remindAt().isBlank()) {
            memo.setRemindAt(LocalDateTime.parse(req.remindAt()));
        }
        memo.setCreatedAt(LocalDateTime.now());
        memo.setUpdatedAt(LocalDateTime.now());
        memoRepository.save(memo);
        return new GuideDtos.LinkMemoResponse(memo.getId());
    }

    private GuideDtos.CategoryResponse toCategoryResponse(GuideCategory x, int count) {
        return new GuideDtos.CategoryResponse(x.getId(), x.getCode(), x.getTitle(), x.getDescription(), x.getSortOrder(), x.getEnabled(), count);
    }

    private GuideDtos.ArticleResponse toArticleResponse(Long userId, GuideArticle x, GuideCategory category) {
        boolean favorite = favoriteRepository.findByUserIdAndArticleId(userId, x.getId()).isPresent();
        return new GuideDtos.ArticleResponse(
                x.getId(),
                x.getCategoryId(),
                category == null ? "" : category.getCode(),
                x.getSlug(),
                x.getTitle(),
                x.getIntro(),
                x.getConditionsText(),
                x.getMaterialsText(),
                x.getProcessText(),
                x.getLocationText(),
                x.getTimeText(),
                x.getPeriodText(),
                x.getFeeText(),
                x.getTipsText(),
                x.getConsultUrl(),
                favorite,
                x.getUpdatedAt()
        );
    }

    private void patchCategory(GuideCategory x, GuideDtos.CategoryRequest req) {
        x.setCode(req.code().trim());
        x.setTitle(req.title().trim());
        x.setDescription(req.description());
        x.setSortOrder(req.sortOrder());
        x.setEnabled(req.enabled());
    }

    private void patchArticle(GuideArticle x, GuideDtos.ArticleRequest req) {
        x.setCategoryId(req.categoryId());
        x.setSlug(req.slug().trim());
        x.setTitle(req.title().trim());
        x.setIntro(req.intro());
        x.setConditionsText(req.conditionsText());
        x.setMaterialsText(req.materialsText());
        x.setProcessText(req.processText());
        x.setLocationText(req.locationText());
        x.setTimeText(req.timeText());
        x.setPeriodText(req.periodText());
        x.setFeeText(req.feeText());
        x.setTipsText(req.tipsText());
        x.setConsultUrl(req.consultUrl());
    }

    private String nullToDash(String value) {
        return (value == null || value.isBlank()) ? "-" : value.trim();
    }
}
