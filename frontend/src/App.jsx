import { useEffect } from 'react'
import { BrowserRouter, Routes, Route, useNavigate } from 'react-router-dom'
import { setNavigator } from './lib/navigation'
import Layout from './components/Layout/Layout'
import Home from './pages/Home/Home'
import Login from './pages/Login/Login'
import Register from './pages/Register'
import { EmConstrucao, Perfil, NaoEncontrada } from './pages/Stub/Stub'

function NavigatorBridge() {
  const navigate = useNavigate()
  useEffect(() => setNavigator(navigate), [navigate])
  return null
}

export default function App() {
  return (
    <BrowserRouter>
      <NavigatorBridge />
      <Routes>
        <Route element={<Layout />}>
          <Route index element={<Home />} />
          <Route path="meus-ingressos" element={<EmConstrucao titulo="Meus ingressos" />} />
          <Route path="perfil" element={<Perfil />} />
        </Route>
        <Route path="login" element={<Login />} />
        <Route path="registro" element={<Register />} />
        <Route path="*" element={<Layout />}>
          <Route index element={<NaoEncontrada />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
