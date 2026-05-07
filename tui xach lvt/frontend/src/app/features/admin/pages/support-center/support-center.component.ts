import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VoiceCallService } from '../../../../core/services/voice-call.service';
import { AdminService } from '../../../../core/services/admin.service';

@Component({
  selector: 'app-support-center',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './support-center.component.html',
  styleUrls: ['./support-center.component.scss']
})
export class SupportCenterComponent implements OnInit {
  callStatus: string = 'idle';
  recentCalls: any[] = [];
  customers: any[] = [];
  todayCalls: number = 0;
  averageCalls: number = 0;

  constructor(
    private voiceCallService: VoiceCallService,
    private adminService: AdminService
  ) {}

  ngOnInit(): void {
    this.voiceCallService.callStatus$.subscribe(status => {
      this.callStatus = status;
    });

    this.loadRecentCustomers();
    this.generateMockStats();
  }

  generateMockStats(): void {
    this.todayCalls = Math.floor(Math.random() * 15) + 5;
    this.averageCalls = Number((8 + Math.random() * 4).toFixed(1));
  }

  loadRecentCustomers(): void {
    this.adminService.getSupportCustomers().subscribe({
      next: (res: any) => {
        if (res.success) {
          this.customers = res.data.map((user: any) => ({
            ...user,
            isOnline: Math.random() > 0.3 // 70% chance to be "online" for the demo
          }));
        }
      }
    });
  }

  startCall(email: string): void {
    this.voiceCallService.startCall(email);
  }
}
