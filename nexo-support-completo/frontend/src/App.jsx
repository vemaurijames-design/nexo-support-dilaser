import { NavLink, Navigate, Route, Routes, useNavigate } from 'react-router-dom'
import Login from './modules/Login.jsx'
import Dashboard from './modules/Dashboard.jsx'
import Clientes from './modules/Clientes.jsx'
import Equipos from './modules/Equipos.jsx'
import Rma from './modules/Rma.jsx'
import Inventario from './modules/Inventario.jsx'
import Cronograma from './modules/Cronograma.jsx'
import Usuarios from './modules/Usuarios.jsx'
import Remisiones from './modules/Remisiones.jsx'
import Configuracion from './modules/Configuracion.jsx'

function Guard({ children }) {
  const token = localStorage.getItem('nexo_token')
  if (!token) return <Navigate to="/login" replace />
  return children
}

function Shell({ children }) {
  const nav = useNavigate()
  const nombre = localStorage.getItem('nexo_nombre') || 'Usuario'
  const rol = localStorage.getItem('nexo_rol') || ''
  const isAdmin = rol === 'SUPERADMIN'
  const logout = () => { localStorage.clear(); nav('/login') }
  const link = ({ isActive }) => (isActive ? 'active' : '')
  return (
    <div className="layout">
      <aside className="sidebar">
        <h1>Nexo Support</h1>
        <small>Dilaser · Soporte técnico</small>
        <nav>
          <NavLink to="/" className={link} end>Dashboard</NavLink>
          <NavLink to="/clientes" className={link}>Clientes</NavLink>
          <NavLink to="/equipos" className={link}>Hojas de vida</NavLink>
          <NavLink to="/rma" className={link}>RMA / Garantías</NavLink>
          <NavLink to="/remisiones" className={link}>Remisiones</NavLink>
          <NavLink to="/inventario" className={link}>Inventario</NavLink>
          <NavLink to="/cronograma" className={link}>Cronograma</NavLink>
          {isAdmin && <NavLink to="/usuarios" className={link}>Usuarios</NavLink>}
          {isAdmin && <NavLink to="/configuracion" className={link}>Configuración</NavLink>}
        </nav>
      </aside>
      <section className="main">
        <div className="top">
          <div className="muted">{nombre} · {rol}</div>
          <button className="btn ghost" onClick={logout}>Salir</button>
        </div>
        {children}
      </section>
    </div>
  )
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/*" element={
        <Guard>
          <Shell>
            <Routes>
              <Route path="/" element={<Dashboard />} />
              <Route path="/clientes" element={<Clientes />} />
              <Route path="/equipos" element={<Equipos />} />
              <Route path="/rma" element={<Rma />} />
              <Route path="/remisiones" element={<Remisiones />} />
              <Route path="/inventario" element={<Inventario />} />
              <Route path="/cronograma" element={<Cronograma />} />
              <Route path="/usuarios" element={<Usuarios />} />
              <Route path="/configuracion" element={<Configuracion />} />
            </Routes>
          </Shell>
        </Guard>
      } />
    </Routes>
  )
}
