import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ExecPlanService {

  constructor(
    private http: HttpClient) { }
  executionPlan: any | undefined;
  activities: string[] = [];
  scenario:any={};
  currentActivity: string | undefined;
  activitySubject = new BehaviorSubject<string>('');

  openExecutionPlan(definition: any): void {
    this.http.get<any>("http://localhost:8080/api/plan/" + definition.bpmnProcessId + "/" + definition.version).subscribe((response: any) => {
      this.executionPlan = response;
      this.scenario = this.executionPlan.scenarii[0];
    });
  }
  updateDef(xml: string): void {
    this.http.post<any>("http://localhost:8080/api/plan/" + this.executionPlan.definition.bpmnProcessId + "/" + this.executionPlan.definition.version + '/xml', xml).subscribe((response: any) => {
      this.executionPlan = response;
    });
  }
  updatePlan(): void {
    this.http.put<any>("http://localhost:8080/api/plan/" + this.executionPlan.definition.bpmnProcessId + "/" + this.executionPlan.definition.version, this.executionPlan).subscribe((response: any) => {
      this.executionPlan = response;
    });
  }
  addScenario(): void {
    this.http.put<any>("http://localhost:8080/api/plan/" + this.executionPlan.definition.bpmnProcessId + "/" + this.executionPlan.definition.version+"/newScenario", this.executionPlan).subscribe((response: any) => {
      this.executionPlan = response;
    });
  }

  clear(): void {
    this.executionPlan = undefined;
    this.scenario = {};
    this.activities = [];
    this.currentActivity = undefined;
  }

  addActivity(activity: string) {
    this.activities.push(activity);
  }
  selectActivity(activity: string): void {
    if (this.activities.indexOf(activity) >= 0 || activity =='startInstances') {
      this.currentActivity = activity;
      this.activitySubject.next(activity);
    }
  }

  selectScenario(scenario: any): void {
    this.selectActivity('startInstances');
    this.scenario = scenario;
  }

  createCurrentStepInScenario(): void {
    this.scenario.steps[this.currentActivity!] = {
      "elementId": this.currentActivity,
      "action": "COMPLETE",
      "duration": {
        "startDesiredAvg": 8000,
        "endDesiredAvg": 4000,
        "minMaxPercent": 0,
        "avgProgression": "LINEAR",
        "progressionSalt": 0
      },
      "jsonTemplate": "{}",
    };
  }
}
