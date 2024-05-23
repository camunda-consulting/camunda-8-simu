import { Component, OnInit } from '@angular/core';
import { ProcessService } from '../services/process.service';
import { ExecPlanService } from '../services/exec-plan.service';

@Component({
  selector: 'app-definitions',
  templateUrl: './definitions.component.html',
  styleUrls: ['./definitions.component.css']
})
export class DefinitionsComponent implements OnInit {

  constructor(private processService: ProcessService, private execPlanService: ExecPlanService) { }

  definitions: any[] = [];

  ngOnInit(): void {
    this.processService.definitions().subscribe((response: any[]) => {
      this.definitions = response;
    });
  }

  openPlan(definition: any): void {
    this.execPlanService.openExecutionPlan(definition);
  }

  executePlan(definition: any): void {
    this.execPlanService.executePlan(definition);
  }
}
