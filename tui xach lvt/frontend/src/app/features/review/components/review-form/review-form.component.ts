import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ReviewService } from '../../../../core/services/review.service';
import { NotificationService } from '../../../../shared/services/notification.service';

@Component({
  selector: 'app-review-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './review-form.component.html',
  styleUrl: './review-form.component.scss'
})
export class ReviewFormComponent {
  @Input() productId: number | undefined;
  @Output() reviewAdded = new EventEmitter<void>();

  rating = 0;
  comment = '';
  submitting = false;
  hoverRating = 0;

  constructor(
    private reviewService: ReviewService,
    private notificationService: NotificationService
  ) {}

  setRating(value: number): void {
    this.rating = value;
  }

  submit(): void {
    if (!this.productId || this.submitting) {
      return;
    }

    if (this.rating < 1 || this.rating > 5) {
      this.notificationService.warning('Vui lòng chọn số sao đánh giá');
      return;
    }

    const trimmedComment = this.comment.trim();
    if (trimmedComment.length < 10) {
      this.notificationService.warning('Nội dung đánh giá cần ít nhất 10 ký tự');
      return;
    }

    this.submitting = true;
    this.reviewService.createReview({
      productId: this.productId,
      rating: this.rating,
      comment: trimmedComment
    }).subscribe({
      next: (response) => {
        if (response.success) {
          this.notificationService.success('Cảm ơn bạn đã gửi đánh giá');
          this.rating = 0;
          this.comment = '';
          this.reviewAdded.emit();
        }
        this.submitting = false;
      },
      error: (err) => {
        this.submitting = false;
        this.notificationService.error(err?.error?.message || 'Gửi đánh giá thất bại');
      }
    });
  }
}
