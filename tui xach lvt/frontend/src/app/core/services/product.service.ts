import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  constructor(private apiService: ApiService) { }

  getProducts(params: any = {}): Observable<any> {
    let query = '';
    const keys = Object.keys(params);
    if (keys.length > 0) {
      query = '?' + keys.map(k => `${k}=${params[k]}`).join('&');
    }
    return this.apiService.get('products' + query);
  }

  getProductById(id: number): Observable<any> {
    return this.apiService.get(`products/${id}`);
  }

  getFeaturedProducts(): Observable<any> {
    return this.apiService.get('products/featured');
  }

  getNewProducts(period: 'day' | 'week' | 'month' | null = null, limit = 8): Observable<any> {
    const parts: string[] = [`limit=${limit}`];
    if (period) parts.push(`period=${period}`);
    return this.apiService.get(`products/new?${parts.join('&')}`);
  }

  getBrands(): Observable<any> {
    return this.apiService.get('products/brands');
  }

  getCategories(): Observable<any> {
    return this.apiService.get('categories');
  }

  aiSuggest(keyword: string): Observable<any> {
    return this.apiService.get(`ai/suggest?keyword=${keyword}`);
  }

  getRelatedProducts(productId: number, limit = 8): Observable<any> {
    return this.apiService.get(`products/${productId}/related?limit=${limit}`);
  }

  getHomePage(): Observable<any> {
    return this.apiService.get('home');
  }
}
