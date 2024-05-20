import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ProcessService {

  constructor(
    private http: HttpClient,
  ) { }

  definitions(): Observable<any[]> {
    return this.http.get<any[]>("http://localhost:8080/api/process/definitions");
  }

  getActiveProcessInstances(): Observable<any[]> {
    return this.http.get<any[]>("http://localhost:8080/api/process/instances/ACTIVE");
  }

  migrateInstance(instanceKeys: number[]): Observable<any[]> {
    return this.http.post<any[]>("http://localhost:8080/api/process/instances/duplicate", instanceKeys);
  }
}
