import { Component } from '@angular/core';
import { ProcessService } from './services/process.service';
import { ExecPlanService } from './services/exec-plan.service';
import { TemplatingService } from './services/templating.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'data loader';
  constructor(public processService: ProcessService, public execPlanService: ExecPlanService, public templatingService: TemplatingService) { }
}
