package com.divedbyz3ro.bot.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    private Long telegramId; // Используем ID из Телеграма как первичный ключ

    // Состояние пользователя: IDLE (ждет), LEARNING (учит слова), TRANSLATING (переводит)
    private String botState = "IDLE";
    private String currentLanguage = "EN"; // По умолчанию английский

    @Column(name = "current_word_ids")
    private String currentWordIds; // "12,45,67,1,9"


    private String firstName;

    private int currentLevel = 1; // Уровень сложности (1 - Beginner, и т.д.)

    @Column(columnDefinition = "TEXT")
    private String currentStory; // Здесь будем хранить сгенерированную историю

    // Конструкторы
    public User() {}

    public User(Long telegramId, String firstName) {
        this.telegramId = telegramId;
        this.firstName = firstName;
    }
}