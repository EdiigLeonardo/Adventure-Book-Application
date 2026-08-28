import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, map, tap } from 'rxjs';

import { GameSession } from '../models/book.model';

@Injectable({ providedIn: 'root' })
export class GameService {
  private readonly gameStateSubject = new BehaviorSubject<GameSession | null>(
    null,
  );

  public readonly gameState$ = this.gameStateSubject.asObservable();
  public readonly healthPercentage$ = this.gameState$.pipe(
    map((state) => {
      if (!state) {
        return 0;
      }

      return Math.max(0, Math.min(100, (state.health / 10) * 100));
    }),
  );

  constructor(private readonly http: HttpClient) {}

  startGame(bookId: string): Observable<GameSession> {
    return this.http
      .post<GameSession>('/api/v1/games/start', { bookId })
      .pipe(tap((session) => this.syncSession(session)));
  }

  makeChoice(sessionId: string, gotoId: string): Observable<GameSession> {
    return this.http
      .post<GameSession>(`/api/v1/games/${sessionId}/choose`, { gotoId })
      .pipe(tap((session) => this.syncSession(session)));
  }

  saveGame(sessionId: string): Observable<void> {
    return this.http.post<void>(`/api/v1/games/${sessionId}/save`, {});
  }

  resumeGame(sessionId: string): Observable<GameSession> {
    return this.http
      .get<GameSession>(`/api/v1/games/${sessionId}/resume`)
      .pipe(tap((session) => this.syncSession(session)));
  }

  syncSession(session: GameSession | null): void {
    this.gameStateSubject.next(session);
    if (session && session.status === 'IN_PROGRESS') {
      sessionStorage.setItem('activeSessionId', session.id);
    } else {
      sessionStorage.removeItem('activeSessionId');
    }
  }
}
