import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-verify-otp',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './verify-otp.component.html',
  styleUrl: './verify-otp.component.scss'
})
export class VerifyOtpComponent implements OnInit {
  verifyForm!: FormGroup;
  loading = false;
  resending = false;
  errorMessage = '';
  successMessage = '';
  email = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.email = localStorage.getItem('verify_email') || '';
    if (!this.email) {
      this.router.navigate(['/register']);
      return;
    }

    this.verifyForm = this.fb.group({
      email: [this.email, [Validators.required, Validators.email]],
      otpCode: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]]
    });
  }

  onSubmit(): void {
    if (this.verifyForm.invalid) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService.verifyOtp(this.verifyForm.value).subscribe({
      next: (response) => {
        if (response.success) {
          localStorage.removeItem('verify_email');
          this.router.navigate(['/']);
        } else {
          this.errorMessage = response.message || 'Mã OTP không chính xác';
        }
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Có lỗi xảy ra, vui lòng thử lại';
        this.loading = false;
      }
    });
  }

  resendOtp(): void {
    this.resending = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService.register({ email: this.email }).subscribe({ // Reuse register or create resendOtp in service
       next: (response) => {
         this.successMessage = 'Đã gửi lại mã OTP mới.';
         this.resending = false;
       },
       error: (err) => {
         this.errorMessage = 'Gửi lại mã OTP thất bại.';
         this.resending = false;
       }
    });
  }
}
