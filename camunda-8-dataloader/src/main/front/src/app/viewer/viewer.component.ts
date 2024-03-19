import { Component, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import NavigatedViewer from 'camunda-bpmn-js/lib/camunda-cloud/NavigatedViewer';
import { ProcessService } from '../services/process.service';
import { ExecPlanService } from '../services/exec-plan.service';

@Component({
  selector: 'app-viewer',
  templateUrl: './viewer.component.html',
  styleUrls: ['./viewer.component.css']
})
export class ViewerComponent implements AfterViewInit {

  constructor(private processService: ProcessService, private execPlanService: ExecPlanService) { }
  @ViewChild('viewer') viewerElt: ElementRef | undefined;
  viewer: NavigatedViewer | undefined;

  ngAfterViewInit(): void {

    this.viewer = new NavigatedViewer({
      container: this.viewerElt!.nativeElement,
      height: 500
    });
    this.viewer.importXML(this.processService.executionPlan.xml).then((result: any) => {
      const eltRegistry: any = this.viewer!.get('elementRegistry');
      eltRegistry.forEach((elt: any) => {
        if (["bpmn:UserTask", "bpmn:ServiceTask"].indexOf(elt.type) >= 0) {
          this.execPlanService.addActivity(elt);
        }
      })
    })

    this.viewer.on('element.click', this.displayInfoBox);
    this.viewer.on('canvas.viewbox.changing', () => {
      console.log('pouet');
    });

  }

  displayInfoBox = (event: any) => {
    console.log(event.element);
    this.execPlanService.selectActivity(event.element.id);
  }

}
