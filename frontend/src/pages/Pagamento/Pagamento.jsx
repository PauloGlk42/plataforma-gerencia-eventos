import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import { usePedido, usePagar } from '../../hooks/usePedido'
import { formatarPreco } from '../../lib/format'
import { Carregando, ErroCarregamento } from '../../components/QueryState/QueryState'
import './Pagamento.css'

function formatarContador(segundos) {
  const min = String(Math.floor(segundos / 60)).padStart(2, '0')
  const seg = String(segundos % 60).padStart(2, '0')
  return `${min}:${seg}`
}

const CARTAO_VAZIO = { numero: '', nomeTitular: '', validade: '', cvv: '' }

export default function Pagamento() {
  const { id } = useParams()
  const location = useLocation()
  const navigate = useNavigate()

  const { data: pedido, isLoading, isError, error, refetch } = usePedido(id, {
    initialData: location.state?.pedido,
  })
  const pagar = usePagar(id)

  const [cartao, setCartao] = useState(CARTAO_VAZIO)
  const [erroSubmissao, setErroSubmissao] = useState(null)

  // `agora` só avança dentro do próprio tick do setInterval (não sincronamente no efeito),
  // e `restam` é derivado dele a cada render — sem estado duplicado pra ressincronizar.
  const [agora, setAgora] = useState(() => Date.now())

  useEffect(() => {
    if (pedido?.status !== 'PENDENTE') return undefined
    const intervalo = setInterval(() => setAgora(Date.now()), 1000)
    return () => clearInterval(intervalo)
  }, [pedido?.status])

  const restam = pedido?.expiraEm
    ? Math.max(0, Math.floor((new Date(pedido.expiraEm).getTime() - agora) / 1000))
    : 0

  if (isLoading) return <Carregando texto="Carregando pedido…" />
  if (isError) {
    return (
      <ErroCarregamento
        mensagem={error?.mensagem ?? 'Não foi possível carregar este pedido.'}
        onTentarNovamente={refetch}
      />
    )
  }
  if (!pedido) return null

  const expirado = pedido.status === 'PENDENTE' && restam <= 0
  const jaPago = pedido.status === 'PAGO'
  const encerrado = pedido.status === 'EXPIRADO' || pedido.status === 'CANCELADO' || expirado

  if (jaPago) {
    return (
      <div className="pagamento-encerrado">
        <p className="query-state-title">Este pedido já foi pago</p>
        <Link className="cta" to="/meus-ingressos">Ver meus ingressos</Link>
      </div>
    )
  }

  if (encerrado) {
    return (
      <div className="pagamento-encerrado">
        <p className="query-state-title">Sua reserva expirou</p>
        <p className="query-state-desc">O prazo de 10 minutos para pagar acabou e os lugares voltaram ao estoque.</p>
        <Link className="cta" to={`/eventos/${pedido.eventoId}`}>Voltar para o evento</Link>
      </div>
    )
  }

  function atualizarCampo(campo, valor) {
    setCartao(c => ({ ...c, [campo]: valor }))
  }

  async function pagarAgora(evento) {
    evento.preventDefault()
    setErroSubmissao(null)
    try {
      const resposta = await pagar.mutateAsync(cartao)
      if (resposta.status === 'APROVADO') {
        navigate('/meus-ingressos')
        return
      }
      setErroSubmissao(resposta.motivo)
      setCartao(CARTAO_VAZIO)
    } catch (err) {
      setErroSubmissao(err.mensagem ?? 'Não foi possível processar o pagamento. Tente novamente.')
    }
  }

  return (
    <section className="pagamento">
      <div className="pagamento-resumo">
        <div className="eyebrow">Pagamento · pedido #{pedido.id}</div>
        <h1>{pedido.eventoTitulo}</h1>
        <ul className="pagamento-itens">
          {pedido.itens.map(item => (
            <li key={item.setorId}>
              <span>{item.quantidade}× {item.setorNome}</span>
              <span>{formatarPreco(item.subtotal)}</span>
            </li>
          ))}
        </ul>
        <div className="pagamento-total">
          <span>Total</span>
          <span>{formatarPreco(pedido.valorTotal)}</span>
        </div>
        <p className={`pagamento-contador ${restam <= 60 ? 'urgente' : ''}`}>
          Pague em até <code>{formatarContador(restam)}</code> ou a reserva expira
        </p>
      </div>

      <div className="pagamento-form-wrap">
        <div className="cartoes-teste">
          <p className="cartoes-teste-titulo">Cartões de teste</p>
          <ul>
            <li><code>•••• 0000</code> — recusado, saldo insuficiente</li>
            <li><code>•••• 1111</code> — recusado, suspeita de fraude</li>
            <li>qualquer outro final — aprovado</li>
          </ul>
        </div>

        <form className="pagamento-form" onSubmit={pagarAgora}>
          <div className="field">
            <label htmlFor="numero">Número do cartão</label>
            <input
              id="numero" inputMode="numeric" required maxLength={19}
              placeholder="0000 0000 0000 0000"
              value={cartao.numero}
              onChange={e => atualizarCampo('numero', e.target.value.replace(/\D/g, ''))}
            />
          </div>
          <div className="field">
            <label htmlFor="nomeTitular">Nome no cartão</label>
            <input
              id="nomeTitular" required placeholder="Como impresso no cartão"
              value={cartao.nomeTitular}
              onChange={e => atualizarCampo('nomeTitular', e.target.value)}
            />
          </div>
          <div className="pagamento-form-row">
            <div className="field">
              <label htmlFor="validade">Validade (MM/AA)</label>
              <input
                id="validade" required placeholder="MM/AA" maxLength={5}
                value={cartao.validade}
                onChange={e => atualizarCampo('validade', e.target.value)}
              />
            </div>
            <div className="field">
              <label htmlFor="cvv">CVV</label>
              <input
                id="cvv" inputMode="numeric" required maxLength={4} placeholder="123"
                value={cartao.cvv}
                onChange={e => atualizarCampo('cvv', e.target.value.replace(/\D/g, ''))}
              />
            </div>
          </div>

          {erroSubmissao && <p className="erro-reserva" role="alert">{erroSubmissao}</p>}

          <button className="cta" type="submit" disabled={pagar.isPending}>
            {pagar.isPending ? 'Processando…' : `Pagar ${formatarPreco(pedido.valorTotal)}`}
          </button>
        </form>
      </div>
    </section>
  )
}
