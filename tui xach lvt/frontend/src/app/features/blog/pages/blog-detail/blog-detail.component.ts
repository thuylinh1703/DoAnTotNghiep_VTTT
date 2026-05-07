import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DomSanitizer, SafeHtml, Title, Meta } from '@angular/platform-browser';
import { BlogService } from '../../../../core/services/blog.service';
import { BlogPost } from '../../../../core/models/blog';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'app-blog-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './blog-detail.component.html',
  styleUrl: './blog-detail.component.scss'
})
export class BlogDetailComponent implements OnInit {
  post: BlogPost | null = null;
  safeContent: SafeHtml | null = null;
  loading = false;

  constructor(
    private route: ActivatedRoute,
    private blogService: BlogService,
    private sanitizer: DomSanitizer,
    private titleService: Title,
    private metaService: Meta
  ) {}

  ngOnInit(): void {
    const slug = this.route.snapshot.paramMap.get('slug');
    if (!slug) {
      return;
    }

    this.loading = true;
    this.blogService.getBySlug(slug).subscribe({
      next: (response) => {
        if (response.success) {
          this.post = response.data;
          this.safeContent = this.sanitizer.bypassSecurityTrustHtml(this.post.content || '');
          this.setSeo(this.post);
          this.blogService.trackView(slug).subscribe({ error: () => {} });
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  getImageUrl(path: string): string {
    if (!path) return '';
    if (path.startsWith('http') || path.startsWith('data:')) return path;
    const baseUrl = environment.apiUrl.replace('/api', '');
    return baseUrl + (path.startsWith('/') ? '' : '/') + path;
  }

  private setSeo(post: BlogPost): void {
    this.titleService.setTitle(post.title + ' | LVT Blog');
    this.metaService.updateTag({ name: 'description', content: post.excerpt || post.title });
    this.metaService.updateTag({ property: 'og:title', content: post.title });
    this.metaService.updateTag({ property: 'og:description', content: post.excerpt || post.title });
    if (post.coverImageUrl) {
      this.metaService.updateTag({ property: 'og:image', content: post.coverImageUrl });
    }
  }
}
