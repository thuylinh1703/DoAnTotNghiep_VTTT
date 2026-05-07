import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AdminService } from '../../../../core/services/admin.service';
import { NotificationService } from '../../../../shared/services/notification.service';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'app-product-management',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './product-management.component.html',
  styleUrl: './product-management.component.scss'
})
export class ProductManagementComponent implements OnInit {
  products: any[] = [];
  categories: any[] = [];
  loading = false;
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;

  // For Add/Edit Modal
  showModal = false;
  editMode = false;
  currentProductId: number | null = null;
  productForm: FormGroup;
  selectedImages: File[] = [];

  constructor(
    private adminService: AdminService,
    private fb: FormBuilder,
    private notify: NotificationService
  ) {
    this.productForm = this.fb.group({
      name: ['', Validators.required],
      description: ['', Validators.required],
      price: [0, [Validators.required, Validators.min(0)]],
      discountPrice: [null],
      quantity: [0, [Validators.required, Validators.min(0)]],
      categoryId: ['', Validators.required],
      brand: [''],
      isNew: [false],
      isFeatured: [false]
    });
  }

  ngOnInit(): void {
    this.loadProducts();
    this.loadCategories();
  }

  getImageUrl(path: string): string {
    if (!path) return '';
    if (path.startsWith('http')) return path;
    const baseUrl = environment.apiUrl.replace('/api', '');
    return baseUrl + path;
  }

  loadProducts(): void {
    this.loading = true;
    this.adminService.getProducts(this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        if (response.success) {
          this.products = response.data.content;
          this.totalElements = response.data.totalElements;
          this.totalPages = response.data.totalPages;
        }
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  loadCategories(): void {
    this.adminService.getCategories().subscribe({
      next: (response) => {
        if (response.success) {
          this.categories = response.data;
        }
      }
    });
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadProducts();
  }

  openAddModal(): void {
    this.editMode = false;
    this.currentProductId = null;
    this.productForm.reset({
      price: 0,
      quantity: 0,
      isNew: false,
      isFeatured: false
    });
    this.selectedImages = [];
    this.showModal = true;
  }

  openEditModal(product: any): void {
    this.editMode = true;
    this.currentProductId = product.id;
    this.productForm.patchValue({
      name: product.name,
      description: product.description,
      price: product.price,
      discountPrice: product.discountPrice,
      quantity: product.quantity,
      categoryId: product.categoryId,
      brand: product.brand,
      isNew: product.isNew,
      isFeatured: product.isFeatured
    });
    this.selectedImages = [];
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  onFileChange(event: any): void {
    if (event.target.files.length > 0) {
      this.selectedImages = Array.from(event.target.files);
    }
  }

  onSubmit(): void {
    if (this.productForm.invalid) return;

    this.loading = true;
    const formData = new FormData();
    formData.append('product', new Blob([JSON.stringify(this.productForm.value)], { type: 'application/json' }));
    
    this.selectedImages.forEach(file => {
      formData.append('images', file);
    });

    const request = this.editMode && this.currentProductId
      ? this.adminService.updateProduct(this.currentProductId, formData)
      : this.adminService.createProduct(formData);

    request.subscribe({
      next: (response) => {
        if (response.success) {
          this.notify.success(this.editMode ? 'Cập nhật sản phẩm thành công' : 'Thêm sản phẩm mới thành công');
          this.closeModal();
          this.loadProducts();
        }
        this.loading = false;
      },
      error: (err) => {
        this.notify.error(err.error?.message || 'Có lỗi xảy ra khi lưu sản phẩm');
        this.loading = false;
      }
    });
  }

  deleteProduct(id: number): void {
    if (!confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')) return;

    this.adminService.deleteProduct(id).subscribe({
      next: (response) => {
        if (response.success) {
          this.notify.success('Đã xóa sản phẩm thành công');
          this.loadProducts();
        }
      },
      error: (err) => {
        this.notify.error('Không thể xóa sản phẩm này');
      }
    });
  }

  getPages(): number[] {
    return Array.from({length: this.totalPages}, (_, i) => i);
  }
}
