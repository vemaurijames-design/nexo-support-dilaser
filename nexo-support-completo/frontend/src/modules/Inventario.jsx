import { useEffect, useMemo, useState } from 'react'
import http from '../api/http'

const emptyItem = {
  referencia: '', descripcion: '', unidadMedida: 'UND',
  costoPromedio: 0, precioSugerido: 0, stockMinimo: 0, observaciones: '', activo: true
}

export default function Inventario() {
  const [rows, setRows] = useState([])
  const [stock, setStock] = useState([])
  const [bodegas, setBodegas] = useState([])
  const [q, setQ] = useState('')
  const [err, setErr] = useState('')
  const [msg, setMsg] = useState('')
  const [form, setForm] = useState(emptyItem)
  const [open, setOpen] = useState(false)
  const [detalle, setDetalle] = useState(null)
  const [kardex, setKardex] = useState([])
  const [stockItem, setStockItem] = useState([])
  const [sendingXls, setSendingXls] = useState(false)

  const load = async () => {
    setErr('')
    try {
      const [r, s, b] = await Promise.all([
        http.get('/inventario/repuestos', { params: q ? { q } : {} }),
        http.get('/inventario/stock'),
        http.get('/catalogos/bodegas')
      ])
      setRows(r.data || [])
      setStock(s.data || [])
      setBodegas(b.data || [])
    } catch (e) {
      setErr(e.response?.data?.message || 'Error al cargar inventario')
    }
  }
  useEffect(() => { load() }, [])

  const stockByRepuesto = useMemo(() => {
    const m = {}
    stock.forEach(s => {
      const id = s.repuesto?.id
      if (!id) return
      if (!m[id]) m[id] = { total: 0, lineas: [] }
      const cant = Number(s.existencia || 0)
      m[id].total += cant
      m[id].lineas.push({ bodega: s.bodega?.nombre, existencia: cant })
    })
    return m
  }, [stock])

  const saveItem = async (e) => {
    e.preventDefault()
    setErr('')
    try {
      const body = {
        ...form,
        costoPromedio: Number(form.costoPromedio || 0),
        precioSugerido: Number(form.precioSugerido || 0),
        stockMinimo: Number(form.stockMinimo || 0)
      }
      if (form.id) await http.put(`/inventario/repuestos/${form.id}`, body)
      else await http.post('/inventario/repuestos', body)
      setOpen(false)
      setForm(emptyItem)
      setMsg(form.id ? 'Repuesto actualizado' : 'Repuesto creado')
      load()
    } catch (ex) {
      setErr(ex.response?.data?.message || 'No se pudo guardar el repuesto')
    }
  }

  const ver = async (item) => {
    setDetalle(item)
    try {
      const [st, kx] = await Promise.all([
        http.get(`/inventario/repuestos/${item.id}/stock`),
        http.get(`/inventario/repuestos/${item.id}/kardex`)
      ])
      setStockItem(st.data || [])
      setKardex(kx.data || [])
    } catch {
      setStockItem([])
      setKardex([])
    }
  }

  const mover = async (e) => {
    e.preventDefault()
    setErr('')
    try {
      await http.post('/inventario/movimientos', {
        tipo: mov.tipo,
        cantidad: Number(mov.cantidad),
        costoUnitario: Number(mov.costoUnitario || 0),
        recibidoPorNombre: mov.recibidoPorNombre,
        observaciones: mov.observaciones,
        repuesto: { id: mov.repuestoId },
        bodegaDestino: mov.bodegaDestinoId ? { id: Number(mov.bodegaDestinoId) } : null,
        bodegaOrigen: mov.bodegaOrigenId ? { id: Number(mov.bodegaOrigenId) } : null
      })
      setMsg('Movimiento registrado')
      setMov({ tipo: 'ENTRADA_COMPRA', cantidad: 1, costoUnitario: 0 })
      load()
      if (detalle) ver(detalle)
    } catch (ex) {
      setErr(ex.response?.data?.message || 'No se pudo registrar el movimiento')
    }
  }

  return (
    <div>
      <div className="page-head">
        <div>
          <h2>Inventario de repuestos</h2>
          <p className="muted">Alta de ítems, existencias por bodega y kardex</p>
        </div>
        <button className="btn" onClick={() => { setForm(emptyItem); setOpen(true) }}>+ Nuevo repuesto</button>
      </div>
      {err && <div className="alert error">{err}</div>}
      {msg && <div className="alert ok">{msg}</div>}

      <div className="toolbar">
        <input className="search" placeholder="Filtrar por referencia o descripción…"
          value={q} onChange={e => setQ(e.target.value)} onKeyDown={e => e.key === 'Enter' && load()} />
        <button className="btn ghost" onClick={load}>Buscar</button>
        <label className="btn ghost" style={{ cursor: 'pointer' }}>
          {sendingXls ? 'Importando…' : 'Importar Excel'}
          <input type="file" accept=".xlsx,.xls" hidden onChange={async e => {
            const f = e.target.files?.[0]
            e.target.value = ''
            if (!f) return
            setSendingXls(true); setErr(''); setMsg('')
            try {
              const fd = new FormData()
              fd.append('file', f)
              const { data } = await http.post('/inventario/importar', fd)
              setMsg(data.message || 'Importación lista')
              load()
            } catch (ex) {
              setErr(ex.response?.data?.message || 'No se pudo importar el Excel')
            } finally { setSendingXls(false) }
          }} />
        </label>
        <button className="btn ghost" onClick={async () => {
          const r = await http.get('/inventario/exportar', { responseType: 'blob' })
          const url = URL.createObjectURL(r.data)
          const a = document.createElement('a')
          a.href = url; a.download = 'inventario-nexo.xlsx'; a.click()
        }}>Exportar Excel</button>
        <button className="btn ghost" onClick={async () => {
          const r = await http.get('/inventario/pdf', { responseType: 'blob' })
          window.open(URL.createObjectURL(r.data), '_blank')
        }}>PDF stock</button>
      </div>

      <div className="card table-wrap">
        <table>
          <thead>
            <tr>
              <th>Referencia</th><th>Descripción</th><th>Unidad</th>
              <th>Stock total</th><th>Mínimo</th><th>Costo</th><th></th>
            </tr>
          </thead>
          <tbody>
            {rows.map(r => {
              const tot = stockByRepuesto[r.id]?.total ?? 0
              const bajo = tot <= Number(r.stockMinimo || 0)
              return (
                <tr key={r.id}>
                  <td><strong>{r.referencia}</strong></td>
                  <td>{r.descripcion}</td>
                  <td>{r.unidadMedida}</td>
                  <td>{bajo ? <span className="tag">{tot} · bajo mínimo</span> : tot}</td>
                  <td>{r.stockMinimo}</td>
                  <td>{r.costoPromedio}</td>
                  <td style={{ whiteSpace: 'nowrap' }}>
                    <button className="btn ghost sm" onClick={() => ver(r)}>Ver</button>
                    <button className="btn ghost sm" onClick={() => { setForm(r); setOpen(true) }}>Editar</button>
                  </td>
                </tr>
              )
            })}
            {!rows.length && <tr><td colSpan={7} className="muted">Sin repuestos. Crea el primero.</td></tr>}
          </tbody>
        </table>
      </div>

      <h3 style={{ marginTop: 28 }}>Stock por bodega</h3>
      <div className="card table-wrap">
        <table>
          <thead><tr><th>Ref</th><th>Ítem</th><th>Bodega</th><th>Existencia</th></tr></thead>
          <tbody>
            {stock.map(s => (
              <tr key={s.id}>
                <td>{s.repuesto?.referencia}</td>
                <td>{s.repuesto?.descripcion}</td>
                <td>{s.bodega?.nombre}</td>
                <td>{s.existencia}</td>
              </tr>
            ))}
            {!stock.length && <tr><td colSpan={4} className="muted">Sin movimientos de stock todavía</td></tr>}
          </tbody>
        </table>
      </div>

      <form className="card" onSubmit={mover} style={{ marginTop: 20 }}>
        <h3>Movimiento / kardex</h3>
        <div className="grid2">
          <div>
            <label>Repuesto</label>
            <select required value={mov.repuestoId || ''} onChange={e => setMov({ ...mov, repuestoId: e.target.value })}>
              <option value="">Seleccione</option>
              {rows.map(r => <option key={r.id} value={r.id}>{r.referencia} · {r.descripcion}</option>)}
            </select>
          </div>
          <div>
            <label>Tipo</label>
            <select value={mov.tipo} onChange={e => setMov({ ...mov, tipo: e.target.value })}>
              <option>ENTRADA_COMPRA</option><option>ENTRADA_RMA</option>
              <option>SALIDA_GARANTIA</option><option>SALIDA_VENTA</option>
              <option>SALIDA_CONSUMO_INTERNO</option><option>SALIDA_RMA</option>
              <option>TRANSFERENCIA</option>
            </select>
          </div>
          <div>
            <label>Cantidad</label>
            <input type="number" min="0.01" step="0.01" value={mov.cantidad} onChange={e => setMov({ ...mov, cantidad: e.target.value })} />
          </div>
          <div>
            <label>Costo unitario</label>
            <input type="number" min="0" step="0.01" value={mov.costoUnitario || 0} onChange={e => setMov({ ...mov, costoUnitario: e.target.value })} />
          </div>
          <div>
            <label>Bodega destino (entradas / traslado)</label>
            <select value={mov.bodegaDestinoId || ''} onChange={e => setMov({ ...mov, bodegaDestinoId: e.target.value })}>
              <option value="">—</option>
              {bodegas.map(b => <option key={b.id} value={b.id}>{b.nombre}</option>)}
            </select>
          </div>
          <div>
            <label>Bodega origen (salidas / traslado)</label>
            <select value={mov.bodegaOrigenId || ''} onChange={e => setMov({ ...mov, bodegaOrigenId: e.target.value })}>
              <option value="">—</option>
              {bodegas.map(b => <option key={b.id} value={b.id}>{b.nombre}</option>)}
            </select>
          </div>
          <div>
            <label>Recibe</label>
            <input value={mov.recibidoPorNombre || ''} onChange={e => setMov({ ...mov, recibidoPorNombre: e.target.value })} />
          </div>
          <div>
            <label>Observación</label>
            <input value={mov.observaciones || ''} onChange={e => setMov({ ...mov, observaciones: e.target.value })} />
          </div>
        </div>
        <p><button className="btn sec" type="submit">Registrar movimiento</button></p>
      </form>

      {open && (
        <div className="modal-backdrop" onClick={() => setOpen(false)}>
          <form className="modal-sheet form-sheet" onClick={e => e.stopPropagation()} onSubmit={saveItem}>
            <div className="form-title-bar">{form.id ? 'Editar repuesto' : 'Nuevo repuesto'}</div>
            <div className="form-body">
              <div className="form-row cols-2">
                <div className="field">
                  <label>Referencia *</label>
                  <input required value={form.referencia || ''} onChange={e => setForm({ ...form, referencia: e.target.value })} disabled={!!form.id} />
                </div>
                <div className="field">
                  <label>Unidad</label>
                  <input value={form.unidadMedida || 'UND'} onChange={e => setForm({ ...form, unidadMedida: e.target.value })} />
                </div>
              </div>
              <div className="field">
                <label>Descripción *</label>
                <input required value={form.descripcion || ''} onChange={e => setForm({ ...form, descripcion: e.target.value })} />
              </div>
              <div className="form-row cols-3">
                <div className="field">
                  <label>Costo promedio</label>
                  <input type="number" step="0.01" value={form.costoPromedio || 0} onChange={e => setForm({ ...form, costoPromedio: e.target.value })} />
                </div>
                <div className="field">
                  <label>Precio sugerido</label>
                  <input type="number" step="0.01" value={form.precioSugerido || 0} onChange={e => setForm({ ...form, precioSugerido: e.target.value })} />
                </div>
                <div className="field">
                  <label>Stock mínimo</label>
                  <input type="number" step="0.01" value={form.stockMinimo || 0} onChange={e => setForm({ ...form, stockMinimo: e.target.value })} />
                </div>
              </div>
              <div className="field">
                <label>Observaciones</label>
                <textarea rows={2} value={form.observaciones || ''} onChange={e => setForm({ ...form, observaciones: e.target.value })} />
              </div>
              <div className="form-actions">
                <button type="button" className="btn ghost" onClick={() => setOpen(false)}>Cancelar</button>
                <button className="btn" type="submit">Guardar en base de datos</button>
              </div>
            </div>
          </form>
        </div>
      )}

      {detalle && (
        <div className="modal-backdrop" onClick={() => setDetalle(null)}>
          <div className="modal-sheet form-sheet" onClick={e => e.stopPropagation()}>
            <div className="form-title-bar">{detalle.referencia} · {detalle.descripcion}</div>
            <div className="form-body">
              <p className="muted">Existencia por bodega y movimientos</p>
              <table>
                <thead><tr><th>Bodega</th><th>Cantidad</th></tr></thead>
                <tbody>
                  {stockItem.map(s => (
                    <tr key={s.id}><td>{s.bodega?.nombre}</td><td>{s.existencia}</td></tr>
                  ))}
                  {!stockItem.length && <tr><td colSpan={2}>Sin stock</td></tr>}
                </tbody>
              </table>
              <h3 style={{ marginTop: 16 }}>Kardex</h3>
              <table>
                <thead><tr><th>Fecha</th><th>Tipo</th><th>Cant.</th><th>Obs.</th></tr></thead>
                <tbody>
                  {kardex.map(k => (
                    <tr key={k.id}>
                      <td>{k.creadoEn ? String(k.creadoEn).slice(0, 16) : ''}</td>
                      <td>{k.tipo}</td>
                      <td>{k.cantidad}</td>
                      <td>{k.observaciones}</td>
                    </tr>
                  ))}
                  {!kardex.length && <tr><td colSpan={4}>Sin movimientos</td></tr>}
                </tbody>
              </table>
              <div className="form-actions">
                <button className="btn ghost" type="button" onClick={() => setDetalle(null)}>Cerrar</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
