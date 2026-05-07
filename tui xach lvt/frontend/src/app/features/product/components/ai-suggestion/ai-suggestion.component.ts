import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductService } from '../../../../core/services/product.service';
import { trigger, transition, style, animate } from '@angular/animations';

@Component({
  selector: 'app-ai-suggestion',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './ai-suggestion.component.html',
  styleUrl: './ai-suggestion.component.scss',
  animations: [
    trigger('slideInOut', [
      transition(':enter', [
        style({ transform: 'translateX(100%)', opacity: 0 }),
        animate('300ms ease-out', style({ transform: 'translateX(0)', opacity: 1 }))
      ]),
      transition(':leave', [
        animate('300ms ease-in', style({ transform: 'translateX(100%)', opacity: 0 }))
      ])
    ])
  ]
})
export class AiSuggestionComponent implements OnChanges {
  @Input() keyword: string = '';
  suggestions: any[] = [];
  isOpen = false;
  loading = false;

  constructor(private productService: ProductService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['keyword'] && this.keyword) {
      this.getAiSuggestions();
    }
  }

  toggle(): void {
    this.isOpen = !this.isOpen;
    if (this.isOpen && this.suggestions.length === 0) {
      this.getAiSuggestions();
    }
  }

  getAiSuggestions(): void {
    this.loading = true;
    this.productService.aiSuggest(this.keyword || 'featured').subscribe({
      next: (response: any) => {
        try {
          this.suggestions = typeof response === 'string' ? JSON.parse(response) : response;
        } catch (e) {
          console.error("AI Response parse error", e);
          this.suggestions = [];
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.suggestions = [];
      }
    });
  }
}
