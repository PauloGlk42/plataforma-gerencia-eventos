const KEY = 'bilheteria.auth'

export function loadAuth() {
  try {
    const raw = localStorage.getItem(KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function saveAuth(auth) {
  localStorage.setItem(KEY, JSON.stringify(auth))
}

export function clearAuth() {
  localStorage.removeItem(KEY)
}

export function getToken() {
  return loadAuth()?.token ?? null
}
