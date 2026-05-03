import { useNavigate } from 'react-router-dom'
import { useStocks } from '../../features/stocks/useStocks'

interface StockDrawerProps {
    isOpen: boolean
    onClose: () => void
    onAddClick: () => void
}

export default function StockDrawer({ isOpen, onClose, onAddClick }: StockDrawerProps) {
    const navigate = useNavigate()
    const { data: stocks = [], isLoading } = useStocks()

    const handleStockClick = (id: number) => {
        onClose()
        navigate(`/stocks/${id}`)
    }

    return (
        <>
            {isOpen && (
                <div
                    onClick={onClose}
                    style={{
                        position: 'fixed',
                        inset: 0,
                        backgroundColor: 'rgba(0,0,0,0.5)',
                        zIndex: 200,
                    }}
                />
            )}

            <div
                style={{
                    position: 'fixed',
                    top: 0,
                    right: 0,
                    height: '100vh',
                    width: '320px',
                    backgroundColor: 'var(--surface)',
                    borderLeft: '1px solid var(--border)',
                    zIndex: 201,
                    transform: isOpen ? 'translateX(0)' : 'translateX(100%)',
                    transition: 'transform 250ms ease',
                    display: 'flex',
                    flexDirection: 'column',
                }}
            >
                <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    padding: '20px 24px',
                    borderBottom: '1px solid var(--border)',
                }}>
                    <span style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text-primary)' }}>관심종목</span>
                    <button
                        onClick={onClose}
                        style={{ background: 'none', border: 'none', color: 'var(--text-muted)', fontSize: '20px', cursor: 'pointer' }}
                    >
                        ✕
                    </button>
                </div>

                <div style={{ flex: 1, overflowY: 'auto', padding: '16px' }}>
                    {isLoading ? (
                        <p style={{ fontSize: '14px', color: 'var(--text-muted)', textAlign: 'center', marginTop: '24px' }}>불러오는 중...</p>
                    ) : stocks.length === 0 ? (
                        <p style={{ fontSize: '14px', color: 'var(--text-muted)', textAlign: 'center', marginTop: '24px' }}>등록된 종목이 없습니다</p>
                    ) : (
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                            {stocks.map(stock => (
                                <div
                                    key={stock.id}
                                    onClick={() => handleStockClick(stock.id)}
                                    style={{
                                        display: 'flex',
                                        alignItems: 'center',
                                        justifyContent: 'space-between',
                                        padding: '12px 16px',
                                        borderRadius: '10px',
                                        border: '1px solid var(--border)',
                                        cursor: 'pointer',
                                        transition: 'border-color 150ms',
                                    }}
                                    onMouseEnter={e => (e.currentTarget.style.borderColor = 'var(--accent)')}
                                    onMouseLeave={e => (e.currentTarget.style.borderColor = 'var(--border)')}
                                >
                                    <div>
                                        <span style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-primary)' }}>{stock.name}</span>
                                        <span style={{ fontSize: '12px', color: 'var(--text-muted)', marginLeft: '8px' }}>{stock.ticker}</span>
                                    </div>
                                    <span style={{
                                        fontSize: '11px', color: 'var(--text-muted)',
                                        border: '1px solid var(--border)', borderRadius: '4px', padding: '1px 5px',
                                    }}>
                                        {stock.market}
                                    </span>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                <div style={{ padding: '16px 24px', borderTop: '1px solid var(--border)' }}>
                    <button
                        onClick={onAddClick}
                        style={{
                            width: '100%',
                            padding: '12px',
                            background: 'var(--accent)',
                            border: 'none',
                            borderRadius: '10px',
                            color: '#0F0F10',
                            fontSize: '14px',
                            fontWeight: 700,
                            cursor: 'pointer',
                        }}
                    >
                        추가
                    </button>
                </div>
            </div>
        </>
    )
}