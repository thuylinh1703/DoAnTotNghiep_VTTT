import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './admin-layout.component.html',
  styleUrl: './admin-layout.component.scss'
})
export class AdminLayoutComponent {
  isSidebarOpen = true;
  user: any;
  activePageName: string = 'Tổng quan';

  constructor(private authService: AuthService) {
    this.user = this.authService.getUser();
  }

  toggleSidebar() {
    this.isSidebarOpen = !this.isSidebarOpen;
  }

  onActivate(component: any) {
    const name = component.constructor.name;
    if (name.includes('Dashboard')) this.activePageName = 'Tổng quan';
    else if (name.includes('Product')) this.activePageName = 'Sản phẩm';
    else if (name.includes('Category')) this.activePageName = 'Danh mục';
    else if (name.includes('Order')) this.activePageName = 'Đơn hàng';
    else if (name.includes('User')) this.activePageName = 'Khách hàng';
    else if (name.includes('Review')) this.activePageName = 'Đánh giá';
    else if (name.includes('Banner')) this.activePageName = 'Banner';
    else if (name.includes('Blog')) this.activePageName = 'Blog';
    else if (name.includes('Revenue')) this.activePageName = 'Thống kê chi tiết';
    else if (name.includes('Performance')) this.activePageName = 'Hiệu suất sản phẩm';
    else if (name.includes('Support')) this.activePageName = 'Trung tâm hỗ trợ';
    else this.activePageName = 'Hệ thống';
  }

  logout() {
    this.authService.logout();
  }
}
