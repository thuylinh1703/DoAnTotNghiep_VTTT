import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  get(path: string, params: any = {}): Observable<any> {
    return this.http.get(`${this.apiUrl}/${path}`, { params });
  }

  post(path: string, body: any, params: any = {}): Observable<any> {
    return this.http.post(`${this.apiUrl}/${path}`, body, { params });
  }

  put(path: string, body: any, params: any = {}): Observable<any> {
    return this.http.put(`${this.apiUrl}/${path}`, body, { params });
  }

  delete(path: string, params: any = {}): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${path}`, { params });
  }
}
