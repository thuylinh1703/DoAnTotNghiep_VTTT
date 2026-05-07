import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../../core/services/admin.service';
import { VoiceCallService } from '../../../../core/services/voice-call.service';
import { NotificationService } from '../../../../shared/services/notification.service';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';

@Component({
  selector: 'app-order-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './order-management.component.html',
  styleUrl: './order-management.component.scss'
})
export class OrderManagementComponent implements OnInit {
  orders: any[] = [];
  loading = false;
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;
  statuses = ['PENDING', 'CONFIRMED', 'SHIPPING', 'DELIVERED', 'COMPLETED', 'CANCELLED'];

  selectedOrder: any = null;
  editableOrder: any = {};
  isEditing = false;
  showDetailModal = false;
  filterStatus = '';
  searchText = '';
  private searchSubject = new Subject<string>();

  constructor(
    private adminService: AdminService,
    private voiceCallService: VoiceCallService,
    private notify: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadOrders();

    this.searchSubject.pipe(
      debounceTime(400),
      distinctUntilChanged()
    ).subscribe(() => {
      this.currentPage = 0;
      this.loadOrders();
    });
  }

  loadOrders(): void {
    this.loading = true;
    this.adminService.getOrders(this.currentPage, this.pageSize, this.filterStatus, this.searchText).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.orders = response.data.content;
          this.totalPages = response.data.totalPages;
          this.totalElements = response.data.totalElements;
        }
        this.loading = false;
      },
      error: () => {
        this.notify.error('Không thể tải danh sách đơn hàng');
        this.loading = false;
      }
    });
  }

  onFilterChange(status: string): void {
    this.filterStatus = status;
    this.currentPage = 0;
    this.loadOrders();
  }

  onSearchChange(): void {
    this.searchSubject.next(this.searchText);
  }

  updateStatus(orderId: number, event: any): void {
    const newStatus = event.target.value;
    this.adminService.updateOrderStatus(orderId, newStatus).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.notify.success('Cập nhật trạng thái thành công');
          this.loadOrders();
        }
      },
      error: (err) => {
        this.notify.error(err?.error?.message || 'Lỗi cập nhật trạng thái');
      }
    });
  }

  openDetail(order: any): void {
    this.selectedOrder = order;
    this.editableOrder = {
      receiverName: order.receiverName,
      receiverPhone: order.receiverPhone,
      receiverAddress: order.receiverAddress,
      note: order.note
    };
    this.isEditing = false;
    this.showDetailModal = true;
  }

  toggleEdit(): void {
    if (this.selectedOrder.status !== 'PENDING') {
      this.notify.warning('Chỉ có thể chỉnh sửa đơn hàng ở trạng thái PENDING');
      return;
    }
    this.isEditing = !this.isEditing;
  }

  saveOrderInfo(): void {
    this.adminService.updateOrderInfo(this.selectedOrder.id, this.editableOrder).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.notify.success('Cập nhật thông tin thành công');
          this.selectedOrder = response.data;
          this.isEditing = false;
          this.loadOrders();
        }
      },
      error: (err) => {
        this.notify.error(err?.error?.message || 'Lỗi cập nhật thông tin');
      }
    });
  }

  closeDetail(): void {
    this.showDetailModal = false;
    this.selectedOrder = null;
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadOrders();
  }

  getPages(): number[] {
    return Array.from({length: this.totalPages}, (_, i) => i);
  }

  getStatusClass(status: string): string {
    switch(status) {
      case 'PENDING':   return 'bg-[#fff7e6] text-[#b26a00]';
      case 'CONFIRMED': return 'bg-[#e6f0ff] text-[#0066cc]';
      case 'SHIPPING':  return 'bg-[#ededf2] text-[#1d1d1f]';
      case 'DELIVERED': return 'bg-[#e6f7f5] text-[#0a7a6e]';
      case 'COMPLETED': return 'bg-[#e9f7ef] text-[#2a7f3f]';
      case 'CANCELLED': return 'bg-[#fff1f0] text-[#ff3b30]';
      default:          return 'bg-[#f5f5f7] text-[#1d1d1f]';
    }
  }

  callCustomer(email: string): void {
    if (!email) {
      alert('Không tìm thấy email khách hàng');
      return;
    }
    this.voiceCallService.startCall(email);
  }

  getImageUrl(url: string | null): string {
    if (!url) return 'assets/images/placeholder.png';
    if (url.startsWith('http') || url.startsWith('data:')) return url;
    const baseUrl = 'http://localhost:8080';
    return `${baseUrl}${url.startsWith('/') ? '' : '/'}${url}`;
  }
}
