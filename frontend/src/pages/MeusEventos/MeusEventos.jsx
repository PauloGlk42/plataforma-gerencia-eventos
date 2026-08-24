import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useMeusEventos, usePublicarEvento } from '../../hooks/useOrganizador'
import { formatarDataHora } from '../../lib/format'
import { corBarraOcupacao } from '../../lib/color'
import { Carregando, ErroCarregamento, Vazio } from '../../components/QueryState/QueryState'
import './MeusEventos.css'

const STATUS_LABEL = { RASCUNHO: 'Rascunho', PUBLICADO: 'Publicado', CANCELADO: 'Cancelado' }

function EventoOrganizadorRow({ evento }) {
  const publicar = usePublicarEvento()
  const [erro, setErro] = useState(null)
  const temOcupacao = evento.capacidade != null && evento.ocupados != null
  const razao = temOcupacao ? evento.ocupados / evento.capacidade : 0

  async function aoPublicar() {
    setErro(null)
    try {
      await publicar.mutateAsync(evento.id)
    } catch (err) {
      setErro(err.mensagem ?? 'Não foi possível publicar.')
    }
  }

  return (
    <li className="evento-row">
      <div className="evento-row-main">
        <div className="evento-row-info">
          <span className={`evento-status status-${evento.status.toLowerCase()}`}>{STATUS_LABEL[evento.status]}</span>
          <span className="evento-row-titulo">{evento.titulo}</span>
          <span className="evento-row-meta">{formatarDataHora(evento.inicio)} · {evento.localNome}, {evento.cidade}</span>
        </div>

        {temOcupacao && (
          <div className="occ">
            <div className="occ-track">
              <div className="occ-fill" style={{ width: `${Math.round(razao * 100)}%`, background: corBarraOcupacao(razao) }} />
            </div>
            <div className="occ-label">
              <span>{evento.ocupados}/{evento.capacidade} ocupados</span>
              <span className="pct">{Math.round(razao * 100)}%</span>
            </div>
          </div>
        )}
      </div>

      {evento.status === 'RASCUNHO' && (
        <div className="evento-row-acao">
          <button type="button" className="btn-publicar" onClick={aoPublicar} disabled={publicar.isPending}>
            {publicar.isPending ? 'Publicando…' : 'Publicar'}
          </button>
          {erro && <p className="auth-error" role="alert">{erro}</p>}
        </div>
      )}
    </li>
  )
}

export default function MeusEventos() {
  const { data: eventos, isLoading, isError, error, refetch } = useMeusEventos()

  return (
    <section className="meus-eventos">
      <div className="page-head">
        <h1>Meus eventos</h1>
        <p>Eventos que você organiza, com status e ocupação.</p>
      </div>

      <Link className="btn-novo-evento" to="/organizador/eventos/novo">+ Criar evento</Link>

      {isLoading && <Carregando texto="Carregando seus eventos…" />}

      {isError && (
        <ErroCarregamento
          mensagem={error?.mensagem ?? 'Não foi possível carregar seus eventos.'}
          onTentarNovamente={refetch}
        />
      )}

      {!isLoading && !isError && (!eventos || eventos.length === 0) && (
        <Vazio
          titulo="Você ainda não criou eventos"
          descricao="Crie um evento a partir do catálogo para começar."
        />
      )}

      {!isLoading && !isError && eventos && eventos.length > 0 && (
        <ul className="eventos-lista">
          {eventos.map(evento => <EventoOrganizadorRow key={evento.id} evento={evento} />)}
        </ul>
      )}
    </section>
  )
}
