export interface Review {
  id: number;
  userId: number;
  userName: string;
  productId: number;
  productName: string;
  orderItemId?: number;
  verifiedPurchase: boolean;
  rating: number;
  comment: string;
  images?: string[];
  createdAt: string;
  active?: boolean;
}

export interface ReviewSummary {
  averageRating: number;
  totalCount: number;
  distribution: Record<string, number>;
}

export interface ReviewEligibility {
  eligible: boolean;
  orderItemId?: number;
  reason?: 'NOT_PURCHASED' | 'ALREADY_REVIEWED' | 'NOT_DELIVERED' | string;
}
