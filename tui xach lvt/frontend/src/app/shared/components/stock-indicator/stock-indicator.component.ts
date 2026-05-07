import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-stock-indicator',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span *ngIf="stock !== null && stock !== undefined"
          class="inline-flex items-center gap-1.5 text-[12px] tracking-[-0.12px] font-medium"
          [ngClass]="{
            'text-[#30a46c]': stock > threshold,
            'text-[#ff9500]': stock > 0 && stock <= threshold,
            'text-[#ff3b30]': stock === 0
          }">
      <span class="w-1.5 h-1.5 rounded-full"
            [ngClass]="{
              'bg-[#30a46c]': stock > threshold,
              'bg-[#ff9500]': stock > 0 && stock <= threshold,
              'bg-[#ff3b30]': stock === 0
            }"></span>
      <ng-container *ngIf="stock === 0">Hết hàng</ng-container>
      <ng-container *ngIf="stock > 0 && stock <= threshold">Sắp hết – chỉ còn {{ stock }}</ng-container>
      <ng-container *ngIf="stock > threshold">Còn hàng ({{ stock }} sản phẩm)</ng-container>
    </span>
  `
})
export class StockIndicatorComponent {
  @Input() stock: number | null | undefined = null;
  @Input() threshold = 10;
}
