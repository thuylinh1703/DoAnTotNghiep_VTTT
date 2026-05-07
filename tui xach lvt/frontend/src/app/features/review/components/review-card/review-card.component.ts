import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { Review } from '../../../../core/models/review';

@Component({
  selector: 'app-review-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './review-card.component.html',
  styleUrl: './review-card.component.scss'
})
export class ReviewCardComponent {
  @Input({ required: true }) review!: Review;

  get stars(): number[] {
    return [1, 2, 3, 4, 5];
  }
}
