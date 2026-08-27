package com.pictet.AdventureBookApplication.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "game_sessions")
public class GameSession {

    @Id
    @JsonProperty("id")
    private String id;

    @Column(name = "book_id", nullable = false)
    @JsonProperty("bookId")
    private String bookId;

    @Column(name = "current_section_id")
    @JsonProperty("currentSectionId")
    private String currentSectionId;

    @Column(name = "health", nullable = false)
    @JsonProperty("health")
    private int health = 10;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @JsonProperty("status")
    private GameStatus status = GameStatus.IN_PROGRESS;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_session_history", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "history_entry")
    @JsonProperty("history")
    private List<String> history = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    @JsonProperty("createdAt")
    private Instant createdAt = Instant.now();

    @jakarta.persistence.Version
    @JsonIgnore
    private Long version;

    @Transient
    @JsonIgnore
    private Book book;

    public void setHistory(List<String> history) {
        this.history = history == null ? new ArrayList<>() : history;
    }
}
