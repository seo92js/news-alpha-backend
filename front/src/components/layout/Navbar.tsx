import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuthStore } from '../../stores/authStore'
import Button from '../ui/Button'
import StockDrawer from '../ui/StockDrawer'
import AddStockModal from '../../features/stocks/AddStockModal'

export default function Navbar() {
    const navigate = useNavigate()
    const user = useAuthStore((s) => s.user)
    const clearAuth = useAuthStore((s) => s.clearAuth)
    const [drawerOpen, setDrawerOpen] = useState(false)
    const [modalOpen, setModalOpen] = useState(false)

    const handleLogout = () => {
        clearAuth()
        navigate('/login')
    }

    return (
        <>
            <header
                style={{
                    position: 'sticky',
                    top: 0,
                    zIndex: 100,
                    backgroundColor: 'var(--bg)',
                    borderBottom: '1px solid var(--border)',
                }}
            >
                <div
                    style={{
                        maxWidth: '800px',
                        margin: '0 auto',
                        padding: '0 24px',
                        height: '56px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                    }}
                >
                    <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: '8px', textDecoration: 'none' }}>
            <span style={{ fontSize: '18px', fontWeight: 700, color: 'var(--text-primary)', letterSpacing: '-0.5px' }}>
              News
            </span>
                        <span style={{
                            fontSize: '11px', fontWeight: 600, color: 'var(--bg)',
                            background: 'var(--accent)', borderRadius: '4px', padding: '2px 6px', letterSpacing: '1px',
                        }}>
              ALPHA
            </span>
                    </Link>

                    {user && (
                        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                            <button
                                onClick={() => setDrawerOpen(true)}
                                style={{
                                    background: 'none',
                                    border: '1px solid var(--border)',
                                    borderRadius: '8px',
                                    color: 'var(--text-primary)',
                                    fontSize: '13px',
                                    padding: '6px 12px',
                                    cursor: 'pointer',
                                }}
                            >
                                관심종목
                            </button>
                            <span style={{ fontSize: '14px', color: 'var(--text-muted)' }}>{user.email}</span>
                            <Button variant="secondary" size="sm" onClick={handleLogout}>로그아웃</Button>
                        </div>
                    )}
                </div>
            </header>

            <StockDrawer
                isOpen={drawerOpen}
                onClose={() => setDrawerOpen(false)}
                onAddClick={() => { setDrawerOpen(false); setModalOpen(true) }}
            />

            <AddStockModal isOpen={modalOpen} onClose={() => setModalOpen(false)} />
        </>
    )
}