import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProcessorConsoleComponent } from './processor-console.component';

describe('ProcessorConsoleComponent', () => {
  let component: ProcessorConsoleComponent;
  let fixture: ComponentFixture<ProcessorConsoleComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ProcessorConsoleComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProcessorConsoleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
