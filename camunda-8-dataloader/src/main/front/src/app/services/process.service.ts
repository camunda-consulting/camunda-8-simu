import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ProcessService {

  executionPlan: any | undefined;

  constructor(
    private http: HttpClient,
  ) { }

  openExecutionPlan(definition: any): void {
    this.http.get<any>("http://localhost:8080/api/plan/" + definition.bpmnProcessId+"/"+definition.version).subscribe((response: any) => {
      this.executionPlan = response;
    });
  }
  updateDef(xml: string): void {
    this.http.post<any>("http://localhost:8080/api/plan/" + this.executionPlan.definition.bpmnProcessId + "/" + this.executionPlan.definition.version+'/xml', xml).subscribe((response: any) => {
      this.executionPlan = response;
    });
  }
  updatePlan(): void {
    this.http.put<any>("http://localhost:8080/api/plan/" + this.executionPlan.definition.bpmnProcessId + "/" + this.executionPlan.definition.version, this.executionPlan).subscribe((response: any) => {
      this.executionPlan = response;
    });
  }
  clear(): void {
    this.executionPlan = undefined;
  }

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
