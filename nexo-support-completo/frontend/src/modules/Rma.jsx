import { useEffect, useState } from 'react'
import http from '../api/http'

const empty = { customerSnapshot: '', descripcionItem: '', serialReportado: '', partNumber: '', problemaReportado: '', motivo: 'GARANTIA', enGarantia: true }

export default function Rma() {
  const [rows, setRows] = useState([])
  const [q, setQ] = useState('')
  const [form, setForm] = useState(empty)
  const [open, setOpen] = useState(false)

  const load = () => http.get('/rma', { params: q ? { q } : {} }).then(r => setRows(r.data))
  useEffect(() => { load() }, [])

  const save = async (e) => {
    e.preventDefault()
    if (form.id) await http.put(`/rma/${form.id}`, form)
    else await http.post('/rma', form)
    setOpen(false); setForm(empty); load()
  }

  const pdf = (id) => {
    http.get(`/rma/${id}/pdf`, { responseType: 'blob' }).then(r => window.open(URL.createObjectURL(r.data)))
  }

  return (
    <div>
      <div className="top">
        <h2>RMA / Garantías / Devoluciones</h2>
        <button className="btn" onClick={() => { setForm(empty); setOpen(true) }}>Nuevo RMA</button>
      </div>
      <input className="search" placeholder="Serial, part number o ítem" value={q} onChange={e => setQ(e.target.value)} onKeyDown={e => e.key === 'Enter' && load()} />
      <p><button className="btn ghost" onClick={load}>Buscar</button></p>
      <table>
        <thead><tr><th>Interno</th><th>Serial</th><th>Part</th><th>Estado</th><th></th></tr></thead>
        <tbody>
          {rows.map(r => (
            <tr key={r.id}>
              <td>{r.numeroInterno}</td><td>{r.serialReportado}</td><td>{r.partNumber}</td><td>{r.estado}</td>
              <td>
                <button className="btn ghost" onClick={() => pdf(r.id)}>PDF</button>
                <button className="btn ghost" onClick={() => http.post(`/rma/${r.id}/enviar`).then(load)}>Enviar</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {open && (
        <form className="card" onSubmit={save} style={{ marginTop: 16 }}>
          <div className="grid2">
            <div><label>CUSTOMER</label><input value={form.customerSnapshot || ''} onChange={e => setForm({ ...form, customerSnapshot: e.target.value })} /></div>
            <div><label>DESCRIPTION</label><input value={form.descripcionItem || ''} onChange={e => setForm({ ...form, descripcionItem: e.target.value })} /></div>
            <div><label>S/N</label><input value={form.serialReportado || ''} onChange={e => setForm({ ...form, serialReportado: e.target.value })} /></div>
            <div><label>PART N°</label><input value={form.partNumber || ''} onChange={e => setForm({ ...form, partNumber: e.target.value })} /></div>
            <div><label>N° OF PULSES</label><input type="number" value={form.numeroPulsos || ''} onChange={e => setForm({ ...form, numeroPulsos: e.target.value })} /></div>
            <div><label>PURCHASE ORDER</label><input value={form.purchaseOrder || ''} onChange={e => setForm({ ...form, purchaseOrder: e.target.value })} /></div>
            <div><label>HP OUTPUT POWER</label><input value={form.hpOutputPower || ''} onChange={e => setForm({ ...form, hpOutputPower: e.target.value })} /></div>
            <div><label>INSTALLATION DATE</label><input type="date" value={form.fechaInstalacion || ''} onChange={e => setForm({ ...form, fechaInstalacion: e.target.value })} /></div>
          </div>
          <label>REPORTED PROBLEM</label>
          <textarea rows={3} value={form.problemaReportado || ''} onChange={e => setForm({ ...form, problemaReportado: e.target.value })} />
          <label>ADDITIONAL DETAILS</label>
          <textarea rows={2} value={form.detallesAdicionales || ''} onChange={e => setForm({ ...form, detallesAdicionales: e.target.value })} />
          <p><button className="btn" type="submit">Guardar RMA</button> <button type="button" className="btn ghost" onClick={() => setOpen(false)}>Cancelar</button></p>
        </form>
      )}
    </div>
  )
}
