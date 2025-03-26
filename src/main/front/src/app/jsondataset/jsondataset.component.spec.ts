import { ComponentFixture, TestBed } from '@angular/core/testing';

import { JsondatasetComponent } from './jsondataset.component';

describe('JsondatasetComponent', () => {
  let component: JsondatasetComponent;
  let fixture: ComponentFixture<JsondatasetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JsondatasetComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(JsondatasetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
