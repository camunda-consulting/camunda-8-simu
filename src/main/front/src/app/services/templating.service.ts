import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TemplatingService {

  dataset?: any;

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
  getDataset(name: string): void {
    this.http.get<any>(environment.backend + "/api/templating/dataset/"+name).subscribe((response: any) => {
      this.dataset = response;
    });
  }
  deleteDataset(name: string): Observable<any> {
    return this.http.delete<any>(environment.backend + "/api/templating/dataset/name");
  }
  saveDataset(dataSet:any): Observable<any> {
    return this.http.post<any>(environment.backend + "/api/templating/datasets", dataSet);
  }
  newDataset() {
    this.dataset = {
      "name": "dataset", categorizedData: { "en": [] }
    };
  }
  clear() {
    this.dataset = undefined;
  }
}
