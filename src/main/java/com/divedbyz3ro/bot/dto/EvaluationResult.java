package com.divedbyz3ro.bot.dto;

public class EvaluationResult {
    private String isCorrect; // "YES" или "NO"
    private int score;
    private String feedback;

    //конструктор для ошибки создания оценки
    public EvaluationResult(String isCorrect, int score, String feedback) {
        this.isCorrect = isCorrect;
        this.score = score;
        this.feedback = feedback;
    }

    // Геттеры
    public String getIsCorrect() {
        return isCorrect;
    }

    public int getScore() {
        return score;
    }

    public String getFeedback() {
        return feedback;
    }

    // Сеттеры
    public void setIsCorrect(String isCorrect) {
        this.isCorrect = isCorrect;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
