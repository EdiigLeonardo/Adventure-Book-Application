import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Book } from '../models/book.model';

@Injectable({ providedIn: 'root' })
export class UiStateService {
  private readonly selectedBookSubject = new BehaviorSubject<Book | null>(null);
  readonly selectedBook$ = this.selectedBookSubject.asObservable();
  setBook(book: Book | null): void { this.selectedBookSubject.next(book); }
}
