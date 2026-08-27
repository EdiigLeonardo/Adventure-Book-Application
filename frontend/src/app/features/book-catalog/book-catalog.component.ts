import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';

import { Book } from '../../core/models/book.model';

@Component({
  selector: 'app-book-catalog',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatCardModule, MatIconModule, MatListModule],
  templateUrl: './book-catalog.component.html',
  styleUrl: './book-catalog.component.scss',
})
export class BookCatalogComponent {
  @Input() books: Book[] = [];
  @Input() selectedBookId: string | null = null;

  @Output() bookSelected = new EventEmitter<Book>();
  @Output() adventureStarted = new EventEmitter<Book>();
  @Output() searchChanged = new EventEmitter<string>();
  @Output() difficultyChanged = new EventEmitter<string>();
  activeDifficulty = 'ALL';

  selectBook(book: Book): void {
    this.bookSelected.emit(book);
  }

  setDifficulty(value: string): void { this.activeDifficulty = value; this.difficultyChanged.emit(value); }

  startAdventure(book: Book, event: Event): void {
    event.stopPropagation();
    this.bookSelected.emit(book);
    this.adventureStarted.emit(book);
  }
}
