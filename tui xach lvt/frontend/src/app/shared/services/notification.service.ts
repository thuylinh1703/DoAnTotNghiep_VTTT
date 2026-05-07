import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ToastLevel = 'success' | 'error' | 'warning' | 'info';

export interface Toast {
  id: number;
  level: ToastLevel;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private counter = 0;
  private toastsSubject = new BehaviorSubject<Toast[]>([]);
  toasts$ = this.toastsSubject.asObservable();

  success(message: string): void { this.push('success', message); }
  error(message: string): void { this.push('error', message); }
  warning(message: string): void { this.push('warning', message); }
  info(message: string): void { this.push('info', message); }

  dismiss(id: number): void {
    this.toastsSubject.next(this.toastsSubject.value.filter(t => t.id !== id));
  }

  private push(level: ToastLevel, message: string): void {
    const id = ++this.counter;
    const toast: Toast = { id, level, message };
    this.toastsSubject.next([...this.toastsSubject.value, toast]);
    setTimeout(() => this.dismiss(id), 4500);
  }
}
