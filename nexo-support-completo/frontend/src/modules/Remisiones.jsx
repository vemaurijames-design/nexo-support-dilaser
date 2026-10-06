import { useEffect, useState } from 'react'
import api from '../api/client.js'

const emptyItem = () => ({ referencia: '', serialLote: '', descripcion: '', cantidad: 1 })
const empty = {
  fecha: new Date().toISOString().slice(0, 10),
  telefonoContacto: '', numeroFactura: '', guiaNumero: '',
  clienteId: null, clienteNombre: '', clienteDireccion: '', empresaDestino: '',
  transportadora: 'OTROS', motivo: 'VENTA', observaciones: '',
  elaboroNombre: '', recibioNombre: '', estado: 'BORRADOR',
  items: [emptyItem(), emptyItem(), emptyItem()]
}

export default function Remisiones() {
  const [rows, setRows] = useState([])
  const [q, setQ] = useState('')
  const [open, setOpen] = useState(false)
  const [form, setForm] = useState(empty)
  const [err, setErr] = useState('')
  const [empresa, setEmpresa] = useState(null)

  const load = async () => {
    const { data } = await api.get('/api/remisiones', { params: { q: q || undefined } })
    setRows(data)
  }
  useEffect(() => {
    load().catch(() => {})
    api.get('/api/empresa').then(r => setEmpresa(r.data)).catch(() => {})
  }, [])

  const save = async (e) => {
    e.preventDefault()
    setErr('')
    const items = (form.items || []).filter(i => (i.descripcion || '').trim())
    if (!items.length) { setErr('Agrega al menos un ítem con descripción'); return }
    try {
      const payload = { ...form, items }
      if (form.id) await api.put(`/api/remisiones/${form.id}`, payload)
      else await api.post('/api/remisiones', payload)
      setOpen(false)
      setForm(empty)
      load()
    } catch (ex) {
      setErr(ex.response?.data?.message || 'Error al guardar')
    }
  }

  const setItem = (idx, key, val) => {
    const items = [...(form.items || [])]
    items[idx] = { ...items[idx], [key]: val }
    setForm({ ...form, items })
  }

  return (
    <div>
      <div className="page-head">
        <div>
          <h2>Remisiones</h2>
          <p className="muted">Salida de repuestos y equipos · formato Dilaser</p>
        </div>
        <button className="btn" onClick={() => {
          setForm({ ...empty, elaboroNombre: localStorage.getItem('nexo_nombre') || '', items: [emptyItem(), emptyItem(), emptyItem()] })
          setOpen(true)
        }}>+ Nueva remisión</button>
      </div>
      {err && !open && <div className="alert error">{err}</div>}
      <div className="toolbar">
        <input className="search" placeholder="Buscar por cliente…" value={q} onChange={e => setQ(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && load()} />
        <button className="btn ghost" onClick={load}>Buscar</button>
      </div>
      <div className="card table-wrap">
        <table>
          <thead>
            <tr><th>Número</th><th>Fecha</th><th>Cliente</th><th>Motivo</th><th>Transporte</th><th>Estado</th><th></th></tr>
          </thead>
          <tbody>
            {rows.map(r => (
              <tr key={r.id}>
                <td><strong>{r.numero}</strong></td>
                <td>{r.fecha}</td>
                <td>{r.clienteNombre}</td>
                <td>{r.motivo}</td>
                <td>{r.transportadora}</td>
                <td><span className="tag">{r.estado}</span></td>
                <td>
                  <button className="btn ghost sm" onClick={() => {
                    setForm({ ...r, items: r.items?.length ? r.items : [emptyItem()] })
                    setOpen(true)
                  }}>Editar</button>
                </td>
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
                <h1>Remisión</h1>
                <p className="sub">{form.numero || 'Nueva'}</p>
                <div className="bars"><span /><span /></div>
              </div>
              <div className="logo-slot">
                {empresa?.logoRuta && (
                  <img src={(api.defaults.baseURL || '') + '/api/empresa/logo'} alt="Logo" />
                )}
              </div>
              <div className="meta">
                <strong>{empresa?.razonSocial || 'DILASER S.A.S'}</strong>
                NIT {empresa?.nit || '811.046.078-4'}<br />
                Medellín: {empresa?.telefonoMedellin || '(4) 311 2280'}<br />
                Bogotá: {empresa?.telefonoBogota || '(601) 622 3358'}<br />
                {empresa?.web || 'www.dilaser.com.co'}
              </div>
            </div>
            <div className="form-title-bar">Documento de remisión / salida</div>
            <div className="form-body">
              {err && <div className="alert error">{err}</div>}

              <div className="form-section">
                <h3 className="form-section-title">Datos del documento</h3>
                <div className="form-row cols-3">
                  <div className="field">
                    <label>Fecha *</label>
                    <input type="date" required value={form.fecha || ''} onChange={e => setForm({ ...form, fecha: e.target.value })} />
                  </div>
                  <div className="field">
                    <label>No. Factura</label>
                    <input value={form.numeroFactura || ''} onChange={e => setForm({ ...form, numeroFactura: e.target.value })} />
                  </div>
                  <div className="field">
                    <label>Guía No.</label>
                    <input value={form.guiaNumero || ''} onChange={e => setForm({ ...form, guiaNumero: e.target.value })} />
                  </div>
                </div>
              </div>

              <div className="form-section">
                <h3 className="form-section-title">Cliente / destino</h3>
                <div className="form-row cols-2">
                  <div className="field">
                    <label>Cliente / Doctor *</label>
                    <input required value={form.clienteNombre || ''} onChange={e => setForm({ ...form, clienteNombre: e.target.value })} />
                  </div>
                  <div className="field">
                    <label>Teléfono</label>
                    <input value={form.telefonoContacto || ''} onChange={e => setForm({ ...form, telefonoContacto: e.target.value })} />
                  </div>
                </div>
                <div className="form-row cols-2">
                  <div className="field">
                    <label>Dirección</label>
                    <input value={form.clienteDireccion || ''} onChange={e => setForm({ ...form, clienteDireccion: e.target.value })} />
                  </div>
                  <div className="field">
                    <label>Empresa destino</label>
                    <input value={form.empresaDestino || ''} onChange={e => setForm({ ...form, empresaDestino: e.target.value })} />
                  </div>
                </div>
              </div>

              <div className="form-section">
                <h3 className="form-section-title">Transporte y motivo</h3>
                <div className="form-row cols-2">
                  <div>
                    <p className="lbl" style={{ fontSize: 12, fontWeight: 600, marginBottom: 8 }}>Transportadora</p>
                    <div className="check-grid">
                      {['TC', 'SOBREENTREGA', 'DEPRISA', 'OTROS'].map(t => (
                        <label key={t} className="check-item">
                          <input type="radio" name="trans" checked={form.transportadora === t}
                            onChange={() => setForm({ ...form, transportadora: t })} />
                          {t === 'SOBREENTREGA' ? 'Sobreentrega' : t.charAt(0) + t.slice(1).toLowerCase()}
                        </label>
                      ))}
                    </div>
                  </div>
                  <div>
                    <p className="lbl" style={{ fontSize: 12, fontWeight: 600, marginBottom: 8 }}>Motivo</p>
                    <div className="check-grid">
                      {[
                        ['VENTA', 'Venta (V)'],
                        ['ALQUILER', 'Alquiler (A)'],
                        ['PRESTAMO', 'Préstamo (P)'],
                        ['DEVOLUCION', 'Devolución (D)'],
                        ['GARANTIA', 'Garantía (G)'],
                        ['REPARACION', 'Reparación (R)'],
                        ['TRASLADO', 'Traslado (T)']
                      ].map(([v, l]) => (
                        <label key={v} className="check-item">
                          <input type="radio" name="motivo" checked={form.motivo === v}
                            onChange={() => setForm({ ...form, motivo: v })} />
                          {l}
                        </label>
                      ))}
                    </div>
                  </div>
                </div>
              </div>

              <div className="form-section">
                <h3 className="form-section-title">Detalle de ítems</h3>
                <table className="form-items">
                  <thead>
                    <tr>
                      <th style={{ width: '18%' }}>Referencia</th>
                      <th style={{ width: '18%' }}>Serie / Lote</th>
                      <th>Descripción</th>
                      <th style={{ width: '10%' }}>Cant.</th>
                    </tr>
                  </thead>
                  <tbody>
                    {(form.items || []).map((it, idx) => (
                      <tr key={idx}>
                        <td><input value={it.referencia || ''} onChange={e => setItem(idx, 'referencia', e.target.value)} placeholder="Ref." /></td>
                        <td><input value={it.serialLote || ''} onChange={e => setItem(idx, 'serialLote', e.target.value)} placeholder="S/N" /></td>
                        <td><input value={it.descripcion || ''} onChange={e => setItem(idx, 'descripcion', e.target.value)} placeholder="Descripción del ítem" /></td>
                        <td><input type="number" min="0.01" step="0.01" value={it.cantidad || 1}
                          onChange={e => setItem(idx, 'cantidad', e.target.value)} /></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                <button type="button" className="btn ghost sm"
                  onClick={() => setForm({ ...form, items: [...(form.items || []), emptyItem()] })}>
                  + Agregar línea
                </button>
              </div>

              <div className="form-section">
                <h3 className="form-section-title">Firmas y observaciones</h3>
                <div className="form-row cols-2">
                  <div className="field">
                    <label>Elaboró</label>
                    <input value={form.elaboroNombre || ''} onChange={e => setForm({ ...form, elaboroNombre: e.target.value })} />
                  </div>
                  <div className="field">
                    <label>Recibió</label>
                    <input value={form.recibioNombre || ''} onChange={e => setForm({ ...form, recibioNombre: e.target.value })} />
                  </div>
                </div>
                <div className="field" style={{ marginTop: 10 }}>
                  <label>Observaciones</label>
                  <textarea rows={2} value={form.observaciones || ''}
                    onChange={e => setForm({ ...form, observaciones: e.target.value })} />
                </div>
              </div>

              <div className="form-actions">
                <button type="button" className="btn ghost" onClick={() => setOpen(false)}>Cancelar</button>
                <button className="btn" type="submit">Guardar remisión</button>
              </div>
            </div>
          </form>
        </div>
      )}
    </div>
  )
}
