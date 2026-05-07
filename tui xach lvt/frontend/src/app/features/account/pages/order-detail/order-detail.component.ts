import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { OrderService } from '../../../../core/services/order.service';
import { ReviewService } from '../../../../core/services/review.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './order-detail.component.html',
  styleUrl: './order-detail.component.scss'
})
export class OrderDetailComponent implements OnInit {
  order: any = null;
  loading = false;
  
  // Review Modal State
  showReviewModal = false;
  selectedItemForReview: any = null;
  reviewData = {
    rating: 5,
    comment: '',
    images: [] as string[]
  };
  isSubmittingReview = false;

  constructor(
    private route: ActivatedRoute,
    private orderService: OrderService,
    private reviewService: ReviewService
  ) {}

  ngOnInit(): void {
    const orderCode = this.route.snapshot.paramMap.get('id');
    if (orderCode) {
      this.loadOrderDetail(orderCode);
    }
  }

  loadOrderDetail(orderCode: string): void {
    this.loading = true;
    this.orderService.getOrderByCode(orderCode).subscribe({
      next: (response) => {
        if (response.success) {
          this.order = response.data;
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

  getImageUrl(imagePath: string): string {
    if (!imagePath) return '';
    if (imagePath.startsWith('http') || imagePath.startsWith('data:')) return imagePath;
    // Ensure path starts with slash
    const path = imagePath.startsWith('/') ? imagePath : `/${imagePath}`;
    return `http://localhost:8080${path}`;
  }

  confirmReceived(): void {
    if (confirm('Bạn xác nhận đã nhận được hàng và hài lòng với sản phẩm?')) {
      this.orderService.confirmReceived(this.order.orderCode).subscribe({
        next: (res) => {
          if (res.success) {
            this.order.status = 'COMPLETED';
            alert('Xác nhận thành công! Bạn có thể đánh giá sản phẩm ngay bây giờ.');
          }
        },
        error: (err) => alert('Có lỗi xảy ra: ' + (err.error?.message || 'Không thể xác nhận'))
      });
    }
  }

  openReviewModal(item: any): void {
    this.selectedItemForReview = item;
    this.reviewData = {
      rating: 5,
      comment: '',
      images: []
    };
    this.showReviewModal = true;
  }

  closeReviewModal(): void {
    this.showReviewModal = false;
    this.selectedItemForReview = null;
  }

  onFileChange(event: any): void {
    const files = event.target.files;
    if (files) {
      for (let i = 0; i < files.length; i++) {
        const reader = new FileReader();
        reader.onload = (e: any) => {
          this.reviewData.images.push(e.target.result);
        };
        reader.readAsDataURL(files[i]);
      }
    }
  }

  removeImage(index: number): void {
    this.reviewData.images.splice(index, 1);
  }

  submitReview(): void {
    const trimmedComment = this.reviewData.comment.trim();
    if (trimmedComment.length < 10) {
      alert('Nội dung đánh giá cần ít nhất 10 ký tự');
      return;
    }

    if (this.reviewData.rating < 1) {
      alert('Vui lòng chọn số sao đánh giá');
      return;
    }

    this.isSubmittingReview = true;
    const payload = {
      productId: this.selectedItemForReview.productId,
      rating: this.reviewData.rating,
      comment: trimmedComment
      // images: this.reviewData.images // Backend doesn't support images yet per API docs
    };

    this.reviewService.createReview(payload).subscribe({
      next: (res) => {
        if (res.success) {
          alert('Cảm ơn bạn đã đánh giá sản phẩm!');
          this.closeReviewModal();
          this.selectedItemForReview.isReviewed = true;
        }
        this.isSubmittingReview = false;
      },
      error: (err) => {
        alert('Lỗi: ' + (err.error?.message || 'Không thể gửi đánh giá. Vui lòng kiểm tra lại nội dung.'));
        this.isSubmittingReview = false;
      }
    });
  }

  cancelOrder(): void {
    if (confirm('Bạn có chắc chắn muốn hủy đơn hàng này?')) {
      alert('Chức năng hủy đơn hàng đang được cập nhật.');
    }
  }
}
