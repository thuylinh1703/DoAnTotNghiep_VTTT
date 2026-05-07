import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable, BehaviorSubject, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private cartSubject = new BehaviorSubject<any>(null);
  cart$ = this.cartSubject.asObservable();

  constructor(private apiService: ApiService) {
    this.loadCart();
  }

  getCartValue(): any {
    return this.cartSubject.value;
  }

  loadCart(): void {
    if (localStorage.getItem('token')) {
      this.apiService.get('cart').subscribe({
        next: (response) => {
          if (response.success) {
            this.cartSubject.next(response.data);
          }
        },
        error: () => this.cartSubject.next(null)
      });
    }
  }

  addToCart(productId: number, quantity: number): Observable<any> {
    return this.apiService.post('cart', { productId, quantity }).pipe(
      tap(response => {
        if (response.success) {
          this.cartSubject.next(response.data);
        }
      })
    );
  }

  updateQuantity(cartItemId: number, quantity: number): Observable<any> {
    return this.apiService.put(`cart/${cartItemId}?quantity=${quantity}`, {}).pipe(
      tap(response => {
        if (response.success) {
          this.cartSubject.next(response.data);
        }
      })
    );
  }

  removeFromCart(cartItemId: number): Observable<any> {
    return this.apiService.delete(`cart/${cartItemId}`).pipe(
      tap(response => {
        if (response.success) {
          this.cartSubject.next(response.data);
        }
      })
    );
  }

  clearCart(): Observable<any> {
    return this.apiService.delete('cart').pipe(
      tap(response => {
        if (response.success) {
          this.cartSubject.next(null);
        }
      })
    );
  }

  validateStock(): Observable<any> {
    return this.apiService.post('cart/validate-stock', {});
  }
}
