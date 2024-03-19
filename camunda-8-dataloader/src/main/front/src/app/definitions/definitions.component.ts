import { Component, OnInit } from '@angular/core';
import { ProcessService } from '../services/process.service';

@Component({
  selector: 'app-definitions',
  templateUrl: './definitions.component.html',
  styleUrls: ['./definitions.component.css']
})
export class DefinitionsComponent implements OnInit {

  constructor(private processService: ProcessService) { }

  definitions: any[] = [];

  ngOnInit(): void {
    this.processService.definitions().subscribe((response: any[]) => {
      this.definitions = response;
    });
  }

  openPlan(definition: any): void {
    this.processService.openExecutionPlan(definition);
  }
}
