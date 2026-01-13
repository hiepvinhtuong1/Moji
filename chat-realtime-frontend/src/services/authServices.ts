import type { LogoutRequest, RefreshTokenRequest, RefreshTokenResponse, SignInRequest, SigninResponse, SignUpRequest } from '@/types/auth';
import { publicAxiosInstance } from './axios'
import type { BaseResponse } from '@/types/ApiResponse';

export const authService = {
    signUpAPI: async (data: SignUpRequest): Promise<BaseResponse> => {
        const res = await publicAxiosInstance.post("/auth/register", data)
        return res.data;
    },

    signInAPI: async (data: SignInRequest): Promise<SigninResponse> => {
        const res = await publicAxiosInstance.post("/auth/login", data);
        return res.data;
    },

    refreshTokenAPI: async (data: RefreshTokenRequest): Promise<RefreshTokenResponse> => {
        const res = await publicAxiosInstance.post("/auth/refresh", data);
        return res.data;
    },

    logoutAPI: async (data: LogoutRequest): Promise<BaseResponse> => {
        const res = await publicAxiosInstance.post("/auth/logout", data)
        return res.data;
    }
}