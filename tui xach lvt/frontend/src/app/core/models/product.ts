export interface Product {
    id: number;
    name: string;
    description: string;
    price: number;
    discountPrice?: number;
    quantity: number;
    brand: string;
    categoryId: number;
    images: ProductImage[];
    reviews?: any[];
    rating?: number;
    isNew?: boolean;
}

export interface ProductImage {
    id: number;
    imageUrl: string;
}
