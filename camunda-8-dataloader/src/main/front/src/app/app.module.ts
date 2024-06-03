import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';
import { ActiveinstancesComponent } from './activeinstances/activeinstances.component';
import { HomeComponent } from './home/home.component';
import { ExecutionplanComponent } from './executionplan/executionplan.component';
import { OptimizePreviewComponent } from './optimize-preview/optimize-preview.component';
import { ModelerComponent } from './modeler/modeler.component';
import { ViewerComponent } from './viewer/viewer.component';
import { ActionsPlanComponent } from './actions-plan/actions-plan.component';
import { InstantiateComponent } from './instantiate/instantiate.component';
import { StepComponent } from './step/step.component';
declare module '@bpmn-io/element-template-icon-renderer';
@NgModule({
  declarations: [
    AppComponent,
    ActiveinstancesComponent,
    HomeComponent,
    ExecutionplanComponent,
    OptimizePreviewComponent,
    ModelerComponent,
    ViewerComponent,
    ActionsPlanComponent,
    InstantiateComponent,
    StepComponent
  ],
  imports: [
    BrowserModule, FormsModule, HttpClientModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
