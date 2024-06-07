import { TestBed } from '@angular/core/testing';

import { TemplatingService } from './templating.service';

describe('TemplatingService', () => {
  let service: TemplatingService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TemplatingService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
