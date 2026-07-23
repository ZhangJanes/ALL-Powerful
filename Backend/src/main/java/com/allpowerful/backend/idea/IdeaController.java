package com.allpowerful.backend.idea;

import com.allpowerful.backend.common.ApiResponse;
import com.allpowerful.backend.common.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ideas")
@RequiredArgsConstructor
public class IdeaController {
    private final IdeaService ideaService;

    @GetMapping
    public ApiResponse<List<IdeaDtos.IdeaResponse>> list() {
        return ApiResponse.ok(ideaService.list(CurrentUser.id()));
    }

    @GetMapping("/trash")
    public ApiResponse<List<IdeaDtos.IdeaResponse>> trash() {
        return ApiResponse.ok(ideaService.trash(CurrentUser.id()));
    }

    @PostMapping
    public ApiResponse<IdeaDtos.IdeaResponse> create(@Valid @RequestBody IdeaDtos.IdeaRequest req) {
        return ApiResponse.ok(ideaService.create(CurrentUser.id(), req));
    }

    @PutMapping("/{id}")
    public ApiResponse<IdeaDtos.IdeaResponse> update(@PathVariable Long id, @Valid @RequestBody IdeaDtos.IdeaRequest req) {
        return ApiResponse.ok(ideaService.update(CurrentUser.id(), id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        ideaService.delete(CurrentUser.id(), id);
        return ApiResponse.ok(null, "已删除");
    }

    @PutMapping("/{id}/restore")
    public ApiResponse<Void> restore(@PathVariable Long id) {
        ideaService.restore(CurrentUser.id(), id);
        return ApiResponse.ok(null, "已恢复");
    }

    @DeleteMapping("/{id}/permanent")
    public ApiResponse<Void> deletePermanent(@PathVariable Long id) {
        ideaService.deletePermanent(CurrentUser.id(), id);
        return ApiResponse.ok(null, "已彻底删除");
    }

    @PutMapping("/{id}/star")
    public ApiResponse<IdeaDtos.IdeaResponse> star(@PathVariable Long id) {
        return ApiResponse.ok(ideaService.toggleStar(CurrentUser.id(), id));
    }
}
