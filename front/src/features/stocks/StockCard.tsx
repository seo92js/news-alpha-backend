import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import type { Stock } from '../../types/stock'
import type { StockReport } from '../../types/report'
import ReportDateLabel from '../../components/ui/ReportDateLabel'

interface StockCardProps {
  stock: Stock
  report?: StockReport
}

export default function StockCard({ stock, report }: StockCardProps) {
  const navigate = useNavigate()
  const [hovered, setHovered] = useState(false)

  const summary = report
    ? report.report.slice(0, 90) + (report.report.length > 90 ? '…' : '')
    : null

  return (
    <div
      role="button"
      tabIndex={0}
      onClick={() => navigate(`/stocks/${stock.id}`)}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') navigate(`/stocks/${stock.id}`)
      }}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      style={{
        background: 'var(--surface)',
        border: `1px solid ${hovered ? 'var(--accent)' : 'var(--border)'}`,
        borderRadius: '12px',
        padding: '20px',
        cursor: 'pointer',
        transition: 'border-color 150ms ease',
        outline: 'none',
      }}
    >
      {/* 상단: 종목 정보 + 시그널 배지 */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          marginBottom: '12px',
          gap: '12px',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', minWidth: 0, flexWrap: 'wrap' }}>
          <span
            style={{
              fontSize: '16px',
              fontWeight: 700,
              color: 'var(--text-primary)',
              letterSpacing: '-0.3px',
              flexShrink: 0,
            }}
          >
            {stock.name}
          </span>
          <span style={{ fontSize: '13px', color: 'var(--text-muted)', flexShrink: 0 }}>
            {stock.ticker}
          </span>
          <span
            style={{
              fontSize: '11px',
              fontWeight: 600,
              color: 'var(--text-muted)',
              background: 'rgba(243,243,243,0.08)',
              border: '1px solid var(--border)',
              borderRadius: '4px',
              padding: '1px 6px',
              letterSpacing: '0.3px',
              flexShrink: 0,
            }}
          >
            {stock.market}
          </span>
        </div>

        {report && (
          <span
            style={{
              fontSize: '12px',
              fontWeight: 700,
              color: '#0F0F10',
              background: 'var(--accent)',
              borderRadius: '20px',
              padding: '2px 10px',
              flexShrink: 0,
            }}
          >
            시그널 {report.signalCount}
          </span>
        )}
      </div>

      {/* 리포트 요약 */}
      <p
        style={{
          fontSize: '13px',
          color: report ? 'var(--text-muted)' : 'rgba(243,243,243,0.3)',
          lineHeight: 1.6,
          margin: '0 0 14px',
          display: '-webkit-box',
          WebkitLineClamp: 2,
          WebkitBoxOrient: 'vertical',
          overflow: 'hidden',
          fontStyle: report ? 'normal' : 'italic',
        }}
      >
        {summary ?? '리포트 생성 대기중'}
      </p>

      {/* 하단: 날짜 + 자세히 보기 */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        {report ? (
          <ReportDateLabel date={report.reportDate} />
        ) : (
          <span style={{ fontSize: '12px', color: 'rgba(243,243,243,0.25)' }}>-</span>
        )}
        <span
          style={{
            fontSize: '13px',
            fontWeight: 600,
            color: hovered ? 'var(--accent-hover)' : 'var(--accent)',
            transition: 'color 150ms ease',
          }}
        >
          자세히 보기 →
        </span>
      </div>
    </div>
  )
}
