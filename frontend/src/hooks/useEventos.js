import { useQuery, keepPreviousData } from '@tanstack/react-query'
import { http } from '../lib/http'
import { montarQueryEventos } from '../lib/queryParams'

const TAMANHO_PAGINA = 12

export function useEventos(filtros, page) {
  const queryString = montarQueryEventos(filtros, page, TAMANHO_PAGINA)

  return useQuery({
    queryKey: ['eventos', queryString],
    queryFn: () => http.get(`/api/eventos?${queryString}`),
    placeholderData: keepPreviousData,
    // 'always': o próprio http.js já distingue falha de rede real (erro genérico
    // de conexão) de erro HTTP da API. Com o networkMode padrão ('online'), o
    // detector interno de conectividade do React Query pode divergir do
    // navigator.onLine real e travar a query em pending/paused pra sempre —
    // tela fica com dado obsoleto do cache, sem loading nem erro. Sem isso,
    // uma falha de verdade nunca aparece pro usuário.
    networkMode: 'always',
  })
}
