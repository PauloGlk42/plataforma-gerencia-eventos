import { useQuery } from '@tanstack/react-query'
import { http } from '../lib/http'

export function useEvento(id) {
  return useQuery({
    queryKey: ['evento', id],
    queryFn: () => http.get(`/api/eventos/${id}`),
    enabled: id != null,
    networkMode: 'always',
  })
}
