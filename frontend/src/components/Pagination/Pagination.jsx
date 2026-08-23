import './Pagination.css'

export default function Pagination({ pageAtual, totalPaginas, onMudarPagina }) {
  if (totalPaginas <= 1) return null

  return (
    <nav className="pagination" aria-label="Paginação de eventos">
      <button type="button" disabled={pageAtual === 0} onClick={() => onMudarPagina(pageAtual - 1)}>
        ← Anterior
      </button>
      <span className="pagination-info">Página {pageAtual + 1} de {totalPaginas}</span>
      <button type="button" disabled={pageAtual >= totalPaginas - 1} onClick={() => onMudarPagina(pageAtual + 1)}>
        Próxima →
      </button>
    </nav>
  )
}
