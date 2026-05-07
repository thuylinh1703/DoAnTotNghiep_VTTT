import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { OrderService } from '../../../../core/services/order.service';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-order-history',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './order-history.component.html',
  styleUrl: './order-history.component.scss'
})
export class OrderHistoryComponent implements OnInit {
  orders: any[] = [];
  loading = false;
  user: any = null;

  constructor(
    private orderService: OrderService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.user = this.authService.getUser();
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.orderService.getUserOrders().subscribe({
      next: (response) => {
        if (response.success) {
          this.orders = response.data.content;
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'PENDING':   return 'bg-[#fff7e6] text-[#b26a00]';
      case 'CONFIRMED': return 'bg-[#e6f0ff] text-[#0066cc]';
      case 'SHIPPING':  return 'bg-[#ededf2] text-[#1d1d1f]';
      case 'COMPLETED': return 'bg-[#e9f7ef] text-[#2a7f3f]';
      case 'CANCELLED': return 'bg-[#fff1f0] text-[#ff3b30]';
      default:          return 'bg-[#f5f5f7] text-[#1d1d1f]';
    }
  }

  getStatusVietnamese(status: string): string {
    switch (status) {
      case 'PENDING': return 'Chờ xác nhận';
      case 'CONFIRMED': return 'Đã xác nhận';
      case 'SHIPPING': return 'Đang giao hàng';
      case 'COMPLETED': return 'Hoàn thành';
      case 'CANCELLED': return 'Đã hủy';
      default: return status;
    }
  }

  logout(): void {
    this.authService.logout();
    window.location.href = '/login';
  }
}
