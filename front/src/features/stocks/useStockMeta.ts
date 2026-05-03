import { useQuery } from '@tanstack/react-query'
import { fetchStockMeta } from '../../api/stocks'

export function useStockMeta(enabled: boolean) {
    return useQuery({
        queryKey: ['stockMeta'],
        queryFn: fetchStockMeta,
        enabled,
        staleTime: 60 * 60 * 1000,
        gcTime: 60 * 60 * 1000,
    })
}