import { useEffect, useState } from 'react'
import http from '../api/http'

export default function Usuarios() {
  const [rows, setRows] = useState([])
  const [form, setForm] = useState({ nombres: '', apellidos: '', email: '', rol: 'INGENIERO', sede: 'Medellín', password: 'Nexo.Temp.2026' })
  const load = () => http.get('/usuarios').then(r => setRows(r.data)).catch(() => setRows([]))
  useEffect(() => { load() }, [])
  const save = async (e) => {
    e.preventDefault()
    await http.post('/usuarios', form)
    load()
  }
  return (
    <div>
      <h2>Usuarios</h2>
      <p className="muted">Solo Superadmin crea. Roles: SUPERADMIN, LIDER_AREA, INGENIERO, AUXILIAR, COMERCIAL.</p>
      <form className="card" onSubmit={save}>
        <div className="grid2">
          <div><label>Nombres</label><input required value={form.nombres} onChange={e => setForm({ ...form, nombres: e.target.value })} /></div>
          <div><label>Apellidos</label><input required value={form.apellidos} onChange={e => setForm({ ...form, apellidos: e.target.value })} /></div>
          <div><label>Email</label><input type="email" required value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} /></div>
          <div><label>Rol</label>
            <select value={form.rol} onChange={e => setForm({ ...form, rol: e.target.value })}>
              <option>SUPERADMIN</option><option>LIDER_AREA</option><option>INGENIERO</option><option>AUXILIAR</option><option>COMERCIAL</option>
            </select>
          </div>
          <div><label>Sede</label><input value={form.sede} onChange={e => setForm({ ...form, sede: e.target.value })} /></div>
        </div>
        <p><button className="btn" type="submit">Crear usuario</button></p>
      </form>
      <table>
        <thead><tr><th>Nombre</th><th>Email</th><th>Rol</th><th>Sede</th></tr></thead>
        <tbody>
          {rows.map(u => (
            <tr key={u.id}><td>{u.nombres} {u.apellidos}</td><td>{u.email}</td><td>{u.rol}</td><td>{u.sede}</td></tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
