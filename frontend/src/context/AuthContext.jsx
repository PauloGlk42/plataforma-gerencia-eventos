import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { http } from '../lib/http'
import { loadAuth, saveAuth, clearAuth } from '../lib/authStorage'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => loadAuth())

  // o interceptor de 401 do http.js roda fora da árvore React; escuta aqui pra
  // sincronizar o estado (o botão "Perfil" precisa voltar a ser "Entrar")
  useEffect(() => {
    function aoDeslogarPorExpirar() {
      setAuth(null)
    }
    window.addEventListener('auth:unauthorized', aoDeslogarPorExpirar)
    return () => window.removeEventListener('auth:unauthorized', aoDeslogarPorExpirar)
  }, [])

  const login = useCallback(async (loginValue, senha) => {
    const data = await http.post('/auth/login', { login: loginValue, password: senha }, { auth: false })
    const proximo = { token: data.token, role: data.role, name: data.name, login: loginValue }
    saveAuth(proximo)
    setAuth(proximo)
    return proximo
  }, [])

  const logout = useCallback(() => {
    clearAuth()
    setAuth(null)
  }, [])

  const value = useMemo(() => ({
    isAuthenticated: !!auth,
    token: auth?.token ?? null,
    role: auth?.role ?? null,
    name: auth?.name ?? null,
    login,
    logout,
  }), [auth, login, logout])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components -- contexto + hook juntos é o padrão usual aqui
export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth precisa estar dentro de <AuthProvider>')
  return ctx
}
