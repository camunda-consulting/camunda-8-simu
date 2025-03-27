import { Component, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import BpmnModeler from 'camunda-bpmn-js/lib/camunda-cloud/Modeler';
import ElementTemplatesIconsRenderer from '@bpmn-io/element-template-icon-renderer';
import { ExecPlanService } from '../services/exec-plan.service';

@Component({
    selector: 'app-modeler',
    templateUrl: './modeler.component.html',
    styleUrls: ['./modeler.component.css'],
    standalone: false
})
export class ModelerComponent implements AfterViewInit {

  constructor(private execPlanService: ExecPlanService) { }

  @ViewChild('modeler') modelerElt: ElementRef | undefined;
  @ViewChild('properties') properties: ElementRef | undefined;

  modeler: BpmnModeler | undefined;

  ngAfterViewInit(): void {

    this.modeler = new BpmnModeler({
      container: this.modelerElt!.nativeElement,
      propertiesPanel: {
        parent: this.properties!.nativeElement
      },
      height: 'calc(100vh - 50px)',
      position: 'center',
      additionalModules: [ElementTemplatesIconsRenderer],
    });
    this.modeler!.importXML(this.execPlanService.executionPlan.xml);
 
  }

  save(): void {
    this.modeler!.saveXML().then((result: any) => {
      this.execPlanService.updateDef(result.xml);
    });
  }

}
