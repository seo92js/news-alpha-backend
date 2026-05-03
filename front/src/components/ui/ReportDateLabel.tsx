interface ReportDateLabelProps {
  date: string
  onDateChange?: (date: string) => void
}

function formatReportDate(dateStr: string): string {
  const parts = dateStr.split('-')
  if (parts.length !== 3) return dateStr
  const year = parts[0]
  const month = String(Number(parts[1]))
  const day = String(Number(parts[2]))
  return `${year}년 ${month}월 ${day}일 리포트`
}

export default function ReportDateLabel({ date }: ReportDateLabelProps) {
  return (
    <span
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: '5px',
        fontSize: '13px',
        color: 'var(--text-muted)',
        userSelect: 'none',
      }}
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="14"
        height="14"
        viewBox="0 0 24 24"
        fill="none"
        stroke="var(--accent)"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
        aria-hidden="true"
        style={{ flexShrink: 0 }}
      >
        <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
        <line x1="16" y1="2" x2="16" y2="6" />
        <line x1="8" y1="2" x2="8" y2="6" />
        <line x1="3" y1="10" x2="21" y2="10" />
      </svg>
      <span style={{ color: 'var(--accent)', fontWeight: 500 }}>
        {formatReportDate(date)}
      </span>
    </span>
  )
}
