import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { ReviewSummary } from '../../../../core/models/review';

@Component({
  selector: 'app-review-summary',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './review-summary.component.html',
  styleUrl: './review-summary.component.scss'
})
export class ReviewSummaryComponent {
  @Input() summary: ReviewSummary | null = null;

  stars = [5, 4, 3, 2, 1];

  getPercent(star: number): number {
    if (!this.summary || this.summary.totalCount === 0) {
      return 0;
    }
    const count = this.summary.distribution[String(star)] || 0;
    return Math.round((count / this.summary.totalCount) * 100);
  }

  getCount(star: number): number {
    if (!this.summary) {
      return 0;
    }
    return this.summary.distribution[String(star)] || 0;
  }
}
