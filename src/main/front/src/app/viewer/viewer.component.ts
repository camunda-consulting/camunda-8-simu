import { Component, AfterViewInit, ViewChild, ElementRef, OnInit } from '@angular/core';
import NavigatedViewer from 'camunda-bpmn-js/lib/camunda-cloud/NavigatedViewer';
import BpmnModeler from 'camunda-bpmn-js/lib/camunda-cloud/Modeler';
import ElementTemplatesIconsRenderer from '@bpmn-io/element-template-icon-renderer';
import { CamundaCloudModeler as DmnModeler } from 'camunda-dmn-js';
import { basicSetup, EditorView } from 'codemirror';
import { xml } from '@codemirror/lang-xml';

import { ExecPlanService } from '../services/exec-plan.service';

@Component({
    selector: 'app-viewer',
    templateUrl: './viewer.component.html',
    styleUrls: ['./viewer.component.css'],
    standalone: false
})
export class ViewerComponent implements AfterViewInit, OnInit {

  @ViewChild('viewer') viewerElt: ElementRef | undefined;
  @ViewChild('dmn') dmnElt: ElementRef | undefined;
  @ViewChild('dmnProperties') dmnPropElt: ElementRef | undefined;
  @ViewChild('jsonDepDefinition') jsonDepDefinition!: ElementRef
  @ViewChild('modelerDepDefinition') modelerDepDefinition!: ElementRef;
  @ViewChild('modelerProperties') modelerProperties!: ElementRef;

  viewer: NavigatedViewer | undefined;
  editDepModeler: BpmnModeler | undefined;
  editDepJson: EditorView | undefined;
  dmnEditor: any | undefined;
  previousActivity?: string;
  showDmn = false;
  xmlDeps = ["Main definition"];
  dmnDeps: string[] = [];
  currentDmn: string = '';
  currentDep: string = 'Main definition';
  selectedElt: any = null;
  origins: any[] = [];
  addDepModal = false;
  editDepModal = false;
  newDepXml?: string;
  intermediateCatchEventSource?: any;
  editDepType = 'modeler';

  constructor(public execPlanService: ExecPlanService) {
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
    if (this.execPlanService.executionPlan.dmnDependencies) {
      for (let prop in this.execPlanService.executionPlan.dmnDependencies) {
        this.dmnDeps.push(prop);
      }
    }
  }

  ngAfterViewInit(): void {
    this.viewer = new NavigatedViewer({
      container: this.viewerElt!.nativeElement,
      height: 400,
      additionalModules: [ElementTemplatesIconsRenderer]
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
    this.showDmn = false;
    this.currentDep = dep;
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

  buildDmnEditor(): void {
    this.showDmn = true;
    if (!this.dmnEditor) {
      this.dmnEditor = new DmnModeler({
        container: this.dmnElt!.nativeElement,
        drd: {
          propertiesPanel: {
            parent: this.dmnPropElt!.nativeElement
          }
        },
        height: 400,
        width: '100%',
        keyboard: {
          bindTo: window
        }
      });
    }

  }

  openDmn(dep: string): void {
    this.buildDmnEditor();
    this.currentDmn = dep;
    let editor = this.dmnEditor;
    this.dmnEditor.importXML(this.execPlanService.executionPlan.dmnDependencies[dep], function (err: any) {
      var activeView = editor.getActiveView();
      if (activeView.type === 'drd') {
        var activeEditor = editor.getActiveViewer();
      }
    });
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
	if (intermediateCatchEvent.incoming) {
      for (let incoming of intermediateCatchEvent.incoming) {
	    if (this.loopControl.indexOf(incoming.sourceRef.id)<0) {
	      console.log(incoming.sourceRef);
	      this.loopControl.push(incoming.sourceRef.id);
          if (incoming.sourceRef.$type == 'bpmn:ServiceTask' || incoming.sourceRef.$type == 'bpmn:UserTask') {
            result.push(incoming.sourceRef);
          } else {
            result = result.concat(this.findOrigins(incoming.sourceRef));
          }
	    }
      }
	}
    return result;
  }

  loopControl:string[]=[];
  opennewstepModal() {
    this.origins = [];
    this.loopControl=[this.selectedElt.id];
    if (this.selectedElt.type == 'bpmn:IntermediateCatchEvent') {
	  console.log(this.selectedElt);
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
        let moddleElt = this.selectedElt.businessObject.eventDefinitions[0];
        let timer = 'PT10M';
        if (moddleElt.timeCycle) {
          timer = moddleElt.timeCycle.body.substring(2);
        } else if (moddleElt.timeDuration) {
          timer = moddleElt.timeDuration.body;
        }
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
    } else if (this.selectedElt.type == 'bpmn:IntermediateCatchEvent' && this.selectedElt.businessObject.eventDefinitions[0].$type == 'bpmn:TimerEventDefinition') {
	  //manage clock intermediate catch event with a specific step.
	  let timer = this.selectedElt.businessObject.eventDefinitions[0].timeDuration.body;
      this.execPlanService.createClockStepInScenario(this.selectedElt.id, timer);
	} else if (this.selectedElt.type == 'bpmn:IntermediateCatchEvent') {
      let type = this.selectedElt.businessObject.eventDefinitions[0].$type;

      if (type == 'bpmn:MessageEventDefinition') {
        let messageRef = this.selectedElt.businessObject.eventDefinitions[0].messageRef;
        this.execPlanService.createPostStepInScenario(this.intermediateCatchEventSource, 'MSG', messageRef, 3600);
      } /*else if (type == 'bpmn:TimerEventDefinition') {
        let timer = this.selectedElt.businessObject.eventDefinitions[0].timeDuration.body;
        this.execPlanService.createPostStepInScenario(this.intermediateCatchEventSource, 'CLOCK', null, timer);
      }*/ else if (type == 'bpmn:ErrorEventDefinition') {
        let errorRef = this.selectedElt.businessObject.eventDefinitions[0].errorRef;
        this.execPlanService.createPostStepInScenario(this.intermediateCatchEventSource, 'BPMN_ERROR', errorRef, 1);
      } else if (type == 'bpmn:SignalEventDefinition') {
        let signalRef = this.selectedElt.businessObject.eventDefinitions[0].signalRef;
        this.execPlanService.createPostStepInScenario(this.intermediateCatchEventSource, 'SIGNAL', signalRef, 1);
      }
      else {
        alert('Implementation for the type ' + type + ' is missing.');
      }

    } else {
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
      let roots: any[] = this.viewer?.getDefinitions().rootElements;
      for (let elt of roots) {
        if (elt.$type == "bpmn:Process") {
          if (!this.execPlanService.executionPlan.xmlDependencies || this.execPlanService.executionPlan.xmlDependencies == null) {
            this.execPlanService.executionPlan.xmlDependencies = {};
          }
          this.execPlanService.executionPlan.xmlDependencies[elt.id] = xml;
          this.xmlDeps.push(elt.id);
        }
      }
    }).catch(() => {
      if (xml.indexOf("decision") > 0) {
        this.buildDmnEditor();
        this.dmnEditor.importXML(xml).then((err: any) => {
          let name = this.dmnEditor._definitions.name;
          if (!this.execPlanService.executionPlan.dmnDependencies || this.execPlanService.executionPlan.dmnDependencies == null) {
            this.execPlanService.executionPlan.dmnDependencies = {};
          }
          this.execPlanService.executionPlan.dmnDependencies[name] = xml;
          this.dmnDeps.push(name);
          this.currentDmn = name;
        });
      }
    });

    this.toggleAddDep();
  }

  saveDmn(): void {

    this.dmnEditor!.saveXML().then((result: any) => {
      this.execPlanService.executionPlan.dmnDependencies[this.currentDmn] = result.xml;
    });
  }

  toggleEditDep(dep: string): void {
    if (dep != '') {
      this.currentDep = dep;
    }
    this.editDepModal = !this.editDepModal;
    if (this.editDepModal) {
      (window as any).bootstrap.Modal.getOrCreateInstance(document.getElementById('editDepModal')).show();
    let content = '';
    if (this.currentDep == "Main definition") {
      content = this.execPlanService.executionPlan.xml;
    } else {
      content = this.execPlanService.executionPlan.xmlDependencies[this.currentDep];
    }
      this.prepareDefinitionEdition(content);
    } else {
      this.newDepXml = undefined;
      (window as any).bootstrap.Modal.getInstance(document.getElementById('editDepModal')).hide();
    }
  }

  setEditDepType(type: string) {
    this.editDepType = type;
	if (this.editDepType=='XML') {
	  this.editDepModeler!.saveXML({format: true}).then((result: any) => {
	    this.prepareDefinitionEdition(result.xml);
	  });
	} else {
	  this.prepareDefinitionEdition(this.editDepJson!.state.doc.toString());
	}
  }

  prepareDefinitionEdition(content: string): void {

    if (this.editDepType == 'XML') {
      if (!this.editDepJson) {
        this.editDepJson = new EditorView({
          doc: content,
          extensions: [
            basicSetup,
            xml()/*,
            this.updateTemplate*/
          ],
          parent: this.jsonDepDefinition.nativeElement,
        });
      } else {
	    this.editDepJson.dispatch({changes: {
		  from: 0,
		  to: this.editDepJson.state.doc.length,
		  insert: content
		}})
	  }

    } else {
      if (!this.editDepModeler) {
        this.editDepModeler = new BpmnModeler({
          container: this.modelerDepDefinition!.nativeElement,
          propertiesPanel: {
            parent: this.modelerProperties!.nativeElement
          },
          height: 600,
          additionalModules: [ElementTemplatesIconsRenderer]
        });
      }
      this.editDepModeler!.importXML(content).then((result: any) => { });
    }

  }

  applyDepModif(): void {
    if (this.editDepType == 'XML') {
      this.execPlanService.updateDep(this.currentDep, this.editDepJson!.state.doc.toString());
      this.toggleEditDep('');
    } else {

      this.editDepModeler!.saveXML({format: true}).then((result: any) => {
        this.execPlanService.updateDep(this.currentDep, result.xml);
        this.toggleEditDep('');
      });
    }
  }
}
