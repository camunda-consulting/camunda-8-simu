import { Component, AfterViewInit, ViewChild, ElementRef, Input } from '@angular/core';
import { basicSetup, EditorView } from 'codemirror';
import { json } from '@codemirror/lang-json';
import { ProcessService } from '../services/process.service';
import { ExecPlanService } from '../services/exec-plan.service';
@Component({
  selector: 'app-step',
  templateUrl: './step.component.html',
  styleUrls: ['./step.component.css']
})
export class StepComponent implements AfterViewInit {

  @Input() step: any;
  @ViewChild('jsonTemplate') jsonTemplate!: ElementRef;
  codeMirror?: EditorView;
  prestep: any;
  poststep: any;

  constructor(private processService: ProcessService, public execPlanService: ExecPlanService) { }

  ngAfterViewInit(): void {
    this.codeMirror = new EditorView({
      doc: this.prettify(),
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

  prettify(): string {
    try {
      return JSON.stringify(JSON.parse(this.step.jsonTemplate), null, 2);
    } catch (error) {
      return this.step.jsonTemplate;
    }
  }

  updateListenerExtension = EditorView.updateListener.of((v) => {
    this.step.jsonTemplate = v.state.doc.toString();
  });

  openDurationModal() {
    (window as any).bootstrap.Modal.getOrCreateInstance(document.getElementById(this.step.elementId +'-durationModal')).show();
  }
  closeDurationModal() {
    (window as any).bootstrap.Modal.getInstance(document.getElementById(this.step.elementId + '-durationModal')).hide();
  }

  openPreStepModal(index:number) {
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
    if (!this.step.preSteps) {
      this.step.preSteps = [];
    }
    this.step.preSteps.push({ "type": "CLOCK", "delay": 360000 });
    this.openPreStepModal(this.step.preSteps.length-1);
  }

  addPostStep() {
    if (!this.step.postSteps) {
      this.step.postSteps = [];
    }
    this.step.postSteps.push({ "type": "CLOCK", "delay": 360000 });
    this.openPostStepModal(this.step.postSteps.length - 1);
  }

}
