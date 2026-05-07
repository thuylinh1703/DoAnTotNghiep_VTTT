import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  constructor(private apiService: ApiService) { }

  createOrder(orderData: any): Observable<any> {
    return this.apiService.post('orders', orderData);
  }

  getUserOrders(page: number = 0, size: number = 10): Observable<any> {
    return this.apiService.get(`orders?page=${page}&size=${size}`);
  }

  getOrderByCode(orderCode: string): Observable<any> {
    return this.apiService.get(`orders/${orderCode}`);
  }

  confirmReceived(orderCode: string): Observable<any> {
    return this.apiService.put(`orders/${orderCode}/confirm-received`, {});
  }

  // VNPay
  createVNPayPayment(orderCode: string, returnUrl?: string): Observable<any> {
    let url = `vnpay/create-payment/${orderCode}`;
    if (returnUrl) url += `?returnUrl=${encodeURIComponent(returnUrl)}`;
    return this.apiService.get(url);
  }

  verifyVNPayPayment(params: any): Observable<any> {
    return this.apiService.get('vnpay/vnpay-payment-return', params);
  }
}
