import { useEffect, useState } from 'react'
import http from '../api/http'

export default function Dashboard() {
  const [k, setK] = useState(null)
  useEffect(() => { http.get('/dashboard/kpis').then(r => setK(r.data)).catch(() => setK({})) }, [])
  const items = [
    ['Equipos', k?.equipos ?? '—'],
    ['RMAs', k?.rmas ?? '—'],
    ['Preventivos del mes', k?.alertasMes ?? '—'],
    ['Stock crítico', k?.stockCritico ?? '—'],
    ['Valor inventario', k?.valorInventario ?? '—'],
  ]
  return (
    <div>
      <h2>Dashboard</h2>
      <p className="muted">Indicadores de soporte · colores Dilaser</p>
      <div className="cards">
        {items.map(([t, n]) => (
          <div className="card" key={t}><h3>{t}</h3><div className="n">{n}</div></div>
        ))}
      </div>
    </div>
  )
}
