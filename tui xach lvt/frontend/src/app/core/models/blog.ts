export type BlogStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

export interface BlogPost {
  id: number;
  title: string;
  slug: string;
  excerpt?: string;
  content?: string;
  coverImageUrl?: string;
  authorId: number;
  authorName: string;
  status: BlogStatus;
  publishedAt?: string;
  viewCount: number;
  createdAt: string;
  updatedAt: string;
}
