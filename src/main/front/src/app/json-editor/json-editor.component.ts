import { Component, Input, Output, AfterViewInit, EventEmitter, ViewChild, ElementRef } from '@angular/core';
import { basicSetup, EditorView } from 'codemirror';
import { json } from '@codemirror/lang-json';
import { environment } from '../../environments/environment';
import { TemplatingService } from '../services/templating.service';

@Component({
  selector: 'app-json-editor',
  templateUrl: './json-editor.component.html',
  styleUrls: ['./json-editor.component.css']
})
export class JsonEditorComponent implements AfterViewInit {

  @ViewChild('jsonTemplate') jsonTemplate!: ElementRef;
  @ViewChild('contextTemplate') contextTemplate!: ElementRef;
  
  @Input() label: string = 'Json Template';
  @Input() classnames: string = '';
  @Input() template: any;
  @Output() templateChange = new EventEmitter<any>();
  displayHelp = false;
  displayCheck = false;
  checkResult?: string;
  templatingMethods?: any[];
  constructor(private templatingService: TemplatingService) { }

  help(): void {
    if (this.templatingMethods) {
      this.displayHelp = true;
    } else {
      this.templatingService.listTemplatingMethods().subscribe((response: any[]) => {
        this.templatingMethods = response;
        this.displayHelp = true;
      });
    }
  }

  closeHelp(): void {
    this.displayHelp = false;
  }

  showExample(i: number): void {
    this.templatingMethods![i].showExample = !this.templatingMethods![i].showExample;
  }

  toggleCheck(): void {
    this.checkResult = undefined;
    this.displayCheck = !this.displayCheck
  }

  checkTemplate(): void {
    this.templatingService.checkTemplate(this.template).subscribe((response: any) => {
      this.checkResult = JSON.stringify(response, null, 2);
    });
  }

  ngAfterViewInit(): void {
    new EditorView({
      doc: this.prettifyJsonTemplate(this.template.template),
      extensions: [
        basicSetup,
        json(),
        this.updateTemplate
      ],
      parent: this.jsonTemplate.nativeElement,
    });
    new EditorView({
      doc: JSON.stringify(this.template.exampleContext, null, 2),
      extensions: [
        basicSetup,
        json(),
        this.updateContext
      ],
      parent: this.contextTemplate.nativeElement,
    });
  }

  updateTemplate = EditorView.updateListener.of((v) => {
    this.template.template = v.state.doc.toString();
    this.templateChange.emit(this.template);
  });
  updateContext = EditorView.updateListener.of((v) => {
    try {
      this.template.exampleContext = JSON.parse(v.state.doc.toString());
      this.templateChange.emit(this.template);
    } catch (error) {

    }
  });

  prettifyJsonTemplate(jsonTemplate: string): string {
    let copy = jsonTemplate;
    let tuIndex = copy.indexOf("templateUtils.");
    let replacements = [];
    let i = 0;
    while (tuIndex > 0) {
      let openingBracket = copy.indexOf("(", tuIndex);
      let closingBracket = this.findClosingBracket(copy, openingBracket);
      let replaced = copy.substring(tuIndex, closingBracket + 1);
      copy = copy.substring(0, tuIndex) + "\"R3P1AC^^3|\|T" + (i++) + "\"" + copy.substring(closingBracket + 1);
      replacements.push(replaced);
      tuIndex = copy.indexOf("templateUtils.");
    }
    try {
      let pretty = JSON.stringify(JSON.parse(copy), null, 2);
      for (let i = 0; i < replacements.length; i++) {
        pretty = pretty.replace("\"R3P1AC^^3|\|T" + i + "\"", replacements[i]);
      }
      return pretty;
    } catch (error) {
      return jsonTemplate;
    }
  }

  findClosingBracket(text: string, openPos: number): number {
    let closePos = openPos;
    let counter = 1;
    while (counter > 0) {
      let c = text.charAt(++closePos);
      if (c == '(') {
        counter++;
      } else if (c == ')') {
        counter--;
      }
    }
    return closePos;
  }
}
