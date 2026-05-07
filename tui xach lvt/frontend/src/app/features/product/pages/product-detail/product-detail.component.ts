import { Component, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../../../core/services/product.service';
import { CartService } from '../../../../core/services/cart.service';
import { AuthService } from '../../../../core/services/auth.service';
import { NotificationService } from '../../../../shared/services/notification.service';
import { StockIndicatorComponent } from '../../../../shared/components/stock-indicator/stock-indicator.component';
import { RelatedProductsComponent } from '../../components/related-products/related-products.component';
import { ReviewService } from '../../../../core/services/review.service';
import { ReviewEligibility, ReviewSummary } from '../../../../core/models/review';
import { ReviewSummaryComponent } from '../../../review/components/review-summary/review-summary.component';
import { ReviewListComponent } from '../../../review/components/review-list/review-list.component';
import { ReviewFormComponent } from '../../../review/components/review-form/review-form.component';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    DecimalPipe,
    StockIndicatorComponent,
    RelatedProductsComponent,
    ReviewSummaryComponent,
    ReviewListComponent,
    ReviewFormComponent
  ],
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.scss'
})
export class ProductDetailComponent implements OnInit {
  product: any;
  quantity: number = 1;
  loading = false;
  selectedImage: string = '';
  reviewSummary: ReviewSummary | null = null;
  reviewEligibility: ReviewEligibility | null = null;
  reviewRefreshKey = 0;
  reviewTotal = 0;

  get outOfStock(): boolean {
    return !!this.product && this.product.quantity <= 0;
  }

  get atMaxStock(): boolean {
    return !!this.product && this.quantity >= this.product.quantity;
  }

  constructor(
    private route: ActivatedRoute,
    private productService: ProductService,
    private cartService: CartService,
    private authService: AuthService,
    private router: Router,
    private notify: NotificationService,
    private reviewService: ReviewService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const id = params['id'];
      if (id) {
        this.loadProduct(+id);
      }
    });
  }

  loadProduct(id: number): void {
    this.loading = true;
    this.productService.getProductById(id).subscribe({
      next: (response) => {
        if (response.success) {
          this.product = response.data;
          this.selectedImage = this.product.primaryImage || (this.product.imageUrls && this.product.imageUrls[0]);
          this.quantity = 1; // Reset quantity when navigation occurs to a new product
          this.loadReviewSummary(this.product.id);
          this.loadReviewEligibility(this.product.id);
        }
        this.loading = false;
        window.scrollTo(0, 0);
      },
      error: () => {
        this.loading = false;
        window.scrollTo(0, 0);
      }
    });
  }

  getImageUrl(imagePath: string): string {
    if (!imagePath) return '';
    if (imagePath.startsWith('http')) return imagePath;
    return `http://localhost:8080${imagePath}`;
  }

  selectImage(image: string): void {
    this.selectedImage = image;
  }

  incrementQuantity(): void {
    if (this.product && this.quantity < this.product.quantity) {
      this.quantity++;
    }
  }

  decrementQuantity(): void {
    if (this.quantity > 1) {
      this.quantity--;
    }
  }

  addToCart(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    if (!this.product || this.outOfStock) return;

    this.loading = true;
    this.cartService.addToCart(this.product.id, this.quantity).subscribe({
      next: (response) => {
        if (response.success) {
          this.notify.success('Đã thêm sản phẩm vào giỏ hàng');
        }
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        // 409 stock error already surfaced by interceptor. Only show generic on others.
        if (err?.status !== 409) {
          this.notify.error(err?.error?.message || 'Có lỗi xảy ra khi thêm vào giỏ hàng');
        }
      }
    });
  }

  buyNow(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    if (!this.product || this.outOfStock) return;

    this.cartService.addToCart(this.product.id, this.quantity).subscribe({
      next: (response) => {
        if (response.success) {
          this.router.navigate(['/cart']);
        }
      }
    });
  }

  onRelatedAddToCart(product: any): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    this.loading = true;
    this.cartService.addToCart(product.id, 1).subscribe({
      next: (response) => {
        if (response.success) {
          this.notify.success('Đã thêm sản phẩm vào giỏ hàng');
          this.router.navigate(['/cart']);
        }
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        if (err?.status !== 409) {
          this.notify.error(err?.error?.message || 'Có lỗi xảy ra khi thêm vào giỏ hàng');
        }
      }
    });
  }

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  onReviewAdded(): void {
    if (!this.product?.id) {
      return;
    }
    this.reviewRefreshKey++;
    this.loadReviewSummary(this.product.id);
    this.loadReviewEligibility(this.product.id);
  }

  onReviewsLoaded(total: number): void {
    this.reviewTotal = total;
  }

  get eligibilityHint(): string {
    if (!this.reviewEligibility || this.reviewEligibility.eligible) {
      return '';
    }

    switch (this.reviewEligibility.reason) {
      case 'ALREADY_REVIEWED':
        return 'Bạn đã đánh giá sản phẩm này rồi.';
      case 'NOT_DELIVERED':
        return 'Bạn chỉ có thể đánh giá sau khi đơn hàng đã giao thành công.';
      case 'NOT_PURCHASED':
      default:
        return 'Bạn cần mua sản phẩm này để viết đánh giá.';
    }
  }

  private loadReviewSummary(productId: number): void {
    this.reviewService.getSummary(productId).subscribe({
      next: (response) => {
        if (response.success) {
          this.reviewSummary = response.data;
        }
      },
      error: () => {
        this.reviewSummary = null;
      }
    });
  }

  private loadReviewEligibility(productId: number): void {
    if (!this.authService.isLoggedIn()) {
      this.reviewEligibility = null;
      return;
    }

    this.reviewService.checkEligibility(productId).subscribe({
      next: (response) => {
        if (response.success) {
          this.reviewEligibility = response.data;
        }
      },
      error: () => {
        this.reviewEligibility = null;
      }
    });
  }
}
