import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import './Stub.css'

export function EmConstrucao({ titulo }) {
  return (
    <div className="stub">
      <p className="stub-eyebrow">Em construção</p>
      <h1>{titulo}</h1>
      <p>Essa tela ainda não foi implementada nesta etapa do projeto.</p>
      <Link className="btn-search" to="/">Voltar para eventos</Link>
    </div>
  )
}

export function Perfil() {
  const { name, role, logout } = useAuth()
  const navigate = useNavigate()

  function sair() {
    logout()
    navigate('/')
  }

  return (
    <div className="stub">
      <p className="stub-eyebrow">Perfil</p>
      <h1>{name}</h1>
      <p>Papel: {role}</p>
      <button type="button" className="btn-conta" onClick={sair}>Sair</button>
    </div>
  )
}

export function NaoEncontrada() {
  return (
    <div className="stub">
      <p className="stub-eyebrow">404</p>
      <h1>Página não encontrada</h1>
      <Link className="btn-search" to="/">Voltar para eventos</Link>
    </div>
  )
}
