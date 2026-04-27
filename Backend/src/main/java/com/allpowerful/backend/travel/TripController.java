package com.allpowerful.backend.travel;

import com.allpowerful.backend.common.ApiResponse;
import com.allpowerful.backend.common.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {
    private final TripService tripService;

    @GetMapping
    public ApiResponse<List<TripDtos.TripResponse>> list() {
        return ApiResponse.ok(tripService.list(CurrentUser.id()));
    }

    @PostMapping
    public ApiResponse<TripDtos.TripResponse> create(@Valid @RequestBody TripDtos.TripRequest req) {
        return ApiResponse.ok(tripService.create(CurrentUser.id(), req));
    }

    @PutMapping("/{id}")
    public ApiResponse<TripDtos.TripResponse> update(@PathVariable Long id, @Valid @RequestBody TripDtos.TripRequest req) {
        return ApiResponse.ok(tripService.update(CurrentUser.id(), id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        tripService.delete(CurrentUser.id(), id);
        return ApiResponse.ok(null, "已删除");
    }
}
