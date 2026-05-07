import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductService } from '../../../../core/services/product.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-filter-sidebar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './filter-sidebar.component.html',
  styleUrl: './filter-sidebar.component.scss'
})
export class FilterSidebarComponent implements OnInit, OnChanges {
  @Input() initialCategoryId: number | null = null;
  @Output() filterChanged = new EventEmitter<any>();

  categories: any[] = [];
  brands: string[] = [];

  selectedCategoryId: number | null = null;
  selectedBrand: string | null = null;
  minPrice: number | null = null;
  maxPrice: number | null = null;

  constructor(private productService: ProductService) {}

  ngOnInit(): void {
    this.loadCategories();
    this.loadBrands();
    this.selectedCategoryId = this.initialCategoryId;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['initialCategoryId']) {
      this.selectedCategoryId = this.initialCategoryId;
    }
  }

  loadCategories(): void {
    this.productService.getCategories().subscribe({
      next: (response) => {
        if (response.success) {
          this.categories = response.data;
        }
      },
      error: (err) => console.error('Error loading categories:', err)
    });
  }

  loadBrands(): void {
    this.productService.getBrands().subscribe({
      next: (response) => {
        if (response.success) {
          this.brands = response.data;
        }
      },
      error: (err) => console.error('Error loading brands:', err)
    });
  }

  onCategoryChange(categoryId: number): void {
    this.selectedCategoryId = this.selectedCategoryId === categoryId ? null : categoryId;
    this.applyFilters();
  }

  onBrandChange(brand: string): void {
    this.selectedBrand = this.selectedBrand === brand ? null : brand;
    this.applyFilters();
  }

  applyFilters(): void {
    this.filterChanged.emit({
      categoryId: this.selectedCategoryId,
      brand: this.selectedBrand,
      minPrice: this.minPrice,
      maxPrice: this.maxPrice
    });
  }

  resetFilters(): void {
    this.selectedCategoryId = null;
    this.selectedBrand = null;
    this.minPrice = null;
    this.maxPrice = null;
    this.applyFilters();
  }
}
