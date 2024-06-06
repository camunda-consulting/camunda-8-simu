import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActionsPlanComponent } from './actions-plan.component';

describe('ActionsPlanComponent', () => {
  let component: ActionsPlanComponent;
  let fixture: ComponentFixture<ActionsPlanComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ActionsPlanComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ActionsPlanComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
