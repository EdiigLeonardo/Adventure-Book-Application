package com.pictet.AdventureBookApplication.controller;

import com.pictet.AdventureBookApplication.model.Book;
import com.pictet.AdventureBookApplication.service.BookCatalogService;
import com.pictet.AdventureBookApplication.validation.BookValidatorService;
import com.pictet.AdventureBookApplication.validation.ValidationResult;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
public class BookController {

    private final BookCatalogService catalogService;
    private final BookValidatorService validatorService;

    public BookController(BookCatalogService catalogService, BookValidatorService validatorService) {
        this.catalogService = catalogService;
        this.validatorService = validatorService;
    }

    @GetMapping("/books")
    public List<Book> listBooks(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) String difficulty,
        @RequestParam(required = false) String status
    ) {
        return catalogService.findAll(query, difficulty, status);
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<Book> getBook(@PathVariable String id) {
        Book book = catalogService.findById(id);
        return book == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(book);
    }

    @PostMapping(value = "/books", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadBook(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("A JSON book file is required.");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".json")) {
            throw new IllegalArgumentException("Only .json files are accepted.");
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.equalsIgnoreCase(MediaType.APPLICATION_JSON_VALUE)
                && !contentType.equalsIgnoreCase(MediaType.TEXT_PLAIN_VALUE)
                && !contentType.equalsIgnoreCase(MediaType.APPLICATION_OCTET_STREAM_VALUE)) {
            throw new IllegalArgumentException("The uploaded file must contain JSON.");
        }

        try {
            String json = new String(file.getBytes(), StandardCharsets.UTF_8);
            ValidationResult validationResult = validatorService.validateRawJson(json);
            if (!validationResult.isValid()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationResult);
            }

            Book book = new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, Book.class);
            BookCatalogService.UploadResult result = catalogService.saveUploadedBookIdempotently(book);
            return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK).body(result.book());
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to parse the uploaded JSON file.", ex);
        }
    }
}
