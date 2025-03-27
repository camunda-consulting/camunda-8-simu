import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { ExecPlanService } from '../services/exec-plan.service';
import { basicSetup, EditorView } from 'codemirror';
import { json } from '@codemirror/lang-json';
import NavigatedViewer from 'camunda-bpmn-js/lib/camunda-cloud/NavigatedViewer';

@Component({
    selector: 'app-test-plan',
    templateUrl: './test-plan.component.html',
    styleUrls: ['./test-plan.component.css'],
    standalone: false
})
export class TestPlanComponent implements OnInit, AfterViewInit {
  @ViewChild('jsonResult') jsonTemplate!: ElementRef;
  @ViewChild('testviewer') viewerElt: ElementRef | undefined;

  constructor(public execPlanService: ExecPlanService) { }
  testResult: any | undefined;
  currentTab: string | undefined;
  editor: EditorView | undefined;
  viewer: NavigatedViewer | undefined;
  alerts: any[] = [];
  testLoading: boolean=false;
  ngOnInit(): void {
    this.testPlan();
  }

  ngAfterViewInit(): void {
    if (this.testResult) {
      this.selectTab(Object.keys(this.testResult)[0]);
    }
  }

  testPlan(): void {
    this.testLoading = true;
    this.execPlanService.testPlan().subscribe((response: any) => {
      this.editor = undefined;
      this.testResult = response;
      this.selectTab(Object.keys(this.testResult)[0]);
      this.testLoading = false;
    });
  }
  selectTab(tab: string): void {
    this.currentTab = tab;
    if (this.currentTab == 'rawResult' && !this.editor) {
      this.jsonTemplate.nativeElement.innerHTML = '';
      this.editor = new EditorView({
        doc: JSON.stringify(this.testResult, null, 2),
        extensions: [
          basicSetup,
          json()
        ],
        parent: this.jsonTemplate.nativeElement,
      });
    } else {
      if (!this.viewer) {
        this.viewer = new NavigatedViewer({
          container: this.viewerElt!.nativeElement,
          height: 400
        });
      }
      this.viewer.importXML(this.execPlanService.executionPlan.xml).then((result: any) => {
        this.checkExecution();
      })
    }
  }

  checkExecution(): void {
    (this.viewer!.get('canvas') as any).zoom('fit-viewport');
    this.alerts = [];
    const eltRegistry: any = this.viewer!.get('elementRegistry');
    eltRegistry.forEach((elt: any) => {

        let result = this.getStatus(elt.id);
        if (result.status != "success") {
          this.alerts.push(result);
        }
    });
    for (let i = 0; i < this.testResult![this.currentTab!].length; i++) {
      let action = this.testResult![this.currentTab!][i];
      if (!action.expected && action.errorCode == 'Engine incident') {
        this.alerts.push({
          "status": "danger", "elementId": action.elementId, "message": "Instance got an incident on " + action.elementId + " with message : " + action.message
        });
      }
    }
    if (this.alerts.length == 0) {
      this.alerts.push({
        "status": "success", "message": "This scenario seems perfect."
      });
    }

  }

  getStatus(elementId: string):any {
    let scenario = null;
    for (let i = 0; i < this.execPlanService.executionPlan.scenarii.length; i++) {
      if (this.currentTab == this.execPlanService.executionPlan.scenarii[i].name) {
        scenario = this.execPlanService.executionPlan.scenarii[i];
        break;
      }
    }
    let scenarioDefined = scenario.steps[elementId];
    let executed = false;

    for (let i = 0; i < this.testResult![this.currentTab!].length; i++) {
      let record = this.testResult![this.currentTab!][i];
      if (elementId == record.elementId) {
        if (!record.jobKey && record.expected) {
          this.colorActivity(elementId, "#009933");
          return { "status": "success" };
        }
        executed = true;
        break;
      }
    }
    if (scenarioDefined && executed) {
      this.colorActivity(elementId, "#009933");
      return { "status": "success" };
    }
    if (scenarioDefined && !executed) {
      return {
        "status": "warning", "message": "The activity is defined but not used. It will cause useless memory consumption.", "elementId": elementId
      };
    }
    if (!scenarioDefined && executed) {
      this.colorActivity(elementId, "#FF0000");
      return {
        "status": "danger", "message": "The activity is not defined but executed. It will cause errors during simulation.", "elementId": elementId
      };
    }

    return { "status": "success" };
  }

  colorActivity(id: string, color: string): void {
    const elementRegistry: any = this.viewer?.get('elementRegistry');
    const graphicsFactory: any = this.viewer?.get('graphicsFactory');
    const element = elementRegistry?.get(id);
    if (element?.di !== undefined) {
      element.di.set('stroke', color);

      const gfx = elementRegistry?.getGraphics(element);
      if (gfx !== undefined) {
        graphicsFactory?.update('connection', element, gfx);
      }
    }
  };



  openDef(dep: any): void {
    if (dep == "Main definition") {
      this.viewer!.importXML(this.execPlanService.executionPlan.xml).then((result: any) => {
        this.checkExecution();
      });
    } else {
      this.viewer!.importXML(this.execPlanService.executionPlan.xmlDependencies[dep]).then((result: any) => {
        this.checkExecution();
      });
    }
  }

  showDeps(): boolean {
    return this.execPlanService.executionPlan.xmlDependencies != null && Object.keys(this.execPlanService.executionPlan.xmlDependencies).length > 0;
  }

}
