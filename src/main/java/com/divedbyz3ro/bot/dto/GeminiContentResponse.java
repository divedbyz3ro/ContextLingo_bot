package com.divedbyz3ro.bot.dto;
import java.util.List;

public class GeminiContentResponse {
    private List<GeminiPartResponse> parts;
    // Геттеры и сеттеры
    public List<GeminiPartResponse> getParts() { return parts; }
    public void setParts(List<GeminiPartResponse> parts) { this.parts = parts; }
}