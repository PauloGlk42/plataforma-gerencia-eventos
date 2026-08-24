import { Link, useParams } from 'react-router-dom'
import { QRCodeSVG } from 'qrcode.react'
import { useIngressoPublico } from '../../hooks/useIngressos'
import { formatarDataHora } from '../../lib/format'
import { Carregando, ErroCarregamento } from '../../components/QueryState/QueryState'
import './IngressoPublico.css'

export default function IngressoPublico() {
  const { token } = useParams()
  const { data: ingresso, isLoading, isError, error, refetch } = useIngressoPublico(token)

  return (
    <div className="ingresso-publico-shell">
      <header className="ingresso-publico-header">
        <Link className="logo" to="/">Bilheteria<em>.</em></Link>
      </header>
      <main className="wrap">
        {isLoading && <Carregando texto="Carregando ingresso…" />}
        {isError && (
          <ErroCarregamento
            mensagem={error?.mensagem ?? 'Este link de ingresso não é válido ou expirou.'}
            onTentarNovamente={refetch}
          />
        )}
        {!isLoading && !isError && ingresso && (
          <div className="ingresso-publico-card">
            <span className="eyebrow">Ingresso</span>
            <h1>{ingresso.eventoTitulo}</h1>
            <p className="sub">{formatarDataHora(ingresso.eventoInicio)} · {ingresso.localNome}, {ingresso.cidade}</p>
            <p className="setor">{ingresso.setorNome}</p>
            <div className="ingresso-publico-qr">
              <QRCodeSVG value={ingresso.codigo} size={200} level="M" />
            </div>
          </div>
        )}
      </main>
    </div>
  )
}
