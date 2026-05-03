import { useEffect } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import LoginPage from './pages/auth/LoginPage'
import RegisterPage from './pages/auth/RegisterPage'
import HomePage from './pages/home/HomePage'
import StockDetailPage from './pages/stocks/StockDetailPage'
import { getMe } from './api/auth'
import { useAuthStore } from './stores/authStore'
import PrivateRoute from "./components/auth/PrivateRoute.tsx";
import { useState } from "react";

function App() {
    const { token, setAuth } = useAuthStore()
    const [ready, setReady] = useState(false)

    useEffect(() => {
        if (token) {
            getMe()
                .then(user => setAuth(token, user))
                .finally(() => setReady(true))
        } else {
            setReady(true)
        }
    }, [])

    if (!ready) return null

    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<PrivateRoute><HomePage /></PrivateRoute>} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/stocks/:id" element={<PrivateRoute><StockDetailPage /></PrivateRoute>} />
                <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
        </BrowserRouter>
    )
}

export default App