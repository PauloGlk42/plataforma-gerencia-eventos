import { useState } from 'react'
import { useNavigate, useParams, useLocation } from 'react-router-dom'
import { useEvento } from '../../hooks/useEvento'
import { useReservar } from '../../hooks/usePedido'
import { useAuth } from '../../context/AuthContext'
import { formatarDataHora, formatarPreco } from '../../lib/format'
import { corBarraOcupacao } from '../../lib/color'
import SectorMap from '../../components/SectorMap/SectorMap'
import { Carregando, ErroCarregamento } from '../../components/QueryState/QueryState'
import './EventoDetalhe.css'

// Espelha app.pedido.limite-itens-por-setor (padrão 6) no application.properties do backend;
// só limita o passo do seletor no cliente, a regra real é validada de novo no POST /api/pedidos.
const LIMITE_ITENS_POR_SETOR = 6

export default function EventoDetalhe() {
  const { id } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const { isAuthenticated } = useAuth()
  const { data: evento, isLoading, isError, error, refetch } = useEvento(id)
  const reservar = useReservar()

  const [slugSelecionado, setSlugSelecionado] = useState(null)
  const [quantidade, setQuantidade] = useState(0)
  const [erroReserva, setErroReserva] = useState(null)

  if (isLoading) return <Carregando texto="Carregando evento…" />
  if (isError) {
    return (
      <ErroCarregamento
        mensagem={error?.mensagem ?? 'Não foi possível carregar este evento.'}
        onTentarNovamente={refetch}
      />
    )
  }
  if (!evento) return null

  const setores = evento.setores ?? []
  const totalLivres = setores.reduce((soma, s) => soma + Math.max(0, s.capacidade - s.ocupados), 0)
  const setorAtual = setores.find(s => s.slug === slugSelecionado) ?? null

  function selecionarSetor(setor) {
    setErroReserva(null)
    setSlugSelecionado(setor.slug)
    setQuantidade(1)
  }

  function alterarQuantidade(delta) {
    if (!setorAtual) return
    const livres = Math.max(0, setorAtual.capacidade - setorAtual.ocupados)
    const max = Math.min(LIMITE_ITENS_POR_SETOR, livres)
    setQuantidade(q => Math.max(0, Math.min(max, q + delta)))
  }

  async function reservarAgora() {
    if (!setorAtual || quantidade === 0) return

    if (!isAuthenticated) {
      navigate('/login', { state: { from: location.pathname } })
      return
    }

    setErroReserva(null)
    try {
      const pedido = await reservar.mutateAsync({
        eventoId: evento.id,
        itens: [{ setorId: setorAtual.id, quantidade }],
      })
      navigate(`/pedidos/${pedido.id}/pagamento`, { state: { pedido } })
    } catch (err) {
      setErroReserva(err.mensagem ?? 'Não foi possível reservar. Tente novamente.')
      refetch()
    }
  }

  return (
    <section className="evento-detalhe">
      <div className="eyebrow">{evento.tipo === 'SHOW' ? 'Show' : 'Filme'} · {formatarDataHora(evento.inicio)}</div>
      <h1>{evento.titulo}</h1>
      <p className="sub">
        {evento.localNome}, {evento.cidade}
        {' · '}<b>{totalLivres.toLocaleString('pt-BR')}</b> {totalLivres === 1 ? 'lugar disponível' : 'lugares disponíveis'}
      </p>

      <div className="detail">
        <div className="detail-left">
          <SectorMap
            tipo={evento.tipo}
            setores={setores}
            selecionado={slugSelecionado}
            onSelecionar={selecionarSetor}
          />
          <div className="legend">
            <span><i className="swatch" style={{ background: 'var(--occ-1)' }} />até 50% vendido</span>
            <span><i className="swatch" style={{ background: 'var(--occ-2)' }} />50–80%</span>
            <span><i className="swatch" style={{ background: 'var(--occ-3)' }} />80–95%</span>
            <span><i className="swatch" style={{ background: 'var(--occ-4)' }} />quase lá</span>
            <span><i className="swatch" style={{ background: 'var(--occ-out)' }} />esgotado</span>
          </div>
        </div>

        <div className="detail-right">
          {setores.length === 0 ? (
            <p className="aviso-vazio">Este evento ainda não tem setores cadastrados.</p>
          ) : (
            <ul className="sectors">
              {setores.map(setor => {
                const razao = setor.ocupados / setor.capacidade
                const livres = Math.max(0, setor.capacidade - setor.ocupados)
                const esgotado = livres <= 0
                return (
                  <li key={setor.id}>
                    <button
                      type="button"
                      className="sector-row"
                      aria-pressed={slugSelecionado === setor.slug}
                      disabled={esgotado}
                      onClick={() => selecionarSetor(setor)}
                    >
                      <span className="sector-top">
                        <span className="nm">{setor.nome}</span>
                        <span className="pr">{formatarPreco(setor.preco)}</span>
                      </span>
                      <span className="occ">
                        <span className="occ-track">
                          <span
                            className="occ-fill"
                            style={{ width: `${Math.round(razao * 100)}%`, background: corBarraOcupacao(razao) }}
                          />
                        </span>
                        <span className="occ-label">
                          {esgotado
                            ? <span className="sold-out">Esgotado</span>
                            : <span>{livres.toLocaleString('pt-BR')} de {setor.capacidade.toLocaleString('pt-BR')} livres</span>}
                          <span className="pct">{Math.round(razao * 100)}%</span>
                        </span>
                      </span>
                    </button>
                  </li>
                )
              })}
            </ul>
          )}

          <div className="checkout">
            <div className="qty">
              <span className="qty-label">
                {setorAtual ? `${setorAtual.nome} · ${formatarPreco(setorAtual.preco)} cada` : 'Selecione um setor'}
              </span>
              <div className="stepper" role="group" aria-label="Quantidade de ingressos">
                <button type="button" aria-label="Diminuir quantidade" disabled={!setorAtual} onClick={() => alterarQuantidade(-1)}>−</button>
                <output>{quantidade}</output>
                <button type="button" aria-label="Aumentar quantidade" disabled={!setorAtual} onClick={() => alterarQuantidade(1)}>+</button>
              </div>
            </div>
            <div className="total">
              <span className="lbl">Total</span>
              <span className="val">{formatarPreco(setorAtual ? quantidade * setorAtual.preco : 0)}</span>
            </div>

            {erroReserva && <p className="erro-reserva" role="alert">{erroReserva}</p>}

            <button
              className="cta"
              disabled={!setorAtual || quantidade === 0 || reservar.isPending}
              onClick={reservarAgora}
            >
              {reservar.isPending
                ? 'Reservando…'
                : quantidade === 0
                  ? 'Reservar'
                  : `Reservar ${quantidade} ${quantidade === 1 ? 'ingresso' : 'ingressos'}`}
            </button>
            <p className="hold">A reserva fica sua por <code>10 min</code> até a confirmação do pagamento.</p>
          </div>
        </div>
      </div>
    </section>
  )
}
