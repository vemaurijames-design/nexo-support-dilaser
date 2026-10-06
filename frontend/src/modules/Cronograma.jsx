import { useEffect, useMemo, useState } from 'react'
import http from '../api/http'

export default function Cronograma() {
  const [alertas, setAlertas] = useState([])
  const [anio, setAnio] = useState(new Date().getFullYear())
  const [mes, setMes] = useState(new Date().getMonth() + 1)
  const [err, setErr] = useState('')
  const [msg, setMsg] = useState('')

  const load = () => {
    http.get('/mantenimiento/alertas').then(r => setAlertas(r.data || [])).catch(() => setAlertas([]))
  }
  useEffect(() => { load() }, [])

  const filtradas = useMemo(() => alertas.filter(a => {
    const y = a.anio || (a.fechaObjetivo ? Number(String(a.fechaObjetivo).slice(0, 4)) : anio)
    const m = a.mes || (a.fechaObjetivo ? Number(String(a.fechaObjetivo).slice(5, 7)) : mes)
    return Number(y) === Number(anio) && Number(m) === Number(mes)
  }), [alertas, anio, mes])

  const enviar = async (a) => {
    const to = window.prompt('Correo del cliente:', a.cliente?.emailPrincipal || '')
    if (!to) return
    setErr(''); setMsg('')
    try {
      await http.post(`/mantenimiento/alertas/${a.id}/notificar`, { to, canal: 'EMAIL' })
      setMsg('Recordatorio enviado')
    } catch (e) {
      setErr(e.response?.data?.message || 'No hay endpoint de notificación aún. Usa el correo del usuario logueado en SMTP.')
    }
  }

  return (
    <div>
      <div className="page-head">
        <div>
          <h2>Cronograma de preventivos</h2>
          <p className="muted">Calendario mensual · cliente, equipo y fecha objetivo</p>
        </div>
        <button className="btn" onClick={() => http.post('/mantenimiento/alertas/recalcular').then(load)}>Recalcular alertas</button>
      </div>
      {err && <div className="alert error">{err}</div>}
      {msg && <div className="alert ok">{msg}</div>}
      <div className="toolbar">
        <label>Año</label>
        <input type="number" value={anio} onChange={e => setAnio(Number(e.target.value))} style={{ width: 90 }} />
        <label>Mes</label>
        <select value={mes} onChange={e => setMes(Number(e.target.value))}>
          {[1,2,3,4,5,6,7,8,9,10,11,12].map(m => <option key={m} value={m}>{m}</option>)}
        </select>
        <span className="muted">{filtradas.length} alertas en el periodo</span>
      </div>
      <div className="card table-wrap">
        <table>
          <thead>
            <tr>
              <th>Año</th><th>Mes</th><th>Serial</th><th>Cliente</th>
              <th>Estado</th><th>Fecha objetivo</th><th></th>
            </tr>
          </thead>
          <tbody>
            {filtradas.map(a => (
              <tr key={a.id}>
                <td>{a.anio}</td>
                <td>{a.mes}</td>
                <td>{a.equipo?.serial || a.serial}</td>
                <td>{a.cliente?.razonSocial || a.razonSocial}</td>
                <td><span className="tag">{a.estado}</span></td>
                <td>{a.fechaObjetivo}</td>
                <td><button className="btn ghost sm" onClick={() => enviar(a)}>Enviar recordatorio</button></td>
              </tr>
            ))}
            {!filtradas.length && (
              <tr><td colSpan={7} className="muted">
                No hay alertas este mes. Crea hojas de vida con fecha de instalación y pulsa Recalcular.
              </td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
