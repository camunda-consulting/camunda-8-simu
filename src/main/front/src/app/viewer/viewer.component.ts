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
  origins: any[] = [];
  addDepModal = false;
  newDepXml?: string;
  intermediateCatchEventSource?: any;

  constructor(private processService: ProcessService, public execPlanService: ExecPlanService) {
    this.execPlanService.activitySubject.subscribe((activity: string) => {
      this.colorSelectedActivity(activity);
    });
  }

  colorSelectedActivity(activity: string) {
    if (this.viewer) {
      if (this.execPlanService.scenario) {
        let stepScenar: string[] = [];
        for (let prop in this.execPlanService.scenario.steps) {
          this.colorActivity(prop, "#009900");
          stepScenar.push(prop);
        }
        const eltRegistry: any = this.viewer!.get('elementRegistry');
        eltRegistry.forEach((elt: any) => {
          if (this.execPlanService.isManagedActivity(elt.type)) {
            console.log(elt.di.bpmnElement.name);

            if (stepScenar.indexOf(elt.id) < 0) {
              this.colorActivity(elt.id, "#000000");
            }
          }
        });
        this.previousActivity = activity;
        this.colorActivity(activity, "#00CCFF");
      }
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
      this.colorSelectedActivity('blop');
    })

    this.viewer.on('element.click', this.selectActivity);
    this.viewer.on('canvas.viewbox.changing', () => {
      //console.log('pouet');
    });
  }

  openDef(dep: string): void {
    if (dep == "Main definition") {
      this.viewer!.importXML(this.execPlanService.executionPlan.xml).then((result: any) => {
        this.colorSelectedActivity('blop');
      });
    } else {
      this.viewer!.importXML(this.execPlanService.executionPlan.xmlDependencies[dep]).then((result: any) => {
        this.colorSelectedActivity('blop');
      });
    }
  }

  selectActivity = (event: any) => {
    this.selectedElt = event.element;
    if (this.execPlanService.isManagedActivity(this.selectedElt.type) && !this.execPlanService.scenario.steps[event.element.id]) {
      this.opennewstepModal();
    } else {
      this.execPlanService.selectActivity(this.selectedElt.id);
    }
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

  findOrigins(intermediateCatchEvent: any): string[] {
    let result: string[] = [];
    for (let incoming of intermediateCatchEvent.incoming) {
      if (incoming.sourceRef.$type == 'bpmn:ServiceTask' || incoming.sourceRef.$type == 'bpmn:UserTask') {
        result.push(incoming.sourceRef);
      } else {
        result = result.concat(this.findOrigins(incoming.sourceRef));
      }
    }
    return result;
  }

  opennewstepModal() {
    this.origins = []
    if (this.selectedElt.type == 'bpmn:IntermediateCatchEvent') {
      this.origins = this.findOrigins(this.selectedElt.businessObject);
      if (this.origins && this.origins.length > 0) {
        this.intermediateCatchEventSource = this.origins[0].id;
      }
    }
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
        this.execPlanService.createPreStepInScenario(parentStep, 'BPMN_ERROR', errorRef, 1);
      } else if (type == 'bpmn:SignalEventDefinition') {
        let signalRef = this.selectedElt.businessObject.eventDefinitions[0].signalRef;
        this.execPlanService.createPreStepInScenario(parentStep, 'SIGNAL', signalRef, 1);
      }
      else {
        alert('Implementation for the type ' + type + ' is missing.');
      }
    } else if (this.selectedElt.type == 'bpmn:IntermediateCatchEvent') {
      let type = this.selectedElt.businessObject.eventDefinitions[0].$type;

      if (type == 'bpmn:MessageEventDefinition') {
        let messageRef = this.selectedElt.businessObject.eventDefinitions[0].messageRef;
        this.execPlanService.createPostStepInScenario(this.intermediateCatchEventSource, 'MSG', messageRef, 3600);
      } else if (type == 'bpmn:TimerEventDefinition') {
        let timer = this.selectedElt.businessObject.eventDefinitions[0].timeDuration.body;
        this.execPlanService.createPostStepInScenario(this.intermediateCatchEventSource, 'CLOCK', null, timer);
      } else if (type == 'bpmn:ErrorEventDefinition') {
        let errorRef = this.selectedElt.businessObject.eventDefinitions[0].errorRef;
        this.execPlanService.createPostStepInScenario(this.intermediateCatchEventSource, 'BPMN_ERROR', errorRef, 1);
      } else if (type == 'bpmn:SignalEventDefinition') {
        let signalRef = this.selectedElt.businessObject.eventDefinitions[0].signalRef;
        this.execPlanService.createPostStepInScenario(this.intermediateCatchEventSource, 'SIGNAL', signalRef, 1);
      }
      else {
        alert('Implementation for the type ' + type + ' is missing.');
      }

    }  else {
      this.execPlanService.createStepInScenario(this.selectedElt.id);
    }
    this.closenewstepModal();
  }


  toggleAddDep(): void {
    this.addDepModal = !this.addDepModal;
    if (this.addDepModal) {
      (window as any).bootstrap.Modal.getOrCreateInstance(document.getElementById('addDepModal')).show();
    } else {
      this.newDepXml = undefined;
      (window as any).bootstrap.Modal.getInstance(document.getElementById('addDepModal')).hide();
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
    this.newDepXml = evt.target.result;
  }
  _handleReaderError() {
    this.newDepXml = undefined;
  }
  addDep(): void {
    let xml = this.newDepXml!;
    this.viewer!.importXML(xml).then((result: any) => {
      const eltRegistry: any = this.viewer!.get('elementRegistry');
      eltRegistry.forEach((elt: any) => {
        if (elt.type == "bpmn:Process") {
          if (!this.execPlanService.executionPlan.xmlDependencies || this.execPlanService.executionPlan.xmlDependencies==null) {
            this.execPlanService.executionPlan.xmlDependencies = {};
          }
          this.execPlanService.executionPlan.xmlDependencies[elt.id] = xml;
          this.xmlDeps.push(elt.id);
        }
      });
    });

    this.toggleAddDep();
  }
}
