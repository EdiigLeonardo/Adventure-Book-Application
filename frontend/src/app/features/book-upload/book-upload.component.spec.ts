import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

import { BookUploadComponent } from './book-upload.component';

describe('BookUploadComponent', () => {
  let fixture: ComponentFixture<BookUploadComponent>;
  let component: BookUploadComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BookUploadComponent],
      providers: [provideNoopAnimations()],
    }).compileComponents();

    fixture = TestBed.createComponent(BookUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should display a polished upload prompt and action text', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Upload a story');
    expect(compiled.textContent).toContain('Drop a JSON adventure file');
    expect(compiled.textContent).toContain('Choose JSON book');
  });
});
