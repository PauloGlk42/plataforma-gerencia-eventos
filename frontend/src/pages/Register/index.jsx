import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useRegister } from '../../hooks/useRegister'
import PasswordField from '../../components/PasswordField/PasswordField'

export default function Register() {
  const navigate = useNavigate()
  const { mutateAsync: registrar, isPending } = useRegister()

  const [nome, setNome] = useState('')
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [confirmacao, setConfirmacao] = useState('')
  const [erro, setErro] = useState(null)

  async function aoSubmeter(evento) {
    evento.preventDefault()
    setErro(null)

    if (senha.length < 6) {
      setErro('A senha precisa ter pelo menos 6 caracteres.')
      return
    }
    if (senha !== confirmacao) {
      setErro('As senhas não coincidem.')
      return
    }

    try {
      await registrar({ name: nome, email, password: senha })
      navigate('/login', { replace: true })
    } catch (err) {
      setErro(err.mensagem ?? 'Não foi possível cadastrar. Tente novamente.')
    }
  }

  return (
    <div className="app-shell auth-page">
      <div className="wrap">
        <div className="auth-card">
          <Link className="auth-back" to="/">← Voltar para eventos</Link>
          <h1>Criar conta</h1>
          <p className="auth-sub">Cadastre-se para comprar ingressos.</p>

          <form onSubmit={aoSubmeter} className="auth-form">
            <div className="field">
              <label htmlFor="nome">Nome</label>
              <input
                id="nome" type="text" autoComplete="name" required
                value={nome} onChange={e => setNome(e.target.value)}
              />
            </div>
            <div className="field">
              <label htmlFor="email">E-mail</label>
              <input
                id="email" type="email" autoComplete="email" required
                value={email} onChange={e => setEmail(e.target.value)}
              />
            </div>

            <PasswordField
              id="senha"
              label="Senha"
              value={senha}
              onChange={e => setSenha(e.target.value)}
              autoComplete="new-password"
              required
            />
            <PasswordField
              id="confirmacao"
              label="Confirmar senha"
              value={confirmacao}
              onChange={e => setConfirmacao(e.target.value)}
              autoComplete="new-password"
              required
            />

            {erro && <p className="auth-error" role="alert">{erro}</p>}

            <button className="cta" type="submit" disabled={isPending}>
              {isPending ? 'Cadastrando…' : 'Criar conta'}
            </button>
          </form>

          <p className="auth-foot">Já tem conta? <Link to="/login">Entrar</Link></p>
        </div>
      </div>
    </div>
  )
}
