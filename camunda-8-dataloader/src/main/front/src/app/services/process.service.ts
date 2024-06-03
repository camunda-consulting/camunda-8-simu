import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProcessService {

  constructor(
    private http: HttpClient,
  ) { }

  definitions(): Observable<any[]> {
    return this.http.get<any[]>(environment.backend +"/api/process/definitions");
  }

  getActiveProcessInstances(): Observable<any[]> {
    return this.http.get<any[]>(environment.backend +"/api/process/instances/ACTIVE");
  }

  migrateInstance(instanceKeys: number[]): Observable<any[]> {
    return this.http.post<any[]>(environment.backend +"/api/process/instances/duplicate", instanceKeys);
  }
}
