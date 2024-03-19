import { Component, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import Chart from 'chart.js/auto';

@Component({
  selector: 'app-optimize-preview',
  templateUrl: './optimize-preview.component.html',
  styleUrls: ['./optimize-preview.component.css']
})
export class OptimizePreviewComponent implements AfterViewInit {

  @ViewChild('chart1') chart1: ElementRef | undefined;

  constructor() { }
  labels = ["January", 'February', 'March', 'April', 'May'];
  data = {
    labels: this.labels,
    datasets: [
      {
        label: 'Fully Rounded',
        data: [80, 70, 50, 90, 60],
        borderColor: 'rgba(255, 99, 132)',
        backgroundColor: 'rgba(255, 99, 132, 0.5)',
        borderWidth: 2,
        borderRadius: Number.MAX_VALUE,
        borderSkipped: false,
      },
      {
        label: 'Small Radius',
        data: [100, 50, -50, 70, 60],
        borderColor: 'rgba(54, 162, 235)',
        backgroundColor: 'rgba(54, 162, 235, 0.5)',
        borderWidth: 2,
        borderRadius: 5,
        borderSkipped: false,
      }
    ]
  };

  config = {
    type: 'bar' as const,
    data: this.data,
    options: {
      responsive: true,
      plugins: {
        legend: {
          position: 'top' as const,
        },
        title: {
          display: true,
          text: 'Chart.js Bar Chart'
        }
      }
    },
  };

  ngAfterViewInit(): void {
    new Chart(this.chart1!.nativeElement, this.config);
  }

}
