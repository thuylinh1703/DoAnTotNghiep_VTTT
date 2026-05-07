import { Component, OnInit } from '@angular/core';
import { Router, RouterOutlet, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from './shared/components/layout/navbar/navbar.component';
import { FooterComponent } from './shared/components/layout/footer/footer.component';
import { TranslateService } from '@ngx-translate/core';
import { VoiceCallComponent } from './shared/components/voice-call/voice-call.component';
import { ToastHostComponent } from './shared/components/toast-host/toast-host.component';
import { AuthService } from './core/services/auth.service';
import { VoiceCallService } from './core/services/voice-call.service';
import { AiChatbotComponent } from './shared/components/ai-chatbot/ai-chatbot.component';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, FooterComponent, VoiceCallComponent, ToastHostComponent, AiChatbotComponent, CommonModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  title = 'Tui Xach LVT';
  isAdminRoute = false;

  constructor(
    private translate: TranslateService,
    private authService: AuthService,
    private voiceCallService: VoiceCallService,
    private router: Router
  ) {
    this.translate.setDefaultLang('vi');
    this.translate.use('vi');
  }

  ngOnInit(): void {
    // Set initial state
    this.isAdminRoute = this.router.url.startsWith('/admin');

    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      this.isAdminRoute = event.url.startsWith('/admin');
    });
  }

  isAdmin(): boolean {
    const user = this.authService.getUser();
    return user?.role === 'ADMIN';
  }

  startCall(): void {
    if (!this.authService.isLoggedIn()) {
      alert('Vui lòng đăng nhập để gọi hỗ trợ');
      return;
    }
    this.voiceCallService.startCall('admin@tuixach.com');
  }
}
