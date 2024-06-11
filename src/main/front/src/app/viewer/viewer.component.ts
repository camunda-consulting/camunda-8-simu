import { Component, AfterViewInit, ViewChild, ElementRef, OnInit } from '@angular/core';
import NavigatedViewer from 'camunda-bpmn-js/lib/camunda-cloud/NavigatedViewer';
import { ProcessService } from '../services/process.service';
import { ExecPlanService } from '../services/exec-plan.service';

@Component({
  selector: 'app-viewer',
  templateUrl: './viewer.component.html',
  styleUrls: ['./viewer.component.css']
})
export class ViewerComponent implements AfterViewInit, OnInit {
  @ViewChild('viewer') viewerElt: ElementRef | undefined;
  viewer: NavigatedViewer | undefined;
  previousActivity?: string;
  xmlDeps = ["Main definition"];
  selectedElt: any = null;

  constructor(private processService: ProcessService, public execPlanService: ExecPlanService) {
    this.execPlanService.activitySubject.subscribe((activity: string) => {
      this.colorSelectedActivity(activity);
    });
  }

  colorSelectedActivity(activity: string) {
    if (this.viewer) {
      if (this.previousActivity) {
        this.colorActivity(this.previousActivity, "#000000");
      }
      this.previousActivity = activity;
      this.colorActivity(activity, "#00CCFF");
    }
  }
    ngOnInit(): void {
      if (this.execPlanService.executionPlan.xmlDependencies) {
        for (let prop in this.execPlanService.executionPlan.xmlDependencies) {
          this.xmlDeps.push(prop);
        }
      }
    }



  ngAfterViewInit(): void {

    this.viewer = new NavigatedViewer({
      container: this.viewerElt!.nativeElement,
      height: 400
    });
    this.viewer.importXML(this.execPlanService.executionPlan.xml).then((result: any) => {
      /*const eltRegistry: any = this.viewer!.get('elementRegistry');
      eltRegistry.forEach((elt: any) => {
        if (["bpmn:UserTask", "bpmn:ServiceTask"].indexOf(elt.type) >= 0) {
          this.execPlanService.addActivity(elt.id);
        }
      })*/
    })

    this.viewer.on('element.click', this.selectActivity);
    this.viewer.on('canvas.viewbox.changing', () => {
      //console.log('pouet');
    });

  }

  openDef(dep: string): void {
    if (dep == "Main definition") {
      this.viewer!.importXML(this.execPlanService.executionPlan.xml).then((result: any) => { });
    } else {
      this.viewer!.importXML(this.execPlanService.executionPlan.xmlDependencies[dep]).then((result: any) => { });
    }
  }

  managedActivities = ["bpmn:ServiceTask", "bpmn:UserTask", "bpmn: SendTask", "bpmn:IntermediateCatchEvent", "bpmn:BoundaryEvent"];
  selectActivity = (event: any) => {
    this.selectedElt = event.element;
    if (this.managedActivities.indexOf(this.selectedElt.type) >= 0 && !this.execPlanService.scenario.steps[event.element.id]) {
      this.opennewstepModal();
    } else {
      this.execPlanService.selectActivity(this.selectedElt.id);
    }
  }
  
  colorActivity(id: string, color: string): void {
    const elementRegistry:any = this.viewer?.get('elementRegistry');
    const graphicsFactory:any = this.viewer?.get('graphicsFactory');
    const element = elementRegistry?.get(id);
    if (element?.di !== undefined) {
      element.di.set('stroke', color);

      const gfx = elementRegistry?.getGraphics(element);
      if (gfx !== undefined) {
        graphicsFactory?.update('connection', element, gfx);
      }
    }
  };

  opennewstepModal() {
    (window as any).bootstrap.Modal.getOrCreateInstance(document.getElementById('newstep')).show();
  }
  closenewstepModal() {
    (window as any).bootstrap.Modal.getInstance(document.getElementById('newstep')).hide();
  }
  createStep() {
    if (this.selectedElt.type == 'bpmn:BoundaryEvent') {
      let type = this.selectedElt.businessObject.eventDefinitions[0].$type;
      let parentStep = this.selectedElt.businessObject.attachedToRef.id;

      if (type == 'bpmn:MessageEventDefinition') {
        let messageRef = this.selectedElt.businessObject.eventDefinitions[0].messageRef;
        this.execPlanService.createPreStepInScenario(parentStep, 'MSG', messageRef, 3600);
      } else if (type == 'bpmn:TimerEventDefinition') {
        let timer = this.selectedElt.businessObject.eventDefinitions[0].timeDuration.body;
        this.execPlanService.createPreStepInScenario(parentStep, 'CLOCK', null, timer);
      } else if (type == 'bpmn:ErrorEventDefinition') {
        let errorRef = this.selectedElt.businessObject.eventDefinitions[0].errorRef;
        this.execPlanService.createPreStepInScenario(parentStep, 'BPMN_ERROR', errorRef, 1000);
      }
      else {
        alert('Implementation for the type ' + type + ' is missing.');
      }
    } else {
      this.execPlanService.createStepInScenario(this.selectedElt.id);
    }
    this.closenewstepModal();
  }
}
