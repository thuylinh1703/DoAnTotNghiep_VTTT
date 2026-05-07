#!/usr/bin/env pwsh

# Generate Models
npx -y @angular/cli@17 g interface core/models/product
npx -y @angular/cli@17 g interface core/models/category
npx -y @angular/cli@17 g interface core/models/order
npx -y @angular/cli@17 g interface core/models/user

# Generate Shared Components
npx -y @angular/cli@17 g c shared/components/layout/navbar --standalone
npx -y @angular/cli@17 g c shared/components/layout/footer --standalone

# Generate Features Components
# Home
npx -y @angular/cli@17 g c features/home/components/banner --standalone
npx -y @angular/cli@17 g c features/home/pages/home --standalone

# Auth
npx -y @angular/cli@17 g c features/auth/pages/login --standalone
npx -y @angular/cli@17 g c features/auth/pages/register --standalone
npx -y @angular/cli@17 g c features/auth/pages/forgot-password --standalone

# Product
npx -y @angular/cli@17 g c features/product/pages/product-list --standalone
npx -y @angular/cli@17 g c features/product/components/product-card --standalone
npx -y @angular/cli@17 g c features/product/components/filter-sidebar --standalone
npx -y @angular/cli@17 g c features/product/pages/product-detail --standalone

# Review
npx -y @angular/cli@17 g c features/review/components/review-list --standalone
npx -y @angular/cli@17 g c features/review/components/review-form --standalone

# Cart & Checkout
npx -y @angular/cli@17 g c features/cart/pages/cart --standalone
npx -y @angular/cli@17 g c features/cart/components/cart-item --standalone
npx -y @angular/cli@17 g c features/checkout/pages/checkout --standalone

# Account
npx -y @angular/cli@17 g c features/account/pages/profile --standalone
npx -y @angular/cli@17 g c features/account/pages/order-history --standalone
npx -y @angular/cli@17 g c features/account/pages/order-detail --standalone

# Admin
npx -y @angular/cli@17 g c features/admin/pages/dashboard --standalone
npx -y @angular/cli@17 g c features/admin/pages/product-management --standalone
npx -y @angular/cli@17 g c features/admin/pages/category-management --standalone
npx -y @angular/cli@17 g c features/admin/pages/order-management --standalone
npx -y @angular/cli@17 g c features/admin/pages/user-management --standalone
npx -y @angular/cli@17 g c features/admin/pages/review-management --standalone
