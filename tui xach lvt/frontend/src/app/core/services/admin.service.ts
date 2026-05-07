import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private baseUrl = 'admin';

  constructor(private apiService: ApiService) {}

  // Dashboard
  getDashboard(year?: number): Observable<any> {
    const params: any = {};
    if (year) params.year = year;
    return this.apiService.get(`${this.baseUrl}/dashboard`, params);
  }

  // Products
  getProducts(page: number = 0, size: number = 10): Observable<any> {
    return this.apiService.get(`${this.baseUrl}/products`, { page, size });
  }

  createProduct(formData: FormData): Observable<any> {
    return this.apiService.post(`${this.baseUrl}/products`, formData);
  }

  updateProduct(id: number, formData: FormData): Observable<any> {
    return this.apiService.put(`${this.baseUrl}/products/${id}`, formData);
  }

  deleteProduct(id: number): Observable<any> {
    return this.apiService.delete(`${this.baseUrl}/products/${id}`);
  }

  // Categories
  getCategories(): Observable<any> {
    return this.apiService.get(`${this.baseUrl}/categories`);
  }

  createCategory(category: any): Observable<any> {
    return this.apiService.post(`${this.baseUrl}/categories`, category);
  }

  updateCategory(id: number, category: any): Observable<any> {
    return this.apiService.put(`${this.baseUrl}/categories/${id}`, category);
  }

  deleteCategory(id: number): Observable<any> {
    return this.apiService.delete(`${this.baseUrl}/categories/${id}`);
  }

  // Orders
  getOrders(page: number = 0, size: number = 10, status?: string, search?: string): Observable<any> {
    const params: any = { page, size };
    if (status) params.status = status;
    if (search) params.search = search;
    return this.apiService.get(`${this.baseUrl}/orders`, params);
  }

  getOrderById(id: number): Observable<any> {
    return this.apiService.get(`${this.baseUrl}/orders/${id}`);
  }

  updateOrderStatus(id: number, status: string): Observable<any> {
    return this.apiService.put(`${this.baseUrl}/orders/${id}/status`, {}, { status });
  }

  updateOrderInfo(id: number, info: any): Observable<any> {
    return this.apiService.put(`${this.baseUrl}/orders/${id}/info`, info);
  }

  // Users
  getUsers(page: number = 0, size: number = 10): Observable<any> {
    return this.apiService.get(`${this.baseUrl}/users`, { page, size });
  }

  toggleUserStatus(id: number): Observable<any> {
    return this.apiService.put(`${this.baseUrl}/users/${id}/toggle-status`, {});
  }

  deleteUser(id: number): Observable<any> {
    return this.apiService.delete(`${this.baseUrl}/users/${id}`);
  }

  updateUser(id: number, userData: any): Observable<any> {
    return this.apiService.put(`${this.baseUrl}/users/${id}`, userData);
  }

  getSupportCustomers(): Observable<any> {
    return this.apiService.get(`${this.baseUrl}/support-customers`);
  }

  // Reviews
  getReviews(page: number = 0, size: number = 10): Observable<any> {
    return this.apiService.get(`${this.baseUrl}/reviews`, { page, size });
  }

  deleteReview(id: number): Observable<any> {
    return this.apiService.delete(`${this.baseUrl}/reviews/${id}`);
  }

  toggleReviewStatus(id: number): Observable<any> {
    // Some backend architectures use POST for toggle actions
    return this.apiService.post(`${this.baseUrl}/reviews/${id}/toggle-status`, {});
  }

  // Banners
  getBanners(): Observable<any> {
    return this.apiService.get(`${this.baseUrl}/banners`);
  }

  createBanner(formData: FormData): Observable<any> {
    return this.apiService.post(`${this.baseUrl}/banners`, formData);
  }

  updateBanner(id: number, formData: FormData): Observable<any> {
    return this.apiService.put(`${this.baseUrl}/banners/${id}`, formData);
  }

  deleteBanner(id: number): Observable<any> {
    return this.apiService.delete(`${this.baseUrl}/banners/${id}`);
  }

  // Upload
  uploadFile(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.apiService.post('uploads', formData);
  }

  // Coupons
  getCoupons(page: number = 0, size: number = 10): Observable<any> {
    return this.apiService.get(`${this.baseUrl}/coupons`, { page, size });
  }

  getCouponById(id: number): Observable<any> {
    return this.apiService.get(`${this.baseUrl}/coupons/${id}`);
  }

  createCoupon(coupon: any): Observable<any> {
    return this.apiService.post(`${this.baseUrl}/coupons`, coupon);
  }

  updateCoupon(id: number, coupon: any): Observable<any> {
    return this.apiService.put(`${this.baseUrl}/coupons/${id}`, coupon);
  }

  deleteCoupon(id: number): Observable<any> {
    return this.apiService.delete(`${this.baseUrl}/coupons/${id}`);
  }

  // Blog
  getBlogs(page: number = 0, size: number = 10, status?: string): Observable<any> {
    const params: any = { page, size };
    if (status) {
      params.status = status;
    }
    return this.apiService.get(`${this.baseUrl}/blog`, params);
  }

  getBlogById(id: number): Observable<any> {
    return this.apiService.get(`${this.baseUrl}/blog/${id}`);
  }

  createBlog(payload: any): Observable<any> {
    return this.apiService.post(`${this.baseUrl}/blog`, payload);
  }

  updateBlog(id: number, payload: any): Observable<any> {
    return this.apiService.put(`${this.baseUrl}/blog/${id}`, payload);
  }

  deleteBlog(id: number): Observable<any> {
    return this.apiService.delete(`${this.baseUrl}/blog/${id}`);
  }

  publishBlog(id: number): Observable<any> {
    return this.apiService.post(`${this.baseUrl}/blog/${id}/publish`, {});
  }

  archiveBlog(id: number): Observable<any> {
    return this.apiService.post(`${this.baseUrl}/blog/${id}/archive`, {});
  }
}
