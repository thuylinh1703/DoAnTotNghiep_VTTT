import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { BlogService } from '../../../../core/services/blog.service';
import { BlogPost } from '../../../../core/models/blog';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'app-blog-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './blog-list.component.html',
  styleUrl: './blog-list.component.scss'
})
export class BlogListComponent implements OnInit {
  posts: BlogPost[] = [];
  page = 0;
  size = 9;
  hasMore = false;
  loading = false;

  constructor(private blogService: BlogService) {}

  ngOnInit(): void {
    this.loadPosts();
  }

  getImageUrl(path: string): string {
    if (!path) return '';
    if (path.startsWith('http') || path.startsWith('data:')) return path;
    const baseUrl = environment.apiUrl.replace('/api', '');
    return baseUrl + (path.startsWith('/') ? '' : '/') + path;
  }

  loadPosts(): void {
    if (this.loading) {
      return;
    }

    this.loading = true;
    this.blogService.getPosts(this.page, this.size).subscribe({
      next: (response) => {
        if (response.success) {
          const pageData = response.data;
          this.posts = [...this.posts, ...pageData.content];
          this.hasMore = !pageData.last;
          this.page = pageData.number + 1;
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
