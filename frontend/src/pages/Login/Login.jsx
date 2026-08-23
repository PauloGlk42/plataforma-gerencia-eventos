import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import './Login.css'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [loginValue, setLoginValue] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState(null)
  const [enviando, setEnviando] = useState(false)

  async function aoSubmeter(evento) {
    evento.preventDefault()
    setErro(null)
    setEnviando(true)
    try {
      await login(loginValue, senha)
      navigate(location.state?.from ?? '/', { replace: true })
    } catch (err) {
      setErro(err.mensagem ?? 'Não foi possível entrar. Tente novamente.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="app-shell auth-page">
      <div className="wrap">
        <div className="auth-card">
          <Link className="auth-back" to="/">← Voltar para eventos</Link>
          <h1>Entrar</h1>
          <p className="auth-sub">Acesse com seu login e senha.</p>

          <form onSubmit={aoSubmeter} className="auth-form">
            <div className="field">
              <label htmlFor="login">Login</label>
              <input
                id="login" type="text" autoComplete="username" required
                value={loginValue} onChange={e => setLoginValue(e.target.value)}
              />
            </div>
            <div className="field">
              <label htmlFor="password">Senha</label>
              <input
                id="password" type="password" autoComplete="current-password" required
                value={senha} onChange={e => setSenha(e.target.value)}
              />
            </div>

            {erro && <p className="auth-error" role="alert">{erro}</p>}

            <button className="cta" type="submit" disabled={enviando}>
              {enviando ? 'Entrando…' : 'Entrar'}
            </button>
          </form>

          <p className="auth-foot">Ainda não tem conta? <Link to="/registro">Cadastre-se</Link></p>
        </div>
      </div>
    </div>
  )
}
