package com.divedbyz3ro.bot.dto;

import java.util.List;

public class GeminiContent {
    private List<GeminiPart> parts;

    public GeminiContent(List<GeminiPart> parts) {
        this.parts = parts;
    }

    public List<GeminiPart> getParts() { return parts; }
    public void setParts(List<GeminiPart> parts) { this.parts = parts; }
}
