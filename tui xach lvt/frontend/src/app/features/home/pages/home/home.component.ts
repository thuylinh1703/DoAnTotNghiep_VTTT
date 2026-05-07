import { Component, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { ProductService } from '../../../../core/services/product.service';
import { CartService } from '../../../../core/services/cart.service';
import { BlogService } from '../../../../core/services/blog.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  providers: [DecimalPipe],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {
  banners: any[] = [];
  latestBlogs: any[] = [];
  newProducts: any[] = [];
  categories: any[] = [];

  constructor(
    private productService: ProductService,
    private cartService: CartService,
    private blogService: BlogService
  ) {}

  ngOnInit(): void {
    this.loadHomePage();
    this.loadLatestBlogs();
  }

  loadHomePage() {
    this.productService.getHomePage().subscribe({
      next: (res: any) => {
        if (res.success) {
          this.banners = res.data.banners ?? [];
          this.categories = res.data.categories ?? [];
          this.newProducts = (res.data.newProducts ?? []).slice(0, 4);
        }
      }
    });
  }

  loadLatestBlogs() {
    this.blogService.getPosts(0, 3).subscribe({
      next: (res: any) => {
        if (res.success) {
          this.latestBlogs = res.data.content ?? res.data;
        }
      }
    });
  }

  getCategoryIdByName(name: string): number {
    const cat = this.categories.find(c => c.name.toLowerCase().includes(name.toLowerCase()));
    return cat ? cat.id : 0;
  }

  getImageUrl(path: string): string {
    if (!path) return '';
    if (path.startsWith('http')) return path;
    return `http://localhost:8080${path}`;
  }

  addToCart(productId: number) {
    this.cartService.addToCart(productId, 1).subscribe({
      next: (_res: any) => {}
    });
  }
}
