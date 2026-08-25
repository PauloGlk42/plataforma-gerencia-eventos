import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { http } from '../lib/http'

export function useMeusEventos() {
  return useQuery({
    queryKey: ['meus-eventos'],
    queryFn: () => http.get('/api/eventos/meus'),
    networkMode: 'always',
  })
}

export function useCriarEvento() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (dto) => http.post('/api/eventos', dto),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['meus-eventos'] })
    },
  })
}

export function usePublicarEvento() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (eventoId) => http.post(`/api/eventos/${eventoId}/publicar`, {}),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['meus-eventos'] })
    },
  })
}
