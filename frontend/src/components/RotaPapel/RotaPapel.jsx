import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'

// Redireciona em vez de quebrar: sem sessão manda pro login (guardando a rota de
// origem); logado com papel diferente do exigido manda pra home, sem tela de erro.
export default function RotaPapel({ papel, children }) {
  const { isAuthenticated, role } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />
  }
  if (role !== papel) {
    return <Navigate to="/" replace />
  }
  return children
}
