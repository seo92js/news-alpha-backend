import { useQuery } from '@tanstack/react-query'
import { fetchStocks } from '../../api/stocks'

export function useStocks() {
  return useQuery({
    queryKey: ['stocks'],
    queryFn: fetchStocks,
    staleTime: 5 * 60 * 1000,
  })
}
