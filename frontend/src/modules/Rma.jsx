import { useEffect, useState } from 'react'
import http from '../api/http'

const empty = {
  customerSnapshot: '', descripcionItem: '', serialReportado: '', partNumber: '',
  problemaReportado: '', motivo: 'GARANTIA', enGarantia: true, detallesAdicionales: '',
  numeroPulsos: '', purchaseOrder: '', hpOutputPower: '', fechaInstalacion: ''
}

export default function Rma() {
  const [rows, setRows] = useState([])
  const [q, setQ] = useState('')
  const [form, setForm] = useState(empty)
  const [open, setOpen] = useState(false)
  const [msg, setMsg] = useState('')
  const [err, setErr] = useState('')
  const [sendOpen, setSendOpen] = useState(false)
  const [sendId, setSendId] = useState(null)
  const [toMail, setToMail] = useState('')
  const [ccMail, setCcMail] = useState('iris.p@example.org')
  const [asunto, setAsunto] = useState('')
  const [sending, setSending] = useState(false)

  const load = () => {
    setErr('')
    http.get('/rma', { params: q ? { q } : {} })
      .then(r => setRows(r.data || []))
      .catch(e => setErr(e.response?.data?.message || 'Error al cargar RMA'))
  }
  useEffect(() => { load() }, [])

  const save = async (e) => {
    e.preventDefault()
    setErr('')
    try {
      if (form.id) await http.put(`/rma/${form.id}`, form)
      else await http.post('/rma', form)
      setOpen(false)
      setForm(empty)
      load()
    } catch (ex) {
      setErr(ex.response?.data?.message || 'No se pudo guardar')
    }
  }

  const pdf = (id) => {
    http.get(`/rma/${id}/pdf`, { responseType: 'blob' }).then(r => {
      window.open(URL.createObjectURL(r.data), '_blank')
    }).catch(() => setErr('No se pudo generar el PDF'))
  }

  const openSend = (row) => {
    setSendId(row.id)
    setToMail('')
    setCcMail('iris.p@example.org')
    setAsunto(`RMA ${row.numeroInterno} — Dilaser Soporte Técnico`)
    setSendOpen(true)
    setMsg('')
    setErr('')
  }

  const doSend = async (e) => {
    e.preventDefault()
    if (!toMail.trim()) {
      setErr('Escribe al menos un correo destino (fabricante)')
      return
    }
    setSending(true)
    setErr('')
    setMsg('')
    try {
      const { data } = await http.post(`/rma/${sendId}/enviar`, {
        to: toMail.split(/[,;]+/).map(s => s.trim()).filter(Boolean),
        cc: ccMail.split(/[,;]+/).map(s => s.trim()).filter(Boolean),
        asunto: asunto || undefined
      })
      setMsg(data.message || 'Enviado')
      setSendOpen(false)
      load()
    } catch (ex) {
      setErr(ex.response?.data?.message || 'Error al enviar. Revisa SMTP en application.properties')
    } finally {
      setSending(false)
    }
  }

  return (
    <div>
      <div className="page-head">
        <div>
          <h2>RMA / Garantías / Devoluciones</h2>
          <p className="muted">Casos al fabricante · PDF y correo</p>
        </div>
        <button className="btn" onClick={() => { setForm(empty); setOpen(true) }}>Nuevo RMA</button>
      </div>

      {err && <div className="alert error">{err}</div>}
      {msg && <div className="alert ok">{msg}</div>}

      <div className="toolbar">
        <input className="search" placeholder="Serial, part number o ítem" value={q}
          onChange={e => setQ(e.target.value)} onKeyDown={e => e.key === 'Enter' && load()} />
        <button className="btn ghost" onClick={load}>Buscar</button>
      </div>

      <div className="card table-wrap">
        <table>
          <thead>
            <tr><th>Interno</th><th>Serial</th><th>Part</th><th>Estado</th><th></th></tr>
          </thead>
          <tbody>
            {rows.map(r => (
              <tr key={r.id}>
                <td>{r.numeroInterno}</td>
                <td>{r.serialReportado}</td>
                <td>{r.partNumber}</td>
                <td><span className="tag">{r.estado}</span></td>
                <td style={{ whiteSpace: 'nowrap' }}>
                  <button className="btn ghost sm" onClick={() => pdf(r.id)}>PDF</button>
                  <button className="btn ghost sm" onClick={() => openSend(r)}>Enviar</button>
                </td>
              </tr>
            ))}
            {!rows.length && (
              <tr><td colSpan={5} className="muted">Sin registros</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {sendOpen && (
        <div className="modal-backdrop" onClick={() => setSendOpen(false)}>
          <form className="modal-sheet form-sheet" style={{ maxWidth: 520 }} onClick={e => e.stopPropagation()} onSubmit={doSend}>
            <div className="form-title-bar">Enviar RMA por correo</div>
            <div className="form-body">
              <p className="muted">Se adjuntará el PDF del RMA. Separa varios correos con coma.</p>
              <div className="field">
                <label>Para (fabricante) *</label>
                <input required type="text" placeholder="fabricante@ejemplo.com, otro@mail.com"
                  value={toMail} onChange={e => setToMail(e.target.value)} />
              </div>
              <div className="field">
                <label>CC (copia)</label>
                <input type="text" placeholder="soporte@dilaser.com.co"
                  value={ccMail} onChange={e => setCcMail(e.target.value)} />
              </div>
              <div className="field">
                <label>Asunto</label>
                <input value={asunto} onChange={e => setAsunto(e.target.value)} />
              </div>
              {err && <div className="alert error">{err}</div>}
              <div className="form-actions">
                <button type="button" className="btn ghost" onClick={() => setSendOpen(false)}>Cancelar</button>
                <button className="btn" type="submit" disabled={sending}>
                  {sending ? 'Enviando…' : 'Enviar con PDF'}
                </button>
              </div>
            </div>
          </form>
        </div>
      )}

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
          <p>
            <button className="btn" type="submit">Guardar RMA</button>{' '}
            <button type="button" className="btn ghost" onClick={() => setOpen(false)}>Cancelar</button>
          </p>
        </form>
      )}
    </div>
  )
}
