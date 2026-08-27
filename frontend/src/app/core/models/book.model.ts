export type BookStatus = 'VALID' | 'INVALID';
export type SectionType = 'BEGIN' | 'NODE' | 'END';
export type GameStatus = 'IN_PROGRESS' | 'VICTORY' | 'GAME_OVER';

export interface Consequence {
  type?: 'LOSE_HEALTH' | 'GAIN_HEALTH';
  value?: string | number;
  text?: string;
}

export interface Option {
  description: string;
  text?: string;
  gotoId: string;
  consequence?: Consequence;
}

export interface Section {
  id: string;
  text: string;
  type: SectionType;
  options?: Option[];
}

export interface Book {
  id: string;
  title: string;
  description?: string;
  difficulty?: string;
  status?: BookStatus;
  sections: Section[];
}

export interface GameSession {
  id: string;
  bookId: string;
  currentSectionId: string;
  health: number;
  status: GameStatus;
  history: string[];
  createdAt: string;
  book?: Book;
}
