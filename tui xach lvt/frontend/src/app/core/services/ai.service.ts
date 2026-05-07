import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AiService {
  private baseUrl = 'ai';

  constructor(private apiService: ApiService) {}

  getSuggestions(keyword: string): Observable<any[]> {
    return this.apiService.get(`${this.baseUrl}/suggest`, { keyword }).pipe(
      map(res => {
        try {
          return typeof res === 'string' ? JSON.parse(res) : res;
        } catch (e) {
          return [];
        }
      })
    );
  }

  chat(message: string): Observable<string> {
    return this.apiService.post(`${this.baseUrl}/chat`, message).pipe(
      map(res => res.data)
    );
  }
}
