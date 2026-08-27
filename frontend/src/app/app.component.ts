import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatToolbarModule } from '@angular/material/toolbar';

import { Book, GameSession, Section } from './core/models/book.model';
import { BookService } from './core/services/book.service';
import { GameService } from './core/services/game.service';
import { BookCatalogComponent } from './features/book-catalog/book-catalog.component';
import { BookUploadComponent } from './features/book-upload/book-upload.component';
import { GamePlayComponent } from './features/game-play/game-play.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    MatToolbarModule,
    MatCardModule,
    MatSnackBarModule,
    BookCatalogComponent,
    BookUploadComponent,
    GamePlayComponent,
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent implements OnInit {
  title = 'Adventure Book Application';
  books: Book[] = [];
  selectedBook: Book | null = null;
  session: GameSession | null = null;
  resumeSessionId = '';
  loading = false;
  isSaving = false;
  isUploading = false;

  constructor(
    private readonly bookService: BookService,
    private readonly gameService: GameService,
    private readonly snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.gameService.gameState$.subscribe((session) => {
      this.session = session;
    });
    this.loadBooks();
  }

  loadBooks(): void {
    this.loading = true;
    this.bookService.listBooks().subscribe({
      next: (books) => {
        this.books = books;
        if (!this.selectedBook && books.length > 0) {
          this.selectedBook = books[0];
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.showMessage('Unable to load adventure books right now.');
      },
    });
  }

  selectBook(book: Book): void {
    this.selectedBook = book;
    this.session = null;
  }

  startGame(): void {
    if (!this.selectedBook) {
      return;
    }

    this.gameService.startGame(this.selectedBook.id).subscribe({
      next: () => {
        this.showMessage(
          `Adventure started for ${this.selectedBook?.title ?? 'this book'}.`,
        );
      },
      error: () => {
        this.showMessage('Unable to start a new game session.');
      },
    });
  }

  chooseOption(option: { description: string; gotoId: string }): void {
    if (!this.session) {
      return;
    }

    this.gameService.makeChoice(this.session.id, option.gotoId).subscribe({
      next: () => {
        this.showMessage(option.description);
      },
      error: () => {
        this.showMessage('The choice could not be applied.');
      },
    });
  }

  saveGame(): void {
    if (!this.session) {
      return;
    }

    this.isSaving = true;
    this.gameService.saveGame(this.session.id).subscribe({
      next: () => {
        this.isSaving = false;
        this.showMessage('Progress saved successfully.');
      },
      error: () => {
        this.isSaving = false;
        this.showMessage('The game could not be saved.');
      },
    });
  }

  resumeGame(sessionId?: string): void {
    const id = (sessionId ?? this.resumeSessionId).trim();
    if (!id) {
      this.showMessage('Please provide a saved session id before resuming.');
      return;
    }

    this.resumeSessionId = id;
    this.gameService.resumeGame(id).subscribe({
      next: () => {
        this.showMessage('Saved session restored.');
      },
      error: () => {
        this.showMessage('The saved session could not be resumed.');
      },
    });
  }

  currentSection(): Section | undefined {
    if (!this.selectedBook || !this.session) {
      return undefined;
    }

    return this.selectedBook.sections.find(
      (section) => section.id === this.session?.currentSectionId,
    );
  }

  healthPercent(): number {
    if (!this.session) {
      return 0;
    }

    return Math.max(0, Math.min(100, (this.session.health / 10) * 100));
  }

  onBookUploaded(file: File): void {
    if (!file) {
      return;
    }

    this.isUploading = true;
    this.bookService.uploadBook(file).subscribe({
      next: (book) => {
        this.isUploading = false;
        this.selectedBook = book;
        this.loadBooks();
        this.showMessage(`Uploaded ${book.title}.`);
      },
      error: () => {
        this.isUploading = false;
        this.showMessage('The book file could not be uploaded.');
      },
    });
  }

  startAdventure(book: Book): void {
    this.selectedBook = book;
    this.startGame();
  }

  private showMessage(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
    });
  }
}
