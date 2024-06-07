import { Component, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { basicSetup, EditorView } from 'codemirror';
import { json } from '@codemirror/lang-json';
import { ProcessService } from '../services/process.service';
import { ExecPlanService } from '../services/exec-plan.service';

@Component({
  selector: 'app-instantiate',
  templateUrl: './instantiate.component.html',
  styleUrls: ['./instantiate.component.css']
})
export class InstantiateComponent implements AfterViewInit {

  @ViewChild('jsonTemplate') jsonTemplate!: ElementRef;
  codeMirror?: EditorView;

  constructor(private processService: ProcessService, public execPlanService: ExecPlanService) { }

  ngAfterViewInit(): void {
    this.codeMirror = new EditorView({
      doc: this.execPlanService.prettifyJsonTemplate(this.execPlanService.scenario.jsonTemplate.template),
      extensions: [
        basicSetup,
        json(),
        this.updateListenerExtension
      ],
      parent: this.jsonTemplate.nativeElement,
    });
  }

  updateListenerExtension = EditorView.updateListener.of((v) => {
    this.execPlanService.scenario.jsonTemplate.template =v.state.doc.toString();
  });
}
