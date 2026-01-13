import { useAuthStore } from '@/stores/useAuthStores';
import axios, { AxiosError, type InternalAxiosRequestConfig, } from 'axios';
import { toast } from 'sonner';
import { authService } from './authServices';

declare module 'axios' {
    interface InternalAxiosRequestConfig {
        _retry?: boolean;
    }
}

const authorizedAxiosInstance = axios.create({
    baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
    timeout: 1000 * 60 * 10, // 10 phút
});

// Biến để quản lý việc gọi Refresh Token duy nhất một lần
let refreshTokenPromise: Promise<string | null> | null = null;

// --- REQUEST INTERCEPTOR ---
authorizedAxiosInstance.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        // Lấy token trực tiếp từ Zustand store
        const accessToken = useAuthStore.getState().accessToken;

        if (accessToken && config.headers) {
            config.headers.Authorization = `Bearer ${accessToken}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// --- RESPONSE INTERCEPTOR ---
authorizedAxiosInstance.interceptors.response.use(
    (response) => response,
    async (error: AxiosError<{ message: string }>) => {
        const originalRequest = error.config;
        if (!originalRequest) return Promise.reject(error);

        const status = error.response?.status;

        // Trường hợp 401 thật sự (Sai token, Token rác) -> Logout luôn
        if (status === 401) {
            const { refreshToken, accessToken } = useAuthStore.getState();
            useAuthStore.getState().logout({ accessToken, refreshToken });
            toast.error("Yêu cầu xác thực không hợp lệ.");
        }


        // THAY ĐỔI Ở ĐÂY: Sử dụng 410 để nhận diện Token hết hạn
        if (status === 410 && !originalRequest._retry) {
            originalRequest._retry = true;

            try {
                if (!refreshTokenPromise) {
                    const { refreshToken, accessToken: oldAccessToken } = useAuthStore.getState();
                    if (!refreshToken || !oldAccessToken) throw new Error("No tokens");

                    refreshTokenPromise = authService.refreshTokenAPI({
                        accessToken: oldAccessToken,
                        refreshToken: refreshToken
                    }).then(res => res.data.accessToken);
                }

                const newAccessToken = await refreshTokenPromise;
                refreshTokenPromise = null;

                if (newAccessToken) {
                    useAuthStore.getState().setAccessToken(newAccessToken);
                    originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
                    return authorizedAxiosInstance(originalRequest);
                }
            } catch (refreshError) {
                refreshTokenPromise = null;
                const { refreshToken, accessToken } = useAuthStore.getState();
                useAuthStore.getState().logout({ accessToken, refreshToken });
                toast.error("Phiên đăng nhập hết hạn.");
                return Promise.reject(refreshError);
            }
        }


        // ... các xử lý 403, 404 khác giữ nguyên
        return Promise.reject(error);
    }
);

export default authorizedAxiosInstance;