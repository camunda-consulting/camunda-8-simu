import { Component, OnInit } from '@angular/core';
import { ProcessService } from '../services/process.service';
import { ExecPlanService } from '../services/exec-plan.service';
import { HistoService } from '../services/histo.service';
import { TemplatingService } from '../services/templating.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  constructor(private processService: ProcessService, private execPlanService: ExecPlanService, private histoService: HistoService, private templatingService: TemplatingService) { }

  plans:any[] = []
  definitions: any[] = [];
  executions: any[] = [];
  histo: any = {};
  execMap: any;
  runningPlan?: any = null;
  filter?: any = null;
  datasets: string[] = [];
  createPlan = false;
  newPlanXml?: string;
  loadFromDefinition = false;
  definitionToLoad?: any;
  ngOnInit(): void {
    this.execPlanService.list().subscribe((response: string[]) => {
      this.plans = [];
      for (let plan of response) {
        let x = plan.lastIndexOf("_v");
        let bpmnProcessId = plan.substring(0, x);
        let v = plan.substring(x+2);
        this.plans.push({ "bpmnProcessId": bpmnProcessId, "version": v });
      }
    });
    this.processService.definitions().subscribe((response: any[]) => {
      this.definitions = response;
    });
    this.reload();
    this.loadDatasets();
  }

  loadDatasets(): void {
    this.templatingService.listDatasets().subscribe((response: string[]) => {
      this.datasets = response;
    });
  }

  reload(): void {
    this.execPlanService.currentlyRunning().subscribe((response: any) => {
      this.runningPlan = response;
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
    this.execPlanService.executePlan(definition).subscribe(() => {
      this.reload();
    });
  }

  stopExecution(): void {
    this.execPlanService.stopPlan().subscribe(() => {
      this.reload();
    });
  }


  openHisto(plan: string) {
    if (!this.filter) { 
      this.filter = {
        "size": 10, "engineDate": { "from": "2020-01-01T00:00:00", "to": new Date().toISOString().substring(0, 11) + new Date().toLocaleTimeString('fr-FR') }, "realDate": { "from": new Date().toISOString().substring(0, 11) + "00:00:00", "to": new Date().toISOString().substring(0, 11) + new Date().toLocaleTimeString('fr-FR') }
      };
      this.filter.engineDate.fromTxt = this.tmpStmpToString(this.filter.engineDate.from);
      this.filter.engineDate.toTxt = this.tmpStmpToString(this.filter.engineDate.to);
      this.filter.realDate.fromTxt = this.tmpStmpToString(this.filter.realDate.from);
      this.filter.realDate.toTxt = this.tmpStmpToString(this.filter.realDate.to);
    }
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
  firstPage() {
    delete this.filter.after;
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

  addDataset(): void {
    this.templatingService.newDataset();
  }
  editDataset(name: string): void {
    this.templatingService.getDataset(name);
  }
  deleteDataset(name: string): void {
    this.templatingService.deleteDataset(name).subscribe(() => {
      this.loadDatasets();
    });
  }
  toggleCreatePlan(): void {
    this.createPlan = !this.createPlan;
    if (this.createPlan) {
      (window as any).bootstrap.Modal.getOrCreateInstance(document.getElementById('createPlanModal')).show();
    } else {
      this.newPlanXml = undefined;
      (window as any).bootstrap.Modal.getInstance(document.getElementById('createPlanModal')).hide();
    }
  }
  loadBpmnFile(event: any): void {
    var file = event.target.files[0];
    if (file) {
      var reader = new FileReader();
      reader.readAsText(file, "UTF-8");
      reader.onload = this._handleReaderLoaded.bind(this); 
      reader.onerror = this._handleReaderError.bind(this); 
    }
  }
  _handleReaderLoaded(evt: any) {
    this.newPlanXml = evt.target.result;
  }
  _handleReaderError() {
    this.newPlanXml = undefined;
  }
  createNewPlan(): void {
    if (this.loadFromDefinition) {
      this.execPlanService.openExecutionPlan(this.definitionToLoad);
    } else {
      this.execPlanService.createExecutionPlan(this.newPlanXml!);
    }
    this.toggleCreatePlan();
  }
}
