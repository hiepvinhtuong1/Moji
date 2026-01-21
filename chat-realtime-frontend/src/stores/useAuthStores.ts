import { create } from 'zustand'
import { toast } from 'sonner'
import type { AuthState } from '@/types/store'
import type { LogoutRequest, SignInRequest } from '@/types/auth';
import { authService } from '@/services/authServices';
import { persist } from 'zustand/middleware';
import { useChatStore } from './useChatStores';

export const useAuthStore = create<AuthState>()(
    persist((set, get) => ({
        accessToken: null, // Chỉ lưu trong RAM (biến mất khi F5)
        user: JSON.parse(localStorage.getItem('user') || 'null'),
        loading: false,
        isRefreshing: false,

        setAccessToken: (token) => set({ accessToken: token }),

        signIn: async (data: SignInRequest) => {
            try {
                set({ loading: true });
                const res = await authService.signInAPI(data);
                // Backend chỉ trả về accessToken và user (refreshToken nằm trong Cookie)
                const { accessToken, user } = res.data;

                set({ accessToken, user, loading: false });
                localStorage.setItem('user', JSON.stringify(user));
                useChatStore.getState().reset();
                toast.success("Đăng nhập thành công!");
            } catch (error) {
                console.log("🚀 ~ error:", error)
                set({ loading: false });
                toast.error("Đăng nhập thất bại");
            }
        },

        logout: async (data: LogoutRequest) => {
            try {
                set({ loading: true });
                await authService.logoutAPI(data);
            } finally {
                set({ accessToken: null, user: null, loading: false });
                localStorage.removeItem('user');
            }
        },

        checkAuth: async () => {
            if (get().isRefreshing) return; // Nếu đang refresh thì thoát
            try {
                set({ isRefreshing: true });
                set({ loading: true });
                // Không gửi chuỗi rỗng nếu accessToken là null
                const token = get().accessToken;
                const res = await authService.refreshTokenAPI(token ? { accessToken: token } : {} as any);
                set({ accessToken: res.data.accessToken, isRefreshing: false });
            } catch (error) {
                console.log("🚀 ~ error:", error)
                set({ accessToken: null, user: null, isRefreshing: false });
                localStorage.removeItem('user');
            } finally {
                set({ loading: false })
            }
        }
    }),
        {
            name: "auth-storage",
            // QUAN TRỌNG: Chỉ chọn lưu 'user', bỏ qua 'accessToken' để bảo mật
            partialize: (state) => ({
                user: state.user
            }),

        })
);