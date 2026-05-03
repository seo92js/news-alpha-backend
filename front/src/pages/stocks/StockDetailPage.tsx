import { useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'
import Navbar from '../../components/layout/Navbar'
import SignalCard from '../../features/signals/SignalCard'
import Input from '../../components/ui/Input'
import Button from '../../components/ui/Button'
import { useStocks } from '../../features/stocks/useStocks'
import { useSignals } from '../../features/signals/useSignals'
import { useAddKeyword } from '../../features/stocks/useAddKeyword'
import type { StockKeyword } from '../../types/stock'

export default function StockDetailPage() {
  const { id } = useParams<{ id: string }>()
  const stockId = Number(id)

  const { data: stocks = [], isLoading } = useStocks()
  const { data: signals = [] } = useSignals()
  const { mutate: addKeywordMutate } = useAddKeyword(stockId)

  const [signalsOpen, setSignalsOpen] = useState(false)
  const [addedKeywords, setAddedKeywords] = useState<StockKeyword[]>([])
  const [isAdding, setIsAdding] = useState(false)
  const [newKeyword, setNewKeyword] = useState('')
  const [keywordError, setKeywordError] = useState('')

  const stock = stocks.find((s) => s.id === stockId)

  useEffect(() => {
    setAddedKeywords([])
  }, [stockId])

  if (isLoading || !stock) {
      return (
          <div style={{ minHeight: '100vh', backgroundColor: 'var(--bg)' }}>
              <Navbar />
              <p style={{ textAlign: 'center', padding: '48px', color: 'var(--text-muted)', fontSize: '14px' }}>
                  불러오는 중...
              </p>
          </div>
      )
  }

  const keywords = [...stock.keywords, ...addedKeywords]
  const relatedSignals = signals.filter(signal =>
      stock.keywords.some(kw => kw.keyword === signal.keyword)
  )

  const handleToggleAdding = () => {
    setIsAdding((prev) => !prev)
    setNewKeyword('')
    setKeywordError('')
  }

  const handleAddKeyword = () => {
    const trimmed = newKeyword.trim()
    if (!trimmed) {
      setKeywordError('키워드를 입력해주세요')
      return
    }
    if (keywords.some((kw) => kw.keyword.toLowerCase() === trimmed.toLowerCase())) {
      setKeywordError('이미 등록된 키워드입니다')
      return
    }

    const optimistic: StockKeyword = {
      id: Date.now(),
      stockId,
      keyword: trimmed,
      enabled: true,
      createdAt: new Date().toISOString(),
    }
    setAddedKeywords((prev) => [...prev, optimistic])
    setNewKeyword('')
    setKeywordError('')

    addKeywordMutate(
      { keyword: trimmed },
      {
        onSuccess: () => {
          setAddedKeywords([])
        },
        onError: () => {
          setAddedKeywords((prev) => prev.filter((kw) => kw.id !== optimistic.id))
          setKeywordError('키워드 추가에 실패했습니다')
        },
      }
    )
  }

  return (
    <div style={{ minHeight: '100vh', backgroundColor: 'var(--bg)' }}>
      <Navbar />

      <main style={{ maxWidth: '800px', margin: '0 auto', padding: '24px' }}>
        <section style={{ marginBottom: '32px' }}>
          <div
            style={{
              display: 'flex',
              alignItems: 'flex-start',
              justifyContent: 'space-between',
              flexWrap: 'wrap',
              gap: '8px',
            }}
          >
            <div>
              <h1
                style={{
                  fontSize: '26px',
                  fontWeight: 700,
                  color: 'var(--text-primary)',
                  letterSpacing: '-0.5px',
                  marginBottom: '6px',
                }}
              >
                {stock.name}
              </h1>
              <span style={{ fontSize: '14px', color: 'var(--text-muted)' }}>
                {stock.ticker} · {stock.market}
              </span>
            </div>
          </div>
        </section>

        <section style={{ marginBottom: '32px' }}>
          <h2
            style={{
              fontSize: '16px',
              fontWeight: 600,
              color: 'var(--text-primary)',
              marginBottom: '12px',
            }}
          >
            오늘의 리포트
          </h2>
          <div
            style={{
              backgroundColor: 'var(--surface)',
              border: '1px solid var(--border)',
              borderRadius: '16px',
              padding: '24px',
            }}
          >
            {/* TODO: 리포트 API 연동 시 교체 */}
            <p
              style={{
                fontSize: '14px',
                color: 'var(--text-muted)',
                textAlign: 'center',
                padding: '16px 0',
                margin: 0,
              }}
            >
              오늘 리포트가 아직 생성되지 않았습니다
            </p>
          </div>
        </section>

        <section style={{ marginBottom: '32px' }}>
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              marginBottom: signalsOpen ? '12px' : 0,
            }}
          >
            <h2
              style={{ fontSize: '16px', fontWeight: 600, color: 'var(--text-primary)', margin: 0 }}
            >
              관련 시그널 {relatedSignals.length}건
            </h2>
            <button
              onClick={() => setSignalsOpen((prev) => !prev)}
              style={{
                background: 'none',
                border: '1px solid var(--border)',
                borderRadius: '6px',
                color: 'var(--text-muted)',
                fontSize: '13px',
                padding: '4px 10px',
                cursor: 'pointer',
                transition: 'color 150ms, border-color 150ms',
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.color = 'var(--text-primary)'
                e.currentTarget.style.borderColor = 'var(--text-muted)'
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.color = 'var(--text-muted)'
                e.currentTarget.style.borderColor = 'var(--border)'
              }}
            >
              {signalsOpen ? '접기' : '펼치기'}
            </button>
          </div>

          {signalsOpen && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {relatedSignals.length === 0 ? (
                <p
                  style={{
                    fontSize: '14px',
                    color: 'var(--text-muted)',
                    textAlign: 'center',
                    padding: '24px 0',
                    margin: 0,
                  }}
                >
                  관련 시그널이 없습니다
                </p>
              ) : (
                relatedSignals.map((signal) => (
                  <SignalCard key={signal.id} signal={signal} compact={false} />
                ))
              )}
            </div>
          )}
        </section>

        <section>
          <h2
            style={{ fontSize: '16px', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '12px' }}
          >
            뉴스 수집 키워드
          </h2>

          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px', marginBottom: '12px' }}>
            {keywords.length === 0 ? (
              <p style={{ fontSize: '14px', color: 'var(--text-muted)', margin: 0 }}>
                등록된 키워드가 없습니다
              </p>
            ) : (
              keywords.map((kw) => (
                <span
                  key={kw.id}
                  style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    fontSize: '13px',
                    fontWeight: 500,
                    padding: '4px 10px',
                    borderRadius: '6px',
                    border: kw.enabled ? '1px solid var(--accent)' : '1px solid var(--border)',
                    color: kw.enabled ? 'var(--accent)' : 'var(--text-muted)',
                    backgroundColor: kw.enabled ? 'rgba(245, 166, 35, 0.08)' : 'transparent',
                    opacity: kw.enabled ? 1 : 0.5,
                  }}
                >
                  {kw.keyword}
                </span>
              ))
            )}
          </div>

          {isAdding ? (
            <div style={{ display: 'flex', gap: '8px', alignItems: 'flex-start' }}>
              <div style={{ flex: 1 }}>
                <Input
                  placeholder="새 키워드 입력"
                  value={newKeyword}
                  onChange={(e) => {
                    setNewKeyword(e.currentTarget.value)
                    if (keywordError) setKeywordError('')
                  }}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') handleAddKeyword()
                    if (e.key === 'Escape') handleToggleAdding()
                  }}
                  error={keywordError}
                  autoFocus
                />
              </div>
              <Button
                size="sm"
                onClick={handleAddKeyword}
                style={{ marginTop: keywordError ? '26px' : 0 }}
              >
                추가
              </Button>
              <Button
                size="sm"
                variant="secondary"
                onClick={handleToggleAdding}
                style={{ marginTop: keywordError ? '26px' : 0 }}
              >
                취소
              </Button>
            </div>
          ) : (
            <button
              onClick={handleToggleAdding}
              style={{
                background: 'none',
                border: '1px solid var(--border)',
                borderRadius: '6px',
                color: 'var(--text-muted)',
                fontSize: '13px',
                padding: '4px 12px',
                cursor: 'pointer',
                transition: 'color 150ms, border-color 150ms',
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.color = 'var(--text-primary)'
                e.currentTarget.style.borderColor = 'var(--text-muted)'
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.color = 'var(--text-muted)'
                e.currentTarget.style.borderColor = 'var(--border)'
              }}
            >
              + 키워드 추가
            </button>
          )}
        </section>
      </main>
    </div>
  )
}
