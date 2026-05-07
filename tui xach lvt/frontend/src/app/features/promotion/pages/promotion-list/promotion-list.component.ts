import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CouponService } from '../../../../core/services/coupon.service';

@Component({
  selector: 'app-promotion-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './promotion-list.component.html',
  styleUrls: ['./promotion-list.component.css']
})
export class PromotionListComponent implements OnInit {
  coupons: any[] = [];
  loading = true;
  copiedCode: string | null = null;

  constructor(
    private couponService: CouponService
  ) {}

  ngOnInit(): void {
    this.fetchCoupons();
  }

  fetchCoupons(): void {
    this.loading = true;
    this.couponService.getAvailableCoupons().subscribe({
      next: (response) => {
        if (response.success) {
          this.coupons = response.data;
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Error fetching coupons:', error);
        this.loading = false;
      }
    });
  }

  copyCode(code: string): void {
    if (navigator.clipboard) {
      navigator.clipboard.writeText(code).then(() => {
        this.copiedCode = code;
        setTimeout(() => {
          this.copiedCode = null;
        }, 2000);
      }).catch(err => {
        console.error('Failed to copy: ', err);
      });
    } else {
      // Fallback for older browsers
      const textArea = document.createElement("textarea");
      textArea.value = code;
      document.body.appendChild(textArea);
      textArea.select();
      try {
        document.execCommand('copy');
        this.copiedCode = code;
        setTimeout(() => {
          this.copiedCode = null;
        }, 2000);
      } catch (err) {
        console.error('Fallback: Oops, unable to copy', err);
      }
      document.body.removeChild(textArea);
    }
  }

  getDiscountText(coupon: any): string {
    if (coupon.type === 'PERCENTAGE') {
      return `Giảm ${coupon.value}%`;
    } else {
      return `Giảm ${new Intl.NumberFormat('vi-VN').format(coupon.value)}₫`;
    }
  }

  getMinOrderText(amount: number): string {
    if (!amount || amount === 0) return 'Mọi đơn hàng';
    return `Đơn tối thiểu ${new Intl.NumberFormat('vi-VN').format(amount)}₫`;
  }
}
