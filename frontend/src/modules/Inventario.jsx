import { useEffect, useState } from 'react'
import http from '../api/http'

export default function Inventario() {
  const [rows, setRows] = useState([])
  const [stock, setStock] = useState([])
  const [bodegas, setBodegas] = useState([])
  const [form, setForm] = useState({ referencia: '', descripcion: '', unidadMedida: 'UND', costoPromedio: 0, precioSugerido: 0, stockMinimo: 0 })
  const [mov, setMov] = useState({ tipo: 'ENTRADA_COMPRA', cantidad: 1 })

  const load = () => {
    http.get('/inventario/repuestos').then(r => setRows(r.data))
    http.get('/inventario/stock').then(r => setStock(r.data))
    http.get('/catalogos/bodegas').then(r => setBodegas(r.data))
  }
  useEffect(() => { load() }, [])

  const saveItem = async (e) => {
    e.preventDefault()
    await http.post('/inventario/repuestos', form)
    setForm({ referencia: '', descripcion: '', unidadMedida: 'UND', costoPromedio: 0, precioSugerido: 0, stockMinimo: 0 })
    load()
  }

  const mover = async (e) => {
    e.preventDefault()
    const payload = {
      tipo: mov.tipo,
      cantidad: Number(mov.cantidad),
      costoUnitario: Number(mov.costoUnitario || 0),
      recibidoPorNombre: mov.recibidoPorNombre,
      observaciones: mov.observaciones,
      repuesto: { id: mov.repuestoId },
      bodegaDestino: mov.bodegaDestinoId ? { id: Number(mov.bodegaDestinoId) } : null,
      bodegaOrigen: mov.bodegaOrigenId ? { id: Number(mov.bodegaOrigenId) } : null
    }
    await http.post('/inventario/movimientos', payload)
    load()
  }

  return (
    <div>
      <h2>Inventario de repuestos</h2>
      <div className="grid2">
        <form className="card" onSubmit={saveItem}>
          <h3>Alta manual</h3>
          <label>Referencia</label><input required value={form.referencia} onChange={e => setForm({ ...form, referencia: e.target.value })} />
          <label>Descripción</label><input required value={form.descripcion} onChange={e => setForm({ ...form, descripcion: e.target.value })} />
          <label>Costo promedio</label><input type="number" value={form.costoPromedio} onChange={e => setForm({ ...form, costoPromedio: e.target.value })} />
          <label>Precio sugerido</label><input type="number" value={form.precioSugerido} onChange={e => setForm({ ...form, precioSugerido: e.target.value })} />
          <p><button className="btn" type="submit">Crear ítem</button></p>
        </form>
        <form className="card" onSubmit={mover}>
          <h3>Movimiento / kardex</h3>
          <label>Repuesto</label>
          <select required value={mov.repuestoId || ''} onChange={e => setMov({ ...mov, repuestoId: e.target.value })}>
            <option value="">Seleccione</option>
            {rows.map(r => <option key={r.id} value={r.id}>{r.referencia} · {r.descripcion}</option>)}
          </select>
          <label>Tipo</label>
          <select value={mov.tipo} onChange={e => setMov({ ...mov, tipo: e.target.value })}>
            <option>ENTRADA_COMPRA</option><option>ENTRADA_RMA</option>
            <option>SALIDA_GARANTIA</option><option>SALIDA_VENTA</option>
            <option>SALIDA_CONSUMO_INTERNO</option><option>SALIDA_RMA</option>
            <option>TRANSFERENCIA</option>
          </select>
          <label>Cantidad</label><input type="number" min="0.01" step="0.01" value={mov.cantidad} onChange={e => setMov({ ...mov, cantidad: e.target.value })} />
          <label>Bodega destino (entradas)</label>
          <select value={mov.bodegaDestinoId || ''} onChange={e => setMov({ ...mov, bodegaDestinoId: e.target.value })}>
            <option value="">—</option>
            {bodegas.map(b => <option key={b.id} value={b.id}>{b.nombre}</option>)}
          </select>
          <label>Bodega origen (salidas / traslado)</label>
          <select value={mov.bodegaOrigenId || ''} onChange={e => setMov({ ...mov, bodegaOrigenId: e.target.value })}>
            <option value="">—</option>
            {bodegas.map(b => <option key={b.id} value={b.id}>{b.nombre}</option>)}
          </select>
          <label>Recibe</label><input value={mov.recibidoPorNombre || ''} onChange={e => setMov({ ...mov, recibidoPorNombre: e.target.value })} />
          <p><button className="btn sec" type="submit">Registrar movimiento</button></p>
        </form>
      </div>
      <h3>Stock por bodega</h3>
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
        </tbody>
      </table>
    </div>
  )
}
