package com.allpowerful.backend.travel;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "trips")
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 32)
    private String category;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false, length = 255)
    private String place;

    @Column(length = 255)
    private String companions;

    @Column(length = 32)
    private String transport;

    @Column(length = 255)
    private String remark;

    @Column(name = "remind_enabled", nullable = false)
    private Boolean remindEnabled = false;

    @Column(name = "remind_minutes_before")
    private Integer remindMinutesBefore;

    @Column(nullable = false)
    private Boolean done = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<TripChecklist> checklist = new ArrayList<>();
}
