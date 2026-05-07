import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AdminService } from '../../../../core/services/admin.service';
import { NotificationService } from '../../../../shared/services/notification.service';

@Component({
  selector: 'app-category-management',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './category-management.component.html',
  styleUrl: './category-management.component.scss'
})
export class CategoryManagementComponent implements OnInit {
  categories: any[] = [];
  loading = false;
  showModal = false;
  editMode = false;
  currentCategoryId: number | null = null;
  categoryForm: FormGroup;

  constructor(
    private adminService: AdminService,
    private fb: FormBuilder,
    private notify: NotificationService
  ) {
    this.categoryForm = this.fb.group({
      name: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.loading = true;
    this.adminService.getCategories().subscribe({
      next: (response) => {
        if (response.success) {
          this.categories = response.data;
        }
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  openAddModal(): void {
    this.editMode = false;
    this.currentCategoryId = null;
    this.categoryForm.reset();
    this.showModal = true;
  }

  openEditModal(category: any): void {
    this.editMode = true;
    this.currentCategoryId = category.id;
    this.categoryForm.patchValue({
      name: category.name
    });
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  onSubmit(): void {
    if (this.categoryForm.invalid) return;

    this.loading = true;
    const request = this.editMode && this.currentCategoryId
      ? this.adminService.updateCategory(this.currentCategoryId, this.categoryForm.value)
      : this.adminService.createCategory(this.categoryForm.value);

    request.subscribe({
      next: (response) => {
        if (response.success) {
          this.notify.success(this.editMode ? 'Đã cập nhật danh mục' : 'Đã thêm danh mục mới');
          this.loadCategories();
          this.closeModal();
        }
        this.loading = false;
      },
      error: () => {
        this.notify.error('Có lỗi xảy ra khi lưu danh mục');
        this.loading = false;
      }
    });
  }

  deleteCategory(id: number): void {
    if (!confirm('Bạn có chắc muốn xóa danh mục này?')) return;
    this.adminService.deleteCategory(id).subscribe({
      next: (response) => {
        if (response.success) {
          this.notify.success('Đã xóa danh mục thành công');
          this.loadCategories();
        }
      },
      error: () => this.notify.error('Không thể xóa danh mục này')
    });
  }
}
