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
  @ViewChild('viewer') viewerElt: ElementRef | undefined;
  viewer: NavigatedViewer | undefined;
  previousActivity?: string;

  constructor(private processService: ProcessService, private execPlanService: ExecPlanService) {
    this.execPlanService.activitySubject.subscribe((activity: string) => {
      if (this.viewer) {
        if (this.previousActivity) {
          this.colorActivity(this.previousActivity, "#000000");
        }
        this.previousActivity = activity;
        this.colorActivity(activity, "#00CCFF");
      }
    });
  }

  ngAfterViewInit(): void {

    this.viewer = new NavigatedViewer({
      container: this.viewerElt!.nativeElement,
      height: 400
    });
    this.viewer.importXML(this.execPlanService.executionPlan.xml).then((result: any) => {
      const eltRegistry: any = this.viewer!.get('elementRegistry');
      eltRegistry.forEach((elt: any) => {
        if (["bpmn:UserTask", "bpmn:ServiceTask"].indexOf(elt.type) >= 0) {
          this.execPlanService.addActivity(elt.id);
        }
      })
    })

    this.viewer.on('element.click', this.selectActivity);
    this.viewer.on('canvas.viewbox.changing', () => {
      //console.log('pouet');
    });

  }

  selectActivity = (event: any) => {
    this.execPlanService.selectActivity(event.element.id);
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

}
