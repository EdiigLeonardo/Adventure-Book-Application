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
    expect(compiled.textContent).toContain('The Adventure Library');
    expect(compiled.querySelector('#book-search')?.getAttribute('placeholder')).toBe('Search adventures...');
    expect(compiled.textContent).toContain('The Whispering Keep');
  });

  it('emits the API difficulty value when a difficulty filter is selected', () => {
    const values: string[] = [];
    component.difficultyChanged.subscribe(value => values.push(value));

    const catalog = fixture.nativeElement as HTMLElement;
    const beginnerFilter = Array.from(catalog.querySelectorAll<HTMLButtonElement>('.filter'))
      .find(button => button.textContent?.trim() === 'Beginner');
    beginnerFilter?.click();

    expect(component.activeDifficulty).toBe('BEGINNER');
    expect(values).toEqual(['BEGINNER']);
  });
});
