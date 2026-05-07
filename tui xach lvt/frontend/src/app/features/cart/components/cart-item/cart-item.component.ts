import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-cart-item',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './cart-item.component.html',
  styleUrl: './cart-item.component.scss'
})
export class CartItemComponent {
  @Input() item: any;
  /** Known stock from latest product fetch; null = unknown, don't cap client-side. */
  @Input() stock: number | null = null;
  @Output() remove = new EventEmitter<number>();
  @Output() updateQuantity = new EventEmitter<{id: number, quantity: number}>();

  get atMaxStock(): boolean {
    return this.stock !== null && this.item && this.item.quantity >= this.stock;
  }

  get lowStockWarning(): string | null {
    if (this.stock === null || !this.item) return null;
    if (this.stock <= 0) return 'Sản phẩm đã hết hàng';
    if (this.stock <= 5) return `Chỉ còn ${this.stock} sản phẩm`;
    return null;
  }

  getImageUrl(url: string | null): string {
    if (!url) return 'assets/images/placeholder.png';
    if (url.startsWith('http') || url.startsWith('data:')) return url;
    const baseUrl = 'http://localhost:8080'; // Should ideally come from environment
    return `${baseUrl}${url.startsWith('/') ? '' : '/'}${url}`;
  }

  onRemove(): void {
    this.remove.emit(this.item.id);
  }

  increment(): void {
    if (this.atMaxStock) return;
    const newQty = this.item.quantity + 1;
    this.updateQuantity.emit({ id: this.item.id, quantity: newQty });
  }

  decrement(): void {
    if (this.item.quantity > 1) {
      const newQty = this.item.quantity - 1;
      this.updateQuantity.emit({ id: this.item.id, quantity: newQty });
    }
  }

  onQuantityChange(qty: number): void {
    if (qty >= 1) {
      const capped = this.stock !== null ? Math.min(qty, this.stock) : qty;
      if (capped !== qty) {
        this.item.quantity = capped;
      }
      this.updateQuantity.emit({ id: this.item.id, quantity: capped });
    }
  }
}
