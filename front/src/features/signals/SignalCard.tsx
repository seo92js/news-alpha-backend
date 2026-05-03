import type { Signal } from '../../types/signal'

interface SignalCardProps {
  signal: Signal
  compact?: boolean
}

function formatDetectedAt(iso: string): string {
  const d = new Date(iso)
  const month = d.getMonth() + 1
  const day = d.getDate()
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  return `${month}월 ${day}일 ${hh}:${mm}`
}

function ScoreBadge({ score }: { score: number }) {
  return (
    <span
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: '3px',
        fontSize: '13px',
        fontWeight: 700,
        color: 'var(--accent)',
      }}
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="12"
        height="12"
        viewBox="0 0 24 24"
        fill="var(--accent)"
        aria-hidden="true"
      >
        <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
      </svg>
      {score.toFixed(1)}
    </span>
  )
}

function KeywordTag({ keyword }: { keyword: string }) {
  return (
    <span
      style={{
        display: 'inline-block',
        padding: '2px 8px',
        borderRadius: '20px',
        backgroundColor: 'var(--accent-glow)',
        border: '1px solid rgba(245,166,35,0.35)',
        color: 'var(--accent)',
        fontSize: '11px',
        fontWeight: 600,
        letterSpacing: '0.01em',
      }}
    >
      {keyword}
    </span>
  )
}

function CompactSignalCard({ signal }: { signal: Signal }) {
  return (
    <div
      style={{
        background: 'var(--surface)',
        border: '1px solid var(--border)',
        borderRadius: '12px',
        padding: '12px 16px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        gap: '12px',
        minWidth: 0,
      }}
    >
      <span
        style={{
          fontSize: '13px',
          fontWeight: 600,
          color: 'var(--text-primary)',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          whiteSpace: 'nowrap',
          flex: 1,
        }}
        title={signal.title}
      >
        {signal.title}
      </span>
      <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexShrink: 0 }}>
        <ScoreBadge score={signal.score} />
        <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
          기사 {signal.relatedNewsCount}건
        </span>
      </div>
    </div>
  )
}

function FullSignalCard({ signal }: { signal: Signal }) {
  return (
    <div
      style={{
        background: 'var(--surface)',
        border: '1px solid var(--border)',
        borderRadius: '12px',
        padding: '20px 24px',
        display: 'flex',
        flexDirection: 'column',
        gap: '14px',
      }}
    >
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: '8px',
          flexWrap: 'wrap',
        }}
      >
        <KeywordTag keyword={signal.keyword} />
        <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
          {formatDetectedAt(signal.detectedAt)} 탐지
        </span>
      </div>

      <p
        style={{
          margin: 0,
          fontSize: '15px',
          fontWeight: 700,
          color: 'var(--text-primary)',
          lineHeight: 1.45,
        }}
      >
        {signal.title}
      </p>

      <p
        style={{
          margin: 0,
          fontSize: '13px',
          color: 'var(--text-muted)',
          lineHeight: 1.65,
        }}
      >
        {signal.summary}
      </p>

      <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
        <ScoreBadge score={signal.score} />
        <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
          기사 {signal.relatedNewsCount}건
        </span>
      </div>

      {signal.evidences.length > 0 && (
        <div
          style={{
            borderTop: '1px solid var(--border)',
            paddingTop: '12px',
            display: 'flex',
            flexDirection: 'column',
            gap: '6px',
          }}
        >
          <span
            style={{
              fontSize: '11px',
              fontWeight: 600,
              color: 'var(--text-muted)',
              textTransform: 'uppercase',
              letterSpacing: '0.06em',
              marginBottom: '2px',
            }}
          >
            근거 뉴스
          </span>
          {signal.evidences.map((ev) => (
            <a
              key={ev.newsId}
              href={ev.url ?? undefined}
              target="_blank"
              rel="noopener noreferrer"
              style={{ display: 'flex', alignItems: 'baseline', gap: '8px', textDecoration: 'none' }}
            >
              <span
                style={{
                  flexShrink: 0,
                  width: '16px',
                  height: '16px',
                  borderRadius: '50%',
                  background: 'var(--accent-glow)',
                  border: '1px solid rgba(245,166,35,0.3)',
                  fontSize: '10px',
                  fontWeight: 700,
                  color: 'var(--accent)',
                  display: 'inline-flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  lineHeight: 1,
                }}
              >
                {ev.rankOrder}
              </span>
              <span
                style={{ fontSize: '13px', color: 'var(--text-primary)', lineHeight: 1.4, transition: 'color 150ms' }}
                onMouseEnter={(e) => { (e.currentTarget as HTMLSpanElement).style.color = 'var(--accent)' }}
                onMouseLeave={(e) => { (e.currentTarget as HTMLSpanElement).style.color = 'var(--text-primary)' }}
              >
                {ev.title}
              </span>
            </a>
          ))}
        </div>
      )}
    </div>
  )
}

export default function SignalCard({ signal, compact = false }: SignalCardProps) {
  if (compact) return <CompactSignalCard signal={signal} />
  return <FullSignalCard signal={signal} />
}
