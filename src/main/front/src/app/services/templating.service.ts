import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TemplatingService {

  dataset?: any
  jsondataset?: any;

  constructor(
    private http: HttpClient,
  ) { }

  checkTemplate(template: any): Observable<any> {
    return this.http.post<any>(environment.backend + "/api/templating/test", template);
  }

  listTemplatingMethodsNames(): Observable<string[]> {
    return this.http.get<string[]>(environment.backend + "/api/templating");
  }

  listTemplatingMethods(name:string): Observable<any[]> {
    return this.http.get<any[]>(environment.backend + "/api/templating/method/"+name);
  }

  listDatasets(): Observable<string[]> {
    return this.http.get<string[]>(environment.backend + "/api/templating/datasets");
  }

  listJsonDatasets(): Observable<string[]> {
    return this.http.get<string[]>(environment.backend + "/api/templating/jsondatasets");
  }
  getDataset(name: string): void {
    this.http.get<any>(environment.backend + "/api/templating/dataset/"+name).subscribe((response: any) => {
      this.dataset = response;
    });
  }
  getJsonDataset(name: string): void {
    this.http.get<any>(environment.backend + "/api/templating/jsondataset/" + name).subscribe((response: any) => {
      this.jsondataset = response;
    });
  }
  deleteDataset(name: string): Observable<any> {
    return this.http.delete<any>(environment.backend + "/api/templating/dataset/"+name);
  }
  deleteJsonDataset(name: string): Observable<any> {
    return this.http.delete<any>(environment.backend + "/api/templating/jsondataset/name"+name);
  }
  saveDataset(dataSet:any): Observable<any> {
    return this.http.post<any>(environment.backend + "/api/templating/datasets", dataSet);
  }
  saveJsonDataset(): Observable<any> {
    return this.http.post<any>(environment.backend + "/api/templating/jsondatasets", this.jsondataset);
  }
  newDataset() {
    this.dataset = {
      "name": "dataset", categorizedData: { "en": [] }
    };
  }
  newJsonDataset() {
    this.jsondataset = {
      "name": "dataset",
      "data": []
    };
  }
  clear() {
    this.dataset = undefined;
    this.jsondataset = undefined;
  }
}
