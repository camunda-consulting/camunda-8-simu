import { Component, OnInit } from '@angular/core';
import { ExecPlanService } from '../services/exec-plan.service';
import { HistoService } from '../services/histo.service';
import { TemplatingService } from '../services/templating.service';

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.css'],
    standalone: false
})
export class HomeComponent implements OnInit {

  constructor(private execPlanService: ExecPlanService, private histoService: HistoService, private templatingService: TemplatingService) { }

  plans:any[] = []
  executions: any[] = [];
  execMap: any;
  runningPlan?: any = null;
  filter?: any = null;
  datasets: string[] = [];
  jsondatasets: string[] = [];
  createPlan = false;
  newPlanXml?: string;
  ngOnInit(): void {
    this.refreshPage();
  }

  refreshPage(): void {
    this.execPlanService.list().subscribe((response: string[]) => {
      this.plans = [];
      for (let plan of response) {
        this.plans.push({ "bpmnProcessId": plan});
      }
    });
    this.reload();
    this.loadDatasets();

  }

  loadDatasets(): void {
    this.templatingService.listDatasets().subscribe((response: string[]) => {
      this.datasets = response;
    });
    this.templatingService.listJsonDatasets().subscribe((response: string[]) => {
      this.jsondatasets = response;
    });
  }

  reload(): void {
    this.execPlanService.currentlyRunning().subscribe((response: any) => {
      this.runningPlan = response;
    });
    this.histoService.executions().subscribe((response: any) => {
      this.execMap = response;
      this.executions = [];
     
      for (let prop in this.execMap) {
        let exec = this.execMap[prop];
        exec.key = prop;
        this.executions.push(exec);
      }
    });
  }

  openPlan(definition: any): void {
    this.execPlanService.openExecutionPlan(definition);
  }
  delete(definition: any): void {
    this.execPlanService.delete(definition).subscribe((response: any) => {
      this.refreshPage();
    });
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

  addDataset(): void {
    this.templatingService.newDataset();
  }

  addJsonDataset(): void {
    this.templatingService.newJsonDataset();
  }
  editDataset(name: string): void {
    this.templatingService.getDataset(name);
  }
  editJsonDataset(name: string): void {
    this.templatingService.getJsonDataset(name);
  }
  deleteDataset(name: string): void {
    this.templatingService.deleteDataset(name).subscribe(() => {
      this.loadDatasets();
    });
  }
  deleteJsonDataset(name: string): void {
    this.templatingService.deleteJsonDataset(name).subscribe(() => {
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
    this.execPlanService.createExecutionPlan(this.newPlanXml!);
    this.toggleCreatePlan();
  }
}
