import {
  Component, OnInit, AfterViewInit, ViewChild, ElementRef, ChangeDetectorRef, inject, NgZone, OnDestroy
} from '@angular/core';
import { fromEvent, map, Observable, Subject, switchMap, takeUntil, tap } from 'rxjs';

@Component({
  selector: 'app-draggable',
  templateUrl: './draggable.component.html',
  styleUrls: ['./draggable.component.css']
})
export class DraggableComponent implements AfterViewInit, OnDestroy {

  @ViewChild("scenarionav") scenarioNav?: ElementRef;

  constructor() { }

  private readonly destroy$$ = new Subject<void>();
  private readonly ngZone = inject(NgZone);
  private readonly cdRef = inject(ChangeDetectorRef);
  public dragging = false;
  ngAfterViewInit(): void {
    console.log(this.scenarioNav);
    let nativeElement = this.scenarioNav!.nativeElement;

    this.cdRef.detach(); // we take care of our own Change Detection.

    // We still want to avoid this component to trigger application wide 
    // Change Detection so we still want to run it outside of ngZone.
    this.ngZone.runOutsideAngular(() => {
      const mouseDown$ = fromEvent<MouseEvent>(nativeElement, 'mousedown').pipe(
        takeUntil(this.destroy$$)
      );
      const mouseMove$ = fromEvent<MouseEvent>(document, 'mousemove').pipe(
        takeUntil(this.destroy$$)
      );
      const mouseUp$ = fromEvent<MouseEvent>(document, 'mouseup').pipe(
        takeUntil(this.destroy$$)
      );
      const dragMove$ = mouseDown$.pipe(
        switchMap((startEvent: MouseEvent) =>
          mouseMove$.pipe(
            map((moveEvent: MouseEvent) => {
              // return both events
              return {
                startEvent,
                moveEvent,
              };
            }),
            takeUntil(mouseUp$)
          )
        ),
        tap(({ startEvent, moveEvent }) => {
          //const x = moveEvent.x - startEvent.offsetX;
          const y = moveEvent.y - startEvent.offsetY - 40;
          // update position
          //nativeElement.parent.style.left = x + 'px';
          nativeElement.offsetParent.style.top = y + 'px';
          //app-actions-plan
          nativeElement.offsetParent.style.height = 'calc(100vh - ' + (y + 40) + 'px)';
          //viewer
          nativeElement.offsetParent.offsetParent.firstChild.firstChild.firstChild.firstChild.style.height = y + 'px';
        }),
        takeUntil(this.destroy$$)
      );
      dragMove$.subscribe();
      mouseUp$.subscribe(() => {
        this.dragging = false;
        this.cdRef.detectChanges();
      });
      mouseDown$.subscribe(() => {
        this.dragging = true;
        this.cdRef.detectChanges();
      });
    });
  }
  public ngOnDestroy(): void {
    this.destroy$$.next();
  }

}
