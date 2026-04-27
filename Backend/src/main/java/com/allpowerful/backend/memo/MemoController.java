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

    @PutMapping("/{id}")
    public ApiResponse<MemoDtos.MemoResponse> update(@PathVariable Long id, @Valid @RequestBody MemoDtos.MemoRequest req) {
        return ApiResponse.ok(memoService.update(CurrentUser.id(), id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        memoService.delete(CurrentUser.id(), id);
        return ApiResponse.ok(null, "已删除");
    }
}
