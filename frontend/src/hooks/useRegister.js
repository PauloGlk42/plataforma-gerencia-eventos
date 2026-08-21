import { useMutation } from "@tanstack/react-query"
import api from '../services/api'

async function registerUser(userData) {
  const response = await api.post('/api/Auth/register', userData)
  return response?.data?.data
}

export function useRegister() {
  return useMutation({
    mutationFn: registerUser
  })
}
