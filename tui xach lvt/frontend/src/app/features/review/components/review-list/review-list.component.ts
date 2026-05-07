import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReviewService } from '../../../../core/services/review.service';
import { Review } from '../../../../core/models/review';
import { ReviewCardComponent } from '../review-card/review-card.component';

@Component({
  selector: 'app-review-list',
  standalone: true,
  imports: [CommonModule, ReviewCardComponent],
  templateUrl: './review-list.component.html',
  styleUrl: './review-list.component.scss'
})
export class ReviewListComponent implements OnChanges {
  @Input() productId: number | undefined;
  @Input() refreshKey: number = 0;
  @Output() loaded = new EventEmitter<number>();

  reviews: Review[] = [];
  page = 0;
  readonly size = 5;
  hasMore = false;
  loading = false;
  sort = 'createdAt,desc';

  constructor(private reviewService: ReviewService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if ((changes['productId'] && this.productId) || changes['refreshKey']) {
      this.resetAndLoad();
    }
  }

  onSortChange(sort: string): void {
    this.sort = sort;
    this.resetAndLoad();
  }

  loadMore(): void {
    if (this.loading || !this.productId) {
      return;
    }

    this.loading = true;
    this.reviewService.getProductReviews(this.productId, this.page, this.size, this.sort).subscribe({
      next: (response) => {
        if (response.success) {
          const pageData = response.data;
          this.reviews = [...this.reviews, ...pageData.content];
          this.hasMore = !pageData.last;
          this.page = pageData.number + 1;
          this.loaded.emit(pageData.totalElements);
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  private resetAndLoad(): void {
    this.reviews = [];
    this.page = 0;
    this.hasMore = false;
    this.loadMore();
  }
}
