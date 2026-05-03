import { Navigate } from "react-router-dom";
import { useAuthStore } from "../../stores/authStore.ts";

export default function PrivateRoute({ children }: { children: React.ReactNode }) {
    const { token } = useAuthStore()
    return token ? <>{children}</> : <Navigate to="/login" replace />
}