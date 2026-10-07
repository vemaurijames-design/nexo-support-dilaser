import { useEffect, useMemo, useState } from 'react'
import http from '../api/http'

const MESES = ['Enero','Febrero','Marzo','Abril','Mayo','Junio','Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre']

export default function Cronograma() {
  const [alertas, setAlertas] = useState([])
  const [hist, setHist] = useState([])
  const [anio, setAnio] = useState(new Date().getFullYear())
  const [mes, setMes] = useState(new Date().getMonth() + 1)
  const [diaSel, setDiaSel] = useState(null)
  const [err, setErr] = useState('')
  const [msg, setMsg] = useState('')

  const load = () => {
    http.get('/mantenimiento/alertas', { params: { anio, mes } })
      .then(r => setAlertas(Array.isArray(r.data) ? r.data : []))
      .catch(() => setAlertas([]))
    http.get('/mantenimiento/historial').then(r => setHist(r.data || [])).catch(() => setHist([]))
  }
  useEffect(() => { load() }, [anio, mes])

  const dias = useMemo(() => Array.from({ length: new Date(anio, mes, 0).getDate() }, (_, i) => i + 1), [anio, mes])
  const delDia = (d) => alertas.filter(a => Number(String(a.fechaObjetivo || '').slice(8, 10)) === d)
  const tipo = (a) => a.equipo?.garantiaFin && String(a.equipo.garantiaFin) >= String(a.fechaObjetivo) ? 'GARANTÍA' : 'NORMAL'

  const recalcular = async () => {
    setErr(''); setMsg('')
    try {
      await http.post('/mantenimiento/alertas/recalcular')
      setMsg('Alertas recalculadas. Elige el mes del servicio.')
      load()
    } catch (e) { setErr(e.response?.data?.message || 'No se pudo recalcular') }
  }

  const enviar = async (a) => {
    const to = window.prompt('Correo del cliente')
    if (!to) return
    const cc = localStorage.getItem('nexo_email') || ''
    const garantia = tipo(a) === 'GARANTÍA'
    const cuerpo = `Equipo ${a.equipo?.serial || ''} de ${a.cliente?.razonSocial || ''}. Servicio ${garantia ? 'en garantía' : 'normal'} programado para ${a.fechaObjetivo}. Si desea agendarlo, responda este correo o comuníquese con soporte técnico Dilaser.`
    try {
      await http.post(`/mantenimiento/alertas/${a.id}/notificar`, { to, cc, cuerpo })
      setMsg('Notificación enviada. Copia a ' + (cc || 'tu usuario'))
      load()
    } catch (e) { setErr(e.response?.data?.message || 'No se pudo enviar') }
  }

  const wa = (a) => {
    const texto = `Hola, Dilaser Soporte Técnico. El equipo ${a.equipo?.serial || ''} (${a.equipo?.nombreEquipo || ''}) de ${a.cliente?.razonSocial || 'su institución'} tiene mantenimiento ${tipo(a)} programado para el ${a.fechaObjetivo || ''}. Si está interesado en el servicio, responda este mensaje y coordinamos la visita.`
    window.open('https://wa.me/?text=' + encodeURIComponent(texto), '_blank')
  }

  return (
    <div>
      <div className="page-head">
        <div>
          <h2>Cronograma de preventivos</h2>
          <p className="muted">Mes y día del servicio · garantía o normal · correo con copia al emisor</p>
        </div>
        <button className="btn" onClick={recalcular}>Recalcular alertas</button>
      </div>
      {err && <div className="alert error">{err}</div>}
      {msg && <div className="alert ok">{msg}</div>}
      <div className="toolbar">
        <label>Año <input type="number" value={anio} onChange={e => setAnio(Number(e.target.value))} style={{ width: 90 }} /></label>
        <label>Mes
          <select value={mes} onChange={e => { setMes(Number(e.target.value)); setDiaSel(null) }}>
            {MESES.map((n, i) => <option key={n} value={i + 1}>{n}</option>)}
          </select>
        </label>
      </div>
      <div className="card" style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: 6 }}>
        {dias.map(d => (
          <div key={d} onClick={() => setDiaSel(d)} style={{ border: '1px solid #d7ebee', borderRadius: 8, minHeight: 72, padding: 6, cursor: 'pointer', background: diaSel === d ? '#e7f6f8' : '#fff' }}>
            <strong>{d}</strong>
            {delDia(d).map(a => <div key={a.id} style={{ fontSize: 11, color: '#03738C' }}>{a.equipo?.serial}</div>)}
          </div>
        ))}
      </div>
      {diaSel && (
        <div className="card" style={{ marginTop: 12 }}>
          <strong>Día {diaSel} de {MESES[mes - 1]} {anio}</strong>
          {!delDia(diaSel).length && <p>Sin equipos este día.</p>}
          {delDia(diaSel).map(a => (
            <p key={a.id}>{a.equipo?.serial} · {a.equipo?.nombreEquipo || 'equipo'} · {a.cliente?.razonSocial} · {tipo(a)} · {a.fechaObjetivo}</p>
          ))}
        </div>
      )}
      <h3>Servicios de {MESES[mes - 1]} {anio}</h3>
      <div className="card table-wrap">
        <table>
          <thead><tr><th>Fecha</th><th>Serial</th><th>Equipo</th><th>Cliente</th><th>Tipo</th><th>Estado</th><th></th></tr></thead>
          <tbody>
            {alertas.map(a => (
              <tr key={a.id}>
                <td>{a.fechaObjetivo}</td>
                <td>{a.equipo?.serial}</td>
                <td>{a.equipo?.nombreEquipo || '—'}</td>
                <td>{a.cliente?.razonSocial}</td>
                <td>{tipo(a)}</td>
                <td>{a.estado}{a.enviadoA ? ' · ' + a.enviadoA : ''}</td>
                <td>
                  <button className="btn ghost sm" onClick={() => enviar(a)}>Correo</button>
                  <button className="btn ghost sm" onClick={() => wa(a)}>WhatsApp</button>
                </td>
              </tr>
            ))}
            {!alertas.length && <tr><td colSpan={7}>No hay servicios este mes. Pulsa Recalcular y revisa la fecha de instalación de la hoja de vida.</td></tr>}
          </tbody>
        </table>
      </div>
      <h3>Historial</h3>
      <div className="card table-wrap">
        <table>
          <thead><tr><th>Fecha</th><th>Serial</th><th>Cliente</th><th>Tipo</th><th>Ingeniero</th><th>Observaciones</th></tr></thead>
          <tbody>
            {hist.map(h => <tr key={h.id}><td>{h.fecha}</td><td>{h.serial}</td><td>{h.cliente}</td><td>{h.tipo}</td><td>{h.ingeniero}</td><td>{h.actividades}</td></tr>)}
            {!hist.length && <tr><td colSpan={6}>Aún no hay mantenimientos registrados.</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  )
}
