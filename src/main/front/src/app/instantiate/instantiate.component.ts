import { Component, ViewChild, ElementRef } from '@angular/core';
import Chart from 'chart.js/auto';
import { ExecPlanService } from '../services/exec-plan.service';

@Component({
    selector: 'app-instantiate',
    templateUrl: './instantiate.component.html',
    styleUrls: ['./instantiate.component.css'],
    standalone: false
})
export class InstantiateComponent {

  @ViewChild('preview') preview: ElementRef | undefined;
  private chart?: Chart;
  duplicateScenarioName: string='';
  constructor(public execPlanService: ExecPlanService) { }

  openStartPlanModal() {
    (window as any).bootstrap.Modal.getOrCreateInstance(document.getElementById('startPlanModal')).show();
  }
  closeStartPlanModal() {
    (window as any).bootstrap.Modal.getInstance(document.getElementById('startPlanModal')).hide();
  }
  previewEvol() {
    this.execPlanService.previewEvol(this.execPlanService.scenario!.evolution).subscribe((response: any) => {
      if (!this.chart) {
        this.chart = new Chart(this.preview!.nativeElement, {
          type: 'line' as const,
          data: response,
        });
      } else {
        this.chart.data = response;
        this.chart.update();
      }
    });
  }
  
  duplicateScenarioModal():void {
    this.duplicateScenarioName = "COPY_"+this.execPlanService.scenario!.name;
    (window as any).bootstrap.Modal.getOrCreateInstance(document.getElementById('duplicateScenarioModal')).show();
  }
  
  duplicateScenario(): void {
    let clone = JSON.parse(JSON.stringify(this.execPlanService.scenario));
	clone.name = this.duplicateScenarioName;
    this.execPlanService.executionPlan.scenarii.push(clone);
  }
}
