import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CouponService } from '../../../../core/services/coupon.service';
import { CartService } from '../../../../core/services/cart.service';
import { NotificationService } from '../../../../shared/services/notification.service';

@Component({
  selector: 'app-coupon-input',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="mt-6 border-t border-gray-100 pt-6">
      <label class="block text-sm font-medium text-gray-700 mb-2">Mã giảm giá</label>
      
      <div *ngIf="appliedCoupon" class="flex items-center justify-between p-3 bg-indigo-50 border border-indigo-100 rounded-lg mb-3 animate-fade-in">
        <div class="flex items-center space-x-2">
          <svg class="w-5 h-5 text-indigo-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"></path>
          </svg>
          <span class="text-sm font-semibold text-indigo-700 uppercase">{{ appliedCoupon.code }}</span>
          <span class="text-xs text-indigo-500">(-{{ appliedCoupon.discountAmount | number:'1.0-0' }}₫)</span>
        </div>
        <button (click)="remove()" class="text-xs text-indigo-600 hover:text-indigo-800 font-medium">Gỡ bỏ</button>
      </div>

      <div *ngIf="!appliedCoupon" class="flex space-x-2">
        <input 
          [(ngModel)]="couponCode" 
          type="text" 
          placeholder="Nhập mã (ví dụ: TET2024)" 
          class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none uppercase transition-all"
          [disabled]="loading"
          (keyup.enter)="apply()"
        />
        <button 
          (click)="apply()" 
          class="px-5 py-2 bg-gray-900 text-white rounded-lg font-medium hover:bg-gray-800 active:scale-95 transition-all disabled:opacity-50"
          [disabled]="loading || !couponCode.trim()"
        >
          <span *ngIf="!loading">Áp dụng</span>
          <span *ngIf="loading" class="flex items-center">
            <svg class="animate-spin h-4 w-4 mr-2" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            ...
          </span>
        </button>
      </div>
      <p *ngIf="error" class="mt-2 text-xs text-red-500 font-medium">{{ error }}</p>
    </div>
  `,
  styles: [`
    @keyframes fade-in {
      from { opacity: 0; transform: translateY(-5px); }
      to { opacity: 1; transform: translateY(0); }
    }
    .animate-fade-in { animation: fade-in 0.3s ease-out; }
  `]
})
export class CouponInputComponent implements OnInit {
  couponCode = '';
  appliedCoupon: any = null;
  loading = false;
  error = '';

  constructor(
    private couponService: CouponService,
    private cartService: CartService,
    private notify: NotificationService
  ) {}

  ngOnInit(): void {
    this.couponService.appliedCoupon$.subscribe(coupon => {
      this.appliedCoupon = coupon;
    });
  }

  apply() {
    if (!this.couponCode.trim()) return;
    
    this.loading = true;
    this.error = '';

    const cart = this.cartService.getCartValue();
    const cartItems = cart?.items || [];

    this.couponService.validate(this.couponCode.trim().toUpperCase(), cartItems).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          const couponData = res.data;
          this.couponService.applyCoupon({
            code: couponData.code,
            type: couponData.type,
            value: couponData.value,
            discountAmount: couponData.discountAmount,
            description: couponData.description
          });
          this.couponCode = '';
          this.notify.success(`Đã áp dụng mã giảm giá ${couponData.discountAmount}₫`);
        } else {
          this.error = this.mapErrorCode(res.data?.errorCode || 'UNKNOWN');
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Mã giảm giá không hợp lệ';
        this.loading = false;
      }
    });
  }

  remove() {
    this.couponService.removeCoupon();
    this.notify.info('Đã gỡ bỏ mã giảm giá');
  }

  private mapErrorCode(code: string): string {
    const messages: any = {
      'COUPON_NOT_FOUND': 'Mã giảm giá không tồn tại',
      'COUPON_INACTIVE': 'Mã giảm giá hiện không hoạt động',
      'COUPON_EXPIRED': 'Mã giảm giá đã hết hạn',
      'COUPON_NOT_STARTED': 'Chương trình giảm giá chưa bắt đầu',
      'COUPON_USAGE_LIMIT_REACHED': 'Mã giảm giá đã hết lượt sử dụng',
      'COUPON_USER_LIMIT_REACHED': 'Bạn đã sử dụng mã này rồi',
      'COUPON_MIN_ORDER_NOT_MET': 'Chưa đạt giá trị đơn hàng tối thiểu',
      'COUPON_NOT_APPLICABLE': 'Mã không áp dụng cho các sản phẩm trong giỏ'
    };
    return messages[code] || 'Mã giảm giá không hợp lệ';
  }
}
