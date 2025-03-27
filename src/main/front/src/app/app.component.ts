import { Component } from '@angular/core';
import { ExecPlanService } from './services/exec-plan.service';
import { TemplatingService } from './services/templating.service';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css'],
    standalone: false
})
export class AppComponent {
  title = 'data loader';
  constructor(public execPlanService: ExecPlanService, public templatingService: TemplatingService) { }
}
