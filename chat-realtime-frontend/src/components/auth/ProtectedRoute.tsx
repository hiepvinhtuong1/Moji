import { useAuthStore } from '@/stores/useAuthStores'
import { Navigate, Outlet } from 'react-router';

const ProtectedRoute = () => {
    const { accessToken, user } = useAuthStore();

    // Nếu không có cả User (localStorage) lẫn AccessToken (RAM) -> Chắc chắn chưa đăng nhập
    if (!user && !accessToken) {
        return <Navigate to="/signin" replace />;
    }

    // Nếu có User nhưng chưa có AccessToken (trường hợp vừa F5 xong)
    // Ta vẫn cho phép vào trang, nhưng API sẽ được xử lý bởi Silent Refresh trong App.tsx
    return <Outlet />;
};
export default ProtectedRoute
