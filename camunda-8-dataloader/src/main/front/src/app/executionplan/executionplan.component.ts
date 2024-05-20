import { Component, OnInit } from '@angular/core';
import { ProcessService } from '../services/process.service';
import { ExecPlanService } from '../services/exec-plan.service';


@Component({
  selector: 'app-executionplan',
  templateUrl: './executionplan.component.html',
  styleUrls: ['./executionplan.component.css']
})
export class ExecutionplanComponent {
  constructor(private processService: ProcessService, private execPlanService: ExecPlanService) { }
  state: string = 'plan';

  back(): void {
    this.execPlanService.clear();
  }

}
