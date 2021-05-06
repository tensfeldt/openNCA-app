import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import { Observable } from 'rxjs';



@Injectable({
  providedIn: 'root'
}
)
export class ProcessorService {

  constructor(private httpClient: HttpClient) { }

  public getProcessors(): Observable<ProcessorState[]> {
    const timestamp = new Date().getTime();
    return this.httpClient.get<ProcessorState[]>(`../api/processors?${timestamp}`);
  }

  public changeState(name: string, state: string): Observable<any> {
     return this.httpClient.put(`../api/processors/${name}/${state}`, null);
  }
}

export interface ProcessorState {
  name: string;
  description: string;
  status: string;
  statusDescription: string;
}
