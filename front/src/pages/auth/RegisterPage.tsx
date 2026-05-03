import { useEffect, useRef, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useNavigate, Link } from 'react-router-dom'
import axios from 'axios'

import Card from '../../components/ui/Card'
import Input from '../../components/ui/Input'
import Button from '../../components/ui/Button'
import { signup } from '../../api/auth'

const schema = z
  .object({
    nickname: z.string().min(2, '닉네임은 최소 2자 이상').max(20, '닉네임은 최대 20자'),
    email: z.string().email('올바른 이메일 형식이 아닙니다'),
    password: z.string().min(8, '비밀번호는 최소 8자 이상'),
    confirmPassword: z.string(),
  })
  .refine((d) => d.password === d.confirmPassword, {
    message: '비밀번호가 일치하지 않습니다',
    path: ['confirmPassword'],
  })

type FormValues = z.infer<typeof schema>

export default function RegisterPage() {
  const navigate = useNavigate()
  const [serverError, setServerError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  useEffect(() => {
    return () => {
      if (timerRef.current) clearTimeout(timerRef.current)
    }
  }, [])

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
  })

  const onSubmit = async (data: FormValues) => {
    setServerError('')
    setSuccessMessage('')
    setIsLoading(true)
    try {
      await signup({ email: data.email, password: data.password, nickname: data.nickname })
      setSuccessMessage('회원가입이 완료되었습니다! 로그인 페이지로 이동합니다.')
      timerRef.current = setTimeout(() => navigate('/login'), 1500)
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
                새 계정 만들기
              </p>
            </div>

            {/* Form */}
            <form
              onSubmit={handleSubmit(onSubmit)}
              style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}
            >
              {successMessage && (
                <p style={{ fontSize: '14px', color: 'var(--success)', margin: 0 }}>
                  {successMessage}
                </p>
              )}

              <Input
                label="닉네임"
                type="text"
                error={errors.nickname?.message}
                {...register('nickname')}
              />
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
              <Input
                label="비밀번호 확인"
                type="password"
                error={errors.confirmPassword?.message}
                {...register('confirmPassword')}
              />

              {serverError && (
                <p style={{ fontSize: '14px', color: 'var(--error)', margin: 0 }}>
                  {serverError}
                </p>
              )}

              <Button type="submit" fullWidth loading={isLoading}>
                회원가입
              </Button>
            </form>

            {/* Footer */}
            <p
              style={{
                textAlign: 'center',
                fontSize: '14px',
                color: 'var(--text-muted)',
              }}
            >
              이미 계정이 있으신가요?{' '}
              <Link
                to="/login"
                style={{ color: 'var(--accent)', textDecoration: 'none', fontWeight: 600 }}
              >
                로그인
              </Link>
            </p>
          </div>
        </Card>
      </div>
    </div>
  )
}
