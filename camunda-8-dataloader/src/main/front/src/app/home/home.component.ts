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
  histo: any = {};
  execMap: any;
  public filter: any = { "size":10, "engineDate": { "from": "2020-01-01T00:00:00", "to": new Date().toISOString().substring(0, 11) + new Date().toLocaleTimeString('fr-FR') }, "realDate": { "from": new Date().toISOString().substring(0, 11) + "00:00:00", "to": new Date().toISOString().substring(0, 11) + new Date().toLocaleTimeString('fr-FR') } };

  ngOnInit(): void {
    this.filter.engineDate.fromTxt = this.tmpStmpToString(this.filter.engineDate.from);
    this.filter.engineDate.toTxt = this.tmpStmpToString(this.filter.engineDate.to);
    this.filter.realDate.fromTxt = this.tmpStmpToString(this.filter.realDate.from);
    this.filter.realDate.toTxt = this.tmpStmpToString(this.filter.realDate.to);
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
    this.filter.planCode = '"' + plan + '"';
    this.searchHisto(true, true);
  }

  search() {
    this.searchHisto(true, false);
  }
  nextPage() {
    this.filter.after = this.histo.sortValues[1];
    this.searchHisto(false, false);
  }

  searchHisto(resetPAgination: boolean, openPopup: boolean) {
    if (resetPAgination) {
      delete this.filter.after;
    }
    this.histoService.histo(this.filter).subscribe((resp: any) => {
      this.histo = resp;
      for (let line of this.histo.items) {
        line.engineDate = new Date(line.engineDate).toLocaleString('fr');
        line.realDate = new Date(line.realDate).toLocaleString('fr');
      }
      if (openPopup) {
        (window as any).bootstrap.Modal.getOrCreateInstance(document.getElementById('planHisto')).show();
      }
    });
  }
  changeFilterDate() {
    this.filter.engineDate.fromTxt = this.tmpStmpToString(this.filter.engineDate.from);
    this.filter.engineDate.toTxt = this.tmpStmpToString(this.filter.engineDate.to);
    this.searchHisto(true, false);
  }


  tmpStmpToString(timestamp: number): string {
    return new Date(timestamp).toLocaleString("en-US", {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    });
  }

  closeHistoModal() {
    (window as any).bootstrap.Modal.getInstance(document.getElementById('planHisto')).hide();
  }
}
