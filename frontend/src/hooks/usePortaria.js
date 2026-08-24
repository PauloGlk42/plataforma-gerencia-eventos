import { useMutation, useQuery } from '@tanstack/react-query'
import { http } from '../lib/http'

export function usePortariaEventos() {
  return useQuery({
    queryKey: ['portaria-eventos'],
    queryFn: () => http.get('/api/portaria/eventos'),
    networkMode: 'always',
  })
}

export function useValidarIngresso() {
  return useMutation({
    mutationFn: ({ codigo, eventoId }) => http.post('/api/validacao', { codigo, eventoId }),
  })
}
