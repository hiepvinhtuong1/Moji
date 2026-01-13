import { create } from 'zustand'
import { toast } from 'sonner'
import type { AuthState } from '@/types/store'
import type { LogoutRequest, RefreshTokenRequest, SignInRequest, SignUpRequest } from '@/types/auth';
import { authService } from '@/services/authServices';

export const useAuthStore = create<AuthState>((set, get) => ({
    accessToken: localStorage.getItem('accessToken'),
    refreshToken: localStorage.getItem('refreshToken'),
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    loading: false,
    setAccessToken: (token) => {
        set({ accessToken: token });
        localStorage.setItem('accessToken', token);
    },
    setRefreshToken: (token) => {
        set({ refreshToken: token });
        localStorage.setItem('refreshToken', token);
    },
    signUp: async (data: SignUpRequest) => {
        try {
            set({ loading: true })
            await authService.signUpAPI(data);
            toast("Đăng ký thành công")
        } catch (error) {
            console.error(error);
            toast.error("Đăng ký không thành công");
        } finally {
            set({ loading: false })
        }
    },

    signIn: async (data: SignInRequest) => {
        try {
            set({ loading: true });
            const res = await authService.signInAPI(data);
            const { accessToken, refreshToken, user } = res.data;

            set({ accessToken, refreshToken, user, loading: false });

            localStorage.setItem('accessToken', accessToken);
            localStorage.setItem('refreshToken', refreshToken);
            localStorage.setItem('user', JSON.stringify(user));
            toast.success("Đăng nhập thành công!");
        } catch (error) {
            console.log("🚀 ~ error:", error)
            set({ loading: false });
            toast.error("Đăng nhập không thành công");
        }
    },

    refresh: async (data: RefreshTokenRequest) => {
        try {
            set({ loading: true })

            const res = await authService.refreshTokenAPI(data);
            const { accessToken, refreshToken } = res.data;
            set({ accessToken, refreshToken, loading: false });
            localStorage.setItem('accessToken', accessToken);
            localStorage.setItem('refreshToken', refreshToken);
        } catch (error) {
            console.error("Refresh token expired/invalid:", error);

            // Gọi hàm logout đã định nghĩa bên dưới thông qua get()
            // Bạn cần truyền đúng LogoutRequest nếu API logout yêu cầu
            const { refreshToken, accessToken } = get();
            const data: LogoutRequest = {
                accessToken: accessToken,
                refreshToken: refreshToken
            }
            get().logout(data);

            toast.error("Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.");
        } finally {
            set({ loading: false });
        }
    },
    logout: async (data: LogoutRequest) => {
        try {
            set({ loading: true })
            await authService.logoutAPI(data);
            set({ accessToken: null, refreshToken: null, user: null });
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            localStorage.removeItem('user');
        } catch (error) {
            console.log("🚀 ~ error:", error)
            set({ accessToken: null, refreshToken: null, user: null });
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            localStorage.removeItem('user');
            set({ loading: false });
            toast.error("Bạn đã hết thời gian truy cập. Hãy đăng nhập lại");
        }
    }
}))