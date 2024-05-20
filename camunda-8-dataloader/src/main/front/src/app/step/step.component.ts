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
}
