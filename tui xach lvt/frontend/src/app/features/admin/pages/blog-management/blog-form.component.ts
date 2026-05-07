import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AdminService } from '../../../../core/services/admin.service';
import { NotificationService } from '../../../../shared/services/notification.service';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'app-blog-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="p-6 max-w-4xl mx-auto">
      <div class="mb-8 flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">{{ isEdit ? 'Sửa' : 'Tạo' }} bài viết</h1>
          <p class="text-gray-500">Nội dung sẽ được làm sạch HTML ở backend trước khi lưu.</p>
        </div>
        <a routerLink="/admin/blog" class="text-sm font-medium text-gray-600 hover:text-gray-900">&larr; Quay lại danh sách</a>
      </div>

      <form [formGroup]="form" (ngSubmit)="save()" class="bg-white rounded-xl shadow-sm border border-gray-100 p-8 space-y-6">
        <div class="grid grid-cols-1 gap-6">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Tiêu đề</label>
            <input type="text" formControlName="title" class="w-full px-4 py-2 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-indigo-500">
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Slug (tùy chọn)</label>
            <input type="text" formControlName="slug" class="w-full px-4 py-2 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-indigo-500" placeholder="de-trong-de-tu-dong-tao">
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Ảnh bìa URL</label>
            <input type="text" formControlName="coverImageUrl" class="w-full px-4 py-2 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-indigo-500" placeholder="https://example.com/image.jpg">
            <div *ngIf="form.get('coverImageUrl')?.value" class="mt-4 rounded-xl overflow-hidden border border-gray-100 bg-gray-50 aspect-video max-w-md flex items-center justify-center">
              <img [src]="getImageUrl(form.get('coverImageUrl')?.value)" class="w-full h-full object-cover" alt="Preview">
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Tóm tắt</label>
            <textarea formControlName="excerpt" rows="3" class="w-full px-4 py-2 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-indigo-500"></textarea>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Nội dung HTML</label>
            <textarea formControlName="content" rows="12" class="w-full px-4 py-2 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-indigo-500 font-mono text-sm"></textarea>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Trạng thái</label>
            <select formControlName="status" class="w-full px-4 py-2 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-indigo-500">
              <option value="DRAFT">Nháp</option>
              <option value="PUBLISHED">Xuất bản</option>
              <option value="ARCHIVED">Lưu trữ</option>
            </select>
          </div>
        </div>

        <div class="flex justify-end pt-6">
          <button type="submit" [disabled]="loading || form.invalid" class="px-8 py-3 bg-gray-900 text-white rounded-lg font-bold hover:bg-gray-800 transition-colors disabled:opacity-50">
            {{ isEdit ? 'Cập nhật' : 'Tạo bài viết' }}
          </button>
        </div>
      </form>
    </div>
  `
})
export class BlogFormComponent implements OnInit {
  form: FormGroup;
  isEdit = false;
  id!: number;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private adminService: AdminService,
    private route: ActivatedRoute,
    private router: Router,
    private notify: NotificationService
  ) {
    this.form = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(200)]],
      slug: [''],
      excerpt: ['', [Validators.maxLength(500)]],
      content: ['', [Validators.required, Validators.minLength(50)]],
      coverImageUrl: [''],
      status: ['DRAFT', [Validators.required]]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.id = +id;
      this.load();
    }
  }

  getImageUrl(path: string): string {
    if (!path) return '';
    if (path.startsWith('http') || path.startsWith('data:')) return path;
    const baseUrl = environment.apiUrl.replace('/api', '');
    return baseUrl + (path.startsWith('/') ? '' : '/') + path;
  }

  load(): void {
    this.adminService.getBlogById(this.id).subscribe({
      next: (res) => {
        if (res.success) {
          this.form.patchValue(res.data);
        }
      }
    });
  }

  save(): void {
    if (this.form.invalid || this.loading) {
      return;
    }

    this.loading = true;
    const payload = this.form.value;
    const request$ = this.isEdit
      ? this.adminService.updateBlog(this.id, payload)
      : this.adminService.createBlog(payload);

    request$.subscribe({
      next: (res) => {
        if (res.success) {
          this.notify.success(this.isEdit ? 'Đã cập nhật bài viết' : 'Đã tạo bài viết');
          this.router.navigate(['/admin/blog']);
        }
        this.loading = false;
      },
      error: (err) => {
        this.notify.error(err?.error?.message || 'Có lỗi xảy ra');
        this.loading = false;
      }
    });
  }
}
