import { useAuthStore } from '@/stores/useAuthStores'
import { Navigate, Outlet } from 'react-router';

const ProtectedRoute = () => {
    const { accessToken } = useAuthStore();
    if (!accessToken) {
        return (
            <Navigate to="/signin" replace />
        )
    }
    return (
        <Outlet></Outlet>
    )
}

export default ProtectedRoute
