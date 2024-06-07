import { Component, AfterViewInit, ViewChild, ElementRef, Input, OnInit } from '@angular/core';
import { basicSetup, EditorView } from 'codemirror';
import { json } from '@codemirror/lang-json';
import { ProcessService } from '../services/process.service';
import { ExecPlanService } from '../services/exec-plan.service';
@Component({
  selector: 'app-step',
  templateUrl: './step.component.html',
  styleUrls: ['./step.component.css']
})
export class StepComponent implements AfterViewInit, OnInit {

  @Input() step: any;
  @ViewChild('jsonTemplate') jsonTemplate!: ElementRef;
  codeMirror?: EditorView;
  preStepcodeMirror?: EditorView;
  postStepcodeMirror?: EditorView;
  prestep: any;
  poststep: any;

  constructor(private processService: ProcessService, public execPlanService: ExecPlanService) { }
  ngOnInit(): void {
    if (this.step.preSteps && this.step.preSteps.length > 0) {
      this.prestep = this.step.preSteps[0];
    }
    if (this.step.postSteps && this.step.postSteps.length > 0) {
      this.poststep = this.step.postSteps[0];
    }
  }

  ngAfterViewInit(): void {
    this.codeMirror = new EditorView({
      doc: this.execPlanService.prettifyJsonTemplate(this.step.jsonTemplate.template),
      extensions: [
        basicSetup,
        json(),
        this.updateListenerExtension
      ],
      parent: this.jsonTemplate.nativeElement,
    });

    var tooltips = document.querySelectorAll('.btn-tooltip')
    for (let i = 0; i < tooltips.length; i++) {
      (window as any).bootstrap.Tooltip.getOrCreateInstance(tooltips[i]);
    }
  }

  buildPreStepJsonEditor(): void {
    if ((this.prestep.type == 'MSG' || this.prestep.type == 'BPMN_ERROR') && !this.preStepcodeMirror) {
      if (!this.prestep.jsonTemplate.template) {
        this.prestep.jsonTemplate.template = '{}';
      }
      this.preStepcodeMirror = new EditorView({
        doc: this.execPlanService.prettifyJsonTemplate(this.prestep.jsonTemplate.template),
        extensions: [
          basicSetup,
          json(),
          this.updatePreStep
        ],
        parent: document.getElementById('preStepJsonTemplate')!,
      });
    }
  }

  buildPostStepJsonEditor(): void {
    if ((this.poststep.type == 'MSG' || this.poststep.type == 'BPMN_ERROR') && !this.postStepcodeMirror) {
      if (!this.poststep.jsonTemplate.template) {
        this.poststep.jsonTemplate.template = '{}';
      }
      this.preStepcodeMirror = new EditorView({
        doc: this.execPlanService.prettifyJsonTemplate(this.poststep.jsonTemplate.template),
        extensions: [
          basicSetup,
          json(),
          this.updatePostStep
        ],
        parent: document.getElementById('postStepJsonTemplate')!,
      });
    }
  }

  updateListenerExtension = EditorView.updateListener.of((v) => {
    this.step.jsonTemplate.template = v.state.doc.toString();
  });

  displayAdditionalStep(step: any): string {
    if (step.type == 'CLOCK') {
      return 'CLOCK at ' + step.feelDelay;
    }
    if (step.type == 'MSG') {
      return 'Publish ' + step.msg + ' after ' + step.msgDelay + 'ms';
    }
    return 'Error ' + step.errorCode + ' after ' + step.errorDelay + 'ms';
  }

  updatePreStep = EditorView.updateListener.of((v) => {
    this.prestep.jsonTemplate.template = v.state.doc.toString();
  });

  updatePostStep = EditorView.updateListener.of((v) => {
    this.poststep.jsonTemplate.template = v.state.doc.toString();
  });

  openDurationModal() {
    (window as any).bootstrap.Modal.getOrCreateInstance(document.getElementById(this.step.elementId + '-durationModal')).show();
  }
  closeDurationModal() {
    (window as any).bootstrap.Modal.getInstance(document.getElementById(this.step.elementId + '-durationModal')).hide();
  }

  openPreStepModal(index: number) {
    this.prestep = this.step.preSteps[index];
    this.buildPreStepJsonEditor();
    (window as any).bootstrap.Modal.getOrCreateInstance(document.getElementById(this.step.elementId + '-prestep')).show();
  }
  closePreStepModal() {
    (window as any).bootstrap.Modal.getInstance(document.getElementById(this.step.elementId + '-prestep')).hide();
  }
  openPostStepModal(index: number) {
    this.poststep = this.step.postSteps[index];
    this.buildPostStepJsonEditor();
    (window as any).bootstrap.Modal.getOrCreateInstance(document.getElementById(this.step.elementId + '-poststep')).show();
  }
  closePostStepModal() {
    (window as any).bootstrap.Modal.getInstance(document.getElementById(this.step.elementId + '-poststep')).hide();
  }

  addPreStep() {
    this.prestep = { "type": "CLOCK", "feelDelay": "PT5M" };
    if (!this.step.preSteps) {
      this.step.preSteps = [];
    }
    this.step.preSteps.push(this.prestep);
    this.openPreStepModal(this.step.preSteps.length - 1);
  }
  addPostStep() {
    this.poststep = { "type": "CLOCK", "feelDelay": "PT5M" };
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
