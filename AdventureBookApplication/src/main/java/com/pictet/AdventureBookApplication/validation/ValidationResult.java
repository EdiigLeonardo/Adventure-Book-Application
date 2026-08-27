package com.pictet.AdventureBookApplication.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {

    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void addError(String message) {
        if (message != null && !message.isBlank()) {
            errors.add(message);
        }
    }

    public void addWarning(String message) {
        if (message != null && !message.isBlank()) {
            warnings.add(message);
        }
    }
}
