import { useEffect, useState } from 'react'
import http from '../api/http'

const empty = { razonSocial: '', nit: '', tipo: 'CLINICA', direccion: '', ciudad: '', telefono: '', whatsapp: '', emailPrincipal: '', contactoNombre: '', activo: true }

export default function Clientes() {
  const [rows, setRows] = useState([])
  const [q, setQ] = useState('')
  const [form, setForm] = useState(empty)
  const [open, setOpen] = useState(false)

  const load = () => http.get('/clientes', { params: q ? { q } : {} }).then(r => setRows(r.data))
  useEffect(() => { load() }, [])

  const save = async (e) => {
    e.preventDefault()
    if (form.id) await http.put(`/clientes/${form.id}`, form)
    else await http.post('/clientes', form)
    setOpen(false); setForm(empty); load()
  }

  return (
    <div>
      <div className="top">
        <h2>Clientes</h2>
        <button className="btn" onClick={() => { setForm(empty); setOpen(true) }}>Nuevo cliente</button>
      </div>
      <input className="search" placeholder="Buscar razón social o NIT" value={q} onChange={(e) => setQ(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && load()} />
      <p><button className="btn ghost" onClick={load}>Buscar</button></p>
      <table>
        <thead><tr><th>Cliente</th><th>NIT</th><th>Ciudad</th><th>WhatsApp</th><th></th></tr></thead>
        <tbody>
          {rows.map(c => (
            <tr key={c.id}>
              <td>{c.razonSocial}</td><td>{c.nit}</td><td>{c.ciudad}</td><td>{c.whatsapp}</td>
              <td><button className="btn ghost" onClick={() => { setForm(c); setOpen(true) }}>Editar</button></td>
            </tr>
          ))}
        </tbody>
      </table>
      {open && (
        <form className="card" onSubmit={save} style={{ marginTop: 16 }}>
          <div className="grid2">
            <div><label>Razón social</label><input required value={form.razonSocial || ''} onChange={e => setForm({ ...form, razonSocial: e.target.value })} /></div>
            <div><label>NIT</label><input value={form.nit || ''} onChange={e => setForm({ ...form, nit: e.target.value })} /></div>
            <div><label>Tipo</label>
              <select value={form.tipo || 'CLINICA'} onChange={e => setForm({ ...form, tipo: e.target.value })}>
                <option>CLINICA</option><option>CONSULTORIO</option><option>MEDICO</option><option>INTERNO_DILASER</option>
              </select>
            </div>
            <div><label>Ciudad</label><input value={form.ciudad || ''} onChange={e => setForm({ ...form, ciudad: e.target.value })} /></div>
            <div><label>Dirección</label><input value={form.direccion || ''} onChange={e => setForm({ ...form, direccion: e.target.value })} /></div>
            <div><label>Teléfono</label><input value={form.telefono || ''} onChange={e => setForm({ ...form, telefono: e.target.value })} /></div>
            <div><label>WhatsApp</label><input value={form.whatsapp || ''} onChange={e => setForm({ ...form, whatsapp: e.target.value })} /></div>
            <div><label>Email</label><input value={form.emailPrincipal || ''} onChange={e => setForm({ ...form, emailPrincipal: e.target.value })} /></div>
            <div><label>Contacto</label><input value={form.contactoNombre || ''} onChange={e => setForm({ ...form, contactoNombre: e.target.value })} /></div>
          </div>
          <p><button className="btn" type="submit">Guardar</button> <button type="button" className="btn ghost" onClick={() => setOpen(false)}>Cancelar</button></p>
        </form>
      )}
    </div>
  )
}
