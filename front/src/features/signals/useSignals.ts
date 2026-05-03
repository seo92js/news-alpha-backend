import { useQuery } from '@tanstack/react-query'
import { fetchSignals } from '../../api/signals'

export function useSignals() {
  return useQuery({
    queryKey: ['signals'],
    queryFn: fetchSignals,
    staleTime: 60 * 1000,
  })
}
