package com.allpowerful.backend.memo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "memo_todos")
public class MemoTodo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memo_id", nullable = false)
    private Memo memo;

    @Column(nullable = false, length = 255)
    private String text;

    @Column(nullable = false)
    private Boolean done = false;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}
