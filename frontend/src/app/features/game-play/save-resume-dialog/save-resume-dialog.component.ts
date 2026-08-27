import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

export interface SaveResumeDialogData {
  sessionId?: string;
  mode?: 'save' | 'resume';
}

export interface SaveResumeDialogResult {
  action: 'save' | 'resume';
  sessionId?: string;
}

@Component({
  selector: 'app-save-resume-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './save-resume-dialog.component.html',
  styleUrl: './save-resume-dialog.component.scss',
})
export class SaveResumeDialogComponent {
  mode: 'save' | 'resume';
  currentSessionId: string;
  inputSessionId: string;
  copied = false;

  constructor(
    public dialogRef: MatDialogRef<SaveResumeDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: SaveResumeDialogData,
  ) {
    this.mode = data?.mode ?? 'save';
    this.currentSessionId = data?.sessionId ?? '';
    this.inputSessionId = '';
  }

  setMode(mode: 'save' | 'resume'): void {
    this.mode = mode;
  }

  copySessionId(): void {
    if (this.currentSessionId) {
      navigator.clipboard.writeText(this.currentSessionId);
      this.copied = true;
      setTimeout(() => (this.copied = false), 2000);
    }
  }

  confirmSave(): void {
    this.dialogRef.close({
      action: 'save',
      sessionId: this.currentSessionId,
    } as SaveResumeDialogResult);
  }

  confirmResume(): void {
    if (!this.inputSessionId.trim()) {
      return;
    }
    this.dialogRef.close({
      action: 'resume',
      sessionId: this.inputSessionId.trim(),
    } as SaveResumeDialogResult);
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
