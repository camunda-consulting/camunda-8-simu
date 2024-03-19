import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActiveinstancesComponent } from './activeinstances.component';

describe('ActiveinstancesComponent', () => {
  let component: ActiveinstancesComponent;
  let fixture: ComponentFixture<ActiveinstancesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ActiveinstancesComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ActiveinstancesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
