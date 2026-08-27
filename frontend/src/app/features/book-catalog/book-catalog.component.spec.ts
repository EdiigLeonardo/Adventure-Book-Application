import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

import { BookCatalogComponent } from './book-catalog.component';
import { Book } from '../../core/models/book.model';

describe('BookCatalogComponent', () => {
  let fixture: ComponentFixture<BookCatalogComponent>;
  let component: BookCatalogComponent;

  const books: Book[] = [
    {
      id: 'book-1',
      title: 'The Whispering Keep',
      description: 'A cursed archive beneath the city.',
      difficulty: 'MEDIUM',
      status: 'VALID',
      sections: [],
    },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BookCatalogComponent],
      providers: [provideNoopAnimations()],
    }).compileComponents();

    fixture = TestBed.createComponent(BookCatalogComponent);
    component = fixture.componentInstance;
    component.books = books;
    component.selectedBookId = 'book-1';
    fixture.detectChanges();
  });

  it('should render the catalog title, search prompt and selected book name', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Adventure library');
    expect(compiled.textContent).toContain('Search library');
    expect(compiled.textContent).toContain('The Whispering Keep');
  });
});
