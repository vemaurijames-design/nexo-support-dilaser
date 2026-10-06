import { useEffect, useState } from 'react'
import api from '../api/client.js'

const empty = {
  razonSocial: '', nombreComercial: '', nit: '', tipo: 'CLINICA',
  direccion: '', ciudad: '', departamento: '', telefono: '', whatsapp: '',
  emailPrincipal: '', contactoNombre: '', contactoCargo: '', observaciones: '', activo: true
}

export default function Clientes() {
  const [rows, setRows] = useState([])
  const [q, setQ] = useState('')
  const [open, setOpen] = useState(false)
  const [form, setForm] = useState(empty)
  const [err, setErr] = useState('')
  const [empresa, setEmpresa] = useState(null)

  const load = async () => {
    try {
      const { data } = await api.get('/api/clientes', { params: { q: q || undefined } })
      setRows(data)
    } catch (e) {
      setErr(e.response?.data?.message || 'Error al cargar')
    }
  }
  useEffect(() => {
    load()
    api.get('/api/empresa').then(r => setEmpresa(r.data)).catch(() => {})
  }, [])

  const save = async (e) => {
    e.preventDefault()
    setErr('')
    try {
      if (form.id) await api.put(`/api/clientes/${form.id}`, form)
      else await api.post('/api/clientes', form)
      setOpen(false)
      setForm(empty)
      load()
    } catch (ex) {
      setErr(ex.response?.data?.message || 'No se pudo guardar')
    }
  }

  return (
    <div>
      <div className="page-head">
        <div>
          <h2>Clientes</h2>
          <p className="muted">Clínicas, consultorios y contactos vinculados a equipos</p>
        </div>
        <button className="btn" onClick={() => { setForm(empty); setOpen(true) }}>+ Nuevo cliente</button>
      </div>
      {err && !open && <div className="alert error">{err}</div>}
      <div className="toolbar">
        <input className="search" placeholder="Buscar por razón social, NIT o ciudad…" value={q}
          onChange={e => setQ(e.target.value)} onKeyDown={e => e.key === 'Enter' && load()} />
        <button className="btn ghost" onClick={load}>Buscar</button>
      </div>
      <div className="card table-wrap">
        <table>
          <thead>
            <tr>
              <th>Razón social</th><th>NIT</th><th>Tipo</th><th>Ciudad</th>
              <th>WhatsApp</th><th>Email</th><th>Contacto</th><th></th>
            </tr>
          </thead>
          <tbody>
            {rows.length === 0 && <tr><td colSpan={8} className="muted">Sin registros</td></tr>}
            {rows.map(c => (
              <tr key={c.id}>
                <td><strong>{c.razonSocial}</strong></td>
                <td>{c.nit}</td>
                <td><span className="tag">{c.tipo}</span></td>
                <td>{c.ciudad}</td>
                <td>{c.whatsapp}</td>
                <td>{c.emailPrincipal}</td>
                <td>{c.contactoNombre}</td>
                <td><button className="btn ghost sm" onClick={() => { setForm(c); setOpen(true) }}>Editar</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {open && (
        <div className="modal-backdrop" onClick={() => setOpen(false)}>
          <form className="modal-sheet form-sheet" onClick={e => e.stopPropagation()} onSubmit={save}>
            <div className="form-sheet-header">
              <div className="brand-block">
                <h1>Nexo Support</h1>
                <p className="sub">Ficha de cliente</p>
                <div className="bars"><span /><span /></div>
              </div>
              <div className="logo-slot">
                {empresa?.logoRuta && (
                  <img src={(api.defaults.baseURL || '') + '/api/empresa/logo'} alt="Logo" />
                )}
              </div>
              <div className="meta">
                <strong>{empresa?.razonSocial || 'DILASER S.A.'}</strong>
                NIT {empresa?.nit || '811.046.078-4'}<br />
                {empresa?.direccionMedellin || 'Cra. 33 No. 7-77'} · {empresa?.ciudadMedellin || 'Medellín'}<br />
                {empresa?.telefonoMedellin || '(4) 311 2280'}
              </div>
            </div>
            <div className="form-title-bar">
              {form.id ? 'Actualizar cliente' : 'Registro de nuevo cliente'}
            </div>
            <div className="form-body">
              {err && <div className="alert error">{err}</div>}

              <div className="form-section">
                <h3 className="form-section-title">Identificación</h3>
                <div className="form-row cols-2">
                  <div className="field">
                    <label>Razón social *</label>
                    <input required value={form.razonSocial || ''} onChange={e => setForm({ ...form, razonSocial: e.target.value })} />
                  </div>
                  <div className="field">
                    <label>Nombre comercial</label>
                    <input value={form.nombreComercial || ''} onChange={e => setForm({ ...form, nombreComercial: e.target.value })} />
                  </div>
                </div>
                <div className="form-row cols-2">
                  <div className="field">
                    <label>NIT / C.C.</label>
                    <input value={form.nit || ''} onChange={e => setForm({ ...form, nit: e.target.value })} />
                  </div>
                  <div className="field">
                    <label>Tipo de cliente</label>
                    <select value={form.tipo || 'CLINICA'} onChange={e => setForm({ ...form, tipo: e.target.value })}>
                      <option value="CLINICA">Clínica</option>
                      <option value="CONSULTORIO">Consultorio</option>
                      <option value="MEDICO">Médico / Doctor</option>
                      <option value="HOSPITAL">Hospital</option>
                      <option value="SPA">Spa / Estética</option>
                      <option value="INTERNO_DILASER">Interno Dilaser</option>
                    </select>
                  </div>
                </div>
              </div>

              <div className="form-section">
                <h3 className="form-section-title">Ubicación y contacto</h3>
                <div className="form-row cols-2">
                  <div className="field">
                    <label>Dirección</label>
                    <input value={form.direccion || ''} onChange={e => setForm({ ...form, direccion: e.target.value })} />
                  </div>
                  <div className="field">
                    <label>Ciudad</label>
                    <input value={form.ciudad || ''} onChange={e => setForm({ ...form, ciudad: e.target.value })} />
                  </div>
                </div>
                <div className="form-row cols-3">
                  <div className="field">
                    <label>Departamento</label>
                    <input value={form.departamento || ''} onChange={e => setForm({ ...form, departamento: e.target.value })} />
                  </div>
                  <div className="field">
                    <label>Teléfono</label>
                    <input value={form.telefono || ''} onChange={e => setForm({ ...form, telefono: e.target.value })} />
                  </div>
                  <div className="field">
                    <label>WhatsApp</label>
                    <input value={form.whatsapp || ''} onChange={e => setForm({ ...form, whatsapp: e.target.value })} />
                  </div>
                </div>
                <div className="form-row cols-2">
                  <div className="field">
                    <label>Email principal</label>
                    <input type="email" value={form.emailPrincipal || ''} onChange={e => setForm({ ...form, emailPrincipal: e.target.value })} />
                  </div>
                  <div className="field">
                    <label>Contacto (nombre)</label>
                    <input value={form.contactoNombre || ''} onChange={e => setForm({ ...form, contactoNombre: e.target.value })} />
                  </div>
                </div>
                <div className="form-row cols-2">
                  <div className="field">
                    <label>Cargo del contacto</label>
                    <input value={form.contactoCargo || ''} onChange={e => setForm({ ...form, contactoCargo: e.target.value })} />
                  </div>
                </div>
              </div>

              <div className="form-section">
                <h3 className="form-section-title">Notas / comentarios</h3>
                <div className="field">
                  <textarea rows={3} placeholder="Observaciones internas…"
                    value={form.observaciones || ''} onChange={e => setForm({ ...form, observaciones: e.target.value })} />
                </div>
              </div>

              <div className="form-actions">
                <button type="button" className="btn ghost" onClick={() => setOpen(false)}>Cancelar</button>
                <button className="btn" type="submit">Guardar cliente</button>
              </div>
            </div>
          </form>
        </div>
      )}
    </div>
  )
}
