import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../../../core/services/admin.service';
import { NotificationService } from '../../../../shared/services/notification.service';

@Component({
  selector: 'app-review-management',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './review-management.component.html',
  styleUrl: './review-management.component.scss'
})
export class ReviewManagementComponent implements OnInit {
  reviews: any[] = [];
  loading = false;

  constructor(
    private adminService: AdminService,
    private notify: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadReviews();
  }

  loadReviews(): void {
    this.loading = true;
    this.adminService.getReviews(0, 100).subscribe({
      next: (res) => {
        if (res.success) {
          this.reviews = res.data.content || [];
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  deleteReview(reviewId: number): void {
    if (!confirm('Bạn có chắc chắn muốn xoá đánh giá này?')) {
      return;
    }

    this.adminService.deleteReview(reviewId).subscribe({
      next: (res) => {
        if (res.success) {
          this.notify.success('Đã xoá đánh giá thành công');
          this.loadReviews();
        }
      },
      error: (err) => {
        this.notify.error(err?.error?.message || 'Không thể xoá đánh giá');
      }
    });
  }

  toggleReviewStatus(id: number): void {
    this.adminService.toggleReviewStatus(id).subscribe({
      next: (res) => {
        if (res.success) {
          this.notify.success('Đã cập nhật trạng thái hiển thị');
          this.loadReviews();
        }
      },
      error: (err) => {
        this.notify.error('Lỗi khi cập nhật trạng thái');
      }
    });
  }

}
