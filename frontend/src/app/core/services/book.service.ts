import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Book, BookUploadResponse, GameSession } from '../models/book.model';

@Injectable({ providedIn: 'root' })
export class BookService {
  constructor(private readonly http: HttpClient) {}

  listBooks(query?: string, difficulty?: string): Observable<Book[]> {
    let params = new HttpParams();
    if (query?.trim()) params = params.set('query', query.trim());
    if (difficulty && difficulty !== 'ALL') params = params.set('difficulty', difficulty);
    return this.http.get<Book[]>('/api/v1/books', { params });
  }

  getBook(id: string): Observable<Book> {
    return this.http.get<Book>(`/api/v1/books/${id}`);
  }

  startGame(bookId: string): Observable<GameSession> {
    return this.http.post<GameSession>('/api/v1/games/start', { bookId });
  }

  choose(sessionId: string, gotoId: string): Observable<GameSession> {
    return this.http.post<GameSession>(`/api/v1/games/${sessionId}/choose`, {
      gotoId,
    });
  }

  saveGame(sessionId: string): Observable<void> {
    return this.http.post<void>(`/api/v1/games/${sessionId}/save`, {});
  }

  resumeGame(sessionId: string): Observable<GameSession> {
    return this.http.get<GameSession>(`/api/v1/games/${sessionId}/resume`);
  }

  uploadBook(file: File): Observable<BookUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<BookUploadResponse>('/api/v1/books', formData);
  }
}
