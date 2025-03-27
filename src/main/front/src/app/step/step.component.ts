import { Component, AfterViewInit, Input, OnInit } from '@angular/core';
import { basicSetup, EditorView } from 'codemirror';
import { json } from '@codemirror/lang-json';
import { ExecPlanService } from '../services/exec-plan.service';

@Component({
    selector: 'app-step',
    templateUrl: './step.component.html',
    styleUrls: ['./step.component.css'],
    standalone: false
})
export class StepComponent implements AfterViewInit, OnInit {

  @Input() step: any;
  prestep: any;
  poststep: any;

  constructor(public execPlanService: ExecPlanService) { }
  ngOnInit(): void {
    if (this.step.preSteps && this.step.preSteps.length > 0) {
      this.prestep = this.step.preSteps[0];
    }
    if (this.step.postSteps && this.step.postSteps.length > 0) {
      this.poststep = this.step.postSteps[0];
    }
  }

  ngAfterViewInit(): void {

    var tooltips = document.querySelectorAll('.btn-tooltip')
    for (let i = 0; i < tooltips.length; i++) {
      (window as any).bootstrap.Tooltip.getOrCreateInstance(tooltips[i]);
    }
  }

  displayAdditionalStep(step: any): string {
    if (step.type == 'CLOCK') {
      return 'CLOCK at ' + step.delay;
    }
    if (step.type == 'MSG') {
      return 'Publish ' + step.msg + ' after ' + step.delay + '(' + this.execPlanService.executionPlan!.durationsType + ')';
    }
    if (step.type == 'SIGNAL') {
      return 'Broadcast ' + step.signal + ' after ' + step.delay + '(' + this.execPlanService.executionPlan!.durationsType + ')';
    }
    return 'Error ' + step.errorCode + ' after ' + step.delay + '(' + this.execPlanService.executionPlan!.durationsType + ')';
  }

  /*updatePreStep = EditorView.updateListener.of((v) => {
    this.prestep.jsonTemplate.template = v.state.doc.toString();
  });

  updatePostStep = EditorView.updateListener.of((v) => {
    this.poststep.jsonTemplate.template = v.state.doc.toString();
  });*/

  openDurationModal() {
    (window as any).bootstrap.Modal.getOrCreateInstance(document.getElementById(this.step.elementId + '-durationModal')).show();
  }
  closeDurationModal() {
    (window as any).bootstrap.Modal.getInstance(document.getElementById(this.step.elementId + '-durationModal')).hide();
  }

  openPreStepModal(index: number) {
    this.prestep = this.step.preSteps[index];
    (window as any).bootstrap.Modal.getOrCreateInstance(document.getElementById(this.step.elementId + '-prestep')).show();
  }
  closePreStepModal() {
    (window as any).bootstrap.Modal.getInstance(document.getElementById(this.step.elementId + '-prestep')).hide();
  }
  openPostStepModal(index: number) {
    this.poststep = this.step.postSteps[index];
    (window as any).bootstrap.Modal.getOrCreateInstance(document.getElementById(this.step.elementId + '-poststep')).show();
  }
  closePostStepModal() {
    (window as any).bootstrap.Modal.getInstance(document.getElementById(this.step.elementId + '-poststep')).hide();
  }

  addPreStep() {
    this.prestep = { "type": "CLOCK", "feelDelay": "PT5M", "jsonTemplate": { "template": "{}", "exampleContext": {} } };
    if (!this.step.preSteps) {
      this.step.preSteps = [];
    }
    this.step.preSteps.push(this.prestep);
    this.openPreStepModal(this.step.preSteps.length - 1);
  }
  addPostStep() {
    this.poststep = { "type": "CLOCK", "feelDelay": "PT5M", "jsonTemplate": { "template": "{}", "exampleContext": {} } };
    if (!this.step.postSteps) {
      this.step.postSteps = [];
    }
    this.step.postSteps.push(this.poststep);
    this.openPostStepModal(this.step.postSteps.length - 1);
  }

  deletePreStep(i: number): void {
    this.step.preSteps.splice(i, 1);
  }
  deletePostStep(i: number): void {
    this.step.postSteps.splice(i, 1);
  }

}
