package com.pictet.AdventureBookApplication.service;

import com.pictet.AdventureBookApplication.model.Book;
import com.pictet.AdventureBookApplication.model.Consequence;
import com.pictet.AdventureBookApplication.model.ConsequenceType;
import com.pictet.AdventureBookApplication.model.GameSession;
import com.pictet.AdventureBookApplication.model.GameStatus;
import com.pictet.AdventureBookApplication.model.Option;
import com.pictet.AdventureBookApplication.model.Section;
import com.pictet.AdventureBookApplication.model.SectionType;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GameEngineService {

    public GameSession startGame(Book book) {
        if (book == null || book.getSections() == null || book.getSections().isEmpty()) {
            throw new IllegalArgumentException("A valid book with at least one section is required.");
        }

        GameSession session = new GameSession();
        session.setId(UUID.randomUUID().toString());
        session.setBookId(book.getId());
        session.setBook(book);
        session.setHealth(10);
        session.setStatus(GameStatus.IN_PROGRESS);

        Section begin = book.getSections().stream()
            .filter(section -> section != null && section.getType() == SectionType.BEGIN)
            .findFirst()
            .orElse(null);

        if (begin != null) {
            session.setCurrentSectionId(begin.getId());
            session.getHistory().add("Started at section " + begin.getId());
        }
        return session;
    }

    public GameSession chooseOption(GameSession session, String gotoId) {
        if (session == null || session.getBook() == null || session.getStatus() != GameStatus.IN_PROGRESS) {
            return session;
        }
        if (gotoId == null || gotoId.isBlank()) {
            return session;
        }

        Section currentSection = session.getBook().getSections().stream()
            .filter(section -> section != null && section.getId().equals(session.getCurrentSectionId()))
            .findFirst()
            .orElse(null);

        Option selected = (currentSection != null && currentSection.getOptions() != null)
            ? currentSection.getOptions().stream()
                .filter(option -> option != null && gotoId.equals(option.getGotoId()))
                .findFirst()
                .orElse(null)
            : null;

        if (selected != null) {
            applyConsequence(session, selected.getConsequence());
            session.setCurrentSectionId(gotoId);
            session.getHistory().add("Chose option -> " + gotoId);
        } else {
            session.getHistory().add("Invalid choice -> " + gotoId);
            return session;
        }

        Section destination = session.getBook().getSections().stream()
            .filter(section -> section != null)
            .filter(section -> gotoId.equals(section.getId()))
            .findFirst()
            .orElse(null);

        if (session.getHealth() <= 0) {
            session.setStatus(GameStatus.GAME_OVER);
        } else if (destination != null && destination.getType() == SectionType.END) {
            session.setStatus(GameStatus.VICTORY);
        }

        return session;
    }

    private void applyConsequence(GameSession session, Consequence consequence) {
        if (consequence == null) {
            return;
        }
        if (consequence.getType() == ConsequenceType.LOSE_HEALTH) {
            session.setHealth(Math.max(0, session.getHealth() - (consequence.getValue() == null ? 0 : consequence.getValue())));
        } else if (consequence.getType() == ConsequenceType.GAIN_HEALTH) {
            session.setHealth(Math.max(0, session.getHealth() + (consequence.getValue() == null ? 0 : consequence.getValue())));
        }
    }
}
