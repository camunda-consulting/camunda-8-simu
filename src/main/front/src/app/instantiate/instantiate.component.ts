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
}
