import { Component, OnInit } from '@angular/core';
import { ExecPlanService } from '../services/exec-plan.service';

@Component({
    selector: 'app-actions-plan',
    templateUrl: './actions-plan.component.html',
    styleUrls: ['./actions-plan.component.css'],
    standalone: false
})
export class ActionsPlanComponent implements OnInit {

  state: string = 'general';
  newStepElementId: string = '';
  constructor(public execPlanService: ExecPlanService) { }

  ngOnInit(): void {

    this.selectScenario(0);
    }



  addScenario(): void {
    this.execPlanService.addScenario();
  }

  selectScenario(i: number): void {
    this.execPlanService.selectScenario(this.execPlanService.executionPlan.scenarii[i]);
    this.state = '' + i;
  }

  deleteScenario(i: number): void {
    this.execPlanService.deleteScenario(i);
    this.state = 'general';
  }

  openAddStepModal(): void {
    (window as any).bootstrap.Modal.getOrCreateInstance(document.getElementById('addnewstep')).show();
  }
  closeAddStepModal() {
    (window as any).bootstrap.Modal.getInstance(document.getElementById('addnewstep')).hide();
  }
  createStep() {
    this.execPlanService.createStepInScenario(this.newStepElementId);
    this.closeAddStepModal();
  }

  labelStep(id: any): string {
    let name = this.execPlanService.getActivityName(id);
    if (name) {
      return name + " (" + id + ")";
    }
    return id;
  }
}
