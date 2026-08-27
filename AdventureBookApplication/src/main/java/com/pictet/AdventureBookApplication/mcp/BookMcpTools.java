package com.pictet.AdventureBookApplication.mcp;

import com.pictet.AdventureBookApplication.model.Book;
import com.pictet.AdventureBookApplication.service.BookCatalogService;
import com.pictet.AdventureBookApplication.validation.BookValidatorService;
import com.pictet.AdventureBookApplication.validation.ValidationResult;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class BookMcpTools {

    private final BookCatalogService catalogService;
    private final BookValidatorService validatorService;

    public BookMcpTools(BookCatalogService catalogService, BookValidatorService validatorService) {
        this.catalogService = catalogService;
        this.validatorService = validatorService;
    }

    @Tool(description = "List books with optional difficulty and validation status filters")
    public List<Book> listBooks(String difficulty, String status) {
        return catalogService.findAll(null, difficulty, status);
    }

    @Tool(description = "Return the complete details of a specific book")
    public Book getBookDetails(String bookId) {
        return catalogService.findById(bookId);
    }

    @Tool(description = "Validate a raw JSON adventure book before publishing it")
    public ValidationResult validateBookJson(String jsonContent) {
        return validatorService.validateRawJson(jsonContent);
    }
}
