import { TestBed } from '@angular/core/testing';

import { ExecPlanService } from './exec-plan.service';

describe('ExecPlanService', () => {
  let service: ExecPlanService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ExecPlanService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
