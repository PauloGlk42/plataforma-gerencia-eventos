import { Link, Outlet } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { iniciais } from '../../lib/text'
import './Layout.css'

export default function Layout() {
  const { isAuthenticated, name } = useAuth()

  return (
    <div className="app-shell">
      <header className="masthead">
        <div className="wrap">
          <Link className="logo" to="/" title="Voltar para os eventos">
            Bilheteria<em>.</em>
          </Link>
          <nav className="top">
            <Link className="btn-search" to="/">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" aria-hidden="true">
                <circle cx="11" cy="11" r="7"></circle>
                <path d="M20 20l-3.6-3.6"></path>
              </svg>
              Buscar eventos
            </Link>
            <Link className="navlink" to="/meus-ingressos">Meus ingressos</Link>
            {isAuthenticated ? (
              <Link className="btn-conta" to="/perfil">
                <span className="avatar" aria-hidden="true">{iniciais(name)}</span>
                Perfil
              </Link>
            ) : (
              <Link className="btn-conta deslogado" to="/login">Entrar</Link>
            )}
          </nav>
        </div>
      </header>
      <main className="wrap">
        <Outlet />
      </main>
    </div>
  )
}
