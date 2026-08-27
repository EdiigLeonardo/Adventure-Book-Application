package com.pictet.AdventureBookApplication.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pictet.AdventureBookApplication.model.Book;
import com.pictet.AdventureBookApplication.model.Consequence;
import com.pictet.AdventureBookApplication.model.ConsequenceType;
import com.pictet.AdventureBookApplication.model.GameSession;
import com.pictet.AdventureBookApplication.model.GameStatus;
import com.pictet.AdventureBookApplication.model.Option;
import com.pictet.AdventureBookApplication.model.Section;
import com.pictet.AdventureBookApplication.model.SectionType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameEngineServiceTest {

    private GameEngineService engine;

    @BeforeEach
    void setUp() {
        engine = new GameEngineService();
    }

    // ── startGame ────────────────────────────────────────────────────────────

    @Test
    void startGame_withValidBook_setsBeginSectionAndInitialHealth() {
        Book book = buildMinimalBook();

        GameSession session = engine.startGame(book);

        assertThat(session).isNotNull();
        assertThat(session.getBookId()).isEqualTo("book-1");
        assertThat(session.getCurrentSectionId()).isEqualTo("begin");
        assertThat(session.getHealth()).isEqualTo(10);
        assertThat(session.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(session.getId()).isNotNull();
    }

    @Test
    void startGame_withNullBook_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> engine.startGame(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void startGame_withNoSections_throwsIllegalArgumentException() {
        Book book = new Book();
        book.setId("book-empty");
        book.setSections(List.of());

        assertThatThrownBy(() -> engine.startGame(book))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void startGame_historyContainsBeginEntry() {
        Book book = buildMinimalBook();

        GameSession session = engine.startGame(book);

        assertThat(session.getHistory()).isNotEmpty();
        assertThat(session.getHistory().getFirst()).contains("begin");
    }

    // ── chooseOption ─────────────────────────────────────────────────────────

    @Test
    void chooseOption_withValidOption_movesToNewSection() {
        Book book = buildTwoSectionBook("node", SectionType.NODE);
        GameSession session = engine.startGame(book);

        session = engine.chooseOption(session, "node");

        assertThat(session.getCurrentSectionId()).isEqualTo("node");
        assertThat(session.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
    }

    @Test
    void chooseOption_reachingEndSection_setsVictory() {
        Book book = buildMinimalBook(); // begin -> end
        GameSession session = engine.startGame(book);

        session = engine.chooseOption(session, "end");

        assertThat(session.getStatus()).isEqualTo(GameStatus.VICTORY);
        assertThat(session.getCurrentSectionId()).isEqualTo("end");
    }

    @Test
    void chooseOption_withLoseHealthConsequence_decreasesHealth() {
        Book book = buildBookWithConsequence(ConsequenceType.LOSE_HEALTH, 3);
        GameSession session = engine.startGame(book);

        session = engine.chooseOption(session, "end");

        assertThat(session.getHealth()).isEqualTo(7);
    }

    @Test
    void chooseOption_withGainHealthConsequence_increasesHealth() {
        Book book = buildBookWithConsequence(ConsequenceType.GAIN_HEALTH, 2);
        GameSession session = engine.startGame(book);
        session.setHealth(5);

        session = engine.chooseOption(session, "end");

        assertThat(session.getHealth()).isEqualTo(7);
    }

    @Test
    void chooseOption_healthCannotGoBelowZero() {
        Book book = buildBookWithConsequence(ConsequenceType.LOSE_HEALTH, 99);
        GameSession session = engine.startGame(book);

        session = engine.chooseOption(session, "end");

        assertThat(session.getHealth()).isZero();
    }

    @Test
    void chooseOption_whenHealthReachesZero_setsGameOver() {
        Book book = buildBookWithConsequence(ConsequenceType.LOSE_HEALTH, 10);
        GameSession session = engine.startGame(book);

        session = engine.chooseOption(session, "end");

        assertThat(session.getHealth()).isZero();
        assertThat(session.getStatus()).isEqualTo(GameStatus.GAME_OVER);
    }

    @Test
    void chooseOption_whenAlreadyGameOver_doesNothing() {
        Book book = buildMinimalBook();
        GameSession session = engine.startGame(book);
        session.setStatus(GameStatus.GAME_OVER);

        GameSession unchanged = engine.chooseOption(session, "end");

        assertThat(unchanged.getCurrentSectionId()).isEqualTo("begin");
    }

    @Test
    void chooseOption_whenAlreadyVictory_doesNothing() {
        Book book = buildMinimalBook();
        GameSession session = engine.startGame(book);
        session.setStatus(GameStatus.VICTORY);

        GameSession unchanged = engine.chooseOption(session, "end");

        assertThat(unchanged.getCurrentSectionId()).isEqualTo("begin");
    }

    @Test
    void chooseOption_withNullGotoId_returnsUnchangedSession() {
        Book book = buildMinimalBook();
        GameSession session = engine.startGame(book);

        GameSession result = engine.chooseOption(session, null);

        assertThat(result.getCurrentSectionId()).isEqualTo("begin");
    }

    @Test
    void chooseOption_withBlankGotoId_returnsUnchangedSession() {
        Book book = buildMinimalBook();
        GameSession session = engine.startGame(book);

        GameSession result = engine.chooseOption(session, "  ");

        assertThat(result.getCurrentSectionId()).isEqualTo("begin");
    }

    @Test
    void chooseOption_withInvalidGotoId_doesNotAdvance() {
        Book book = buildMinimalBook();
        GameSession session = engine.startGame(book);

        GameSession result = engine.chooseOption(session, "non-existent");

        assertThat(result.getCurrentSectionId()).isEqualTo("begin");
        assertThat(result.getHistory()).anyMatch(e -> e.contains("Invalid choice"));
    }

    @Test
    void chooseOption_withNullSession_returnsNull() {
        GameSession result = engine.chooseOption(null, "end");
        assertThat(result).isNull();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Book buildMinimalBook() {
        Section begin = new Section();
        begin.setId("begin");
        begin.setType(SectionType.BEGIN);
        begin.setText("Start here");

        Section end = new Section();
        end.setId("end");
        end.setType(SectionType.END);
        end.setText("The end");

        Option opt = new Option();
        opt.setGotoId("end");
        opt.setDescription("Proceed");
        begin.setOptions(List.of(opt));

        Book book = new Book();
        book.setId("book-1");
        book.setTitle("Test Book");
        book.setSections(List.of(begin, end));
        return book;
    }

    private Book buildTwoSectionBook(String nextId, SectionType nextType) {
        Section begin = new Section();
        begin.setId("begin");
        begin.setType(SectionType.BEGIN);
        begin.setText("Start");

        Section next = new Section();
        next.setId(nextId);
        next.setType(nextType);
        next.setText("Next scene");

        Option opt = new Option();
        opt.setGotoId(nextId);
        opt.setDescription("Go forward");
        begin.setOptions(List.of(opt));

        if (nextType == SectionType.NODE) {
            Section end = new Section();
            end.setId("end");
            end.setType(SectionType.END);
            end.setText("End");
            next.setOptions(List.of());
            Book book = new Book();
            book.setId("book-1");
            book.setTitle("Test");
            book.setSections(List.of(begin, next, end));
            return book;
        }

        Book book = new Book();
        book.setId("book-1");
        book.setTitle("Test");
        book.setSections(List.of(begin, next));
        return book;
    }

    private Book buildBookWithConsequence(ConsequenceType type, int value) {
        Section begin = new Section();
        begin.setId("begin");
        begin.setType(SectionType.BEGIN);
        begin.setText("Start");

        Section end = new Section();
        end.setId("end");
        end.setType(SectionType.END);
        end.setText("The End");

        Consequence consequence = new Consequence();
        consequence.setType(type);
        consequence.setValue(value);

        Option opt = new Option();
        opt.setGotoId("end");
        opt.setDescription("Proceed");
        opt.setConsequence(consequence);
        begin.setOptions(List.of(opt));

        Book book = new Book();
        book.setId("book-1");
        book.setTitle("Test");
        book.setSections(List.of(begin, end));
        return book;
    }
}
