import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { Book, GameSession, Option } from '../../core/models/book.model';
import { SaveResumeDialogComponent, SaveResumeDialogResult } from './save-resume-dialog/save-resume-dialog.component';

@Component({
  selector: 'app-game-play',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatDialogModule,
    MatDividerModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
  ],
  templateUrl: './game-play.component.html',
  styleUrl: './game-play.component.scss',
})
export class GamePlayComponent {
  @Input() book: Book | null = null;
  @Input() session: GameSession | null = null;
  @Input() healthPercent = 0;
  @Input() resumeSessionId = '';
  @Input() isSaving = false;

  @Output() startAdventure = new EventEmitter<void>();
  @Output() chooseOption = new EventEmitter<Option>();
  @Output() saveGame = new EventEmitter<void>();
  @Output() resumeSession = new EventEmitter<string>();

  constructor(private readonly dialog: MatDialog) {}

  get currentSection() {
    if (!this.book || !this.session) {
      return undefined;
    }

    return this.book.sections.find(
      (section) => section.id === this.session?.currentSectionId,
    );
  }

  start(): void {
    this.startAdventure.emit();
  }

  pickOption(option: Option): void {
    this.chooseOption.emit(option);
  }

  save(): void {
    this.saveGame.emit();
  }

  resume(): void {
    this.resumeSession.emit(this.resumeSessionId);
  }

  openSaveResumeDialog(mode: 'save' | 'resume' = 'save'): void {
    const dialogRef = this.dialog.open(SaveResumeDialogComponent, {
      width: '450px',
      data: {
        sessionId: this.session?.id,
        mode,
      },
    });

    dialogRef.afterClosed().subscribe((result: SaveResumeDialogResult | undefined) => {
      if (!result) {
        return;
      }
      if (result.action === 'save') {
        this.save();
      } else if (result.action === 'resume' && result.sessionId) {
        this.resumeSessionId = result.sessionId;
        this.resumeSession.emit(result.sessionId);
      }
    });
  }
}

