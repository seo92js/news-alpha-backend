import { create } from 'zustand'
import type { MemberInfo } from '../types/auth'

interface AuthState {
  token: string | null
  user: MemberInfo | null
  setAuth: (token: string, user: MemberInfo) => void
  clearAuth: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
  token: localStorage.getItem('token'),
  user: null,
  setAuth: (token, user) => {
    localStorage.setItem('token', token)
    set({ token, user })
  },
  clearAuth: () => {
    localStorage.removeItem('token')
    set({ token: null, user: null })
  },
}))
