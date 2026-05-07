import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable, BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CouponService {
  private appliedCouponSubject = new BehaviorSubject<any>(null);
  appliedCoupon$ = this.appliedCouponSubject.asObservable();

  constructor(private apiService: ApiService) { }

  validate(code: string, cartItems: any[]): Observable<any> {
    return this.apiService.post('coupons/validate', { code, cartItems });
  }

  getAvailableCoupons(): Observable<any> {
    return this.apiService.get('home/coupons');
  }

  applyCoupon(coupon: any) {
    this.appliedCouponSubject.next(coupon);
  }

  removeCoupon() {
    this.appliedCouponSubject.next(null);
  }

  getCurrentCoupon() {
    return this.appliedCouponSubject.value;
  }
}
