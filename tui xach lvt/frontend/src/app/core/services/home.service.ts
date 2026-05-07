import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class HomeService {
  constructor(private apiService: ApiService) { }

  getHomeData(): Observable<any> {
    return this.apiService.get('home');
  }
}
