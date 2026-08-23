import { useMutation } from "@tanstack/react-query"
import { http } from '../lib/http'

async function registerUser({ name, email, password }) {
  return http.post('/auth/register', { name, login: email, password }, { auth: false })
}

export function useRegister() {
  return useMutation({
    mutationFn: registerUser
  })
}
