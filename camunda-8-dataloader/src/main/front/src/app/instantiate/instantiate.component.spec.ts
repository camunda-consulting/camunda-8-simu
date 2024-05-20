import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InstantiateComponent } from './instantiate.component';

describe('InstantiateComponent', () => {
  let component: InstantiateComponent;
  let fixture: ComponentFixture<InstantiateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ InstantiateComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InstantiateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
