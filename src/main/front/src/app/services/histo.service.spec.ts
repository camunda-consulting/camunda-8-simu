import { TestBed } from '@angular/core/testing';

import { HistoService } from './histo.service';

describe('HistoService', () => {
  let service: HistoService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(HistoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
