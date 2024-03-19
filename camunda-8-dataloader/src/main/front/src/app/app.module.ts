import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';
import { ActiveinstancesComponent } from './activeinstances/activeinstances.component';
import { DefinitionsComponent } from './definitions/definitions.component';
import { ExecutionplanComponent } from './executionplan/executionplan.component';
import { OptimizePreviewComponent } from './optimize-preview/optimize-preview.component';
import { ModelerComponent } from './modeler/modeler.component';
import { ViewerComponent } from './viewer/viewer.component';
import { ActionsPlanComponent } from './actions-plan/actions-plan.component';
declare module '@bpmn-io/element-template-icon-renderer';
@NgModule({
  declarations: [
    AppComponent,
    ActiveinstancesComponent,
    DefinitionsComponent,
    ExecutionplanComponent,
    OptimizePreviewComponent,
    ModelerComponent,
    ViewerComponent,
    ActionsPlanComponent
  ],
  imports: [
    BrowserModule, FormsModule, HttpClientModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
