export type SignalType = 'EMERGING_CLUSTER'

export interface SignalEvidence {
  newsId: number
  rankOrder: number
  title: string
  url: string | null
  publishedAt: string | null
}

export interface Signal {
  id: number
  keyword: string
  type: SignalType
  title: string
  summary: string
  score: number
  relatedNewsCount: number
  firstPublishedAt: string | null
  lastPublishedAt: string | null
  detectedAt: string
  evidences: SignalEvidence[]
}
