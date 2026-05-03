import { useMutation, useQueryClient } from '@tanstack/react-query'
import { addStock } from '../../api/stocks'
import type { StockSaveRequest } from '../../types/stock'

export function useAddStock() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: StockSaveRequest) => addStock(data),
    onSuccess: async() => {
      await queryClient.invalidateQueries({ queryKey: ['stocks'] })
    },
  })
}
