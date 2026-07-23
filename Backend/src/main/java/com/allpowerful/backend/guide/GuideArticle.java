package com.allpowerful.backend.guide;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "guide_articles")
public class GuideArticle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(nullable = false, length = 64, unique = true)
    private String slug;

    @Column(nullable = false, length = 128)
    private String title;

    @Column(length = 255)
    private String intro;

    @Column(name = "conditions_text", columnDefinition = "TEXT")
    private String conditionsText;

    @Column(name = "materials_text", columnDefinition = "TEXT")
    private String materialsText;

    @Column(name = "process_text", columnDefinition = "TEXT")
    private String processText;

    @Column(name = "location_text", columnDefinition = "TEXT")
    private String locationText;

    @Column(name = "time_text", columnDefinition = "TEXT")
    private String timeText;

    @Column(name = "period_text", columnDefinition = "TEXT")
    private String periodText;

    @Column(name = "fee_text", columnDefinition = "TEXT")
    private String feeText;

    @Column(name = "tips_text", columnDefinition = "TEXT")
    private String tipsText;

    @Column(name = "consult_url", length = 255)
    private String consultUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
