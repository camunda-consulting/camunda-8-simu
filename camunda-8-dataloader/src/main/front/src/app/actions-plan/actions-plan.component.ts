import { Component } from '@angular/core';
import { ExecPlanService } from '../services/exec-plan.service';
import { ProcessService } from '../services/process.service';

@Component({
  selector: 'app-actions-plan',
  templateUrl: './actions-plan.component.html',
  styleUrls: ['./actions-plan.component.css']
})
export class ActionsPlanComponent  {

  state: string = 'general';
  newStepElementId: string = '';
  constructor(public execPlanService: ExecPlanService, public processService: ProcessService) { }

  


  addScenario(): void {
    this.execPlanService.addScenario();
  }

  save(): void {
    this.execPlanService.updatePlan();
  }

  execute(): void {
    this.execPlanService.executeCurrentPlan();
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
}
