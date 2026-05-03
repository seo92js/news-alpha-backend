export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  token: string
  email: string
  nickname: string
  role: string
}

export interface SignupRequest {
  email: string
  password: string
  nickname: string
}

export interface MemberInfo {
  email: string
  role: string
}
