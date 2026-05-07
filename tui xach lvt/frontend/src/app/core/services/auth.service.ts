import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private apiService: ApiService) { }

  login(credentials: any): Observable<any> {
    return this.apiService.post('auth/login', credentials).pipe(
      tap(response => {
        if (response.success && response.data?.token) {
          localStorage.setItem('token', response.data.token);
          const userData = {
            email: response.data.email,
            fullName: response.data.fullName,
            role: response.data.role,
            phone: response.data.phone,
            address: response.data.address
          };
          localStorage.setItem('user', JSON.stringify(userData));
        }
      })
    );
  }

  register(userData: any): Observable<any> {
    return this.apiService.post('auth/register', userData);
  }

  verifyOtp(otpData: any): Observable<any> {
    return this.apiService.post('auth/verify-otp', otpData).pipe(
      tap(response => {
        if (response.success && response.data?.token) {
          localStorage.setItem('token', response.data.token);
          const userData = {
            email: response.data.email,
            fullName: response.data.fullName,
            role: response.data.role,
            phone: response.data.phone,
            address: response.data.address
          };
          localStorage.setItem('user', JSON.stringify(userData));
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getUser(): any {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  }
}
