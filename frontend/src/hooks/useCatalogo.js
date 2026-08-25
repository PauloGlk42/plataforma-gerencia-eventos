import { useQuery } from '@tanstack/react-query'
import { http } from '../lib/http'

export function useCatalogo(termo, tipo) {
  const params = new URLSearchParams()
  if (termo?.trim()) params.set('q', termo.trim())
  if (tipo) params.set('tipo', tipo)

  return useQuery({
    queryKey: ['catalogo', params.toString()],
    queryFn: () => http.get(`/api/catalogo?${params.toString()}`),
    networkMode: 'always',
  })
}
