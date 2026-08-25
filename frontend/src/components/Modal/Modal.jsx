import { useEffect } from 'react'
import './Modal.css'

// Overlay genérico: fecha com clique fora ou Esc — usado no aviso de pagamento
// recusado e na ampliação do QR em "Meus ingressos", mesmo comportamento nos dois.
export default function Modal({ onFechar, labelledBy, children }) {
  useEffect(() => {
    function aoTeclar(evento) {
      if (evento.key === 'Escape') onFechar()
    }
    window.addEventListener('keydown', aoTeclar)
    return () => window.removeEventListener('keydown', aoTeclar)
  }, [onFechar])

  return (
    <div className="modal-overlay" onClick={onFechar}>
      <div
        className="modal-card"
        role="dialog"
        aria-modal="true"
        aria-labelledby={labelledBy}
        onClick={evento => evento.stopPropagation()}
      >
        {children}
      </div>
    </div>
  )
}
