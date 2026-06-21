package com.divedbyz3ro.bot.dto;

import java.util.List;

public class GeminiRequest {
    private List<GeminiContent> contents;

    public GeminiRequest(List<GeminiContent> contents) {
        this.contents = contents;
    }

    public List<GeminiContent> getContents() { return contents; }
    public void setContents(List<GeminiContent> contents) { this.contents = contents; }
}
