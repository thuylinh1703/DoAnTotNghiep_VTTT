import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AdminService } from '../../../../core/services/admin.service';
import { NotificationService } from '../../../../shared/services/notification.service';

@Component({
  selector: 'app-blog-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="p-6">
      <div class="flex justify-between items-center mb-6">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Quản lý Blog</h1>
          <p class="text-gray-500">Soạn thảo, xuất bản và lưu trữ bài viết.</p>
        </div>
        <a routerLink="new" class="px-4 py-2 bg-gray-900 text-white rounded-lg hover:bg-gray-800 transition-colors">
          Bài viết mới
        </a>
      </div>

            <div class="mb-4 flex gap-2 flex-wrap">
        <button type="button" (click)="setFilter('')"
          [ngClass]="statusFilter === '' ? 'bg-gray-900 text-white' : 'bg-white text-gray-700 border border-gray-200'"
          class="px-3 py-1.5 rounded-lg text-sm">Tất cả</button>
        <button type="button" (click)="setFilter('DRAFT')"
          [ngClass]="statusFilter === 'DRAFT' ? 'bg-gray-900 text-white' : 'bg-white text-gray-700 border border-gray-200'"
          class="px-3 py-1.5 rounded-lg text-sm">Nháp</button>
        <button type="button" (click)="setFilter('PUBLISHED')"
          [ngClass]="statusFilter === 'PUBLISHED' ? 'bg-gray-900 text-white' : 'bg-white text-gray-700 border border-gray-200'"
          class="px-3 py-1.5 rounded-lg text-sm">Đã xuất bản</button>
        <button type="button" (click)="setFilter('ARCHIVED')"
          [ngClass]="statusFilter === 'ARCHIVED' ? 'bg-gray-900 text-white' : 'bg-white text-gray-700 border border-gray-200'"
          class="px-3 py-1.5 rounded-lg text-sm">Lưu trữ</button>
            </div>

      <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <table class="w-full text-left">
          <thead>
            <tr class="bg-gray-50 border-b border-gray-100">
              <th class="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Tiêu đề</th>
              <th class="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Tác giả</th>
              <th class="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Ngày xuất bản</th>
              <th class="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Lượt xem</th>
              <th class="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Trạng thái</th>
              <th class="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider text-right">Thao tác</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr *ngFor="let post of posts" class="hover:bg-gray-50 transition-colors">
              <td class="px-6 py-4">
                <div class="font-semibold text-gray-900 line-clamp-1">{{ post.title }}</div>
                <div class="text-xs text-gray-500">/{{ post.slug }}</div>
              </td>
              <td class="px-6 py-4 text-sm text-gray-600">{{ post.authorName }}</td>
              <td class="px-6 py-4 text-sm text-gray-600">{{ post.publishedAt ? (post.publishedAt | date:'dd/MM/yyyy HH:mm') : '-' }}</td>
              <td class="px-6 py-4 text-sm text-gray-600">{{ post.viewCount }}</td>
              <td class="px-6 py-4">
                <span class="px-2 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider"
                      [ngClass]="statusClass(post.status)">
                  {{ post.status }}
                </span>
              </td>
              <td class="px-6 py-4 text-right space-x-3">
                <a [routerLink]="[post.id, 'edit']" class="text-indigo-600 hover:text-indigo-900 text-sm font-medium">Sửa</a>
                <button *ngIf="post.status !== 'PUBLISHED'" (click)="publish(post.id)" class="text-green-600 hover:text-green-900 text-sm font-medium">Xuất bản</button>
                <button *ngIf="post.status === 'PUBLISHED'" (click)="archive(post.id)" class="text-amber-600 hover:text-amber-900 text-sm font-medium">Lưu trữ</button>
                <button (click)="remove(post.id)" class="text-red-600 hover:text-red-900 text-sm font-medium">Xoá</button>
              </td>
            </tr>
            <tr *ngIf="posts.length === 0 && !loading">
              <td colspan="6" class="px-6 py-10 text-center text-gray-500">Chưa có bài viết nào.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `
})
export class BlogListComponent implements OnInit {
  posts: any[] = [];
  loading = false;
  statusFilter: '' | 'DRAFT' | 'PUBLISHED' | 'ARCHIVED' = '';

  constructor(private adminService: AdminService, private notify: NotificationService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.adminService.getBlogs(0, 50, this.statusFilter || undefined).subscribe({
      next: (res) => {
        if (res.success) {
          this.posts = res.data.content || [];
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  setFilter(status: '' | 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'): void {
    this.statusFilter = status;
    this.load();
  }

  publish(id: number): void {
    this.adminService.publishBlog(id).subscribe({
      next: (res) => {
        if (res.success) {
          this.notify.success('Đã xuất bản bài viết');
          this.load();
        }
      }
    });
  }

  archive(id: number): void {
    this.adminService.archiveBlog(id).subscribe({
      next: (res) => {
        if (res.success) {
          this.notify.success('Đã lưu trữ bài viết');
          this.load();
        }
      }
    });
  }

  remove(id: number): void {
    if (!confirm('Bạn có chắc chắn muốn xoá bài viết này?')) {
      return;
    }

    this.adminService.deleteBlog(id).subscribe({
      next: (res) => {
        if (res.success) {
          this.notify.success('Đã xoá bài viết');
          this.load();
        }
      }
    });
  }

  statusClass(status: string): string {
    if (status === 'PUBLISHED') {
      return 'bg-green-100 text-green-700';
    }
    if (status === 'ARCHIVED') {
      return 'bg-amber-100 text-amber-700';
    }
    return 'bg-gray-100 text-gray-700';
  }
}
