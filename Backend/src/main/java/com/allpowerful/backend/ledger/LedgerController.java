package com.allpowerful.backend.ledger;

import com.allpowerful.backend.common.ApiResponse;
import com.allpowerful.backend.common.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ledgers")
@RequiredArgsConstructor
public class LedgerController {
    private final LedgerService ledgerService;

    @GetMapping("/categories")
    public ApiResponse<List<LedgerDtos.CategoryResponse>> listCategories() {
        return ApiResponse.ok(ledgerService.listCategories(CurrentUser.id()));
    }

    @PostMapping("/categories")
    public ApiResponse<LedgerDtos.CategoryResponse> createCategory(@Valid @RequestBody LedgerDtos.CategoryRequest req) {
        return ApiResponse.ok(ledgerService.createCategory(CurrentUser.id(), req));
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<LedgerDtos.CategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody LedgerDtos.CategoryRequest req) {
        return ApiResponse.ok(ledgerService.updateCategory(CurrentUser.id(), id, req));
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        ledgerService.deleteCategory(CurrentUser.id(), id);
        return ApiResponse.ok(null, "已删除");
    }

    @GetMapping("/entries")
    public ApiResponse<List<LedgerDtos.EntryResponse>> listEntries(
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String type
    ) {
        return ApiResponse.ok(ledgerService.listEntries(CurrentUser.id(), range, type));
    }

    @PostMapping("/entries")
    public ApiResponse<LedgerDtos.EntryResponse> createEntry(@Valid @RequestBody LedgerDtos.EntryRequest req) {
        return ApiResponse.ok(ledgerService.createEntry(CurrentUser.id(), req));
    }

    @PutMapping("/entries/{id}")
    public ApiResponse<LedgerDtos.EntryResponse> updateEntry(@PathVariable Long id, @Valid @RequestBody LedgerDtos.EntryRequest req) {
        return ApiResponse.ok(ledgerService.updateEntry(CurrentUser.id(), id, req));
    }

    @DeleteMapping("/entries/{id}")
    public ApiResponse<Void> deleteEntry(@PathVariable Long id) {
        ledgerService.deleteEntry(CurrentUser.id(), id);
        return ApiResponse.ok(null, "已删除");
    }

    @GetMapping("/budget")
    public ApiResponse<LedgerDtos.BudgetResponse> getBudget(@RequestParam(required = false) String month) {
        return ApiResponse.ok(ledgerService.getBudget(CurrentUser.id(), month));
    }

    @PutMapping("/budget")
    public ApiResponse<LedgerDtos.BudgetResponse> upsertBudget(@Valid @RequestBody LedgerDtos.BudgetRequest req) {
        return ApiResponse.ok(ledgerService.upsertBudget(CurrentUser.id(), req));
    }

    @GetMapping("/stats")
    public ApiResponse<LedgerDtos.StatsResponse> stats(
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String type
    ) {
        return ApiResponse.ok(ledgerService.stats(CurrentUser.id(), range, type));
    }

    @GetMapping("/export")
    public ApiResponse<LedgerDtos.ExportResponse> export(
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String type
    ) {
        return ApiResponse.ok(ledgerService.export(CurrentUser.id(), range, type));
    }
}
