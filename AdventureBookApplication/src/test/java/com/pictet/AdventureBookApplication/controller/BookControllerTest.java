package com.pictet.AdventureBookApplication.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pictet.AdventureBookApplication.model.Book;
import com.pictet.AdventureBookApplication.model.Option;
import com.pictet.AdventureBookApplication.model.Section;
import com.pictet.AdventureBookApplication.model.SectionType;
import com.pictet.AdventureBookApplication.service.BookCatalogService;
import com.pictet.AdventureBookApplication.validation.BookValidatorService;
import com.pictet.AdventureBookApplication.validation.ValidationResult;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookCatalogService catalogService;

    @MockitoBean
    private BookValidatorService validatorService;

    // ── GET /api/v1/books ──────────────────────────────────────────────────

    @Test
    void listBooks_withNoParams_returnsAllBooks() throws Exception {
        when(catalogService.findAll(isNull(), isNull(), isNull()))
            .thenReturn(List.of(buildBook("b1", "Pirates"), buildBook("b2", "Garden")));

        mockMvc.perform(get("/api/v1/books"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is("b1")))
            .andExpect(jsonPath("$[1].id", is("b2")));
    }

    @Test
    void listBooks_withQueryParam_returnsFiltered() throws Exception {
        when(catalogService.findAll("Pirates", null, null))
            .thenReturn(List.of(buildBook("b1", "Pirates")));

        mockMvc.perform(get("/api/v1/books").param("query", "Pirates"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title", is("Pirates")));
    }

    // ── GET /api/v1/books/{id} ─────────────────────────────────────────────

    @Test
    void getBook_existingId_returnsBook() throws Exception {
        when(catalogService.findById("b1")).thenReturn(buildBook("b1", "Pirates"));

        mockMvc.perform(get("/api/v1/books/b1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is("b1")))
            .andExpect(jsonPath("$.title", is("Pirates")));
    }

    @Test
    void getBook_unknownId_returns404() throws Exception {
        when(catalogService.findById("ghost")).thenReturn(null);

        mockMvc.perform(get("/api/v1/books/ghost"))
            .andExpect(status().isNotFound());
    }

    // ── POST /api/v1/books ─────────────────────────────────────────────────

    @Test
    void uploadBook_withValidJsonFile_returnsCreated() throws Exception {
        String json = """
            {
              "id": "new-book",
              "title": "New Adventure",
              "sections": [
                {"id":"1","type":"BEGIN","text":"Go","options":[{"description":"Forward","gotoId":"2"}]},
                {"id":"2","type":"END","text":"Done"}
              ]
            }
            """;

        ValidationResult valid = new ValidationResult();
        when(validatorService.validateRawJson(anyString())).thenReturn(valid);
        when(catalogService.saveUploadedBookIdempotently(any()))
            .thenAnswer(inv -> new BookCatalogService.UploadResult(inv.getArgument(0), true));

        MockMultipartFile file = new MockMultipartFile(
            "file", "book.json", MediaType.APPLICATION_JSON_VALUE, json.getBytes()
        );

        mockMvc.perform(multipart("/api/v1/books").file(file))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is("new-book")));
    }

    @Test
    void uploadBook_withAnIdenticalBook_returnsOk() throws Exception {
        String json = """
            {
              "id": "existing-book",
              "title": "Existing Adventure",
              "sections": [
                {"id":"1","type":"BEGIN","text":"Go","options":[{"description":"Forward","gotoId":"2"}]},
                {"id":"2","type":"END","text":"Done"}
              ]
            }
            """;

        ValidationResult valid = new ValidationResult();
        when(validatorService.validateRawJson(anyString())).thenReturn(valid);
        when(catalogService.saveUploadedBookIdempotently(any()))
            .thenAnswer(inv -> new BookCatalogService.UploadResult(inv.getArgument(0), false));

        MockMultipartFile file = new MockMultipartFile(
            "file", "book.json", MediaType.APPLICATION_JSON_VALUE, json.getBytes()
        );

        mockMvc.perform(multipart("/api/v1/books").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is("existing-book")));
    }

    @Test
    void uploadBook_withEmptyFile_returnsBadRequest() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file", "empty.json", MediaType.APPLICATION_JSON_VALUE, new byte[0]
        );

        mockMvc.perform(multipart("/api/v1/books").file(emptyFile))
            .andExpect(status().isBadRequest());
    }

    @Test
    void uploadBook_withInvalidJson_returnsBadRequest() throws Exception {
        String badJson = "{ not valid }";
        ValidationResult invalid = new ValidationResult();
        invalid.addError("Invalid JSON.");
        when(validatorService.validateRawJson(anyString())).thenReturn(invalid);

        MockMultipartFile file = new MockMultipartFile(
            "file", "bad.json", MediaType.APPLICATION_JSON_VALUE, badJson.getBytes()
        );

        mockMvc.perform(multipart("/api/v1/books").file(file))
            .andExpect(status().isBadRequest());
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private Book buildBook(String id, String title) {
        Option opt = new Option();
        opt.setGotoId("end");
        opt.setDescription("Proceed");

        Section begin = new Section();
        begin.setId("begin");
        begin.setType(SectionType.BEGIN);
        begin.setText("Start");
        begin.setOptions(List.of(opt));

        Section end = new Section();
        end.setId("end");
        end.setType(SectionType.END);
        end.setText("Finish");

        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setSections(List.of(begin, end));
        return book;
    }
}
