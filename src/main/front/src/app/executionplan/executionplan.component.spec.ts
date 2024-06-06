import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExecutionplanComponent } from './executionplan.component';

describe('ExecutionplanComponent', () => {
  let component: ExecutionplanComponent;
  let fixture: ComponentFixture<ExecutionplanComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ExecutionplanComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ExecutionplanComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
