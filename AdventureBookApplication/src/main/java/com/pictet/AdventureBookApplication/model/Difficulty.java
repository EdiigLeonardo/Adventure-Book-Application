package com.pictet.AdventureBookApplication.model;

import java.util.Locale;
import java.util.Map;

public enum Difficulty {
    BEGINNER, INTERMEDIATE, ADVANCED;

    private static final Map<String, Difficulty> ALIASES = Map.ofEntries(
        Map.entry("EASY", BEGINNER),
        Map.entry("BEGINNER", BEGINNER),
        Map.entry("MEDIUM", INTERMEDIATE),
        Map.entry("INTERMEDIATE", INTERMEDIATE),
        Map.entry("HARD", ADVANCED),
        Map.entry("ADVANCED", ADVANCED)
    );

    public static Difficulty normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Difficulty value is missing");
        }
        Difficulty mapped = ALIASES.get(raw.trim().toUpperCase(Locale.ROOT));
        if (mapped == null) {
            throw new IllegalArgumentException("Unknown difficulty value: " + raw);
        }
        return mapped;
    }
}
