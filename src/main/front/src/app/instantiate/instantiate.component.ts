import { Component } from '@angular/core';
import { ProcessService } from '../services/process.service';
import { ExecPlanService } from '../services/exec-plan.service';

@Component({
  selector: 'app-instantiate',
  templateUrl: './instantiate.component.html',
  styleUrls: ['./instantiate.component.css']
})
export class InstantiateComponent {

  constructor(private processService: ProcessService, public execPlanService: ExecPlanService) { }


  openStartPlanModal() {
    (window as any).bootstrap.Modal.getOrCreateInstance(document.getElementById('startPlanModal')).show();
  }
  closeStartPlanModal() {
    (window as any).bootstrap.Modal.getInstance(document.getElementById('startPlanModal')).hide();
  }
}
