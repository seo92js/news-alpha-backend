import { useState, useRef, useEffect } from 'react'
import Modal from '../../components/ui/Modal'
import Input from '../../components/ui/Input'
import Button from '../../components/ui/Button'
import { useAddStock } from './useAddStock'
import { useAddKeyword } from './useAddKeyword'
import { useStocks } from './useStocks'
import { useStockMeta } from './useStockMeta'
import type { Stock, StockMeta } from '../../types/stock'

interface AddStockModalProps {
    isOpen: boolean
    onClose: () => void
}

type Tab = 'stock' | 'keyword'

const AUTOCOMPLETE_LIMIT = 8

function normalize(str: string) {
    return str.replace(/\s/g, '').toLowerCase()
}

function filterMeta(list: StockMeta[], query: string): StockMeta[] {
    if (!query.trim()) return []
    const q = normalize(query)
    return list
        .filter(item => normalize(item.name).includes(q) || normalize(item.ticker).includes(q))
        .slice(0, AUTOCOMPLETE_LIMIT)
}

export default function AddStockModal({ isOpen, onClose }: AddStockModalProps) {
    const [activeTab, setActiveTab] = useState<Tab>('stock')

    const [query, setQuery] = useState('')
    const [selected, setSelected] = useState<StockMeta | null>(null)
    const [suggestions, setSuggestions] = useState<StockMeta[]>([])
    const [highlightIdx, setHighlightIdx] = useState(-1)
    const [stockError, setStockError] = useState('')
    const dropdownRef = useRef<HTMLDivElement>(null)
    const metaRef = useRef<StockMeta[]>([])

    const [selectedStockId, setSelectedStockId] = useState<number | null>(null)
    const [keyword, setKeyword] = useState('')
    const [keywordError, setKeywordError] = useState('')

    const { data: stocks = [] } = useStocks()
    const { data: metaList = [] } = useStockMeta(isOpen)
    metaRef.current = metaList
    const { mutate: addStock, isPending: isAddingStock } = useAddStock()
    const { mutate: addKeyword, isPending: isAddingKeyword } = useAddKeyword(selectedStockId ?? 0)

    useEffect(() => {
        if (selected || !query.trim()) {
            setSuggestions([])
            return
        }
        setSuggestions(filterMeta(metaRef.current, query))
        setHighlightIdx(-1)
    }, [query, selected])

    const handleSelect = (item: StockMeta) => {
        setSelected(item)
        setQuery(`${item.name} (${item.ticker})`)
        setSuggestions([])
        setStockError('')
    }

    const handleQueryChange = (value: string) => {
        setQuery(value)
        setSelected(null)
    }

    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (!suggestions.length) return
        if (e.key === 'ArrowDown') {
            e.preventDefault()
            setHighlightIdx(i => Math.min(i + 1, suggestions.length - 1))
        } else if (e.key === 'ArrowUp') {
            e.preventDefault()
            setHighlightIdx(i => Math.max(i - 1, -1))
        } else if (e.key === 'Enter' && highlightIdx >= 0) {
            e.preventDefault()
            handleSelect(suggestions[highlightIdx])
        } else if (e.key === 'Escape') {
            setSuggestions([])
        }
    }

    const handleClose = () => {
        setQuery('')
        setSelected(null)
        setSuggestions([])
        setStockError('')
        setSelectedStockId(null)
        setKeyword('')
        setKeywordError('')
        onClose()
    }

    const handleAddStock = () => {
        if (!selected) return setStockError('종목을 선택해주세요')
        addStock(
            { ticker: selected.ticker, name: selected.name, market: selected.market },
            {
                onSuccess: () => handleClose(),
                onError: () => setStockError('이미 등록된 종목이거나 추가에 실패했습니다'),
            }
        )
    }

    const handleAddKeyword = () => {
        if (!selectedStockId) return setKeywordError('종목을 선택해주세요')
        if (!keyword.trim()) return setKeywordError('키워드를 입력해주세요')
        addKeyword(
            { keyword: keyword.trim() },
            {
                onSuccess: () => handleClose(),
                onError: () => setKeywordError('키워드 추가에 실패했습니다'),
            }
        )
    }

    const tabStyle = (tab: Tab): React.CSSProperties => ({
        flex: 1,
        padding: '8px',
        border: 'none',
        borderBottom: `2px solid ${activeTab === tab ? 'var(--accent)' : 'var(--border)'}`,
        background: 'none',
        color: activeTab === tab ? 'var(--accent)' : 'var(--text-muted)',
        fontWeight: activeTab === tab ? 700 : 400,
        fontSize: '14px',
        cursor: 'pointer',
    })

    return (
        <Modal isOpen={isOpen} onClose={handleClose} title="추가">
            <div style={{ display: 'flex', marginBottom: '8px' }}>
                <button style={tabStyle('stock')} onClick={() => setActiveTab('stock')}>종목 추가</button>
                <button style={tabStyle('keyword')} onClick={() => setActiveTab('keyword')}>키워드 추가</button>
            </div>

            {activeTab === 'stock' && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    <div style={{ position: 'relative' }}>
                        <Input
                            value={query}
                            onChange={e => handleQueryChange(e.target.value)}
                            onKeyDown={handleKeyDown}
                            placeholder="종목명 또는 종목코드를 입력하세요"
                            autoComplete="off"
                        />
                        {suggestions.length > 0 && (
                            <div
                                ref={dropdownRef}
                                style={{
                                    position: 'absolute',
                                    top: 'calc(100% + 4px)',
                                    left: 0,
                                    right: 0,
                                    background: 'var(--surface)',
                                    border: '1px solid var(--border)',
                                    borderRadius: '10px',
                                    overflow: 'hidden',
                                    zIndex: 200,
                                    boxShadow: '0 8px 24px rgba(0,0,0,0.4)',
                                }}
                            >
                                {suggestions.map((item, idx) => (
                                    <div
                                        key={item.ticker}
                                        onMouseDown={() => handleSelect(item)}
                                        style={{
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: '10px',
                                            padding: '10px 14px',
                                            cursor: 'pointer',
                                            background: highlightIdx === idx ? 'rgba(245,166,35,0.10)' : 'transparent',
                                            borderBottom: idx < suggestions.length - 1 ? '1px solid var(--border)' : 'none',
                                        }}
                                        onMouseEnter={() => setHighlightIdx(idx)}
                                        onMouseLeave={() => setHighlightIdx(-1)}
                                    >
                                        <span style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-primary)', flex: 1 }}>
                                            {item.name}
                                        </span>
                                        <span style={{ fontSize: '12px', color: 'var(--text-muted)', fontVariantNumeric: 'tabular-nums' }}>
                                            {item.ticker}
                                        </span>
                                        <span style={{
                                            fontSize: '11px',
                                            color: 'var(--text-muted)',
                                            background: 'rgba(243,243,243,0.08)',
                                            border: '1px solid var(--border)',
                                            borderRadius: '4px',
                                            padding: '1px 5px',
                                        }}>
                                            {item.market}
                                        </span>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>

                    {selected && (
                        <div style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: '8px',
                            padding: '10px 14px',
                            background: 'rgba(245,166,35,0.08)',
                            border: '1px solid rgba(245,166,35,0.3)',
                            borderRadius: '8px',
                        }}>
                            <span style={{ fontSize: '13px', color: 'var(--accent)', fontWeight: 600 }}>✓</span>
                            <span style={{ fontSize: '14px', color: 'var(--text-primary)', fontWeight: 600 }}>{selected.name}</span>
                            <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{selected.ticker}</span>
                            <span style={{
                                fontSize: '11px',
                                color: 'var(--text-muted)',
                                background: 'rgba(243,243,243,0.08)',
                                border: '1px solid var(--border)',
                                borderRadius: '4px',
                                padding: '1px 5px',
                                marginLeft: 'auto',
                            }}>
                                {selected.market}
                            </span>
                        </div>
                    )}

                    {stockError && <p style={{ color: 'var(--error, red)', fontSize: '13px', margin: 0 }}>{stockError}</p>}
                    <Button onClick={handleAddStock} loading={isAddingStock} fullWidth>추가</Button>
                </div>
            )}

            {activeTab === 'keyword' && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                        <label style={{ fontSize: '13px', color: 'var(--text-muted)' }}>종목 선택</label>
                        <select
                            value={selectedStockId ?? ''}
                            onChange={e => setSelectedStockId(Number(e.target.value))}
                            style={{
                                background: 'var(--surface)',
                                border: '1px solid var(--border)',
                                borderRadius: '8px',
                                color: 'var(--text-primary)',
                                fontSize: '14px',
                                padding: '10px 12px',
                            }}
                        >
                            <option value="">종목을 선택하세요</option>
                            {stocks.map((stock: Stock) => (
                                <option key={stock.id} value={stock.id}>{stock.name} ({stock.ticker})</option>
                            ))}
                        </select>
                    </div>
                    <Input value={keyword} onChange={e => setKeyword(e.target.value)} placeholder="키워드를 입력하세요" />
                    {keywordError && <p style={{ color: 'var(--error, red)', fontSize: '13px', margin: 0 }}>{keywordError}</p>}
                    <Button onClick={handleAddKeyword} loading={isAddingKeyword} fullWidth>추가</Button>
                </div>
            )}
        </Modal>
    )
}