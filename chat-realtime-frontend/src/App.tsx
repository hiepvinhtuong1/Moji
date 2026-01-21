import { BrowserRouter, Routes, Route } from "react-router"
import SignInPage from "./pages/SignInPage"
import SignUpPage from "./pages/SignUpPage"
import ChatAppPage from "./pages/ChatAppPage"
import { Toaster } from "sonner"
import ProtectedRoute from "./components/auth/ProtectedRoute"
import { useEffect } from "react"
import { useAuthStore } from "./stores/useAuthStores"
function App() {

  const { checkAuth, user, loading } = useAuthStore();

  useEffect(() => {
    // Nếu thấy có user trong máy nhưng chưa có token (do F5) thì mới check
    if (user) {
      checkAuth();
    }
  }, []);

  if (loading && user) return <div>Loading session...</div>;

  return <>
    <Toaster />
    <BrowserRouter>
      <Routes>
        {/* public route  */}
        <Route path="/signin" element={<SignInPage />} />
        <Route path="/signup" element={<SignUpPage />} />

        {/* protected route  */}
        <Route element={<ProtectedRoute />}>
          <Route path="/" element={<ChatAppPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  </>
}

export default App
