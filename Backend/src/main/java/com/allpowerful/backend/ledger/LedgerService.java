package com.allpowerful.backend.ledger;

import com.allpowerful.backend.common.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LedgerService {
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("MM-dd");
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final LedgerCategoryRepository categoryRepository;
    private final LedgerEntryRepository entryRepository;
    private final LedgerBudgetRepository budgetRepository;

    @Transactional(readOnly = true)
    public List<LedgerDtos.CategoryResponse> listCategories(Long userId) {
        ensureDefaultCategories(userId);
        return categoryRepository.findByUserIdAndDeletedAtIsNullOrderByTypeAscNameAsc(userId).stream().map(this::toCategoryResponse).toList();
    }

    @Transactional
    public LedgerDtos.CategoryResponse createCategory(Long userId, LedgerDtos.CategoryRequest req) {
        LedgerCategory entity = new LedgerCategory();
        entity.setUserId(userId);
        entity.setType(normalizeType(req.type()));
        entity.setName(req.name().trim());
        entity.setIcon(req.icon());
        entity.setColor(req.color());
        entity.setIsDefault(false);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(entity);
        return toCategoryResponse(entity);
    }

    @Transactional
    public LedgerDtos.CategoryResponse updateCategory(Long userId, Long id, LedgerDtos.CategoryRequest req) {
        LedgerCategory entity = categoryRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId).orElseThrow(() -> new AppException("分类不存在"));
        if (Boolean.TRUE.equals(entity.getIsDefault())) {
            throw new AppException("系统默认分类不支持修改");
        }
        entity.setType(normalizeType(req.type()));
        entity.setName(req.name().trim());
        entity.setIcon(req.icon());
        entity.setColor(req.color());
        entity.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(entity);
        return toCategoryResponse(entity);
    }

    @Transactional
    public void deleteCategory(Long userId, Long id) {
        LedgerCategory entity = categoryRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId).orElseThrow(() -> new AppException("分类不存在"));
        if (Boolean.TRUE.equals(entity.getIsDefault())) {
            throw new AppException("系统默认分类不支持删除");
        }
        entity.setDeletedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<LedgerDtos.EntryResponse> listEntries(Long userId, String range, String type) {
        List<LedgerEntry> rows = resolveEntries(userId, range);
        if (type != null && !type.isBlank()) {
            String normalized = normalizeType(type);
            rows = rows.stream().filter(x -> normalized.equals(x.getType())).toList();
        }
        return rows.stream().map(this::toEntryResponse).toList();
    }

    @Transactional
    public LedgerDtos.EntryResponse createEntry(Long userId, LedgerDtos.EntryRequest req) {
        LedgerEntry entity = new LedgerEntry();
        patchEntry(entity, userId, req);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entryRepository.save(entity);
        return toEntryResponse(entity);
    }

    @Transactional
    public LedgerDtos.EntryResponse updateEntry(Long userId, Long id, LedgerDtos.EntryRequest req) {
        LedgerEntry entity = entryRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId).orElseThrow(() -> new AppException("记录不存在"));
        patchEntry(entity, userId, req);
        entity.setUpdatedAt(LocalDateTime.now());
        entryRepository.save(entity);
        return toEntryResponse(entity);
    }

    @Transactional
    public void deleteEntry(Long userId, Long id) {
        LedgerEntry entity = entryRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId).orElseThrow(() -> new AppException("记录不存在"));
        entity.setDeletedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entryRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public LedgerDtos.BudgetResponse getBudget(Long userId, String month) {
        String targetMonth = (month == null || month.isBlank()) ? YearMonth.now().format(MONTH_FMT) : month;
        LedgerBudget budget = budgetRepository.findByUserIdAndBudgetMonth(userId, targetMonth).orElseGet(() -> {
            LedgerBudget b = new LedgerBudget();
            b.setUserId(userId);
            b.setBudgetMonth(targetMonth);
            b.setTotalBudget(BigDecimal.ZERO);
            b.setAlertThreshold(80);
            b.setCreatedAt(LocalDateTime.now());
            b.setUpdatedAt(LocalDateTime.now());
            return b;
        });
        return toBudgetResponse(userId, budget);
    }

    @Transactional
    public LedgerDtos.BudgetResponse upsertBudget(Long userId, LedgerDtos.BudgetRequest req) {
        LedgerBudget budget = budgetRepository.findByUserIdAndBudgetMonth(userId, req.month()).orElseGet(() -> {
            LedgerBudget b = new LedgerBudget();
            b.setUserId(userId);
            b.setBudgetMonth(req.month());
            b.setCreatedAt(LocalDateTime.now());
            return b;
        });
        budget.setTotalBudget(req.totalBudget());
        budget.setAlertThreshold(req.alertThreshold() == null ? 80 : req.alertThreshold());
        budget.setUpdatedAt(LocalDateTime.now());
        budget.getItems().clear();
        if (req.items() != null) {
            for (LedgerDtos.BudgetItemRequest item : req.items()) {
                LedgerBudgetItem x = new LedgerBudgetItem();
                x.setBudget(budget);
                x.setCategoryName(item.categoryName().trim());
                x.setAmount(item.amount());
                x.setCreatedAt(LocalDateTime.now());
                x.setUpdatedAt(LocalDateTime.now());
                budget.getItems().add(x);
            }
        }
        budgetRepository.save(budget);
        return toBudgetResponse(userId, budget);
    }

    @Transactional(readOnly = true)
    public LedgerDtos.StatsResponse stats(Long userId, String range, String type) {
        List<LedgerEntry> rows = resolveEntries(userId, range);
        if (type != null && !type.isBlank()) {
            String normalized = normalizeType(type);
            rows = rows.stream().filter(x -> normalized.equals(x.getType())).toList();
        }

        BigDecimal income = sum(rows.stream().filter(x -> "income".equals(x.getType())).map(LedgerEntry::getAmount).toList());
        BigDecimal expense = sum(rows.stream().filter(x -> "expense".equals(x.getType())).map(LedgerEntry::getAmount).toList());

        Map<LocalDate, BigDecimal> dayExpense = new TreeMap<>();
        for (LedgerEntry row : rows) {
            if (!"expense".equals(row.getType())) continue;
            LocalDate d = row.getOccurredAt().toLocalDate();
            dayExpense.put(d, dayExpense.getOrDefault(d, BigDecimal.ZERO).add(row.getAmount()));
        }
        BigDecimal maxDay = dayExpense.values().stream().max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
        BigDecimal avgDay = dayExpense.isEmpty() ? BigDecimal.ZERO : sum(new ArrayList<>(dayExpense.values()))
                .divide(BigDecimal.valueOf(dayExpense.size()), 2, RoundingMode.HALF_UP);

        Map<String, BigDecimal> catMap = new HashMap<>();
        for (LedgerEntry row : rows) {
            if (!"expense".equals(row.getType())) continue;
            catMap.put(row.getCategoryName(), catMap.getOrDefault(row.getCategoryName(), BigDecimal.ZERO).add(row.getAmount()));
        }

        List<LedgerDtos.TrendPoint> trend = dayExpense.entrySet().stream()
                .map(e -> new LedgerDtos.TrendPoint(e.getKey().format(DAY_FMT), e.getValue()))
                .toList();

        List<LedgerDtos.CategoryStat> categoryStats = catMap.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(e -> new LedgerDtos.CategoryStat(e.getKey(), e.getValue()))
                .toList();

        LedgerDtos.SummaryResponse summary = new LedgerDtos.SummaryResponse(income, expense, avgDay, maxDay);
        return new LedgerDtos.StatsResponse(summary, trend, categoryStats, rows.stream().map(this::toEntryResponse).toList());
    }

    @Transactional(readOnly = true)
    public LedgerDtos.ExportResponse export(Long userId, String range, String type) {
        List<LedgerDtos.EntryResponse> rows = listEntries(userId, range, type);
        StringBuilder csv = new StringBuilder();
        csv.append("时间,类型,分类,金额,备注\n");
        for (LedgerDtos.EntryResponse row : rows) {
            csv.append(row.occurredAt()).append(",")
                    .append("expense".equals(row.type()) ? "支出" : "收入").append(",")
                    .append(escapeCsv(row.categoryName())).append(",")
                    .append(row.amount()).append(",")
                    .append(escapeCsv(row.note())).append("\n");
        }
        String fileName = "ledger-export-" + LocalDate.now() + ".csv";
        String base64 = Base64.getEncoder().encodeToString(csv.toString().getBytes(StandardCharsets.UTF_8));
        return new LedgerDtos.ExportResponse(fileName, "text/csv;charset=utf-8", base64);
    }

    private LedgerDtos.BudgetResponse toBudgetResponse(Long userId, LedgerBudget budget) {
        List<LedgerEntry> monthEntries = entriesInMonth(userId, budget.getBudgetMonth());
        BigDecimal spent = sum(monthEntries.stream().filter(e -> "expense".equals(e.getType())).map(LedgerEntry::getAmount).toList());
        BigDecimal left = budget.getTotalBudget().subtract(spent).max(BigDecimal.ZERO);

        Map<String, BigDecimal> spentByCategory = monthEntries.stream()
                .filter(e -> "expense".equals(e.getType()))
                .collect(Collectors.groupingBy(LedgerEntry::getCategoryName, Collectors.reducing(BigDecimal.ZERO, LedgerEntry::getAmount, BigDecimal::add)));

        List<LedgerDtos.BudgetItemResponse> items = budget.getItems().stream().map(i -> {
            BigDecimal itemSpent = spentByCategory.getOrDefault(i.getCategoryName(), BigDecimal.ZERO);
            BigDecimal itemLeft = i.getAmount().subtract(itemSpent).max(BigDecimal.ZERO);
            return new LedgerDtos.BudgetItemResponse(i.getCategoryName(), i.getAmount(), itemSpent, itemLeft);
        }).toList();

        return new LedgerDtos.BudgetResponse(budget.getBudgetMonth(), budget.getTotalBudget(), spent, left, budget.getAlertThreshold(), items);
    }

    private List<LedgerEntry> entriesInMonth(Long userId, String month) {
        YearMonth ym = YearMonth.parse(month, MONTH_FMT);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59);
        return entryRepository.findByUserIdAndDeletedAtIsNullAndOccurredAtBetweenOrderByOccurredAtDesc(userId, start, end);
    }

    private void patchEntry(LedgerEntry entity, Long userId, LedgerDtos.EntryRequest req) {
        entity.setUserId(userId);
        entity.setType(normalizeType(req.type()));
        entity.setAmount(req.amount());
        entity.setCategoryId(req.categoryId());
        entity.setCategoryName(req.categoryName().trim());
        entity.setNote(req.note());
        entity.setPhotoUrl(req.photoUrl());
        entity.setOccurredAt(req.occurredAt());
    }

    private List<LedgerEntry> resolveEntries(Long userId, String range) {
        if (range == null || range.isBlank() || "all".equalsIgnoreCase(range)) {
            return entryRepository.findByUserIdAndDeletedAtIsNullOrderByOccurredAtDesc(userId);
        }
        LocalDate today = LocalDate.now();
        LocalDateTime start;
        switch (range) {
            case "today" -> start = today.atStartOfDay();
            case "week" -> start = today.with(DayOfWeek.MONDAY).atStartOfDay();
            case "year" -> start = LocalDate.of(today.getYear(), 1, 1).atStartOfDay();
            case "month" -> start = today.withDayOfMonth(1).atStartOfDay();
            default -> throw new AppException("不支持的时间范围");
        }
        return entryRepository.findByUserIdAndDeletedAtIsNullAndOccurredAtBetweenOrderByOccurredAtDesc(userId, start, LocalDateTime.now());
    }

    private String normalizeType(String type) {
        String v = type == null ? "" : type.trim().toLowerCase(Locale.ROOT);
        if (!"income".equals(v) && !"expense".equals(v)) {
            throw new AppException("type 仅支持 income / expense");
        }
        return v;
    }

    private void ensureDefaultCategories(Long userId) {
        if (!categoryRepository.findByUserIdAndDeletedAtIsNullOrderByTypeAscNameAsc(userId).isEmpty()) return;
        List<String> expenses = List.of("餐饮", "交通", "日用品", "医疗", "教育", "娱乐", "房贷/房租", "红包", "其他");
        List<String> incomes = List.of("工资", "奖金", "兼职", "理财", "红包", "其他");
        LocalDateTime now = LocalDateTime.now();
        for (String x : expenses) {
            LedgerCategory c = new LedgerCategory();
            c.setUserId(userId);
            c.setType("expense");
            c.setName(x);
            c.setIsDefault(true);
            c.setCreatedAt(now);
            c.setUpdatedAt(now);
            categoryRepository.save(c);
        }
        for (String x : incomes) {
            LedgerCategory c = new LedgerCategory();
            c.setUserId(userId);
            c.setType("income");
            c.setName(x);
            c.setIsDefault(true);
            c.setCreatedAt(now);
            c.setUpdatedAt(now);
            categoryRepository.save(c);
        }
    }

    private LedgerDtos.CategoryResponse toCategoryResponse(LedgerCategory x) {
        return new LedgerDtos.CategoryResponse(x.getId(), x.getType(), x.getName(), x.getIcon(), x.getColor(), x.getIsDefault());
    }

    private LedgerDtos.EntryResponse toEntryResponse(LedgerEntry x) {
        return new LedgerDtos.EntryResponse(
                x.getId(),
                x.getType(),
                x.getAmount(),
                x.getCategoryId(),
                x.getCategoryName(),
                x.getNote(),
                x.getPhotoUrl(),
                x.getOccurredAt()
        );
    }

    private BigDecimal sum(List<BigDecimal> values) {
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
