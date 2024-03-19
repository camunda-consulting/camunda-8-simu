import { Injectable } from '@angular/core';
import { ProcessService } from './process.service';

@Injectable({
  providedIn: 'root'
})
export class ExecPlanService {

  constructor(processService: ProcessService) { }
  activities: any[] = [];
  actions: any = {};
  currentActivity: string | undefined;

  addActivity(activity: any) {
    this.activities.push(activity);
    this.actions[activity.id] = {};
    if (!this.currentActivity) {
      this.currentActivity = activity.id;
    }
  }
  selectActivity(activity: string): void {
    this.currentActivity = activity;
  }


  clear(): void {
    this.activities = [];
    this.actions = {};
    this.currentActivity = undefined;
  }
}
