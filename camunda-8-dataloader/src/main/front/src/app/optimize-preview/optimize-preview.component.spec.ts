import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OptimizePreviewComponent } from './optimize-preview.component';

describe('OptimizePreviewComponent', () => {
  let component: OptimizePreviewComponent;
  let fixture: ComponentFixture<OptimizePreviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OptimizePreviewComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OptimizePreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
