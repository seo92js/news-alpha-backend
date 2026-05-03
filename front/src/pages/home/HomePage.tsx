import { useSignals } from '../../features/signals/useSignals'
import Navbar from '../../components/layout/Navbar'
import SignalCard from '../../features/signals/SignalCard'

const TOP_SIGNAL_COUNT = 3

export default function HomePage() {
    const { data: signals = [], isLoading: signalsLoading } = useSignals()

    const topSignals = [...signals]
        .sort((a, b) => b.score - a.score)
        .slice(0, TOP_SIGNAL_COUNT)

    return (
        <div
            style={{
                minHeight: '100vh',
                backgroundColor: 'var(--bg)',
                fontFamily: "'Geist', 'Inter', system-ui, sans-serif",
            }}
        >
            <Navbar />

            <main
                style={{
                    maxWidth: '800px',
                    margin: '0 auto',
                    padding: '24px',
                    animation: 'fadeInUp 300ms ease-out',
                }}
            >
                <section style={{ marginBottom: '32px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '14px' }}>
                        <h2
                            style={{
                                fontSize: '16px',
                                fontWeight: 700,
                                color: 'var(--text-primary)',
                                margin: 0,
                            }}
                        >
                            오늘의 시그널
                        </h2>
                        <span
                            style={{
                                fontSize: '12px',
                                fontWeight: 700,
                                color: 'var(--accent)',
                                background: 'rgba(245,166,35,0.12)',
                                borderRadius: '20px',
                                padding: '2px 10px',
                            }}
                        >
              {topSignals.length}
            </span>
                    </div>

                    {signalsLoading ? (
                        <p style={{ fontSize: '14px', color: 'var(--text-muted)' }}>불러오는 중...</p>
                    ) : topSignals.length === 0 ? (
                        <p style={{ fontSize: '14px', color: 'var(--text-muted)' }}>탐지된 시그널이 없습니다</p>
                    ) : (
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                            {topSignals.map((signal) => (
                                <SignalCard key={signal.id} signal={signal} compact />
                            ))}
                        </div>
                    )}
                </section>

            </main>
        </div>
    )
}