import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { http } from '../lib/http'

export function useMeusIngressos() {
  return useQuery({
    queryKey: ['meus-ingressos'],
    queryFn: () => http.get('/api/meus-ingressos'),
    networkMode: 'always',
  })
}

export function useCompartilharIngresso() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (ingressoId) => http.post(`/api/ingressos/${ingressoId}/compartilhar`, {}),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['meus-ingressos'] })
    },
  })
}

export function useIngressoPublico(token) {
  return useQuery({
    queryKey: ['ingresso-publico', token],
    queryFn: () => http.get(`/api/publico/ingressos/${token}`, { auth: false }),
    enabled: !!token,
    networkMode: 'always',
    retry: false,
  })
}
