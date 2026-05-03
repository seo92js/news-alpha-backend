import api from './axios'
import type { LoginRequest, LoginResponse, SignupRequest, MemberInfo } from '../types/auth'

export const login = (data: LoginRequest) =>
  api.post<LoginResponse>('/auth/login', data).then((r) => r.data)

export const signup = (data: SignupRequest) =>
  api.post('/auth/signup', data).then((r) => r.data)

export const getMe = () =>
  api.get<MemberInfo>('/me').then((r) => r.data)
