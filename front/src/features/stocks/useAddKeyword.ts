import { useMutation, useQueryClient } from '@tanstack/react-query'
import { addKeyword } from '../../api/stocks'
import type { StockKeywordSaveRequest } from '../../types/stock'

export function useAddKeyword(stockId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: StockKeywordSaveRequest) => addKeyword(stockId, data),
    onSuccess: async() => {
      await queryClient.invalidateQueries({ queryKey: ['stocks'] }) // 키워드 캐시만 무효화 하기 위해서는 백엔드 별도 api 구성 필요
    },
  })
}
