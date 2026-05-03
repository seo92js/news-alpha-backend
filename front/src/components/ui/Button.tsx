import React from 'react'

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary'
  size?: 'sm' | 'md'
  loading?: boolean
  fullWidth?: boolean
}

export default function Button({
  variant = 'primary',
  size = 'md',
  loading = false,
  fullWidth = false,
  children,
  disabled,
  style,
  ...rest
}: ButtonProps) {
  const baseStyle: React.CSSProperties = {
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: '8px',
    fontWeight: 700,
    border: 'none',
    cursor: disabled || loading ? 'not-allowed' : 'pointer',
    opacity: disabled || loading ? 0.6 : 1,
    transition: 'background-color 150ms, color 150ms, border-color 150ms',
    width: fullWidth ? '100%' : undefined,
    ...(size === 'md'
      ? { height: '48px', padding: '0 20px', fontSize: '15px' }
      : { height: '36px', padding: '0 14px', fontSize: '14px' }),
    ...(variant === 'primary'
      ? { backgroundColor: 'var(--accent)', color: '#0F0F10' }
      : {
          backgroundColor: 'transparent',
          color: 'var(--text-primary)',
          border: '1px solid var(--border)',
        }),
    ...style,
  }

  return (
    <button
      disabled={disabled || loading}
      style={baseStyle}
      onMouseEnter={(e) => {
        if (!disabled && !loading && variant === 'primary') {
          (e.currentTarget as HTMLButtonElement).style.backgroundColor = 'var(--accent-hover)'
        }
      }}
      onMouseLeave={(e) => {
        if (!disabled && !loading && variant === 'primary') {
          (e.currentTarget as HTMLButtonElement).style.backgroundColor = 'var(--accent)'
        }
      }}
      {...rest}
    >
      {loading ? (
        <svg
          style={{ width: '18px', height: '18px', animation: 'spin 1s linear infinite' }}
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
        >
          <circle
            style={{ opacity: 0.25 }}
            cx="12"
            cy="12"
            r="10"
            stroke="currentColor"
            strokeWidth="4"
          />
          <path
            style={{ opacity: 0.75 }}
            fill="currentColor"
            d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"
          />
        </svg>
      ) : (
        children
      )}
    </button>
  )
}
