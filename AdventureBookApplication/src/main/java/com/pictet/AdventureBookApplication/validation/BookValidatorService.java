package com.pictet.AdventureBookApplication.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pictet.AdventureBookApplication.model.Book;
import com.pictet.AdventureBookApplication.model.Option;
import com.pictet.AdventureBookApplication.model.Section;
import com.pictet.AdventureBookApplication.model.SectionType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class BookValidatorService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ValidationResult validate(Book book) {
        ValidationResult result = new ValidationResult();

        if (book == null) {
            result.addError("Book is required.");
            return result;
        }

        List<Section> sections = book.getSections() == null ? new ArrayList<>() : book.getSections();
        if (sections.isEmpty()) {
            result.addError("Book must contain at least one section.");
            return result;
        }

        Map<String, Section> byId = new HashMap<>();
        for (Section section : sections) {
            if (section == null || section.getId() == null || section.getId().isBlank()) {
                result.addError("A section is missing a valid identifier.");
                continue;
            }
            byId.put(section.getId(), section);
        }

        long beginCount = sections.stream().filter(s -> s != null && s.getType() == SectionType.BEGIN).count();
        if (beginCount != 1) {
            result.addError("A book must contain exactly one BEGIN section.");
        }

        long endCount = sections.stream().filter(s -> s != null && s.getType() == SectionType.END).count();
        if (endCount == 0) {
            result.addError("A book must contain at least one END section.");
        }

        Section begin = sections.stream().filter(s -> s != null && s.getType() == SectionType.BEGIN).findFirst().orElse(null);
        if (begin == null || begin.getId() == null) {
            return result;
        }

        Set<String> visited = new HashSet<>();
        Queue<String> queue = new ArrayDeque<>();
        queue.add(begin.getId());

        while (!queue.isEmpty()) {
            String currentId = queue.poll();
            if (!visited.add(currentId)) {
                continue;
            }

            Section current = byId.get(currentId);
            if (current == null) {
                result.addError("The graph references a section that does not exist: " + currentId);
                continue;
            }

            if (current.getOptions() == null) {
                continue;
            }

            for (Option option : current.getOptions()) {
                if (option == null) {
                    continue;
                }
                if (option.getGotoId() == null || option.getGotoId().isBlank()) {
                    result.addError("Section " + currentId + " contains an option without a destination.");
                    continue;
                }
                if (!byId.containsKey(option.getGotoId())) {
                    result.addError("Section " + currentId + " points to a non-existent target: " + option.getGotoId());
                    continue;
                }
                queue.add(option.getGotoId());
            }
        }

        Set<String> reachable = visited;
        for (Section section : sections) {
            if (section == null || section.getId() == null) {
                continue;
            }
            if (!reachable.contains(section.getId())) {
                result.addWarning("Section " + section.getId() + " is unreachable from the BEGIN node and is ignored as an orphan.");
            }
        }

        for (Section section : sections) {
            if (section == null || section.getId() == null) {
                continue;
            }
            if (!reachable.contains(section.getId())) {
                continue;
            }
            if (section.getType() != SectionType.END && (section.getOptions() == null || section.getOptions().isEmpty())) {
                result.addError("Reachable section " + section.getId() + " does not contain any options and is not an END node.");
            }
        }

        return result;
    }

    public ValidationResult validateRawJson(String jsonContent) {
        ValidationResult result = new ValidationResult();
        if (jsonContent == null || jsonContent.isBlank()) {
            result.addError("The JSON content is empty.");
            return result;
        }

        try {
            Book book = objectMapper.readValue(jsonContent, Book.class);
            return validate(book);
        } catch (JsonProcessingException ex) {
            result.addError("Invalid JSON structure: " + ex.getOriginalMessage());
            return result;
        }
    }
}
