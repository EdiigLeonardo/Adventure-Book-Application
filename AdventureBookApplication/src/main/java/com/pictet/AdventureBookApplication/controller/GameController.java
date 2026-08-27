package com.pictet.AdventureBookApplication.controller;

import com.pictet.AdventureBookApplication.model.Book;
import com.pictet.AdventureBookApplication.model.GameSession;
import com.pictet.AdventureBookApplication.persistence.GameSessionRepository;
import com.pictet.AdventureBookApplication.service.BookCatalogService;
import com.pictet.AdventureBookApplication.service.GameEngineService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class GameController {

    private final BookCatalogService catalogService;
    private final GameEngineService gameEngineService;
    private final GameSessionRepository gameSessionRepository;

    public GameController(BookCatalogService catalogService, GameEngineService gameEngineService,
            GameSessionRepository gameSessionRepository) {
        this.catalogService = catalogService;
        this.gameEngineService = gameEngineService;
        this.gameSessionRepository = gameSessionRepository;
    }

    @PostMapping("/games/start")
    public ResponseEntity<GameSession> startGame(@RequestBody Map<String, String> payload) {
        if (payload == null || payload.get("bookId") == null || payload.get("bookId").isBlank()) {
            throw new IllegalArgumentException("bookId is required.");
        }
        String bookId = payload.get("bookId");
        Book book = catalogService.findById(bookId);
        if (book == null) {
            return ResponseEntity.notFound().build();
        }

        GameSession session = gameEngineService.startGame(book);
        GameSession saved = gameSessionRepository.save(session);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/games/{id}/choose")
    public ResponseEntity<GameSession> chooseOption(@PathVariable String id, @RequestBody Map<String, String> payload) {
        GameSession session = gameSessionRepository.findById(id).orElse(null);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        Book book = catalogService.findById(session.getBookId());
        if (book == null) {
            return ResponseEntity.notFound().build();
        }

        session.setBook(book);
        String gotoId = payload.get("gotoId");
        if (gotoId == null || gotoId.isBlank()) {
            throw new IllegalArgumentException("gotoId is required.");
        }
        var current = book.getSections().stream().filter(s -> s != null && s.getId() != null && s.getId().equals(session.getCurrentSectionId())).findFirst().orElse(null);
        if (current == null || current.getOptions() == null || current.getOptions().stream().noneMatch(o -> gotoId.equals(o.getGotoId()))) {
            throw new IllegalArgumentException("The selected option is not available from the current section.");
        }
        GameSession updated = gameEngineService.chooseOption(session, gotoId);
        GameSession saved = gameSessionRepository.save(updated);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/games/{id}/save")
    public ResponseEntity<Void> saveGame(@PathVariable String id) {
        GameSession session = gameSessionRepository.findById(id).orElse(null);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        Book book = catalogService.findById(session.getBookId());
        if (book != null) {
            session.setBook(book);
        }
        gameSessionRepository.save(session);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/games/{id}/resume")
    public ResponseEntity<GameSession> resumeGame(@PathVariable String id) {
        GameSession session = gameSessionRepository.findById(id).orElse(null);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        Book book = catalogService.findById(session.getBookId());
        if (book != null) {
            session.setBook(book);
        }
        return ResponseEntity.ok(session);
    }
}
