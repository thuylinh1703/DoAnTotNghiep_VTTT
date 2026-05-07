import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductCardComponent } from '../../components/product-card/product-card.component';
import { FilterSidebarComponent } from '../../components/filter-sidebar/filter-sidebar.component';
import { ProductService } from '../../../../core/services/product.service';
import { AiSuggestionComponent } from '../../components/ai-suggestion/ai-suggestion.component';
import { CartService } from '../../../../core/services/cart.service';
import { AuthService } from '../../../../core/services/auth.service';
import { NotificationService } from '../../../../shared/services/notification.service';
import { ActivatedRoute, Router } from '@angular/router';

type Period = 'day' | 'week' | 'month' | null;

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, ProductCardComponent, FilterSidebarComponent, AiSuggestionComponent],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.scss'
})
export class ProductListComponent implements OnInit {
  products: any[] = [];
  totalPages: number = 0;
  totalElements: number = 0;
  currentPage: number = 0;
  pageSize: number = 12;
  sortBy: string = 'newest';
  loading = false;
  keyword: string = '';
  categoryId: number | null = null;
  period: Period = null;
  readonly periodOptions: Array<{ value: Period; label: string }> = [
    { value: null, label: 'Tất cả' },
    { value: 'day', label: 'Mới hôm nay' },
    { value: 'week', label: 'Mới tuần này' },
    { value: 'month', label: 'Mới tháng này' }
  ];
  activeFilters: {
    categoryId: number | null;
    brand: string | null;
    minPrice: number | null;
    maxPrice: number | null;
  } = {
    categoryId: null,
    brand: null,
    minPrice: null,
    maxPrice: null
  };

  constructor(
    private route: ActivatedRoute,
    private productService: ProductService,
    private cartService: CartService,
    private authService: AuthService,
    private router: Router,
    private notify: NotificationService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.keyword = params['keyword'] || '';
      this.categoryId = params['category'] ? +params['category'] : null;
      this.activeFilters.categoryId = this.categoryId;
      const rawPeriod = params['period'];
      this.period = (rawPeriod === 'day' || rawPeriod === 'week' || rawPeriod === 'month')
        ? rawPeriod
        : null;
      this.currentPage = 0;
      this.fetchProducts();
    });
  }

  fetchProducts(): void {
    this.loading = true;
    const params: any = {
      page: this.currentPage,
      size: this.pageSize,
      sortBy: this.sortBy
    };

    if (this.keyword) params.keyword = this.keyword;
    if (this.activeFilters.categoryId) params.categoryId = this.activeFilters.categoryId;
    if (this.activeFilters.brand) params.brand = this.activeFilters.brand;
    if (this.activeFilters.minPrice !== null) params.minPrice = this.activeFilters.minPrice;
    if (this.activeFilters.maxPrice !== null) params.maxPrice = this.activeFilters.maxPrice;
    if (this.period) params.period = this.period;

    this.productService.getProducts(params).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.products = response.data.content;
          this.totalPages = response.data.totalPages;
          this.totalElements = response.data.totalElements;
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.fetchProducts();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  onSortChange(event: any): void {
    const value = event.target.value;
    switch(value) {
      case 'Mới nhất': this.sortBy = 'newest'; break;
      case 'Giá thấp → cao': this.sortBy = 'priceAsc'; break;
      case 'Giá cao → thấp': this.sortBy = 'priceDesc'; break;
      default: this.sortBy = 'newest';
    }
    this.currentPage = 0;
    this.fetchProducts();
  }

  onFilterChange(filters: {
    categoryId: number | null;
    brand: string | null;
    minPrice: number | null;
    maxPrice: number | null;
  }): void {
    this.activeFilters = {
      categoryId: filters.categoryId,
      brand: filters.brand,
      minPrice: filters.minPrice,
      maxPrice: filters.maxPrice
    };
    this.currentPage = 0;
    this.fetchProducts();
  }

  setPeriod(period: Period): void {
    if (this.period === period) return;
    this.period = period;
    this.currentPage = 0;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { period: period ?? null },
      queryParamsHandling: 'merge'
    });
  }

  resetFilters(): void {
    this.activeFilters = {
      categoryId: null,
      brand: null,
      minPrice: null,
      maxPrice: null
    };
    this.period = null;
    this.keyword = '';
    this.currentPage = 0;
    this.fetchProducts();
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { 
        category: null, 
        brand: null, 
        minPrice: null, 
        maxPrice: null, 
        period: null,
        keyword: null 
      },
      queryParamsHandling: 'merge'
    });
  }

  addToCart(product: any): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    this.cartService.addToCart(product.id, 1).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.notify.success('Đã thêm sản phẩm vào giỏ hàng');
        }
      },
      error: (err) => {
        if (err?.status !== 409) {
          this.notify.error('Có lỗi xảy ra khi thêm vào giỏ hàng');
        }
      }
    });
  }

  getPages(): any[] {
    const pages: any[] = [];
    const maxVisible = 5;
    
    if (this.totalPages <= maxVisible + 2) {
      for (let i = 0; i < this.totalPages; i++) {
        pages.push({ value: i, label: (i + 1).toString(), isEllipsis: false });
      }
      return pages;
    }

    // Always show first
    pages.push({ value: 0, label: '1', isEllipsis: false });

    let start = Math.max(1, this.currentPage - 1);
    let end = Math.min(this.totalPages - 2, this.currentPage + 1);

    if (this.currentPage <= 2) {
      end = maxVisible - 1;
    } else if (this.currentPage >= this.totalPages - 3) {
      start = this.totalPages - maxVisible;
    }

    if (start > 1) pages.push({ value: -1, label: '...', isEllipsis: true });
    for (let i = start; i <= end; i++) {
      pages.push({ value: i, label: (i + 1).toString(), isEllipsis: false });
    }
    if (end < this.totalPages - 2) pages.push({ value: -1, label: '...', isEllipsis: true });

    // Always show last
    pages.push({ value: this.totalPages - 1, label: this.totalPages.toString(), isEllipsis: false });

    return pages;
  }
}
