import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AdminService } from '../../../../core/services/admin.service';
import { forkJoin, of } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'app-banner-management',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './banner-management.component.html',
  styleUrl: './banner-management.component.scss'
})
export class BannerManagementComponent implements OnInit {
  banners: any[] = [];
  loading = false;
  showModal = false;
  editMode = false;
  currentBannerId: number | null = null;
  bannerForm: FormGroup;

  selectedMainFile: File | null = null;
  selectedSubFile: File | null = null;
  mainPreview: string | null = null;
  subPreview: string | null = null;

  constructor(
    private adminService: AdminService,
    private fb: FormBuilder
  ) {
    this.bannerForm = this.fb.group({
      title: ['', Validators.required],
      imageUrl: [''],
      subImageUrl: [''],
      linkUrl: [''],
      displayOrder: [0, Validators.required],
      active: [true]
    });
  }

  ngOnInit(): void {
    this.loadBanners();
  }

  getImageUrl(path: string): string {
    if (!path) return '';
    if (path.startsWith('http') || path.startsWith('data:')) return path;
    const baseUrl = environment.apiUrl.replace('/api', '');
    return baseUrl + (path.startsWith('/') ? '' : '/') + path;
  }

  loadBanners(): void {
    this.loading = true;
    this.adminService.getBanners().subscribe({
      next: (response) => {
        if (response.success) {
          this.banners = response.data;
        }
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  openAddModal(): void {
    this.editMode = false;
    this.currentBannerId = null;
    this.bannerForm.reset({
      displayOrder: 0,
      active: true
    });
    this.selectedMainFile = null;
    this.selectedSubFile = null;
    this.mainPreview = null;
    this.subPreview = null;
    this.showModal = true;
  }

  openEditModal(banner: any): void {
    this.editMode = true;
    this.currentBannerId = banner.id;
    this.bannerForm.patchValue({
      title: banner.title,
      imageUrl: banner.imageUrl,
      subImageUrl: banner.subImageUrl,
      linkUrl: banner.linkUrl,
      displayOrder: banner.displayOrder,
      active: banner.active
    });
    this.mainPreview = banner.imageUrl;
    this.subPreview = banner.subImageUrl;
    this.selectedMainFile = null;
    this.selectedSubFile = null;
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  onMainFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedMainFile = file;
      const reader = new FileReader();
      reader.onload = (e: any) => this.mainPreview = e.target.result;
      reader.readAsDataURL(file);
    }
  }

  onSubFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedSubFile = file;
      const reader = new FileReader();
      reader.onload = (e: any) => this.subPreview = e.target.result;
      reader.readAsDataURL(file);
    }
  }

  onSubmit(): void {
    if (this.bannerForm.invalid) return;

    this.loading = true;

    const formData = new FormData();
    const bannerData = { ...this.bannerForm.value };
    
    formData.append('banner', new Blob([JSON.stringify(bannerData)], { type: 'application/json' }));

    if (this.selectedMainFile) {
      formData.append('image', this.selectedMainFile);
    }
    if (this.selectedSubFile) {
      formData.append('subImage', this.selectedSubFile);
    }

    const request$ = this.editMode && this.currentBannerId
      ? this.adminService.updateBanner(this.currentBannerId, formData)
      : this.adminService.createBanner(formData);

    request$.subscribe({
      next: (response) => {
        if (response.success) {
          this.loadBanners();
          this.closeModal();
        }
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  deleteBanner(id: number): void {
    if (!confirm('Bạn có chắc muốn xóa banner này?')) return;
    this.adminService.deleteBanner(id).subscribe({
      next: (response) => {
        if (response.success) {
          this.loadBanners();
        }
      }
    });
  }
}
