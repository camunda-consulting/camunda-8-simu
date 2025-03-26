import { Component, OnInit } from '@angular/core';
import { TemplatingService } from '../services/templating.service';

@Component({
    selector: 'app-dataset',
    templateUrl: './dataset.component.html',
    styleUrls: ['./dataset.component.css'],
    standalone: false
})
export class DatasetComponent implements OnInit {
  category: string = 'en';
  newCategory: string = 'Cat1';
  constructor(public templatingService: TemplatingService) { }

  ngOnInit(): void {
  }

  back(): void {
    this.templatingService.clear();
  }
  addCategory(): void {
    this.templatingService.dataset.categorizedData[this.newCategory] = [];
    this.newCategory = 'Cat' + Object.keys(this.templatingService.dataset.categorizedData).length;
  }
  save(): void {
    this.templatingService.saveDataset(this.templatingService.dataset).subscribe();
  }
  selectCategory(cat: any): void {
    this.category = cat;
  }
  deleteCategory(cat: any): void {
    delete this.templatingService.dataset.categorizedData[cat];
  }
  addValue(): void {
    this.templatingService.dataset.categorizedData[this.category].push("");
  }
  updateVal(i: number, event: any): void {
    this.templatingService.dataset.categorizedData[this.category][i] = event.target.value;
  }
}
