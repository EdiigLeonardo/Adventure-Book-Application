import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { Router } from '@angular/router';
import { Book, GameSession, Option } from '../../core/models/book.model';
import { GameService } from '../../core/services/game.service';
import { BookService } from '../../core/services/book.service';
import { UiStateService } from '../../core/services/ui-state.service';
import { GamePlayComponent } from '../../features/game-play/game-play.component';

@Component({
  selector: 'app-game-screen',
  standalone: true,
  imports: [CommonModule, MatButtonModule, GamePlayComponent],
  templateUrl: './game-screen.component.html',
  styleUrl: './game-screen.component.scss'
})
export class GameScreenComponent implements OnInit {
  book: Book | null = null;
  session: GameSession | null = null;
  health = 0;
  isSaving = false;

  constructor(
    private ui: UiStateService,
    private game: GameService,
    private bookService: BookService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.ui.selectedBook$.subscribe(b => this.book = b);
    this.game.gameState$.subscribe(s => {
      this.session = s;
      this.health = s ? Math.max(0, Math.min(100, s.health * 10)) : 0;
    });

    const cachedSessionId = sessionStorage.getItem('activeSessionId');
    if (!this.book && cachedSessionId) {
      this.game.resumeGame(cachedSessionId).subscribe({
        next: (session) => {
          this.bookService.getBook(session.bookId).subscribe({
            next: (book) => this.ui.setBook(book),
            error: () => this.handleRecoveryFailure()
          });
        },
        error: () => this.handleRecoveryFailure()
      });
    } else if (!this.book) {
      this.router.navigate(['/']);
    }
  }

  start(): void {
    if (this.book) this.game.startGame(this.book.id).subscribe();
  }

  choose(o: Option): void {
    if (this.session) this.game.makeChoice(this.session.id, o.gotoId).subscribe();
  }

  save(): void {
    if (this.session) {
      this.isSaving = true;
      this.game.saveGame(this.session.id).subscribe({
        complete: () => this.isSaving = false,
        error: () => this.isSaving = false
      });
    }
  }

  resume(id: string): void {
    this.game.resumeGame(id).subscribe();
  }

  back(): void {
    this.router.navigate(['/']);
  }

  private handleRecoveryFailure(): void {
    sessionStorage.removeItem('activeSessionId');
    this.router.navigate(['/']);
  }
}
