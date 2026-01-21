import { useAuthStore } from '@/stores/useAuthStores';
import type { RefreshTokenResponse } from '@/types/auth';
import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { toast } from 'sonner';

// Mở rộng interface của Axios để tránh lặp lại request refresh vô hạn
declare module 'axios' {
    interface InternalAxiosRequestConfig {
        _retry?: boolean;
    }
}

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

// Cấu hình dùng chung
const commonConfig = {
    baseURL: BASE_URL,
    timeout: 1000 * 60 * 5, // 5 phút
    withCredentials: true, // QUAN TRỌNG: Để trình duyệt gửi/nhận Cookie HttpOnly
};

// 1. Instance cho các API cần Token (Private)
const authorizedAxiosInstance = axios.create(commonConfig);

// 2. Instance cho các API công khai (Public: Login, Register, Refresh)
export const publicAxiosInstance = axios.create(commonConfig);

// Biến quản lý việc gọi Refresh Token duy nhất một lần (Tránh race condition khi nhiều API lỗi 410 cùng lúc)
let refreshTokenPromise: Promise<RefreshTokenResponse> | null = null;

// --- REQUEST INTERCEPTOR (Gắn Access Token vào Header) ---
authorizedAxiosInstance.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const accessToken = useAuthStore.getState().accessToken;

        if (accessToken && config.headers) {
            config.headers.Authorization = `Bearer ${accessToken}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// --- RESPONSE INTERCEPTOR (Xử lý lỗi Token hết hạn) ---
authorizedAxiosInstance.interceptors.response.use(
    (response) => response,
    async (error: AxiosError<{ message: string }>) => {
        const originalRequest = error.config;
        if (!originalRequest) return Promise.reject(error);

        const status = error.response?.status;

        // Trường hợp 401: Token không hợp lệ hoặc bị cấm (Unauthorized)
        if (status === 401) {
            useAuthStore.getState().logout({
                accessToken: useAuthStore.getState().accessToken,
                refreshToken: "" // Backend sẽ tự lấy từ Cookie nên để rỗng
            });
            toast.error("Phiên làm việc không hợp lệ. Vui lòng đăng nhập lại.");
            return Promise.reject(error);
        }

        // Trường hợp 410: Access Token hết hạn, cần gọi Refresh Token
        // Lưu ý: Backend của bạn cần trả về đúng mã 410 khi Access Token hết hạn
        if (status === 410 && !originalRequest._retry) {
            originalRequest._retry = true;

            try {
                if (!refreshTokenPromise) {
                    const oldAccessToken = useAuthStore.getState().accessToken;

                    // Gọi API refresh: TRÌNH DUYỆT TỰ GỬI REFRESH TOKEN TRONG COOKIE
                    // Ta chỉ cần gửi accessToken cũ trong body như logic Backend yêu cầu
                    refreshTokenPromise = publicAxiosInstance.post("/auth/refresh", {
                        accessToken: oldAccessToken
                    }).then(res => res.data);
                }

                const response = await refreshTokenPromise;
                refreshTokenPromise = null; // Reset sau khi thành công

                const newAccessToken = response.data.accessToken;

                if (newAccessToken) {
                    // 1. Cập nhật Token mới vào Zustand (RAM)
                    useAuthStore.getState().setAccessToken(newAccessToken);

                    // 2. Gắn token mới vào request bị lỗi ban đầu và thực hiện lại (Retry)
                    originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
                    return authorizedAxiosInstance(originalRequest);
                }
            } catch (refreshError) {
                refreshTokenPromise = null;

                // Nếu refresh thất bại (Refresh Token cũng hết hạn) -> Logout sạch sẽ
                useAuthStore.getState().logout({
                    accessToken: useAuthStore.getState().accessToken,
                    refreshToken: ""
                });

                toast.error("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
                return Promise.reject(refreshError);
            }
        }

        return Promise.reject(error);
    }
);

export default authorizedAxiosInstance;