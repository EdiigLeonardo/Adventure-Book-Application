package com.pictet.AdventureBookApplication.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pictet.AdventureBookApplication.model.Book;
import com.pictet.AdventureBookApplication.model.BookStatus;
import com.pictet.AdventureBookApplication.model.GameSession;
import com.pictet.AdventureBookApplication.model.GameStatus;
import com.pictet.AdventureBookApplication.model.Option;
import com.pictet.AdventureBookApplication.model.Section;
import com.pictet.AdventureBookApplication.model.SectionType;
import com.pictet.AdventureBookApplication.persistence.GameSessionRepository;
import com.pictet.AdventureBookApplication.service.BookCatalogService;
import com.pictet.AdventureBookApplication.service.GameEngineService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GameController.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private BookCatalogService catalogService;

    @MockitoBean
    private GameEngineService gameEngineService;

    @MockitoBean
    private GameSessionRepository gameSessionRepository;

    // ── POST /api/v1/games/start ──────────────────────────────────────────

    @Test
    void startGame_withValidBookId_returnsSession() throws Exception {
        Book book = buildBook("book-1");
        GameSession session = buildSession("sess-1", "book-1");

        when(catalogService.findById("book-1")).thenReturn(book);
        when(gameEngineService.startGame(book)).thenReturn(session);
        when(gameSessionRepository.save(session)).thenReturn(session);

        mockMvc.perform(post("/api/v1/games/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("bookId", "book-1"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is("sess-1")))
            .andExpect(jsonPath("$.bookId", is("book-1")));
    }

    @Test
    void startGame_withUnknownBookId_returns404() throws Exception {
        when(catalogService.findById("ghost-book")).thenReturn(null);

        mockMvc.perform(post("/api/v1/games/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("bookId", "ghost-book"))))
            .andExpect(status().isNotFound());
    }

    @Test
    void startGame_withInvalidBook_returnsConflict() throws Exception {
        Book invalidBook = buildBook("invalid-book");
        invalidBook.setStatus(BookStatus.INVALID);

        when(catalogService.findById("invalid-book")).thenReturn(invalidBook);

        mockMvc.perform(post("/api/v1/games/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("bookId", "invalid-book"))))
            .andExpect(status().isConflict());
    }

    // ── POST /api/v1/games/{id}/choose ────────────────────────────────────

    @Test
    void chooseOption_withValidSessionAndOption_returnsUpdatedSession() throws Exception {
        GameSession session = buildSession("sess-1", "book-1");
        Book book = buildBook("book-1");
        GameSession updated = buildSession("sess-1", "book-1");
        updated.setCurrentSectionId("end");

        when(gameSessionRepository.findById("sess-1")).thenReturn(Optional.of(session));
        when(catalogService.findById("book-1")).thenReturn(book);
        when(gameEngineService.chooseOption(any(), eq("end"))).thenReturn(updated);
        when(gameSessionRepository.save(updated)).thenReturn(updated);

        mockMvc.perform(post("/api/v1/games/sess-1/choose")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("gotoId", "end"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is("sess-1")));
    }

    @Test
    void chooseOption_withUnknownSessionId_returns404() throws Exception {
        when(gameSessionRepository.findById("bad-sess")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/games/bad-sess/choose")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("gotoId", "s-2"))))
            .andExpect(status().isNotFound());
    }

    @Test
    void chooseOption_whenBookNotFound_returns404() throws Exception {
        GameSession session = buildSession("sess-1", "ghost-book");

        when(gameSessionRepository.findById("sess-1")).thenReturn(Optional.of(session));
        when(catalogService.findById("ghost-book")).thenReturn(null);

        mockMvc.perform(post("/api/v1/games/sess-1/choose")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("gotoId", "s-2"))))
            .andExpect(status().isNotFound());
    }

    // ── POST /api/v1/games/{id}/save ──────────────────────────────────────

    @Test
    void saveGame_withValidSession_returnsOk() throws Exception {
        GameSession session = buildSession("sess-1", "book-1");
        Book book = buildBook("book-1");

        when(gameSessionRepository.findById("sess-1")).thenReturn(Optional.of(session));
        when(catalogService.findById("book-1")).thenReturn(book);
        when(gameSessionRepository.save(any())).thenReturn(session);

        mockMvc.perform(post("/api/v1/games/sess-1/save"))
            .andExpect(status().isOk());
    }

    @Test
    void saveGame_withUnknownSession_returns404() throws Exception {
        when(gameSessionRepository.findById("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/games/unknown/save"))
            .andExpect(status().isNotFound());
    }

    // ── GET /api/v1/games/{id}/resume ─────────────────────────────────────

    @Test
    void resumeGame_withValidSession_returnsSession() throws Exception {
        GameSession session = buildSession("sess-1", "book-1");
        Book book = buildBook("book-1");

        when(gameSessionRepository.findById("sess-1")).thenReturn(Optional.of(session));
        when(catalogService.findById("book-1")).thenReturn(book);

        mockMvc.perform(get("/api/v1/games/sess-1/resume"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is("sess-1")))
            .andExpect(jsonPath("$.bookId", is("book-1")));
    }

    @Test
    void resumeGame_withUnknownSession_returns404() throws Exception {
        when(gameSessionRepository.findById("gone")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/games/gone/resume"))
            .andExpect(status().isNotFound());
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private Book buildBook(String id) {
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
        book.setTitle("Adventure");
        book.setSections(List.of(begin, end));
        return book;
    }

    private GameSession buildSession(String id, String bookId) {
        GameSession session = new GameSession();
        session.setId(id);
        session.setBookId(bookId);
        session.setCurrentSectionId("begin");
        session.setHealth(10);
        session.setStatus(GameStatus.IN_PROGRESS);
        session.setHistory(new ArrayList<>());
        return session;
    }
}
