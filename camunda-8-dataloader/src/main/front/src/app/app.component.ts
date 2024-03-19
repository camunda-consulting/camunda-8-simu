import { Component } from '@angular/core';
import { ProcessService } from './services/process.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'data loader';
  constructor(public processService: ProcessService) { }
}
