import { z } from "zod";
import type { User } from "./user";
import type { ApiResponse } from "./api";

export const signUpSchema = z.object({
    displayName: z.string().min(1, "Họ và tên bắt buộc phải có"),
    username: z.string().min(1, "Tên đăng nhập bắt buộc phải có"),
    email: z.string().email("Email không phù hợp").min(1, "Email bắt buộc phải có"),
    password: z.string().min(6, "Mật khẩu phải có ít nhất 6 ký tự")
});

export type SignUpRequest = z.infer<typeof signUpSchema>;

export const signInSchema = z.object({
    username: z.string().min(1, "Tên đăng nhập không được để trống"),
    password: z.string().min(1, "Mật khẩu không được để trống")
});

export type SignInRequest = z.infer<typeof signInSchema>;

export interface RefreshTokenRequest {
    accessToken: string | null,
    refreshToken: string | null
}

export interface LogoutRequest {
    accessToken: string | null,
    refreshToken: string | null
}

export interface AuthResult {
    accessToken: string;
    refreshToken: string;
    user: User;
}

export interface RefreshResult {
    accessToken: string;
    refreshToken: string;
}

// Sau đó bọc chúng vào ApiResponse
export type SigninResponse = ApiResponse<AuthResult>;
export type RefreshTokenResponse = ApiResponse<RefreshResult>;

