export interface Order {
    id: number;
    orderCode: string;
    receiverName: string;
    receiverPhone: string;
    receiverAddress: string;
    paymentMethod: string;
    status: string;
    totalAmount: number;
    note?: string;
    items: OrderItem[];
    createdAt: string;
}

export interface OrderItem {
    id: number;
    productId: number;
    productName: string;
    price: number;
    quantity: number;
    subtotal: number;
}
