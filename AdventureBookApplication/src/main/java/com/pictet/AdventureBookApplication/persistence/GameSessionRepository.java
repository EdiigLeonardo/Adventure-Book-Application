package com.pictet.AdventureBookApplication.persistence;

import com.pictet.AdventureBookApplication.model.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameSessionRepository extends JpaRepository<GameSession, String> {
}
