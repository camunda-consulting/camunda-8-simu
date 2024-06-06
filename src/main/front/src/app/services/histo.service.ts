import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class HistoService {

  constructor(
    private http: HttpClient,
  ) { }

  executions(): Observable<any> {
    return this.http.get<any>(environment.backend+"/api/histo");
  }

  progresses(): Observable<any[]> {
    return this.http.get<any[]>(environment.backend + "/api/histo/progresses");
  }

  histo(filter: any): Observable<any> {
    console.log(filter);
    let luceneQuery = 'planCode:' + filter.planCode +
      ' AND engineDate:[' + this.getTime(filter.engineDate.from) + ' TO ' + this.getTime(filter.engineDate.to) + ']' +
      ' AND realDate:[' + this.getTime(filter.realDate.from) + ' TO ' + this.getTime(filter.realDate.to) + ']';

    if (filter.log && filter.log != '') {
      luceneQuery = luceneQuery + ' AND log:' + filter.log;
    }
    let params = "?size=" + filter.size;
    if (filter.after) {
      params += "&after=" + filter.after;
    }
    return this.http.post<any>(environment.backend + "/api/histo/search"+params, luceneQuery);
  }

  getTime(date: string): string {
    let result = "" + new Date(date).getTime();
    if (result.length < 13) {
      result = "0" + result;
    }
    return result;
  }
}
