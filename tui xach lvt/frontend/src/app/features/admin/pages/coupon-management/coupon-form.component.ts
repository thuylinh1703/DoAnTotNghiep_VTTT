import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AdminService } from '../../../../core/services/admin.service';
import { NotificationService } from '../../../../shared/services/notification.service';

@Component({
  selector: 'app-coupon-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterLink],
  template: `
    <div class="p-6 max-w-4xl mx-auto">
      <div class="mb-8 flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">{{ isEdit ? 'Sửa' : 'Thêm' }} Mã giảm giá</h1>
          <p class="text-gray-500">Thiết lập quy tắc và giới hạn cho mã giảm giá.</p>
        </div>
        <a routerLink="/admin/coupons" class="text-sm font-medium text-gray-600 hover:text-gray-900">&larr; Quay lại danh sách</a>
      </div>

      <form [formGroup]="couponForm" (ngSubmit)="onSubmit()" class="bg-white rounded-xl shadow-sm border border-gray-100 p-8 space-y-6">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- Code -->
          <div class="md:col-span-1">
            <label class="block text-sm font-medium text-gray-700 mb-2">Mã code</label>
            <input type="text" formControlName="code" class="w-full px-4 py-2 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-indigo-500 uppercase" placeholder="TET2024">
            <p *ngIf="couponForm.get('code')?.touched && couponForm.get('code')?.invalid" class="mt-1 text-xs text-red-500">Vui lòng nhập mã code.</p>
          </div>

          <!-- Active status -->
          <div class="md:col-span-1 flex items-end pb-2">
            <label class="flex items-center space-x-3 cursor-pointer">
              <input type="checkbox" formControlName="active" class="w-5 h-5 accent-indigo-600 rounded">
              <span class="text-sm font-medium text-gray-700">Đang kích hoạt</span>
            </label>
          </div>

          <!-- Description -->
          <div class="md:col-span-2">
            <label class="block text-sm font-medium text-gray-700 mb-2">Mô tả</label>
            <textarea formControlName="description" class="w-full px-4 py-2 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-indigo-500 min-h-[80px]" placeholder="Giảm giá chào xuân..."></textarea>
          </div>

          <!-- Type -->
          <div class="md:col-span-1">
            <label class="block text-sm font-medium text-gray-700 mb-2">Loại giảm giá</label>
            <select formControlName="type" class="w-full px-4 py-2 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-indigo-500">
              <option value="PERCENTAGE">Phần trăm (%)</option>
              <option value="FIXED">Số tiền cố định (₫)</option>
            </select>
          </div>

          <!-- Value -->
          <div class="md:col-span-1">
            <label class="block text-sm font-medium text-gray-700 mb-2">Giá trị ({{ couponForm.get('type')?.value === 'PERCENTAGE' ? '%' : '₫' }})</label>
            <input type="number" formControlName="value" class="w-full px-4 py-2 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-indigo-500">
          </div>

          <!-- Extra Fields for Percentage -->
          <div class="md:col-span-1" *ngIf="couponForm.get('type')?.value === 'PERCENTAGE'">
            <label class="block text-sm font-medium text-gray-700 mb-2">Số tiền giảm tối đa (₫)</label>
            <input type="number" formControlName="maxDiscountAmount" class="w-full px-4 py-2 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-indigo-500" placeholder="Để trống nếu không giới hạn">
          </div>

          <!-- Min Order Amount -->
          <div class="md:col-span-1">
            <label class="block text-sm font-medium text-gray-700 mb-2">Giá trị đơn hàng tối thiểu (₫)</label>
            <input type="number" formControlName="minOrderAmount" class="w-full px-4 py-2 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-indigo-500" placeholder="0">
          </div>

          <!-- Limits -->
          <div class="md:col-span-1">
            <label class="block text-sm font-medium text-gray-700 mb-2">Giới hạn tổng lượt sử dụng</label>
            <input type="number" formControlName="usageLimit" class="w-full px-4 py-2 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-indigo-500" placeholder="Mặc định: không giới hạn">
          </div>

          <div class="md:col-span-1">
            <label class="block text-sm font-medium text-gray-700 mb-2">Giới hạn mỗi khách hàng</label>
            <input type="number" formControlName="perUserLimit" class="w-full px-4 py-2 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-indigo-500" placeholder="1">
          </div>

          <!-- Dates -->
          <div class="md:col-span-1">
            <label class="block text-sm font-medium text-gray-700 mb-2">Ngày bắt đầu</label>
            <input type="datetime-local" formControlName="startDate" class="w-full px-4 py-2 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-indigo-500">
          </div>

          <div class="md:col-span-1">
            <label class="block text-sm font-medium text-gray-700 mb-2">Ngày kết thúc</label>
            <input type="datetime-local" formControlName="endDate" class="w-full px-4 py-2 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-indigo-500">
          </div>
        </div>

        <div class="flex justify-end pt-6">
          <button type="submit" [disabled]="loading || couponForm.invalid" class="px-8 py-3 bg-gray-900 text-white rounded-lg font-bold hover:bg-gray-800 transition-colors disabled:opacity-50">
            {{ isEdit ? 'Cập nhật' : 'Tạo mã mới' }}
          </button>
        </div>
      </form>
    </div>
  `,
  styles: []
})
export class CouponFormComponent implements OnInit {
  couponForm!: FormGroup;
  isEdit = false;
  couponId!: number;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private adminService: AdminService,
    private route: ActivatedRoute,
    private router: Router,
    private notify: NotificationService
  ) {
    this.couponForm = this.fb.group({
      code: ['', [Validators.required]],
      description: [''],
      type: ['PERCENTAGE', [Validators.required]],
      value: [1, [Validators.required, Validators.min(0.01)]],
      maxDiscountAmount: [null],
      minOrderAmount: [0],
      usageLimit: [null],
      perUserLimit: [1],
      startDate: [this.formatDate(new Date())],
      endDate: [this.formatDate(this.addDays(new Date(), 30))],
      active: [true]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.couponId = +id;
      this.loadCoupon();
    }
  }

  loadCoupon() {
    this.adminService.getCouponById(this.couponId).subscribe({
      next: (res) => {
        if (res.success) {
          const coupon = res.data;
          this.couponForm.patchValue({
            ...coupon,
            startDate: this.formatDate(new Date(coupon.startDate)),
            endDate: this.formatDate(new Date(coupon.endDate))
          });
        }
      }
    });
  }

  onSubmit() {
    if (this.couponForm.invalid) return;

    this.loading = true;
    const val = this.couponForm.value;
    const request = {
      ...val,
      code: val.code.toUpperCase()
    };

    const action = this.isEdit 
      ? this.adminService.updateCoupon(this.couponId, request)
      : this.adminService.createCoupon(request);

    action.subscribe({
      next: (res) => {
        if (res.success) {
          this.notify.success(this.isEdit ? 'Đã cập nhật mã giảm giá' : 'Đã tạo mã giảm giá mới');
          this.router.navigate(['/admin/coupons']);
        }
        this.loading = false;
      },
      error: (err) => {
        this.notify.error(err?.error?.message || 'Có lỗi xảy ra');
        this.loading = false;
      }
    });
  }

  private formatDate(date: Date): string {
    const d = new Date(date);
    d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
    return d.toISOString().slice(0, 16);
  }

  private addDays(date: Date, days: number): Date {
    const d = new Date(date);
    d.setDate(d.getDate() + days);
    return d;
  }
}
