import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useNavigate, Link } from 'react-router-dom'
import axios from 'axios'

import Card from '../../components/ui/Card'
import Input from '../../components/ui/Input'
import Button from '../../components/ui/Button'
import { login } from '../../api/auth'
import { useAuthStore } from '../../stores/authStore'

const schema = z.object({
  email: z.string().email('올바른 이메일 형식이 아닙니다'),
  password: z.string().min(1, '비밀번호를 입력해주세요'),
})

type FormValues = z.infer<typeof schema>

const GoogleIcon = () => (
  <svg width="18" height="18" viewBox="0 0 24 24">
    <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
    <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
    <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z"/>
    <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
  </svg>
)

export default function LoginPage() {
  const navigate = useNavigate()
  const setAuth = useAuthStore((s) => s.setAuth)
  const [serverError, setServerError] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
  })

  const onSubmit = async (data: FormValues) => {
    setServerError('')
    setIsLoading(true)
    try {
      const res = await login(data)
      setAuth(res.token, { email: res.email, role: res.role })
      navigate('/')
    } catch (err) {
      if (axios.isAxiosError(err)) {
        const msg = (err.response?.data as { message?: string })?.message
        setServerError(msg ?? '오류가 발생했습니다')
      } else {
        setServerError('오류가 발생했습니다')
      }
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div
      style={{
        minHeight: '100vh',
        backgroundColor: 'var(--bg)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '24px',
      }}
    >
      <div
        style={{
          maxWidth: '400px',
          width: '100%',
          animation: 'fadeInUp 300ms ease-out',
        }}
      >
        <Card>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
            {/* Header */}
            <div style={{ textAlign: 'center' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', marginBottom: '8px' }}>
                <span style={{ fontSize: '22px', color: 'var(--text-primary)', fontWeight: 700, letterSpacing: '-0.5px' }}>News</span>
                <span style={{
                  fontSize: '11px', fontWeight: 600, color: 'var(--bg)',
                  background: 'var(--accent)', borderRadius: '4px', padding: '2px 6px', letterSpacing: '1px',
                }}>ALPHA</span>
              </div>
              <p style={{ fontSize: '14px', color: 'var(--text-muted)' }}>
                만나서 반갑습니다
              </p>
            </div>

            {/* Form */}
            <form
              onSubmit={handleSubmit(onSubmit)}
              style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}
            >
              <Input
                label="이메일"
                type="email"
                error={errors.email?.message}
                {...register('email')}
              />
              <Input
                label="비밀번호"
                type="password"
                error={errors.password?.message}
                {...register('password')}
              />

              {serverError && (
                <p style={{ fontSize: '14px', color: 'var(--error)', margin: 0 }}>
                  {serverError}
                </p>
              )}

              <Button type="submit" fullWidth loading={isLoading}>
                로그인
              </Button>
            </form>

            {/* Divider */}
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '12px',
              }}
            >
              <div style={{ flex: 1, height: '1px', backgroundColor: 'var(--border)' }} />
              <span style={{ fontSize: '13px', color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>
                또는
              </span>
              <div style={{ flex: 1, height: '1px', backgroundColor: 'var(--border)' }} />
            </div>

            {/* Google login */}
            <Button
              type="button"
              variant="secondary"
              fullWidth
              disabled
              style={{ gap: '10px' }}
            >
              <GoogleIcon />
              Google로 계속하기
            </Button>

            {/* Footer */}
            <p
              style={{
                textAlign: 'center',
                fontSize: '14px',
                color: 'var(--text-muted)',
              }}
            >
              계정이 없으신가요?{' '}
              <Link
                to="/register"
                style={{ color: 'var(--accent)', textDecoration: 'none', fontWeight: 600 }}
              >
                회원가입
              </Link>
            </p>
          </div>
        </Card>
      </div>
    </div>
  )
}
