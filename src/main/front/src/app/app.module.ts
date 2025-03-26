import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';
import { HomeComponent } from './home/home.component';
import { ExecutionplanComponent } from './executionplan/executionplan.component';
import { OptimizePreviewComponent } from './optimize-preview/optimize-preview.component';
import { ModelerComponent } from './modeler/modeler.component';
import { ViewerComponent } from './viewer/viewer.component';
import { ActionsPlanComponent } from './actions-plan/actions-plan.component';
import { InstantiateComponent } from './instantiate/instantiate.component';
import { StepComponent } from './step/step.component';
import { DraggableComponent } from './draggable/draggable.component';
import { JsonEditorComponent } from './json-editor/json-editor.component';
import { DatasetComponent } from './dataset/dataset.component';
import { TestPlanComponent } from './test-plan/test-plan.component';
import { JsondatasetComponent } from './jsondataset/jsondataset.component';
declare module '@bpmn-io/element-template-icon-renderer';
@NgModule({ declarations: [
        AppComponent,
        HomeComponent,
        ExecutionplanComponent,
        OptimizePreviewComponent,
        ModelerComponent,
        ViewerComponent,
        ActionsPlanComponent,
        InstantiateComponent,
        StepComponent,
        DraggableComponent,
        JsonEditorComponent,
        DatasetComponent,
        TestPlanComponent,
        JsondatasetComponent
    ],
    bootstrap: [AppComponent], imports: [BrowserModule, FormsModule], providers: [provideHttpClient(withInterceptorsFromDi())] })
export class AppModule { }
