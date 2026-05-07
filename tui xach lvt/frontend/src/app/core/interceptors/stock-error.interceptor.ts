import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { NotificationService } from '../../shared/services/notification.service';

/**
 * Surfaces 409 INSUFFICIENT_STOCK errors (emitted by the backend when a cart
 * add/update or order placement runs out of stock) as a toast. The error is
 * still re-thrown so caller-specific handling (revert optimistic UI, scroll to
 * cart item) can run.
 */
export const stockErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const notify = inject(NotificationService);
  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 409) {
        const body: any = err.error;
        const code = body?.data?.code;
        const msg = body?.message;
        if (code === 'INSUFFICIENT_STOCK' && msg) {
          notify.error(msg);
        }
      }
      return throwError(() => err);
    })
  );
};
