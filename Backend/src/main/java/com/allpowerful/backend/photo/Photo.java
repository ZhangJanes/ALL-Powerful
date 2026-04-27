package com.allpowerful.backend.photo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "photos")
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "library_id", nullable = false)
    private Long libraryId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 32)
    private String category;

    @Column(nullable = false)
    private Boolean locked = false;

    @Column(name = "object_key", length = 255)
    private String objectKey;

    @Column(name = "at_date", nullable = false)
    private LocalDate atDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
