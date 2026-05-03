export interface StockMeta {
  ticker: string
  name: string
  market: string
}

export interface StockKeyword {
  id: number
  stockId: number
  keyword: string
  enabled: boolean
  createdAt: string
}

export interface Stock {
  id: number
  ticker: string
  name: string
  market: string
  createdAt: string
  keywords: StockKeyword[]
}

export interface StockSaveRequest {
  ticker: string
  name: string
  market: string
}

export interface StockKeywordSaveRequest {
  keyword: string
}
