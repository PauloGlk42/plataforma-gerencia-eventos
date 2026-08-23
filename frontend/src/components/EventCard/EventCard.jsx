import { useState } from 'react'
import { formatarDataHora, formatarPreco } from '../../lib/format'
import { corDoTexto, corBarraOcupacao } from '../../lib/color'
import './EventCard.css'

function MedidorOcupacao({ capacidade, ocupados }) {
  const razao = ocupados / capacidade
  const livres = capacidade - ocupados
  const esgotado = livres <= 0

  return (
    <div className="occ">
      <div className="occ-track">
        <div className="occ-fill" style={{ width: `${Math.round(razao * 100)}%`, background: corBarraOcupacao(razao) }} />
      </div>
      <div className="occ-label">
        {esgotado ? (
          <>
            <span className="sold-out">Esgotado</span>
            <span className="pct">100%</span>
          </>
        ) : (
          <>
            <span>{livres.toLocaleString('pt-BR')} lugares livres</span>
            <span className="pct">{Math.round(razao * 100)}%</span>
          </>
        )}
      </div>
    </div>
  )
}

export default function EventCard({ evento }) {
  const [imagemFalhou, setImagemFalhou] = useState(false)
  const temImagem = !!evento.imagemUrl && !imagemFalhou
  // capacidade/ocupados não vêm da listagem pública hoje (ver README) — o
  // medidor só aparece se um dia esses campos passarem a existir aqui.
  const temOcupacao = evento.capacidade != null && evento.ocupados != null

  return (
    <article className="card">
      <div className="poster" style={temImagem ? undefined : { '--p': corDoTexto(evento.titulo) }}>
        <span className="kind">{evento.tipo}</span>
        {temImagem ? (
          <img
            className="poster-img"
            src={evento.imagemUrl}
            alt={evento.titulo}
            onError={() => setImagemFalhou(true)}
          />
        ) : (
          <h3>{evento.titulo}</h3>
        )}
      </div>
      <div className="card-body">
        <div className="meta">
          <span className="when">{formatarDataHora(evento.inicio)}</span>
          <span>{evento.localNome} · {evento.cidade}</span>
        </div>
        {temOcupacao && <MedidorOcupacao capacidade={evento.capacidade} ocupados={evento.ocupados} />}
        <div className="price">
          <span className="from">a partir de</span>
          <strong>{evento.precoMinimo != null ? formatarPreco(evento.precoMinimo) : '—'}</strong>
        </div>
      </div>
    </article>
  )
}
