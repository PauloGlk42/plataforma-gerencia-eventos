import { useId, useState } from 'react'
import './PasswordField.css'

function IconCadeado() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <rect x="5" y="11" width="14" height="9" rx="2" />
      <path d="M8 11V8a4 4 0 0 1 8 0v3" />
    </svg>
  )
}

function IconOlho({ aberto }) {
  if (aberto) {
    return (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7z" />
        <circle cx="12" cy="12" r="3" />
      </svg>
    )
  }
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M6.2 6.2C3.6 7.9 2 12 2 12s3.5 7 10 7c1.4 0 2.7-.3 3.8-.8" />
      <path d="M9.9 5.2A10.4 10.4 0 0 1 12 5c6.5 0 10 7 10 7a17.6 17.6 0 0 1-3.1 4.1" />
      <path d="M10.6 10.6a3 3 0 0 0 4.24 4.24" />
      <path d="M3 3l18 18" />
    </svg>
  )
}

// Cadeado + olho iguais em Login e Registro (SVG inline, sem lib de ícone nova).
export default function PasswordField({ id, label, value, onChange, autoComplete, required }) {
  const gerarId = useId()
  const inputId = id ?? gerarId
  const [visivel, setVisivel] = useState(false)

  return (
    <div className="field password-field">
      <label htmlFor={inputId}>{label}</label>
      <div className="password-row">
        <span className="password-icon"><IconCadeado /></span>
        <input
          id={inputId}
          type={visivel ? 'text' : 'password'}
          autoComplete={autoComplete}
          required={required}
          value={value}
          onChange={onChange}
        />
        <button
          type="button"
          className="password-toggle"
          onClick={() => setVisivel(v => !v)}
          aria-label={visivel ? 'Ocultar senha' : 'Mostrar senha'}
        >
          <IconOlho aberto={visivel} />
        </button>
      </div>
    </div>
  )
}
