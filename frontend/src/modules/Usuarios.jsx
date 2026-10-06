import { useEffect, useState } from 'react'
import http from '../api/http'

const empty = { nombres: '', apellidos: '', email: '', rol: 'INGENIERO', sede: 'Medellín', password: '', cargo: '' }

export default function Usuarios() {
  const [rows, setRows] = useState([])
  const [form, setForm] = useState(empty)
  const [err, setErr] = useState('')
  const [msg, setMsg] = useState('')

  const load = () => http.get('/usuarios').then(r => setRows(r.data)).catch(() => setRows([]))
  useEffect(() => { load() }, [])

  const save = async (e) => {
    e.preventDefault()
    setErr(''); setMsg('')
    if (!form.password || form.password.length < 8) {
      setErr('La contraseña debe tener al menos 8 caracteres')
      return
    }
    try {
      await http.post('/usuarios', form)
      setMsg(`Usuario creado. Entra con ${form.email} y la contraseña que definiste.`)
      setForm(empty)
      load()
    } catch (ex) {
      setErr(ex.response?.data?.message || 'No se pudo crear')
    }
  }

  return (
    <div>
      <div className="page-head">
        <div>
          <h2>Usuarios</h2>
          <p className="muted">El superadmin crea quién entra. Roles: SUPERADMIN, LIDER_AREA, INGENIERO, AUXILIAR, COMERCIAL.</p>
        </div>
      </div>
      {err && <div className="alert error">{err}</div>}
      {msg && <div className="alert ok">{msg}</div>}
      <form className="card" onSubmit={save}>
        <div className="grid2">
          <div><label>Nombres</label><input required value={form.nombres} onChange={e => setForm({ ...form, nombres: e.target.value })} /></div>
          <div><label>Apellidos</label><input required value={form.apellidos} onChange={e => setForm({ ...form, apellidos: e.target.value })} /></div>
          <div><label>Email</label><input type="email" required value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} /></div>
          <div><label>Contraseña</label><input type="text" required minLength={8} value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} placeholder="Mínimo 8 caracteres" /></div>
          <div><label>Rol</label>
            <select value={form.rol} onChange={e => setForm({ ...form, rol: e.target.value })}>
              <option>SUPERADMIN</option><option>LIDER_AREA</option><option>INGENIERO</option><option>AUXILIAR</option><option>COMERCIAL</option>
            </select>
          </div>
          <div><label>Sede</label><input value={form.sede} onChange={e => setForm({ ...form, sede: e.target.value })} /></div>
          <div><label>Cargo</label><input value={form.cargo} onChange={e => setForm({ ...form, cargo: e.target.value })} /></div>
        </div>
        <p><button className="btn" type="submit">Crear usuario</button></p>
      </form>
      <div className="card table-wrap">
        <table>
          <thead><tr><th>Nombre</th><th>Email</th><th>Rol</th><th>Sede</th><th>Cargo</th><th></th></tr></thead>
          <tbody>
            {rows.map(u => (
              <tr key={u.id}>
                <td>{u.nombres} {u.apellidos}</td>
                <td>{u.email}</td>
                <td>{u.rol}</td>
                <td>{u.sede}</td>
                <td>{u.cargo}</td>
                <td>
                  <button type="button" className="btn ghost sm" onClick={async () => {
                    const password = window.prompt('Nueva contraseña (mínimo 8)', '')
                    if (!password || password.length < 8) return
                    await http.put('/usuarios/' + u.id, { ...u, password })
                    setMsg('Contraseña actualizada')
                  }}>Clave</button>
                  <button type="button" className="btn ghost sm" onClick={async () => {
                    if (!window.confirm('Desactivar usuario?')) return
                    await http.delete('/usuarios/' + u.id)
                    load()
                  }}>Desactivar</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
