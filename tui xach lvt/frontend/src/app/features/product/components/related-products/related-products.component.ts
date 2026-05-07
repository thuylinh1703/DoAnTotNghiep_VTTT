import { Component, Input, OnInit, OnChanges, SimpleChanges, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductService } from '../../../../core/services/product.service';
import { ProductCardComponent } from '../product-card/product-card.component';

@Component({
  selector: 'app-related-products',
  standalone: true,
  imports: [CommonModule, RouterLink, ProductCardComponent],
  template: `
    <div class="mt-20" *ngIf="related && (related.similar?.length || related.accessories?.length)">
      <!-- Similar Products -->
      <div *ngIf="related.similar?.length" class="mb-16">
        <div class="flex items-end justify-between mb-8">
          <div>
            <h2 class="text-[32px] font-semibold tracking-[-0.022em] text-[#1d1d1f]">Sản phẩm tương tự</h2>
            <p class="text-[17px] text-[rgba(0,0,0,0.56)] mt-1">Có thể bạn cũng sẽ thích những mẫu túi này.</p>
          </div>
        </div>
        
        <div class="flex overflow-x-auto pb-8 -mx-6 px-6 space-x-6 scrollbar-hide">
          <div *ngFor="let product of related.similar" class="w-[280px] flex-shrink-0 animate-fade-in">
            <app-product-card [product]="product" (addToCart)="onAddToCart($event)"></app-product-card>
          </div>
        </div>
      </div>

      <!-- Accessories -->
      <div *ngIf="related.accessories?.length">
        <div class="flex items-end justify-between mb-8">
          <div>
            <h2 class="text-[32px] font-semibold tracking-[-0.022em] text-[#1d1d1f]">Phụ kiện phù hợp</h2>
            <p class="text-[17px] text-[rgba(0,0,0,0.56)] mt-1">Kết hợp cùng túi xách của bạn để thêm phần cá tính.</p>
          </div>
        </div>
        
        <div class="flex overflow-x-auto pb-8 -mx-6 px-6 space-x-6 scrollbar-hide">
          <div *ngFor="let product of related.accessories" class="w-[280px] flex-shrink-0 animate-fade-in">
            <app-product-card [product]="product" (addToCart)="onAddToCart($event)"></app-product-card>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .scrollbar-hide::-webkit-scrollbar { display: none; }
    .scrollbar-hide { -ms-overflow-style: none; scrollbar-width: none; }
    
    @keyframes fade-in {
      from { opacity: 0; transform: translateY(20px); }
      to { opacity: 1; transform: translateY(0); }
    }
    .animate-fade-in { animation: fade-in 0.6s cubic-bezier(0.4, 0, 0.2, 1) both; }
  `]
})
export class RelatedProductsComponent implements OnInit, OnChanges {
  @Input() productId!: number;
  @Output() addToCart = new EventEmitter<any>();
  related: any = null;

  constructor(private productService: ProductService) {}

  ngOnInit(): void {
    if (this.productId) {
      this.loadRelated();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['productId'] && !changes['productId'].firstChange) {
      this.loadRelated();
    }
  }

  loadRelated() {
    this.productService.getRelatedProducts(this.productId).subscribe({
      next: (res) => {
        if (res.success) {
          this.related = res.data;
        }
      }
    });
  }

  onAddToCart(product: any) {
    this.addToCart.emit(product);
  }
}
