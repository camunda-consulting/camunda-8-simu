import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import NavigatedViewer from 'camunda-bpmn-js/lib/camunda-cloud/NavigatedViewer';

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
  activities: any = {};

  managedActivities = ["bpmn:ServiceTask", "bpmn:UserTask", "bpmn:SendTask", "bpmn:IntermediateCatchEvent", "bpmn:BoundaryEvent"];

  isManagedActivity(type: string): boolean {
    return this.managedActivities.indexOf(type) >= 0;
  }
  list(): Observable<string[]> {
    return this.http.get<string[]>(environment.backend + "/api/plan");
  }

  mapActivityName(id: string, name: string) {
    this.activities[id] = name;
  }
  getActivityName(id: any) {
    return this.activities[id];
  }

  loadXmlActivities(xml: string) {
    let viewer = new NavigatedViewer();
    viewer.importXML(xml).then((result: any) => {
      const eltRegistry: any = viewer!.get('elementRegistry');
      eltRegistry.forEach((elt: any) => {
        if (this.isManagedActivity(elt.type)) {
          this.mapActivityName(elt.di.bpmnElement.id, elt.di.bpmnElement.name);
        }
      });
    });
  }

  loadActivities() {
    this.loadXmlActivities(this.executionPlan.xml);
   
    if (this.executionPlan.xmlDependencies) {
      for (const dep in this.executionPlan.xmlDependencies) {
        this.loadXmlActivities(this.executionPlan.xmlDependencies[dep]);
      }
    }
  }

  stepLabel(id: any): string {
    if (this.executionPlan.stepLabel == 'id') {
      return id;
    } else if (this.executionPlan.stepLabel == 'name') {
      return this.getActivityName(id);
    }
    return this.getActivityName(id) + ' ('+ id + ')';
  }

  createExecutionPlan(xml: string): void {
    this.http.post<any>(environment.backend + "/api/plan", xml).subscribe((response: any) => {
      this.executionPlan = response;
      this.loadActivities();
      this.selectScenario(response.scenarii[0]);
    });
  }

  openExecutionPlan(plan: any): void {
    this.http.get<any>(environment.backend + "/api/plan/" + plan.bpmnProcessId).subscribe((response: any) => {
      this.executionPlan = response;
      this.loadActivities();
      this.selectScenario(response.scenarii[0]);
    });
  }
  delete(plan: any): Observable<any> {
    return this.http.delete<any>(environment.backend + "/api/plan/" + plan.bpmnProcessId);
  }

  executePlan(definition: any): Observable<any> {
    return this.http.get<any>(environment.backend + "/api/plan/" + definition.bpmnProcessId + "/start");
  }

  executeCurrentPlan(): void {
    this.http.post<any>(environment.backend + "/api/plan/start", this.executionPlan).subscribe((response: any) => {
      console.log("status " + response);
    });
  }

  testPlan(): Observable<any> {
    return this.http.post<any>(environment.backend + "/api/plan/test", this.executionPlan);
  }

  executeCurrentScenario(): void {
    this.http.post<any>(environment.backend + "/api/plan/start?scenario=" + this.scenario!.name, this.executionPlan).subscribe((response: any) => {
      console.log("status " + response);
    });
  }
  updateDep(dep: string, xml: string): void {

    if (dep == "Main definition") {
      this.executionPlan.xml = xml;
    } else {
      this.executionPlan.xmlDependencies[dep] = xml;
    }
  }
  updateDef(xml: string): void {
    this.http.post<any>(environment.backend + "/api/plan/" + this.executionPlan.definition.bpmnProcessId + '/xml', xml).subscribe((response: any) => {
      this.executionPlan = response;
    });
  }
  updatePlan(): void {
    this.http.put<any>(environment.backend + "/api/plan", this.executionPlan).subscribe((response: any) => {
      this.executionPlan = response;
    });
  }
  addScenario(): void {
    this.http.put<any>(environment.backend + "/api/plan/newScenario", this.executionPlan).subscribe((response: any) => {
      this.executionPlan = response;
    });
  }
  currentlyRunning(): Observable<any> {
    return this.http.get<any>(environment.backend + "/api/plan/running")
  }
  stopPlan(): Observable<any> {
    return this.http.get<any>(environment.backend + "/api/plan/stop")
  }
  previewEvol(evol: any): Observable<any> {
    return this.http.post<any>(environment.backend + "/api/plan/preview/evol", evol);
  }
  previewPlan(): Observable<any> {
    return this.http.post<any>(environment.backend + "/api/plan/preview/plan", this.executionPlan);
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

  createDuration(seconds: number) {
    if (this.executionPlan.durationsType == 'FEEL') {
      return "PT" + seconds+ "S";
    } else if (this.executionPlan.durationsType == 'MILLIS') {
      return seconds*1000;
    }
    return seconds;
  }

  createStepInScenario(elementId: string): void {
    this.scenario.steps[elementId] = {
      "elementId": elementId,
      "action": "COMPLETE",
      "duration": {
        "startDesiredAvg": this.createDuration(8),
        "endDesiredAvg": this.createDuration(4),
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
  isNumber(value: any) {
  return typeof value === 'number';
}
  createPreStepInScenario(parentStep: string, type: string, elementRef: any, time: any): void {
    if (this.isNumber(time)) {
      time = this.createDuration(time);
    }
    if (!this.scenario.steps[parentStep]) {
      this.scenario.steps[parentStep] = {
        "elementId": parentStep,
        "action": "DO_NOTHING",
        "duration": {
          "startDesiredAvg": this.createDuration(1),
          "endDesiredAvg": this.createDuration(1),
          "minMaxPercent": 0,
          "avgProgression": "LINEAR",
          "progressionSalt": 0
        },
        "jsonTemplate": { "template": "", "exampleContext": {} },
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
        "delay": time,
        "msg": elementRef.name,
        "correlationKey": elementRef.extensionElements.valueOf("correlationKey").values[0].correlationKey.replace("=", "").trim(),
        "jsonTemplate": {
          template: "{}", exampleContext: {}
        }
      });
    } else if (type == 'CLOCK') {
      this.scenario.steps[parentStep].preSteps.push({
        "type": type,
        "delay": time
      });
    } else if (type == 'BPMN_ERROR') {
      this.scenario.steps[parentStep].preSteps.push({
        "type": type,
        "errorCode": elementRef.errorCode,
        "delay": time,
        "jsonTemplate": {
          template: "{}", exampleContext: {}
        }
      });
    } else if (type == 'SIGNAL') {
      this.scenario.steps[parentStep].preSteps.push({
        "type": type,
        "signal": elementRef.name,
        "delay": time,
        "jsonTemplate": {
          template: "{}", exampleContext: {}
        }
      });
    }
    this.selectActivity(parentStep);
  }

  createPostStepInScenario(parentStep: string, type: string, elementRef: any, time: any): void {
    if (this.isNumber(time)) {
      time = this.createDuration(time);
    }
    if (!this.scenario.steps[parentStep]) {
      this.scenario.steps[parentStep] = {
        "elementId": parentStep,
        "action": "COMPLETE",
        "duration": {
          "startDesiredAvg": 1000,
          "endDesiredAvg": 1000,
          "minMaxPercent": 0,
          "avgProgression": "LINEAR",
          "progressionSalt": 0
        },
        "jsonTemplate": { "template": "{}", "exampleContext": {} },
        "preSteps": [],
        "postSteps": []
      };
    }
    if (!this.scenario.steps[parentStep].preSteps) {
      this.scenario.steps[parentStep].postSteps = [];
    }
    if (type == 'MSG') {
      this.scenario.steps[parentStep].postSteps.push({
        "type": type,
        "delay": time,
        "msg": elementRef.name,
        "correlationKey": elementRef.extensionElements.valueOf("correlationKey").values[0].correlationKey.replace("=", "").trim(),
        "jsonTemplate": {
          template: "{}", exampleContext: {}
        }
      });
    } else if (type == 'CLOCK') {
      this.scenario.steps[parentStep].postSteps.push({
        "type": type,
        "delay": time
      });
    } else if (type == 'BPMN_ERROR') {
      this.scenario.steps[parentStep].postSteps.push({
        "type": type,
        "errorCode": elementRef.errorCode,
        "delay": time,
        "jsonTemplate": {
          template: "{}", exampleContext: {}
        }
      });
    } else if (type == 'SIGNAL') {
      this.scenario.steps[parentStep].postSteps.push({
        "type": type,
        "signal": elementRef.name,
        "delay": time,
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
