import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-toast-host',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="fixed top-4 right-4 z-[9999] flex flex-col gap-2 pointer-events-none">
      <div *ngFor="let t of (notify.toasts$ | async)"
           (click)="notify.dismiss(t.id)"
           class="pointer-events-auto cursor-pointer min-w-[280px] max-w-[360px] px-4 py-3 rounded-xl shadow-[0_8px_30px_rgba(0,0,0,0.12)] text-[14px] leading-[1.4] backdrop-blur-xl"
           [ngClass]="{
             'bg-[#ff3b30]/95 text-white': t.level === 'error',
             'bg-[#34c759]/95 text-white': t.level === 'success',
             'bg-[#ff9500]/95 text-white': t.level === 'warning',
             'bg-[#1d1d1f]/95 text-white': t.level === 'info'
           }">
        {{ t.message }}
      </div>
    </div>
  `
})
export class ToastHostComponent {
  constructor(public notify: NotificationService) {}
}
