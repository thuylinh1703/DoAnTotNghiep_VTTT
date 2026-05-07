import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  constructor(private apiService: ApiService) {}

  getProfile(): Observable<any> {
    return this.apiService.get('user/profile');
  }

  updateProfile(data: any): Observable<any> {
    return this.apiService.put('user/profile', data);
  }
}
