package com.divedbyz3ro.bot.dto;

import java.util.List;

public class GeminiResponse {
    private List<GeminiCandidate> candidates;
    // Геттеры и сеттеры
    public List<GeminiCandidate> getCandidates() { return candidates; }
    public void setCandidates(List<GeminiCandidate> candidates) { this.candidates = candidates; }
}