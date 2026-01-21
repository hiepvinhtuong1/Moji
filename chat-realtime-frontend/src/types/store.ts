import type { LogoutRequest, SignInRequest } from "./auth";
import type { Conversation, Message } from "./chat";
import type { User } from "./user";

export interface AuthState {
    accessToken: string | null;
    user: User | null,
    loading: boolean;
    isRefreshing: boolean;
    setAccessToken: (token: string) => void;
    signIn: (
        data: SignInRequest
    ) => Promise<void>

    logout: (
        data: LogoutRequest
    ) => Promise<void>

    checkAuth: () => Promise<void>
}

export interface ThemeState {
    isDark: boolean;
    toggleTheme: () => void;
    setTheme: (dark: boolean) => void
}

export interface ChatState {
    conversations: Conversation[];
    messages: Record<
        string,
        {
            items: Message[];
            hasMore: boolean;
            nextCursor?: string | null;
        }
    >;
    activeConversationId: string | null;
    loading: boolean;
    reset: () => void;
    setActiveConversation: (id: string | null) => void
}