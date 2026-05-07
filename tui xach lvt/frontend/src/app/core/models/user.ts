export interface User {
    id?: number;
    fullName: string;
    email: string;
    phone?: string;
    address?: string;
    role: 'USER' | 'ADMIN';
    token?: string;
}
