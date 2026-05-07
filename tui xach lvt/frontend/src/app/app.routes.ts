import { Routes } from '@angular/router';

// Home
import { HomeComponent } from './features/home/pages/home/home.component';

// Auth
import { LoginComponent } from './features/auth/pages/login/login.component';
import { RegisterComponent } from './features/auth/pages/register/register.component';
import { ForgotPasswordComponent } from './features/auth/pages/forgot-password/forgot-password.component';
import { VerifyOtpComponent } from './features/auth/pages/verify-otp/verify-otp.component';

// Product
import { ProductListComponent } from './features/product/pages/product-list/product-list.component';
import { ProductDetailComponent } from './features/product/pages/product-detail/product-detail.component';
import { BlogListComponent } from './features/blog/pages/blog-list/blog-list.component';
import { BlogDetailComponent } from './features/blog/pages/blog-detail/blog-detail.component';
import { PromotionListComponent } from './features/promotion/pages/promotion-list/promotion-list.component';

// Cart & Checkout
import { CartComponent } from './features/cart/pages/cart/cart.component';
import { CheckoutComponent } from './features/checkout/pages/checkout/checkout.component';

// Account
import { ProfileComponent } from './features/account/pages/profile/profile.component';
import { OrderHistoryComponent } from './features/account/pages/order-history/order-history.component';
import { OrderDetailComponent } from './features/account/pages/order-detail/order-detail.component';

// Admin
import { AdminLayoutComponent } from './features/admin/components/admin-layout/admin-layout.component';
import { DashboardComponent } from './features/admin/pages/dashboard/dashboard.component';
import { ProductManagementComponent } from './features/admin/pages/product-management/product-management.component';
import { CategoryManagementComponent } from './features/admin/pages/category-management/category-management.component';
import { OrderManagementComponent } from './features/admin/pages/order-management/order-management.component';
import { UserManagementComponent } from './features/admin/pages/user-management/user-management.component';
import { ReviewManagementComponent } from './features/admin/pages/review-management/review-management.component';
import { SupportCenterComponent } from './features/admin/pages/support-center/support-center.component';
import { BannerManagementComponent } from './features/admin/pages/banner-management/banner-management.component';
import { CouponListComponent } from './features/admin/pages/coupon-management/coupon-list.component';
import { CouponFormComponent } from './features/admin/pages/coupon-management/coupon-form.component';
import { BlogListComponent as AdminBlogListComponent } from './features/admin/pages/blog-management/blog-list.component';
import { BlogFormComponent } from './features/admin/pages/blog-management/blog-form.component';
import { RevenueDashboardComponent } from './features/admin/pages/reports/revenue-dashboard.component';
import { ProductPerformanceComponent } from './features/admin/pages/reports/product-performance.component';

import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
    // Public Routes
    { path: '', component: HomeComponent },
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'verify-otp', component: VerifyOtpComponent },
    { path: 'forgot-password', component: ForgotPasswordComponent },
    { path: 'products', component: ProductListComponent },
    { path: 'products/:id', component: ProductDetailComponent },
    { path: 'blog', component: BlogListComponent },
    { path: 'blog/:slug', component: BlogDetailComponent },
    { path: 'promotions', component: PromotionListComponent },

    // User Authenticated Routes
    { path: 'cart', component: CartComponent, canActivate: [authGuard] },
    { path: 'checkout', component: CheckoutComponent, canActivate: [authGuard] },
    { path: 'account/profile', component: ProfileComponent, canActivate: [authGuard] },
    { path: 'account/orders', component: OrderHistoryComponent, canActivate: [authGuard] },
    { path: 'account/orders/:id', component: OrderDetailComponent, canActivate: [authGuard] },

    // Admin Routes
    {
        path: 'admin',
        component: AdminLayoutComponent,
        canActivate: [adminGuard],
        children: [
            { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
            { path: 'dashboard', component: DashboardComponent },
            { path: 'products', component: ProductManagementComponent },
            { path: 'categories', component: CategoryManagementComponent },
            { path: 'orders', component: OrderManagementComponent },
            { path: 'users', component: UserManagementComponent },
            { path: 'reviews', component: ReviewManagementComponent },
            { path: 'support-center', component: SupportCenterComponent },
            { path: 'banners', component: BannerManagementComponent },
            { path: 'coupons', component: CouponListComponent },
            { path: 'coupons/new', component: CouponFormComponent },
            { path: 'coupons/:id/edit', component: CouponFormComponent },
            { path: 'blog', component: AdminBlogListComponent },
            { path: 'blog/new', component: BlogFormComponent },
            { path: 'blog/:id/edit', component: BlogFormComponent },
            { path: 'reports', component: RevenueDashboardComponent },
            { path: 'reports/products', component: ProductPerformanceComponent }
        ]
    },

    // Fallback
    { path: '**', redirectTo: '' }
];
