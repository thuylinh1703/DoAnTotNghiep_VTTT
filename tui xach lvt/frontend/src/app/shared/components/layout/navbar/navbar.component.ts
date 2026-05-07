import { Component, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, TranslateModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent {
  isHidden = false;
  isScrolled = false;
  lastScrollTop = 0;

  constructor(public authService: AuthService, private router: Router) {}

  @HostListener('window:scroll', [])
  onWindowScroll() {
    const st = window.pageYOffset || document.documentElement.scrollTop;
    
    // Scrolled state for background
    this.isScrolled = st > 50;

    // Show/Hide logic
    if (st > this.lastScrollTop && st > 100) {
      this.isHidden = true;
    } else {
      this.isHidden = false;
    }
    this.lastScrollTop = st <= 0 ? 0 : st;
  }

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  isAdmin(): boolean {
    const user = this.authService.getUser();
    return user && user.role === 'ADMIN';
  }

  onSearch(event: any): void {
    const keyword = event.target.value;
    if (keyword && keyword.trim() !== '') {
      this.router.navigate(['/products'], { queryParams: { keyword: keyword.trim() } });
    }
  }

  logout(): void {
    this.authService.logout();
  }
}
