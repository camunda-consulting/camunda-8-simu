import { Component, OnInit } from '@angular/core';
import { ExecPlanService } from '../services/exec-plan.service';
import { ProcessService } from '../services/process.service';

@Component({
  selector: 'app-actions-plan',
  templateUrl: './actions-plan.component.html',
  styleUrls: ['./actions-plan.component.css']
})
export class ActionsPlanComponent implements OnInit {
  state: string = 'general';
  constructor(public execPlanService: ExecPlanService, public processService: ProcessService) { }

  ngOnInit(): void {
  }

  save(): void {
    this.processService.updatePlan();
  }
}
