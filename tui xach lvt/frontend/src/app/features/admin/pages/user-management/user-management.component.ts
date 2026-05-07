import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../../../core/services/admin.service';
import { VoiceCallService } from '../../../../core/services/voice-call.service';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NotificationService } from '../../../../shared/services/notification.service';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.scss'
})
export class UserManagementComponent implements OnInit {
  users: any[] = [];
  loading = false;
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;
  currentUserId: number | null = null;

  showModal = false;
  selectedUser: any = null;
  userForm: FormGroup;

  constructor(
    private adminService: AdminService,
    private voiceCallService: VoiceCallService,
    private notify: NotificationService,
    private fb: FormBuilder,
    private authService: AuthService
  ) {
    this.userForm = this.fb.group({
      fullName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: [''],
      address: ['']
    });
  }

  ngOnInit(): void {
    this.currentUserId = this.authService.getUser()?.id || null;
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.adminService.getUsers(this.currentPage, this.pageSize).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.users = response.data.content;
          this.totalPages = response.data.totalPages;
          this.totalElements = response.data.totalElements;
        }
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  toggleStatus(userId: number): void {
    this.adminService.toggleUserStatus(userId).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.notify.success('Cập nhật trạng thái khách hàng thành công');
          this.loadUsers();
        }
      }
    });
  }

  deleteUser(userId: number): void {
    if (!confirm('Bạn có chắc muốn xóa người dùng này?')) return;
    this.adminService.deleteUser(userId).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.notify.success('Đã xóa người dùng thành công');
          this.loadUsers();
        }
      }
    });
  }

  openEdit(user: any): void {
    this.selectedUser = user;
    this.userForm.patchValue({
      fullName: user.fullName,
      email: user.email,
      phone: user.phone,
      address: user.address
    });
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.selectedUser = null;
  }

  onSubmit(): void {
    if (this.userForm.invalid || !this.selectedUser) return;

    this.loading = true;
    this.adminService.updateUser(this.selectedUser.id, this.userForm.value).subscribe({
      next: (res) => {
        if (res.success) {
          this.notify.success('Cập nhật thông tin khách hàng thành công');
          this.closeModal();
          this.loadUsers();
        }
        this.loading = false;
      },
      error: (err) => {
        this.notify.error(err?.error?.message || 'Lỗi khi cập nhật thông tin');
        this.loading = false;
      }
    });
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadUsers();
  }

  getPages(): number[] {
    return Array.from({length: this.totalPages}, (_, i) => i);
  }

  callUser(email: string): void {
    if (!email) {
      alert('Không tìm thấy email người dùng');
      return;
    }
    this.voiceCallService.startCall(email);
  }
}
