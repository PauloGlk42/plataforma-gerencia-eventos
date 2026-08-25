import { useState } from 'react'
// qrcode.react: sem dependência de canvas, renderiza SVG puro (~3kb) direto do código do ingresso.
import { QRCodeSVG } from 'qrcode.react'
import { useMeusIngressos, useCompartilharIngresso } from '../../hooks/useIngressos'
import { formatarDataHora } from '../../lib/format'
import { Carregando, ErroCarregamento, Vazio } from '../../components/QueryState/QueryState'
import Modal from '../../components/Modal/Modal'
import './MeusIngressos.css'

const STATUS_LABEL = { VALIDO: 'Válido', UTILIZADO: 'Utilizado', CANCELADO: 'Cancelado' }

function BotaoCompartilhar({ ingressoId }) {
  const compartilhar = useCompartilharIngresso()
  const [copiado, setCopiado] = useState(false)

  async function aoClicar() {
    setCopiado(false)
    const resposta = await compartilhar.mutateAsync(ingressoId)
    const link = `${window.location.origin}/p/${resposta.tokenPublico}`
    try {
      await navigator.clipboard.writeText(link)
      setCopiado(true)
      setTimeout(() => setCopiado(false), 2500)
    } catch {
      window.prompt('Copie o link do ingresso:', link)
    }
  }

  return (
    <button type="button" className="btn-compartilhar" onClick={aoClicar} disabled={compartilhar.isPending}>
      {compartilhar.isPending ? 'Gerando link…' : copiado ? 'Link copiado!' : 'Compartilhar'}
    </button>
  )
}

export default function MeusIngressos() {
  const { data: grupos, isLoading, isError, error, refetch } = useMeusIngressos()
  const [ingressoAmpliado, setIngressoAmpliado] = useState(null)

  if (isLoading) return <Carregando texto="Carregando seus ingressos…" />
  if (isError) {
    return (
      <ErroCarregamento
        mensagem={error?.mensagem ?? 'Não foi possível carregar seus ingressos.'}
        onTentarNovamente={refetch}
      />
    )
  }

  if (!grupos || grupos.length === 0) {
    return (
      <Vazio
        titulo="Você ainda não tem ingressos"
        descricao="Compre um ingresso em um evento para vê-lo aqui."
      />
    )
  }

  return (
    <section className="meus-ingressos">
      <div className="page-head">
        <h1>Meus ingressos</h1>
      </div>

      {grupos.map(grupo => (
        <div key={grupo.eventoId} className="grupo-evento">
          <div className="grupo-evento-cabecalho">
            <h2>{grupo.eventoTitulo}</h2>
            <span>{formatarDataHora(grupo.eventoInicio)} · {grupo.localNome}, {grupo.cidade}</span>
          </div>
          <div className="ingressos-grid">
            {grupo.ingressos.map(ingresso => (
              <div key={ingresso.id} className="ingresso-card">
                <button
                  type="button"
                  className="ingresso-qr"
                  onClick={() => setIngressoAmpliado(ingresso)}
                  aria-label="Ampliar QR Code para leitura na portaria"
                >
                  <QRCodeSVG value={ingresso.codigo} size={140} level="M" />
                </button>
                <div className="ingresso-info">
                  <span className="ingresso-setor">{ingresso.setorNome}</span>
                  <span className={`ingresso-status status-${ingresso.status.toLowerCase()}`}>
                    {STATUS_LABEL[ingresso.status] ?? ingresso.status}
                  </span>
                </div>
                <BotaoCompartilhar ingressoId={ingresso.id} />
              </div>
            ))}
          </div>
        </div>
      ))}

      {ingressoAmpliado && (
        <Modal onFechar={() => setIngressoAmpliado(null)} labelledBy="qr-ampliado-titulo">
          <h2 id="qr-ampliado-titulo" className="qr-ampliado-titulo">{ingressoAmpliado.setorNome}</h2>
          <div className="qr-ampliado">
            <QRCodeSVG value={ingressoAmpliado.codigo} size={280} level="M" />
          </div>
        </Modal>
      )}
    </section>
  )
}
