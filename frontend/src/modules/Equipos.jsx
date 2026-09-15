import { useEffect, useState } from 'react'
import http from '../api/http'

export default function Equipos() {
  const [rows, setRows] = useState([])
  const [cats, setCats] = useState({ modelos: [] })
  const [clientes, setClientes] = useState([])
  const [q, setQ] = useState('')
  const [form, setForm] = useState({ serial: '', propiedad: 'CLIENTE', estado: 'ACTIVO', accesorios: [{ descripcion: 'Unidad principal', cantidad: 1 }] })
  const [open, setOpen] = useState(false)

  const load = () => http.get('/equipos', { params: q ? { q } : {} }).then(r => setRows(r.data))
  useEffect(() => {
    load()
    http.get('/catalogos').then(r => setCats(r.data))
    http.get('/clientes').then(r => setClientes(r.data))
  }, [])

  const save = async (e) => {
    e.preventDefault()
    const payload = {
      ...form,
      modelo: form.modeloId ? { id: Number(form.modeloId) } : form.modelo,
      cliente: form.clienteId ? { id: form.clienteId } : form.cliente
    }
    if (form.id) await http.put(`/equipos/${form.id}`, payload)
    else await http.post('/equipos', payload)
    setOpen(false)
    load()
  }

  const pdf = (id) => {
    http.get(`/equipos/${id}/hoja-vida.pdf`, { responseType: 'blob' }).then(r => {
      window.open(URL.createObjectURL(r.data), '_blank')
    })
  }

  return (
    <div>
      <div className="top">
        <h2>Hojas de vida</h2>
        <button className="btn" onClick={() => setOpen(true)}>Nueva hoja de vida</button>
      </div>
      <input className="search" placeholder="Serial, modelo o cliente" value={q} onChange={e => setQ(e.target.value)} onKeyDown={e => e.key === 'Enter' && load()} />
      <p><button className="btn ghost" onClick={load}>Buscar</button></p>
      <table>
        <thead><tr><th>Serial</th><th>Modelo</th><th>Cliente</th><th>Estado</th><th></th></tr></thead>
        <tbody>
          {rows.map(eq => (
            <tr key={eq.id}>
              <td>{eq.serial}</td>
              <td>{eq.modelo?.nombre}</td>
              <td>{eq.cliente?.razonSocial}</td>
              <td>{eq.estado}</td>
              <td><button className="btn ghost" onClick={() => pdf(eq.id)}>PDF</button></td>
            </tr>
          ))}
        </tbody>
      </table>
      {open && (
        <form className="card" onSubmit={save} style={{ marginTop: 16 }}>
          <div className="grid2">
            <div><label>Serial *</label><input required value={form.serial} onChange={e => setForm({ ...form, serial: e.target.value })} /></div>
            <div><label>Modelo *</label>
              <select required value={form.modeloId || ''} onChange={e => setForm({ ...form, modeloId: e.target.value })}>
                <option value="">Seleccione</option>
                {(cats.modelos || []).map(m => <option key={m.id} value={m.id}>{m.marca?.nombre} · {m.nombre}</option>)}
              </select>
            </div>
            <div><label>Cliente</label>
              <select value={form.clienteId || ''} onChange={e => setForm({ ...form, clienteId: e.target.value })}>
                <option value="">— Dilaser / sin asignar —</option>
                {clientes.map(c => <option key={c.id} value={c.id}>{c.razonSocial}</option>)}
              </select>
            </div>
            <div><label>Propiedad</label>
              <select value={form.propiedad} onChange={e => setForm({ ...form, propiedad: e.target.value })}>
                <option>CLIENTE</option><option>DILASER_ALQUILER</option><option>DILASER_DEMO</option><option>DILASER_STOCK</option>
              </select>
            </div>
            <div><label>País origen</label><input value={form.paisOrigen || ''} onChange={e => setForm({ ...form, paisOrigen: e.target.value })} /></div>
            <div><label>Voltage</label><input value={form.voltageAlimentacion || ''} onChange={e => setForm({ ...form, voltageAlimentacion: e.target.value })} /></div>
            <div><label>Fecha instalación</label><input type="date" value={form.fechaInstalacion || ''} onChange={e => setForm({ ...form, fechaInstalacion: e.target.value })} /></div>
            <div><label>Registro sanitario</label><input value={form.registroSanitario || ''} onChange={e => setForm({ ...form, registroSanitario: e.target.value })} /></div>
            <div><label>Ciudad</label><input value={form.ciudadUbicacion || ''} onChange={e => setForm({ ...form, ciudadUbicacion: e.target.value })} /></div>
            <div><label>N° acta entrega</label><input value={form.numeroActaEntrega || ''} onChange={e => setForm({ ...form, numeroActaEntrega: e.target.value })} /></div>
          </div>
          <p><button className="btn" type="submit">Guardar hoja de vida</button> <button type="button" className="btn ghost" onClick={() => setOpen(false)}>Cancelar</button></p>
        </form>
      )}
    </div>
  )
}
