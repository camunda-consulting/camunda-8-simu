import { Component, OnInit } from '@angular/core';
import { ExecPlanService } from '../services/exec-plan.service';


@Component({
    selector: 'app-executionplan',
    templateUrl: './executionplan.component.html',
    styleUrls: ['./executionplan.component.css'],
    standalone: false
})
export class ExecutionplanComponent implements OnInit {
  constructor(private execPlanService: ExecPlanService) { }

  state: string = 'plan';
  running = false;

  ngOnInit(): void {
    this.execPlanService.currentlyRunning().subscribe((response: any) => {
      if (response) {
        this.running = true;
      }
    });
  }

  back(): void {
    this.execPlanService.clear();
  }

  save(): void {
    this.execPlanService.updatePlan();
  }

  execute(): void {
    this.execPlanService.executeCurrentPlan();
  }
}
