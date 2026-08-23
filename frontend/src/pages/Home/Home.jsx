import { useMemo, useState } from 'react'
import { useEventos } from '../../hooks/useEventos'
import { useDebouncedValue } from '../../hooks/useDebouncedValue'
import { FILTROS_VAZIOS } from '../../lib/queryParams'
import FiltersPanel from '../../components/FiltersPanel/FiltersPanel'
import EventCard from '../../components/EventCard/EventCard'
import Pagination from '../../components/Pagination/Pagination'
import { Carregando, Vazio, ErroCarregamento } from '../../components/QueryState/QueryState'
import './Home.css'

export default function Home() {
  const [filtros, setFiltros] = useState(FILTROS_VAZIOS)
  const [page, setPage] = useState(0)
  const qDebounced = useDebouncedValue(filtros.q, 400)

  const filtrosEfetivos = useMemo(() => ({ ...filtros, q: qDebounced }), [filtros, qDebounced])
  const { data, isLoading, isError, error, refetch, isFetching } = useEventos(filtrosEfetivos, page)

  function atualizarFiltro(campo, valor) {
    setFiltros(f => ({ ...f, [campo]: valor }))
    setPage(0)
  }

  function limparFiltros() {
    setFiltros(FILTROS_VAZIOS)
    setPage(0)
  }

  const eventos = data?.content ?? []
  const totalPaginas = data?.totalPages ?? 0
  const totalEventos = data?.totalElements ?? 0

  return (
    <>
      <div className="page-head">
        <h1>Em cartaz</h1>
        <p>Shows e sessões com ingressos à venda.</p>
      </div>

      <FiltersPanel filtros={filtros} onFiltroChange={atualizarFiltro} onLimpar={limparFiltros} />

      <section>
        <div className="sec-head">
          <h2>{isLoading ? 'Eventos' : `${totalEventos} ${totalEventos === 1 ? 'evento' : 'eventos'}`}</h2>
          {isFetching && !isLoading && <span className="count">atualizando…</span>}
        </div>

        {isLoading && <Carregando texto="Carregando eventos…" />}

        {isError && (
          <ErroCarregamento
            mensagem={error?.mensagem ?? 'Erro inesperado ao carregar os eventos.'}
            onTentarNovamente={refetch}
          />
        )}

        {!isLoading && !isError && eventos.length === 0 && (
          <Vazio
            titulo="Nenhum evento encontrado"
            descricao="Tente ajustar os filtros ou limpar a busca."
            acao={<button type="button" onClick={limparFiltros}>Limpar filtros</button>}
          />
        )}

        {!isLoading && !isError && eventos.length > 0 && (
          <>
            <div className="grid">
              {eventos.map(evento => <EventCard key={evento.id} evento={evento} />)}
            </div>
            <Pagination pageAtual={page} totalPaginas={totalPaginas} onMudarPagina={setPage} />
          </>
        )}
      </section>
    </>
  )
}
