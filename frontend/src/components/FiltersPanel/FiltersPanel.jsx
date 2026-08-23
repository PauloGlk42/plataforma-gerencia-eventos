import './FiltersPanel.css'

const TIPOS = [
  { valor: '', rotulo: 'Tudo' },
  { valor: 'SHOW', rotulo: 'Shows' },
  { valor: 'FILME', rotulo: 'Filmes' },
]

export default function FiltersPanel({ filtros, onFiltroChange, onLimpar }) {
  return (
    <div className="panel">
      <div className="panel-top">
        <h2>Filtrar</h2>
        <button type="button" onClick={onLimpar}>Limpar filtros</button>
      </div>
      <div className="panel-row">
        <div className="field grow">
          <label htmlFor="f-q">Busca</label>
          <input
            id="f-q" type="search" placeholder="artista, filme, local…"
            value={filtros.q} onChange={e => onFiltroChange('q', e.target.value)}
          />
        </div>
        <div className="field">
          <label htmlFor="f-cidade">Cidade</label>
          <input
            id="f-cidade" type="text" placeholder="qualquer cidade"
            value={filtros.cidade} onChange={e => onFiltroChange('cidade', e.target.value)}
          />
        </div>
        <div className="field">
          <label htmlFor="f-de">De</label>
          <input id="f-de" type="date" value={filtros.de} onChange={e => onFiltroChange('de', e.target.value)} />
        </div>
        <div className="field">
          <label htmlFor="f-ate">Até</label>
          <input id="f-ate" type="date" value={filtros.ate} onChange={e => onFiltroChange('ate', e.target.value)} />
        </div>
        <div className="field">
          <label htmlFor="f-preco-min">Preço min.</label>
          <input
            id="f-preco-min" type="number" min="0" step="1" inputMode="numeric" placeholder="R$"
            value={filtros.precoMin} onChange={e => onFiltroChange('precoMin', e.target.value)}
          />
        </div>
        <div className="field">
          <label htmlFor="f-preco-max">Preço máx.</label>
          <input
            id="f-preco-max" type="number" min="0" step="1" inputMode="numeric" placeholder="R$"
            value={filtros.precoMax} onChange={e => onFiltroChange('precoMax', e.target.value)}
          />
        </div>
        <div className="seg" role="group" aria-label="Tipo de evento">
          {TIPOS.map(t => (
            <button
              key={t.valor} type="button"
              aria-pressed={filtros.tipo === t.valor}
              onClick={() => onFiltroChange('tipo', t.valor)}
            >
              {t.rotulo}
            </button>
          ))}
        </div>
      </div>
    </div>
  )
}
