import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TestPlanComponent } from './test-plan.component';

describe('TestPlanComponent', () => {
  let component: TestPlanComponent;
  let fixture: ComponentFixture<TestPlanComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TestPlanComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TestPlanComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
