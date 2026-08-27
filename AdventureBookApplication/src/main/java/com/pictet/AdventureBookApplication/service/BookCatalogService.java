package com.pictet.AdventureBookApplication.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pictet.AdventureBookApplication.model.Book;
import com.pictet.AdventureBookApplication.validation.BookValidatorService;
import com.pictet.AdventureBookApplication.validation.ValidationResult;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class BookCatalogService {

    private static final Logger log = LoggerFactory.getLogger(BookCatalogService.class);

    private final Map<String, Book> booksById = new LinkedHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BookValidatorService validatorService;

    public BookCatalogService(BookValidatorService validatorService) {
        this.validatorService = validatorService;
        loadDefaultBooks();
    }

    public List<Book> findAll(String query, String difficulty, String status) {
        return booksById.values().stream()
            .filter(book -> query == null || query.isBlank() || containsQuery(book, query))
            .filter(book -> difficulty == null || difficulty.isBlank()
                || Objects.equals(book.getDifficulty(), difficulty.trim().toUpperCase(java.util.Locale.ROOT)))
            .filter(book -> status == null || status.isBlank() || Objects.equals(book.getStatus(), status))
            .collect(Collectors.toList());
    }

    public Book findById(String id) {
        return booksById.get(id);
    }

    public Book saveUploadedBook(Book book) {
        ValidationResult validationResult = validatorService.validate(book);
        if (!validationResult.isValid()) {
            throw new IllegalArgumentException(String.join("; ", validationResult.getErrors()));
        }
        normalizeDifficulty(book);
        book.setStatus("VALID");
        booksById.put(book.getId(), book);
        return book;
    }

    private void loadDefaultBooks() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:/books/*.json");
            for (Resource resource : resources) {
                if (resource == null || resource.contentLength() == 0) {
                    continue;
                }
                try (InputStream inputStream = resource.getInputStream()) {
                    Object raw = objectMapper.readValue(inputStream, Object.class);
                    List<Book> books = new ArrayList<>();

                    if (raw instanceof java.util.List<?> list) {
                        for (Object item : list) {
                            if (item instanceof java.util.Map<?, ?>) {
                                books.add(objectMapper.convertValue(item, Book.class));
                            }
                        }
                    } else if (raw instanceof java.util.Map<?, ?>) {
                        books.add(objectMapper.convertValue(raw, Book.class));
                    }

                    for (Book book : books) {
                        if (book == null || book.getId() == null || book.getId().isBlank()) {
                            continue;
                        }
                        normalizeDifficulty(book);
                        ValidationResult validationResult = validatorService.validate(book);
                        book.setStatus(validationResult.isValid() ? "VALID" : "INVALID");
                        booksById.put(book.getId(), book);
                    }
                } catch (Exception ex) {
                    // Keep the application running, but make malformed user-provided resources observable.
                    log.warn("Unable to load book resource {}: {}", resource.getDescription(), ex.getMessage());
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load seeded books from classpath", e);
        }
    }

    private boolean containsQuery(Book book, String query) {
        String q = query.toLowerCase();
        return (book.getTitle() != null && book.getTitle().toLowerCase().contains(q))
            || (book.getDescription() != null && book.getDescription().toLowerCase().contains(q))
            || (book.getDifficulty() != null && book.getDifficulty().toLowerCase().contains(q));
    }

    private void normalizeDifficulty(Book book) {
        if ("BEGINNER".equalsIgnoreCase(book.getDifficulty())) {
            book.setDifficulty("BEGINNER");
        } else if ("INTERMEDIATE".equalsIgnoreCase(book.getDifficulty())) {
            book.setDifficulty("INTERMEDIATE");
        } else {
            book.setDifficulty("ADVANCED");
        }
    }
}
