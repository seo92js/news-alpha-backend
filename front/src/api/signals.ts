import api from './axios'
import type { Signal } from '../types/signal'

export const fetchSignals = (): Promise<Signal[]> =>
  api.get<Signal[]>('/signals').then(r => r.data)
