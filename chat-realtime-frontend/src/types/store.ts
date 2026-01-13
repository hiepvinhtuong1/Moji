import type { LogoutRequest, RefreshTokenRequest, SignInRequest, SignUpRequest } from "./auth";
import type { User } from "./user";

export interface AuthState {
    accessToken: string | null;
    refreshToken: string | null;
    user: User | null,
    loading: boolean;
    setAccessToken: (token: string) => void;
    setRefreshToken: (token: string) => void;
    signUp: (
        data: SignUpRequest
    ) => Promise<void>

    signIn: (
        data: SignInRequest
    ) => Promise<void>

    refresh: (
        data: RefreshTokenRequest
    ) => Promise<void>

    logout: (
        data: LogoutRequest
    ) => Promise<void>
}