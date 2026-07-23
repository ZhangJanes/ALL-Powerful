package com.allpowerful.backend.memo;

import com.allpowerful.backend.common.ApiResponse;
import com.allpowerful.backend.common.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/memos")
@RequiredArgsConstructor
public class MemoController {
    private final MemoService memoService;

    @GetMapping
    public ApiResponse<List<MemoDtos.MemoResponse>> list() {
        return ApiResponse.ok(memoService.list(CurrentUser.id()));
    }

    @PostMapping
    public ApiResponse<MemoDtos.MemoResponse> create(@Valid @RequestBody MemoDtos.MemoRequest req) {
        return ApiResponse.ok(memoService.create(CurrentUser.id(), req));
    }

    @GetMapping("/trash")
    public ApiResponse<List<MemoDtos.MemoResponse>> trash() {
        return ApiResponse.ok(memoService.trash(CurrentUser.id()));
    }

    @PutMapping("/{id}")
    public ApiResponse<MemoDtos.MemoResponse> update(@PathVariable Long id, @Valid @RequestBody MemoDtos.MemoRequest req) {
        return ApiResponse.ok(memoService.update(CurrentUser.id(), id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        memoService.delete(CurrentUser.id(), id);
        return ApiResponse.ok(null, "已删除");
    }

    @PutMapping("/{id}/restore")
    public ApiResponse<Void> restore(@PathVariable Long id) {
        memoService.restore(CurrentUser.id(), id);
        return ApiResponse.ok(null, "已恢复");
    }

    @DeleteMapping("/{id}/permanent")
    public ApiResponse<Void> deletePermanent(@PathVariable Long id) {
        memoService.deletePermanent(CurrentUser.id(), id);
        return ApiResponse.ok(null, "已彻底删除");
    }
}
