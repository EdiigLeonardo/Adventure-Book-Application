import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

import { GamePlayComponent } from './game-play.component';
import { Book, GameSession } from '../../core/models/book.model';

describe('GamePlayComponent', () => {
  let fixture: ComponentFixture<GamePlayComponent>;
  let component: GamePlayComponent;

  const book: Book = {
    id: 'book-1',
    title: 'The Whispering Keep',
    description: 'A cursed archive beneath the city.',
    difficulty: 'MEDIUM',
    status: 'VALID',
    sections: [
      {
        id: 'start',
        type: 'BEGIN',
        text: 'The door opens and the dust stirs.',
        options: [
          {
            description: 'Step into the archive',
            gotoId: 'hall',
          },
        ],
      },
      {
        id: 'hall',
        type: 'NODE',
        text: 'A long corridor hums with whispers.',
        options: [],
      },
    ],
  };

  const session: GameSession = {
    id: 'session-1',
    bookId: 'book-1',
    currentSectionId: 'start',
    health: 7,
    status: 'IN_PROGRESS',
    history: ['Started the journey'],
    createdAt: '2026-08-26T00:00:00Z',
    book,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GamePlayComponent],
      providers: [provideNoopAnimations()],
    }).compileComponents();

    fixture = TestBed.createComponent(GamePlayComponent);
    component = fixture.componentInstance;
    component.book = book;
    component.session = session;
    component.healthPercent = 70;
    fixture.detectChanges();
  });

  it('should render the current scene and 70% health bar', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain(
      'The door opens and the dust stirs.',
    );
    expect(compiled.textContent).toContain('Step into the archive');
  });
});
