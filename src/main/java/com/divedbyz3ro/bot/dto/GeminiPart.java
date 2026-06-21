package com.divedbyz3ro.bot.dto;

public class GeminiPart {
    private String text;

    public GeminiPart(String text) {
        this.text = text;
    }

    // Геттеры и сеттеры обязательны для работы Jackson (библиотека парсинга)
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}