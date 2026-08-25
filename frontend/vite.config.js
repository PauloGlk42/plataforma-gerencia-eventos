import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // evita CORS em dev: o front chama caminhos relativos (/api, /auth) e o Vite
    // encaminha pro backend Spring por baixo dos panos, mesma origem pro navegador
    proxy: {
      '/api': 'http://localhost:8080',
      '/auth': 'http://localhost:8080',
    },
  },
})
