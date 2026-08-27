import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { Book } from '../../core/models/book.model';
import { BookService } from '../../core/services/book.service';
import { UiStateService } from '../../core/services/ui-state.service';
import { BookCatalogComponent } from '../../features/book-catalog/book-catalog.component';
import { BookUploadComponent } from '../../features/book-upload/book-upload.component';
import { Subject, debounceTime, distinctUntilChanged, switchMap, takeUntil } from 'rxjs';

interface CatalogFilters {
  query: string;
  difficulty: string;
}

@Component({ selector:'app-main-screen', standalone:true, imports:[CommonModule, MatSnackBarModule, BookCatalogComponent, BookUploadComponent], templateUrl:'./main-screen.component.html', styleUrl:'./main-screen.component.scss' })
export class MainScreenComponent implements OnInit, OnDestroy {
  books: Book[] = []; isUploading = false;
  private readonly filters$ = new Subject<CatalogFilters>();
  private readonly destroy$ = new Subject<void>();
  private query = '';
  private difficulty = 'ALL';
  constructor(private booksApi: BookService, private ui: UiStateService, private router: Router, private snack: MatSnackBar) {}
  ngOnInit(): void {
    this.filters$.pipe(
      debounceTime(250),
      distinctUntilChanged((previous, current) => previous.query === current.query && previous.difficulty === current.difficulty),
      switchMap(({ query, difficulty }) => this.booksApi.listBooks(query, difficulty)),
      takeUntil(this.destroy$),
    ).subscribe({ next: b => this.books = b, error: () => this.snack.open('Unable to load adventures.', 'Close', {duration:3000}) });
    this.refreshBooks();
  }
  search(query: string): void { this.query = query; this.refreshBooks(); }
  filter(difficulty: string): void { this.difficulty = difficulty; this.refreshBooks(); }
  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }
  select(book: Book): void { this.ui.setBook(book); }
  start(book: Book): void { this.ui.setBook(book); this.router.navigate(['/game']); }
  upload(file: File): void { this.isUploading=true; this.booksApi.uploadBook(file).subscribe({next: book => {this.isUploading=false; this.books=[...this.books,book]; this.snack.open('Adventure uploaded.', 'Close',{duration:3000});}, error:()=>{this.isUploading=false; this.snack.open('The book file could not be uploaded.', 'Close',{duration:3000});}}); }

  private refreshBooks(): void {
    this.filters$.next({ query: this.query, difficulty: this.difficulty });
  }
}
