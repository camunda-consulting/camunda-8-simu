import { Component, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import Chart from 'chart.js/auto';
import { Colors } from 'chart.js';
import { ExecPlanService } from '../services/exec-plan.service';

@Component({
  selector: 'app-optimize-preview',
  templateUrl: './optimize-preview.component.html',
  styleUrls: ['./optimize-preview.component.css']
})
export class OptimizePreviewComponent implements AfterViewInit {

  @ViewChild('chart1') chart1: ElementRef | undefined;
  chart?: Chart;
  constructor(private execPlanService: ExecPlanService) { }



  ngAfterViewInit(): void {
    this.execPlanService.previewPlan().subscribe((response: any) => {
      if (!this.chart) {

        Chart.register(Colors);
        this.chart = new Chart(this.chart1!.nativeElement, {
          type: 'line' as const,
          data: response,
          options: {
            responsive: true,
            plugins: {
              legend: {
                position: 'top' as const,
              },
              title: {
                display: true,
                text: 'Instances start progression'
              }
            }
          }
        });
      } else {
        this.chart.data = response;
        this.chart.update();
      }
    });
  }

}
