import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ExecPlanService {

  constructor(
    private http: HttpClient) { }
  executionPlan: any | undefined;
  scenario: any = {};
  currentActivity: string | undefined;
  activitySubject = new BehaviorSubject<string>('');

  list(): Observable<string[]> {
    return this.http.get<string[]>(environment.backend + "/api/plan");
  }

  createExecutionPlan(xml: string): void {
    this.http.post<any>(environment.backend + "/api/plan", xml).subscribe((response: any) => {
      this.executionPlan = response;
      this.selectScenario(response.scenarii[0]);
    });
  }

  openExecutionPlan(definition: any): void {
    this.http.get<any>(environment.backend + "/api/plan/" + definition.bpmnProcessId + "/" + definition.version).subscribe((response: any) => {
      this.executionPlan = response;
      this.selectScenario(response.scenarii[0]);
    });
  }

  executePlan(definition: any): Observable<any> {
    return this.http.get<any>(environment.backend + "/api/plan/" + definition.bpmnProcessId + "/" + definition.version + "/start");
  }

  executeCurrentPlan(): void {
    this.http.post<any>(environment.backend + "/api/plan/start", this.executionPlan).subscribe((response: any) => {
      console.log("status " + response);
    });
  }

  executeCurrentScenario(): void {
    this.http.post<any>(environment.backend + "/api/plan/start?scenario=" + this.scenario!.name, this.executionPlan).subscribe((response: any) => {
      console.log("status " + response);
    });
  }

  updateDef(xml: string): void {
    this.http.post<any>(environment.backend + "/api/plan/" + this.executionPlan.definition.bpmnProcessId + "/" + this.executionPlan.definition.version + '/xml', xml).subscribe((response: any) => {
      this.executionPlan = response;
    });
  }
  updatePlan(): void {
    this.http.put<any>(environment.backend + "/api/plan/" + this.executionPlan.definition.bpmnProcessId + "/" + this.executionPlan.definition.version, this.executionPlan).subscribe((response: any) => {
      this.executionPlan = response;
    });
  }
  addScenario(): void {
    this.http.put<any>(environment.backend + "/api/plan/" + this.executionPlan.definition.bpmnProcessId + "/" + this.executionPlan.definition.version + "/newScenario", this.executionPlan).subscribe((response: any) => {
      this.executionPlan = response;
    });
  }
  currentlyRunning(): Observable<any> {
    return this.http.get<any>(environment.backend + "/api/plan/running")
  }
  stopPlan(): Observable<any> {
    return this.http.get<any>(environment.backend + "/api/plan/stop")
  }

  clear(): void {
    this.executionPlan = undefined;
    this.scenario = {};
    this.currentActivity = undefined;
  }

  selectActivity(activity: string): void {
    this.currentActivity = activity;
    this.activitySubject.next(activity);
  }

  selectScenario(scenario: any): void {
    this.scenario = scenario;
    this.selectActivity('startInstances');
  }
  createStepInScenario(elementId: string): void {
    this.scenario.steps[elementId] = {
      "elementId": elementId,
      "action": "COMPLETE",
      "duration": {
        "startDesiredAvg": 8000,
        "endDesiredAvg": 4000,
        "minMaxPercent": 0,
        "avgProgression": "LINEAR",
        "progressionSalt": 0
      },
      "jsonTemplate": {
        template: "{}", exampleContext: {}
      },
    };
    this.selectActivity(elementId);
  }

  createPreStepInScenario(parentStep: string, type: string, elementRef: any, time: number): void {
    if (!this.scenario.steps[parentStep]) {
      this.scenario.steps[parentStep] = {
        "elementId": parentStep,
        "action": "DO_NOTHING",
        "duration": {
          "startDesiredAvg": 1000,
          "endDesiredAvg": 1000,
          "minMaxPercent": 0,
          "avgProgression": "LINEAR",
          "progressionSalt": 0
        },
        "jsonTemplate": { "template": "", "exampleContext": {}},
        "preSteps": [],
        "postSteps": []
      };
    }
    if (!this.scenario.steps[parentStep].preSteps) {
      this.scenario.steps[parentStep].preSteps = [];
    }
    if (type == 'MSG') {
      this.scenario.steps[parentStep].preSteps.push({
        "type": type,
        "msgDelay": time,
        "msg": elementRef.name,
        "correlationKey": elementRef.extensionElements.valueOf("correlationKey").values[0].correlationKey.replace("=", "").trim(),
        "jsonTemplate": {
          template: "{}", exampleContext: {}
        }
      });
    } else if (type == 'CLOCK') {
      this.scenario.steps[parentStep].preSteps.push({
        "type": type,
        "feelDelay": time
      });
    } else if (type == 'BPMN_ERROR') {
      this.scenario.steps[parentStep].preSteps.push({
        "type": type,
        "errorCode": elementRef.errorCode,
        "errorDelay": time,
        "jsonTemplate": {
          template: "{}", exampleContext: {}
        }
      });
    }
    this.selectActivity(parentStep);
  }

  deleteCurrentStep(): void {
    delete this.scenario.steps[this.currentActivity!];
    this.selectActivity('startInstances');
  }

  deleteScenario(index: number): void {
    this.executionPlan.scenarii.splice(index, 1);
  }

}
