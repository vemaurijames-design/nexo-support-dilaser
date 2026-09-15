import { useEffect, useState } from 'react'
import http from '../api/http'

export default function Cronograma() {
  const [alertas, setAlertas] = useState([])
  const load = () => http.get('/mantenimiento/alertas').then(r => setAlertas(r.data))
  useEffect(() => { load() }, [])
  return (
    <div>
      <div className="top">
        <h2>Cronograma de preventivos</h2>
        <button className="btn" onClick={() => http.post('/mantenimiento/alertas/recalcular').then(load)}>Recalcular alertas</button>
      </div>
      <p className="muted">Algoritmo anual anclado al mes del acta / instalación. A 45 días pasa a pendiente; vencidas en rojo en una siguiente iteración de UI.</p>
      <table>
        <thead><tr><th>Año</th><th>Mes</th><th>Serial</th><th>Cliente</th><th>Estado</th><th>Fecha objetivo</th></tr></thead>
        <tbody>
          {alertas.map(a => (
            <tr key={a.id}>
              <td>{a.anio}</td><td>{a.mes}</td>
              <td>{a.equipo?.serial}</td>
              <td>{a.cliente?.razonSocial}</td>
              <td>{a.estado}</td>
              <td>{a.fechaObjetivo}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
