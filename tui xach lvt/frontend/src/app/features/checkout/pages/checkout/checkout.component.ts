import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CartService } from '../../../../core/services/cart.service';
import { OrderService } from '../../../../core/services/order.service';
import { AuthService } from '../../../../core/services/auth.service';
import { NotificationService } from '../../../../shared/services/notification.service';
import { CouponService } from '../../../../core/services/coupon.service';
import { UserService } from '../../../../core/services/user.service';
import { CouponInputComponent } from '../../components/coupon-input/coupon-input.component';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, ReactiveFormsModule, CouponInputComponent],
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.scss'
})
export class CheckoutComponent implements OnInit {
  checkoutForm!: FormGroup;
  cart: any = null;
  loading = false;
  paymentMethod = 'COD';
  appliedCoupon: any = null;

  constructor(
    private fb: FormBuilder,
    private cartService: CartService,
    private orderService: OrderService,
    private authService: AuthService,
    private router: Router,
    private notify: NotificationService,
    private couponService: CouponService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    const user = this.authService.getUser();
    this.checkoutForm = this.fb.group({
      fullName: [user?.fullName || '', Validators.required],
      phone: [user?.phone || '', Validators.required],
      address: [user?.address || '', Validators.required],
      note: [''],
      paymentMethod: ['COD', Validators.required]
    });

    // Fetch latest profile to ensure phone/address are filled if available
    this.userService.getProfile().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.checkoutForm.patchValue({
            fullName: res.data.fullName,
            phone: res.data.phone,
            address: res.data.address
          });
        }
      }
    });

    this.cartService.cart$.subscribe(cart => {
      this.cart = cart;
      if (cart && (!cart.items || cart.items.length === 0)) {
        // this.router.navigate(['/cart']);
      }
    });

    this.couponService.appliedCoupon$.subscribe(coupon => {
      this.appliedCoupon = coupon;
    });
  }

  onSubmit(): void {
    if (this.checkoutForm.invalid || !this.cart) {
      return;
    }

    this.loading = true;

    // Pre-validate stock before creating the order. If anything is short, bounce
    // back to cart with a toast — cheaper than a failed order creation round-trip
    // and clearer UX (user sees the issue in their cart context).
    this.cartService.validateStock().subscribe({
      next: (res) => {
        if (res?.success && res.data?.allAvailable) {
          this.placeOrder();
        } else {
          const issues = res?.data?.issues ?? [];
          const first = issues[0];
          if (first) {
            this.notify.error(
              `Sản phẩm '${first.productName}' chỉ còn ${first.available} trong kho`
            );
          } else {
            this.notify.error('Một số sản phẩm trong giỏ không đủ số lượng');
          }
          this.loading = false;
          this.router.navigate(['/cart']);
        }
      },
      error: () => {
        // Validation endpoint failed; proceed anyway — server-side lock is the
        // real safety net.
        this.placeOrder();
      }
    });
  }

  private placeOrder(): void {
    const orderData = {
      receiverName: this.checkoutForm.value.fullName,
      receiverPhone: this.checkoutForm.value.phone,
      receiverAddress: this.checkoutForm.value.address,
      note: this.checkoutForm.value.note,
      paymentMethod: this.checkoutForm.value.paymentMethod,
      couponCode: this.appliedCoupon?.code || null
    };

    this.orderService.createOrder(orderData).subscribe({
      next: (response) => {
        if (response.success) {
          const order = response.data;

          if (orderData.paymentMethod === 'VNPAY') {
            this.orderService.createVNPayPayment(order.orderCode, window.location.origin + '/account/orders').subscribe({
              next: (vnpRes) => {
                if (vnpRes.success) {
                  window.location.href = vnpRes.data.paymentUrl;
                } else {
                  this.notify.error('Không thể tạo liên kết thanh toán VNPay');
                  this.router.navigate(['/account/orders']);
                }
              },
              error: () => {
                this.notify.error('Có lỗi xảy ra khi tạo liên kết thanh toán');
                this.router.navigate(['/account/orders']);
              }
            });
          } else {
            this.notify.success('Đặt hàng thành công');
            this.router.navigate(['/account/orders']);
          }

          this.cartService.clearCart().subscribe();
          this.couponService.removeCoupon();
        }
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        if (err?.status === 409) {
          // Stock error already toasted by interceptor. Send user back to cart
          // where the offending line item is now marked out-of-stock.
          this.router.navigate(['/cart']);
        } else {
          this.notify.error(err?.error?.message || 'Có lỗi xảy ra khi đặt hàng');
        }
      }
    });
  }

  getImageUrl(url: string | null): string {
    if (!url) return 'assets/images/placeholder.png';
    if (url.startsWith('http') || url.startsWith('data:')) return url;
    const baseUrl = 'http://localhost:8080';
    return `${baseUrl}${url.startsWith('/') ? '' : '/'}${url}`;
  }
}
