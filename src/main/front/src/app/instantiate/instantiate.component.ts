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
      doc: this.prettify(),
      extensions: [
        basicSetup,
        json(),
        this.updateListenerExtension
      ],
      parent: this.jsonTemplate.nativeElement,
    });
  }

  prettify():string {
    try {
      return JSON.stringify(JSON.parse(this.execPlanService.scenario.jsonTemplate), null, 2);
    } catch(error) {
      return this.execPlanService.scenario.jsonTemplate;
    }
  }

  updateListenerExtension = EditorView.updateListener.of((v) => {
    this.execPlanService.scenario.jsonTemplate =v.state.doc.toString();
  });
}
