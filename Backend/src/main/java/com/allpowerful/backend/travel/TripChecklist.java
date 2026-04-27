package com.allpowerful.backend.travel;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "trip_checklists")
public class TripChecklist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(nullable = false, length = 255)
    private String text;

    @Column(nullable = false)
    private Boolean done = false;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}
