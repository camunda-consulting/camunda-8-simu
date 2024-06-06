import { Component, OnInit } from '@angular/core';
import { ProcessService } from '../services/process.service';

@Component({
  selector: 'app-activeinstances',
  templateUrl: './activeinstances.component.html',
  styleUrls: ['./activeinstances.component.css']
})
export class ActiveinstancesComponent implements OnInit {


  instances: any[] = [];
  public JSON: JSON;
  constructor(private processService: ProcessService) {
    this.JSON = JSON;
  }

  ngOnInit(): void {
    this.processService.getActiveProcessInstances().subscribe((response: any[]) => {
      this.instances = response;
    });
  }

  migrate(): void {
    let instanceKeys = [];
    for (let i = 0; i < this.instances.length; i++) {
      if (this.instances[i].migrate) {
        instanceKeys.push(this.instances[i].key);
      }
    }

    this.processService.migrateInstance(instanceKeys).subscribe((response: any[]) => {
      for (let i = 0; i < response.length; i++) {
        for (let j = 0; j < this.instances.length; j++) {
          if (response[i].originalProcessInstanceKey == this.instances[j].key) {
            this.instances[j].result = response[i];
            break;
          }
        }
      }
    });
  }
}
