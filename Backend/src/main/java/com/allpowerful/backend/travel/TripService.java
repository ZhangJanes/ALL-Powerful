package com.allpowerful.backend.travel;

import com.allpowerful.backend.common.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {
    private final TripRepository tripRepository;

    @Transactional(readOnly = true)
    public List<TripDtos.TripResponse> list(Long userId) {
        return tripRepository.findByUserIdOrderByStartAtDesc(userId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public TripDtos.TripResponse create(Long userId, TripDtos.TripRequest req) {
        Trip trip = new Trip();
        patch(trip, userId, req);
        trip.setCreatedAt(LocalDateTime.now());
        trip.setUpdatedAt(LocalDateTime.now());
        tripRepository.save(trip);
        return toResponse(trip);
    }

    @Transactional
    public TripDtos.TripResponse update(Long userId, Long id, TripDtos.TripRequest req) {
        Trip trip = tripRepository.findById(id).orElseThrow(() -> new AppException("行程不存在"));
        if (!trip.getUserId().equals(userId)) throw new AppException("无权限");
        patch(trip, userId, req);
        trip.setUpdatedAt(LocalDateTime.now());
        tripRepository.save(trip);
        return toResponse(trip);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Trip trip = tripRepository.findById(id).orElseThrow(() -> new AppException("行程不存在"));
        if (!trip.getUserId().equals(userId)) throw new AppException("无权限");
        tripRepository.delete(trip);
    }

    private void patch(Trip trip, Long userId, TripDtos.TripRequest req) {
        trip.setUserId(userId);
        trip.setTitle(req.title());
        trip.setCategory(req.category());
        trip.setStartAt(req.startAt());
        trip.setEndAt(req.endAt());
        trip.setPlace(req.place());
        trip.setCompanions(req.companions());
        trip.setTransport(req.transport());
        trip.setRemark(req.remark());
        trip.setRemindEnabled(req.remindEnabled());
        trip.setRemindMinutesBefore(req.remindMinutesBefore());
        trip.setDone(req.done());
        trip.getChecklist().clear();
        if (req.checklist() != null) {
            for (int i = 0; i < req.checklist().size(); i++) {
                TripDtos.ChecklistItem item = req.checklist().get(i);
                TripChecklist entity = new TripChecklist();
                entity.setTrip(trip);
                entity.setText(item.text());
                entity.setDone(item.done());
                entity.setSortOrder(i);
                trip.getChecklist().add(entity);
            }
        }
    }

    private TripDtos.TripResponse toResponse(Trip trip) {
        List<TripDtos.ChecklistItem> items = trip.getChecklist().stream()
                .map(c -> new TripDtos.ChecklistItem(c.getId(), c.getText(), c.getDone()))
                .toList();
        return new TripDtos.TripResponse(
                trip.getId(),
                trip.getTitle(),
                trip.getCategory(),
                trip.getStartAt(),
                trip.getEndAt(),
                trip.getPlace(),
                trip.getCompanions(),
                trip.getTransport(),
                trip.getRemark(),
                trip.getRemindEnabled(),
                trip.getRemindMinutesBefore(),
                trip.getDone(),
                items
        );
    }
}
