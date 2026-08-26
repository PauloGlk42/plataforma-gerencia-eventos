import { getToken, clearAuth } from './authStorage'
import { redirectToLogin } from './navigation'

// vazia em dev: caminho relativo cai no proxy do Vite (vite.config.js), mesma
// origem pro navegador. Em produção aponta pro backend publicado (Railway).
const API_BASE = import.meta.env.VITE_API_URL ?? ''

// Erro no mesmo formato do RestControllerAdvice do backend: status, mensagem e,
// quando é validação de campo, o nome do campo.
export class ApiError extends Error {
  constructor({ status, mensagem, campo }) {
    super(mensagem)
    this.name = 'ApiError'
    this.status = status
    this.mensagem = mensagem
    this.campo = campo ?? null
  }
}

async function request(path, { method = 'GET', body, auth = true, headers = {} } = {}) {
  const finalHeaders = { ...headers }
  if (body !== undefined) finalHeaders['Content-Type'] = 'application/json'

  const token = auth ? getToken() : null
  if (token) finalHeaders['Authorization'] = `Bearer ${token}`

  let response
  try {
    response = await fetch(`${API_BASE}${path}`, {
      method,
      headers: finalHeaders,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new ApiError({ status: 0, mensagem: 'Não foi possível conectar ao servidor. Verifique sua conexão.' })
  }

  // só é "sessão expirada" quando a chamada pedia autenticação (auth !== false) e o
  // servidor recusou com 401 — o backend responde 401 pra rota protegida sem token válido
  // (ausente, adulterado ou expirado; ver HttpStatusEntryPoint no SecurityConfigurations) e
  // reserva 403 pra sessão válida com papel errado, que não deve deslogar. Chamada de login
  // sempre passa auth:false, então senha errada (401 do AuthenticationController) nunca cai
  // aqui — a mensagem real do backend segue adiante pro fluxo normal de erro abaixo.
  if (response.status === 401 && auth) {
    clearAuth()
    window.dispatchEvent(new Event('auth:unauthorized'))
    redirectToLogin()
    throw new ApiError({ status: 401, mensagem: 'Sessão expirada. Faça login novamente.' })
  }

  if (response.status === 204) return null

  const contentType = response.headers.get('content-type') ?? ''
  const data = contentType.includes('application/json') ? await response.json().catch(() => null) : null

  if (!response.ok) {
    throw new ApiError({
      status: data?.status ?? response.status,
      mensagem: data?.mensagem ?? 'Erro inesperado. Tente novamente.',
      campo: data?.campo,
    })
  }

  return data
}

export const http = {
  get: (path, options) => request(path, { ...options, method: 'GET' }),
  post: (path, body, options) => request(path, { ...options, method: 'POST', body }),
  put: (path, body, options) => request(path, { ...options, method: 'PUT', body }),
  delete: (path, options) => request(path, { ...options, method: 'DELETE' }),
}
