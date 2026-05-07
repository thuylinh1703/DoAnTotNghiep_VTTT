import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { BlogPost } from '../models/blog';

@Injectable({
  providedIn: 'root'
})
export class BlogService {
  constructor(private apiService: ApiService) {}

  getPosts(page: number = 0, size: number = 10): Observable<any> {
    return this.apiService.get('blog', { page, size });
  }

  getBySlug(slug: string): Observable<{ success: boolean; data: BlogPost }> {
    return this.apiService.get(`blog/${slug}`);
  }

  trackView(slug: string): Observable<any> {
    return this.apiService.post(`blog/${slug}/view`, {});
  }
}
