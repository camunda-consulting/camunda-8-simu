import { Component, OnInit } from '@angular/core';
import { TemplatingService } from '../services/templating.service';

@Component({
    selector: 'app-jsondataset',
    templateUrl: './jsondataset.component.html',
    styleUrls: ['./jsondataset.component.css'],
    standalone: false
})
export class JsondatasetComponent implements OnInit {

  constructor(public templatingService: TemplatingService) { }

  ngOnInit(): void {
  }

  back(): void {
    this.templatingService.clear();
  }
 
  save(): void {
    this.templatingService.saveJsonDataset().subscribe();
  }
 
  addValue(): void {
    this.templatingService.jsondataset.data.push("{}");
  }
  updateVal(i: number, event: any): void {
    this.templatingService.jsondataset.data[i] = event.target.value;
  }

}
