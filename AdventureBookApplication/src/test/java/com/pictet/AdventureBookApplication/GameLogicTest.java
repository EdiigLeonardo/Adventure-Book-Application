package com.pictet.AdventureBookApplication;

import static org.assertj.core.api.Assertions.assertThat;

import com.pictet.AdventureBookApplication.model.Book;
import com.pictet.AdventureBookApplication.model.Consequence;
import com.pictet.AdventureBookApplication.model.ConsequenceType;
import com.pictet.AdventureBookApplication.model.GameSession;
import com.pictet.AdventureBookApplication.model.GameStatus;
import com.pictet.AdventureBookApplication.model.Option;
import com.pictet.AdventureBookApplication.model.Section;
import com.pictet.AdventureBookApplication.model.SectionType;
import com.pictet.AdventureBookApplication.persistence.GameSessionRepository;
import com.pictet.AdventureBookApplication.service.GameEngineService;
import com.pictet.AdventureBookApplication.validation.BookValidatorService;
import com.pictet.AdventureBookApplication.validation.ValidationResult;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GameLogicTest {

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Test
    void validatorAcceptsAValidAdventureGraph() {
        Book book = new Book();
        book.setId("test-book");
        book.setTitle("Test Adventure");
        book.setDifficulty("easy");

        Section begin = new Section();
        begin.setId("1");
        begin.setType(SectionType.BEGIN);
        begin.setText("Start");
        begin.setOptions(List.of(new Option("2", "Walk on", null)));

        Section end = new Section();
        end.setId("2");
        end.setType(SectionType.END);
        end.setText("Finish");
        end.setOptions(List.of());

        begin.getOptions().getFirst().setGotoId("2");
        book.setSections(List.of(begin, end));

        BookValidatorService validator = new BookValidatorService();
        ValidationResult result = validator.validate(book);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void engineAppliesLossAndEndsTheGameWhenHpReachesZero() {
        Book book = new Book();
        book.setId("test-book");
        book.setTitle("Test Adventure");
        book.setDifficulty("easy");

        Section begin = new Section();
        begin.setId("1");
        begin.setType(SectionType.BEGIN);
        begin.setText("Start");

        Consequence consequence = new Consequence();
        consequence.setType(ConsequenceType.LOSE_HEALTH);
        consequence.setValue(10);

        Option option = new Option();
        option.setId("opt-1");
        option.setText("Take the risk");
        option.setGotoId("2");
        option.setConsequence(consequence);
        begin.setOptions(List.of(option));

        Section end = new Section();
        end.setId("2");
        end.setType(SectionType.END);
        end.setText("You have survived");
        end.setOptions(List.of());

        book.setSections(List.of(begin, end));

        GameEngineService engine = new GameEngineService();
        GameSession session = engine.startGame(book);
        session = engine.chooseOption(session, "2");

        assertThat(session.getHealth()).isZero();
        assertThat(session.getStatus()).isEqualTo(GameStatus.GAME_OVER);
    }

    @Test
    void saveAndResumePersistsSessionState() {
        GameSession session = new GameSession();
        session.setId("session-123");
        session.setBookId("book-1");
        session.setCurrentSectionId("s-2");
        session.setHealth(7);
        session.setStatus(GameStatus.IN_PROGRESS);
        session.setHistory(List.of("start", "choice-a"));

        GameSession saved = gameSessionRepository.save(session);
        GameSession resumed = gameSessionRepository.findById(saved.getId()).orElseThrow();

        assertThat(saved.getId()).isEqualTo("session-123");
        assertThat(resumed.getHealth()).isEqualTo(7);
        assertThat(resumed.getCurrentSectionId()).isEqualTo("s-2");
        assertThat(resumed.getHistory()).containsExactly("start", "choice-a");
    }
}
