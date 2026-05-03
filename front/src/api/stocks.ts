import api from './axios'
import type { Stock, StockKeyword, StockMeta, StockSaveRequest, StockKeywordSaveRequest } from '../types/stock'

export const fetchStocks = (): Promise<Stock[]> =>
  api.get<Stock[]>('/stocks').then(r => r.data)

export const fetchStockMeta = (): Promise<StockMeta[]> =>
    api.get<StockMeta[]>('/stocks/meta').then(r => r.data)

export const addStock = (data: StockSaveRequest): Promise<Stock> =>
  api.post<Stock>('/stocks', data).then(r => r.data)

export const addKeyword = (stockId: number, data: StockKeywordSaveRequest): Promise<StockKeyword> =>
  api.post<StockKeyword>(`/stocks/${stockId}/keywords`, data).then(r => r.data)
