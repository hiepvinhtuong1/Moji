import { useAuthStore } from '@/stores/useAuthStores';
import type { RefreshTokenResponse } from '@/types/auth';
import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { toast } from 'sonner';

declare module 'axios' {
    interface InternalAxiosRequestConfig {
        _retry?: boolean;
    }
}

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

// 1. Instance dành cho các API cần Token (Private)
const authorizedAxiosInstance = axios.create({
    baseURL: BASE_URL,
    timeout: 1000 * 60 * 10, // 10 phút
});

// 2. Instance dành cho các API KHÔNG cần Token (Public: Login, Register, Refresh)
// Việc tách riêng giúp tránh gửi nhầm Access Token hết hạn lên API Refresh
export const publicAxiosInstance = axios.create({
    baseURL: BASE_URL,
    timeout: 1000 * 60,
});

// Biến quản lý việc gọi Refresh Token duy nhất một lần (Tránh race condition)
let refreshTokenPromise: Promise<RefreshTokenResponse> | null = null;

// --- REQUEST INTERCEPTOR (Chỉ cho Private Instance) ---
authorizedAxiosInstance.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const accessToken = useAuthStore.getState().accessToken;

        // Chỉ đính kèm Token nếu có trong store
        if (accessToken && config.headers) {
            config.headers.Authorization = `Bearer ${accessToken}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// --- RESPONSE INTERCEPTOR (Chỉ cho Private Instance) ---
authorizedAxiosInstance.interceptors.response.use(
    (response) => response,
    async (error: AxiosError<{ message: string }>) => {
        const originalRequest = error.config;
        if (!originalRequest) return Promise.reject(error);

        const status = error.response?.status;

        // Trường hợp 401: Token không hợp lệ hoặc bị từ chối thẳng
        if (status === 401) {
            const { refreshToken, accessToken } = useAuthStore.getState();
            useAuthStore.getState().logout({ accessToken, refreshToken });
            toast.error("Yêu cầu xác thực không hợp lệ. Vui lòng đăng nhập lại.");
            return Promise.reject(error);
        }

        // Trường hợp 410: Access Token hết hạn, cần Refresh
        if (status === 410 && !originalRequest._retry) {
            originalRequest._retry = true;

            try {
                if (!refreshTokenPromise) {
                    const { refreshToken, accessToken: oldAccessToken } = useAuthStore.getState();

                    if (!refreshToken) throw new Error("No refresh token available");

                    // Gọi API refresh thông qua publicAxiosInstance để KHÔNG gửi kèm Access Token cũ
                    refreshTokenPromise = publicAxiosInstance.post("/auth/refresh", {
                        accessToken: oldAccessToken,
                        refreshToken: refreshToken
                    }).then(res => res.data); // .data.data tùy theo cấu trúc ApiResponse của bạn
                }

                const tokens = await refreshTokenPromise
                refreshTokenPromise = null;

                if (tokens && tokens.data.accessToken && tokens.data.refreshToken) {
                    // Cập nhật token mới vào Zustand Store
                    useAuthStore.getState().setAccessToken(tokens.data.accessToken);
                    useAuthStore.getState().setRefreshToken(tokens.data.refreshToken)
                    // Gắn token mới vào request bị lỗi ban đầu và thực hiện lại
                    originalRequest.headers.Authorization = `Bearer ${tokens.data.accessToken}`;
                    return authorizedAxiosInstance(originalRequest);
                }
            } catch (refreshError) {
                refreshTokenPromise = null;

                // Nếu refresh thất bại, xóa sạch store và đẩy ra trang login
                const { refreshToken, accessToken } = useAuthStore.getState();
                useAuthStore.getState().logout({ accessToken, refreshToken });

                toast.error("Phiên đăng nhập đã hết hạn hoàn toàn.");
                return Promise.reject(refreshError);
            }
        }

        return Promise.reject(error);
    }
);

export default authorizedAxiosInstance;