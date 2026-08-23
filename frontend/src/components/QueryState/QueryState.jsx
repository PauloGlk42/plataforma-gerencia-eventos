import './QueryState.css'

export function Carregando({ texto = 'Carregando…' }) {
  return (
    <div className="query-state" role="status">
      <span className="query-state-spinner" aria-hidden="true" />
      {texto}
    </div>
  )
}

export function Vazio({ titulo, descricao, acao }) {
  return (
    <div className="query-state">
      <p className="query-state-title">{titulo}</p>
      {descricao && <p className="query-state-desc">{descricao}</p>}
      {acao}
    </div>
  )
}

export function ErroCarregamento({ mensagem, onTentarNovamente }) {
  return (
    <div className="query-state query-state-error" role="alert">
      <p className="query-state-title">Não deu pra carregar</p>
      <p className="query-state-desc">{mensagem}</p>
      {onTentarNovamente && (
        <button type="button" onClick={onTentarNovamente}>Tentar novamente</button>
      )}
    </div>
  )
}
