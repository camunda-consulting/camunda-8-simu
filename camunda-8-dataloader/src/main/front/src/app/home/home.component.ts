import { Component, OnInit } from '@angular/core';
import { ProcessService } from '../services/process.service';
import { ExecPlanService } from '../services/exec-plan.service';
import { HistoService } from '../services/histo.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  constructor(private processService: ProcessService, private execPlanService: ExecPlanService, private histoService: HistoService) { }

  definitions: any[] = [];
  executions: any[] = [];
  histo: string[] = []
  execMap: any;
  ngOnInit(): void {
    this.processService.definitions().subscribe((response: any[]) => {
      this.definitions = response;
    });
    this.histoService.executions().subscribe((response: any) => {
      this.execMap = response;
      this.executions = [];
      this.histoService.progresses().subscribe((reponse: any) => {
        for (let prop in reponse) {
          this.execMap[prop].progress = reponse[prop];
        }
      });
      for (let prop in this.execMap) {
        let exec = this.execMap[prop];
        exec.date = prop.substring(prop.lastIndexOf("_") + 1).substring(0, 19).replace('T', ' ');
        exec.key = prop;
        this.executions.push(exec);
      }
    });
  }

  openPlan(definition: any): void {
    this.execPlanService.openExecutionPlan(definition);
  }

  executePlan(definition: any): void {
    this.execPlanService.executePlan(definition);
  }


  openHisto(plan: string) {
    this.histoService.histo(plan).subscribe((resp: string[]) => {
      this.histo = resp;
      (window as any).bootstrap.Modal.getOrCreateInstance(document.getElementById('planHisto')).show();
    });
  }
  closeHistoModal() {
    (window as any).bootstrap.Modal.getInstance(document.getElementById('planHisto')).hide();
  }
}
