import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';
import { Review, ReviewEligibility, ReviewSummary } from '../models/review';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {
  constructor(private apiService: ApiService) { }

  createReview(reviewData: {
    productId: number;
    rating: number;
    comment: string;
    images?: string[];
  }): Observable<any> {
    return this.apiService.post('reviews', reviewData);
  }

  getProductReviews(
    productId: number,
    page: number = 0,
    size: number = 10,
    sort: string = 'createdAt,desc'
  ): Observable<{ success: boolean; data: { content: Review[]; totalElements: number; number: number; last: boolean } }> {
    return this.apiService.get(`products/${productId}/reviews`, { page, size, sort });
  }

  getSummary(productId: number): Observable<{ success: boolean; data: ReviewSummary }> {
    return this.apiService.get(`products/${productId}/reviews/summary`);
  }

  checkEligibility(productId: number): Observable<{ success: boolean; data: ReviewEligibility }> {
    return this.apiService.get(`reviews/eligibility/${productId}`);
  }
}
