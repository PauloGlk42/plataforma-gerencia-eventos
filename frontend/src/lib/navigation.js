// Ponte entre o cliente HTTP (módulo puro, fora da árvore React) e o React Router.
// App.jsx injeta a função `navigate` do router aqui; sem ela (ex.: módulo carregado
// antes do router montar), cai para navegação de página cheia.
let navigateFn = null

export function setNavigator(fn) {
  navigateFn = fn
}

export function redirectToLogin() {
  if (navigateFn) {
    navigateFn('/login', { replace: true })
  } else {
    window.location.assign('/login')
  }
}
