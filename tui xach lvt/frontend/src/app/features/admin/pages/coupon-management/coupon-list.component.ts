import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AdminService } from '../../../../core/services/admin.service';
import { NotificationService } from '../../../../shared/services/notification.service';

@Component({
  selector: 'app-coupon-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="p-6">
      <div class="flex justify-between items-center mb-6">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Quản lý Mã giảm giá</h1>
          <p class="text-gray-500">Tạo và quản lý các chương trình khuyến mãi.</p>
        </div>
        <a routerLink="new" class="px-4 py-2 bg-gray-900 text-white rounded-lg hover:bg-gray-800 transition-colors">
          Thêm mã mới
        </a>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <table class="w-full text-left">
          <thead>
            <tr class="bg-gray-50 border-b border-gray-100">
              <th class="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Mã / Mô tả</th>
              <th class="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Giá trị</th>
              <th class="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Sử dụng</th>
              <th class="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Thời hạn</th>
              <th class="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Trạng thái</th>
              <th class="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider text-right">Thao tác</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr *ngFor="let coupon of coupons" class="hover:bg-gray-50 transition-colors">
              <td class="px-6 py-4">
                <div class="font-bold text-gray-900 uppercase">{{ coupon.code }}</div>
                <div class="text-xs text-gray-500">{{ coupon.description }}</div>
              </td>
              <td class="px-6 py-4">
                <div class="text-sm font-medium text-gray-900">
                  <ng-container *ngIf="coupon.type === 'PERCENTAGE'">
                    {{ coupon.value }}%
                    <span class="text-xs text-gray-500 block" *ngIf="coupon.maxDiscountAmount">
                      Tối đa {{ coupon.maxDiscountAmount | number }}₫
                    </span>
                  </ng-container>
                  <ng-container *ngIf="coupon.type === 'FIXED'">
                    {{ coupon.value | number }}₫
                  </ng-container>
                </div>
              </td>
              <td class="px-6 py-4 text-sm text-gray-600">
                {{ coupon.usedCount }} / {{ coupon.usageLimit || '∞' }}
              </td>
              <td class="px-6 py-4 text-xs text-gray-500">
                <div>Bắt đầu: {{ coupon.startDate | date:'dd/MM/yyyy' }}</div>
                <div>Kết thúc: {{ coupon.endDate | date:'dd/MM/yyyy' }}</div>
              </td>
              <td class="px-6 py-4">
                <span [class]="getStatusClass(coupon)" class="px-2 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider">
                  {{ getStatusLabel(coupon) }}
                </span>
              </td>
              <td class="px-6 py-4 text-right space-x-2">
                <a [routerLink]="[coupon.id, 'edit']" class="text-indigo-600 hover:text-indigo-900 text-sm font-medium">Sửa</a>
                <button (click)="deleteCoupon(coupon)" class="text-red-600 hover:text-red-900 text-sm font-medium">Xoá</button>
              </td>
            </tr>
            <tr *ngIf="coupons.length === 0">
              <td colspan="6" class="px-6 py-10 text-center text-gray-500">
                Chưa có mã giảm giá nào được tạo.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: []
})
export class CouponListComponent implements OnInit {
  coupons: any[] = [];
  loading = false;

  constructor(
    private adminService: AdminService,
    private notify: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadCoupons();
  }

  loadCoupons() {
    this.loading = true;
    this.adminService.getCoupons().subscribe({
      next: (res) => {
        if (res.success) {
          this.coupons = res.data.content || res.data;
        }
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  deleteCoupon(coupon: any) {
    if (confirm(`Bạn có chắc chắn muốn xoá mã ${coupon.code}?`)) {
      this.adminService.deleteCoupon(coupon.id).subscribe({
        next: (res) => {
          if (res.success) {
            this.notify.success('Đã xoá mã giảm giá');
            this.loadCoupons();
          }
        }
      });
    }
  }

  getStatusLabel(coupon: any): string {
    const now = new Date();
    const start = new Date(coupon.startDate);
    const end = new Date(coupon.endDate);

    if (!coupon.active) return 'Tắt';
    if (now < start) return 'Sắp diễn ra';
    if (now > end) return 'Hết hạn';
    if (coupon.usageLimit && coupon.usedCount >= coupon.usageLimit) return 'Hết lượt';
    return 'Đang chạy';
  }

  getStatusClass(coupon: any): string {
    const label = this.getStatusLabel(coupon);
    switch (label) {
      case 'Đang chạy': return 'bg-green-100 text-green-700';
      case 'Sắp diễn ra': return 'bg-blue-100 text-blue-700';
      case 'Hết hạn':
      case 'Hết lượt':
      case 'Tắt': return 'bg-red-100 text-red-700';
      default: return 'bg-gray-100 text-gray-700';
    }
  }
}
