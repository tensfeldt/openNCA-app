import { Component, OnInit, OnDestroy } from '@angular/core';
import {ProcessorState} from './processor.service';
import { Observable, Subject, timer } from 'rxjs';
import {ProcessorService} from './processor.service';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-processor-console',
  templateUrl: './processor-console.component.html',
  styleUrls: ['./processor-console.component.css']
})
export class ProcessorConsoleComponent implements OnInit, OnDestroy {
  private _processorStates: ProcessorState[];
  private subject$ = new Subject();
  private _interval: number;

  get processorStates():  ProcessorState[] {
    return this._processorStates;
  }

  set processorStates(processors:  ProcessorState[]) {
    this._processorStates = processors;
  }

  constructor(private processorService: ProcessorService) {
    this._interval = 5000;
   }

  ngOnInit() {
    timer(1000, this._interval).pipe(takeUntil(this.subject$)).subscribe(() => {
      this.refresh();
    });
  }

  ngOnDestroy() {
    this.subject$.next();
    this.subject$.complete();
  }

  trackByFn(index, item: ProcessorState) {
    return item.name;

  }

  refresh() {
    this.processorService.getProcessors().subscribe(res => {
      this.processorStates = res;
    });

  }

  start(name: string) {
    this.processorService.changeState(name, 'start').subscribe(res => {
      this.refresh();
    }, err => {
      console.error(err);
    });
  }

  stop(name: string) {
    this.processorService.changeState(name, 'stop').subscribe(res => {
      this.refresh();
    }, err => {
      console.error(err);
    });
  }

  runNow(name: string) {
    this.processorService.changeState(name, 'run').subscribe(res => {
      this.refresh();
    }, err => {
      console.error(err);
    });
  }


}


