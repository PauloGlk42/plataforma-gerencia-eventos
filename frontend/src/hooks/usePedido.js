import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { http } from '../lib/http'

export function usePedido(id, { initialData } = {}) {
  return useQuery({
    queryKey: ['pedido', id],
    queryFn: () => http.get(`/api/pedidos/${id}`),
    enabled: id != null,
    networkMode: 'always',
    initialData,
  })
}

export function useReservar() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ eventoId, itens }) => http.post('/api/pedidos', { eventoId, itens }),
    onSuccess: (pedido) => {
      queryClient.setQueryData(['pedido', pedido.id], pedido)
    },
  })
}

export function usePagar(pedidoId) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (dadosCartao) => http.post(`/api/pedidos/${pedidoId}/pagamento`, dadosCartao),
    onSuccess: (resposta) => {
      queryClient.setQueryData(['pedido', pedidoId], resposta.pedido)
    },
  })
}
