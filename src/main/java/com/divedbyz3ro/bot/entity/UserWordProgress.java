package com.divedbyz3ro.bot.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_word_progress")
@Data
public class UserWordProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "word_id")
    private Word word;

    private int masteryLevel = 0; // Насколько слово выучено (например, от 0 до 5)

    private boolean isLearned = false; // Флаг: выучено ли слово окончательно

    public UserWordProgress() {}

    public UserWordProgress(User user, Word word) {
        this.user = user;
        this.word = word;
    }
}