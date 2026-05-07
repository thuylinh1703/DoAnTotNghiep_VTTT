import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  constructor(private apiService: ApiService) {}

  getRevenueSeries(params: {
    groupBy?: 'day' | 'week' | 'month';
    from?: string;
    to?: string;
  }): Observable<any> {
    return this.apiService.get('admin/reports/revenue', params);
  }

  getRevenueSummary(): Observable<any> {
    return this.apiService.get('admin/reports/revenue/summary');
  }

  getRevenueByPaymentMethod(from?: string, to?: string): Observable<any> {
    return this.apiService.get('admin/reports/revenue/by-payment-method', { from, to });
  }

  getRevenueByCategory(from?: string, to?: string): Observable<any> {
    return this.apiService.get('admin/reports/revenue/by-category', { from, to });
  }

  getTopSellers(period: string = 'month', limit: number = 20): Observable<any> {
    return this.apiService.get('admin/reports/products/top-sellers', { period, limit });
  }

  getSlowMovers(limit: number = 20, threshold: number = 3): Observable<any> {
    return this.apiService.get('admin/reports/products/slow-movers', { limit, threshold });
  }

  getZeroSales(days: number = 60, limit: number = 20): Observable<any> {
    return this.apiService.get('admin/reports/products/zero-sales', { days, limit });
  }
}
