import { Component, ViewChild, ElementRef } from '@angular/core';
import Chart from 'chart.js/auto';
import { ProcessService } from '../services/process.service';
import { ExecPlanService } from '../services/exec-plan.service';

@Component({
  selector: 'app-instantiate',
  templateUrl: './instantiate.component.html',
  styleUrls: ['./instantiate.component.css']
})
export class InstantiateComponent {

  @ViewChild('preview') preview: ElementRef | undefined;
  private chart?: Chart;
  constructor(private processService: ProcessService, public execPlanService: ExecPlanService) { }

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
}
