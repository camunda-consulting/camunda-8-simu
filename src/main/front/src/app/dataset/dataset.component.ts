import { Component, OnInit } from '@angular/core';
import { TemplatingService } from '../services/templating.service';

@Component({
  selector: 'app-dataset',
  templateUrl: './dataset.component.html',
  styleUrls: ['./dataset.component.css']
})
export class DatasetComponent implements OnInit {
  language: string = 'en';
  newLanguage: string = 'LN1';
  constructor(public templatingService: TemplatingService) { }

  ngOnInit(): void {
  }

  back(): void {
    this.templatingService.clear();
  }
  addLanguage(): void {
    this.templatingService.dataset.localizedData[this.newLanguage] = [];
    this.newLanguage = 'LN' + Object.keys(this.templatingService.dataset.localizedData).length;
  }
  save(): void {

  }
  selectLanguage(ln: any): void {
    this.language = ln;
  }
  deleteLanguage(ln: any): void {
    delete this.templatingService.dataset.localizedData[ln];
  }
}
