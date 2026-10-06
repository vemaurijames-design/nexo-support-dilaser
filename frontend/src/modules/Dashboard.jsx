import { useEffect, useState } from 'react'
import http from '../api/http'

export default function Dashboard() {
  const [k, setK] = useState(null)
  useEffect(() => { http.get('/dashboard/kpis').then(r => setK(r.data || {})).catch(() => setK({})) }, [])
  const items = [
    ['Equipos', Number(k?.equipos || 0)],
    ['RMA', Number(k?.rmas || 0)],
    ['Remisiones', Number(k?.remisiones || 0)],
    ['Preventivos', Number(k?.alertasMes || 0)],
    ['Stock crítico', Number(k?.stockCritico || 0)],
  ]
  const max = Math.max(1, ...items.map(i => i[1]))
  return (
    <div>
      <div className="page-head">
        <div>
          <h2>Dashboard</h2>
          <p className="muted">Indicadores de soporte técnico Dilaser</p>
        </div>
      </div>
      <div className="cards">
        {items.map(([t, n]) => (
          <div className="card" key={t}><h3>{t}</h3><div className="n">{n}</div></div>
        ))}
        <div className="card"><h3>Valor inventario</h3><div className="n">{Number(k?.valorInventario || 0).toLocaleString('es-CO')}</div></div>
      </div>
      <div className="card" style={{ marginTop: 16 }}>
        <h3>Comparativo</h3>
        <svg viewBox="0 0 640 220" width="100%" height="220">
          {items.map(([t, n], i) => {
            const h = Math.round((n / max) * 150)
            const x = 40 + i * 120
            return (
              <g key={t}>
                <rect x={x} y={180 - h} width="70" height={h} rx="6" fill="#03738C" />
                <text x={x + 35} y={174 - h} textAnchor="middle" fontSize="12" fill="#12363A">{n}</text>
                <text x={x + 35} y="202" textAnchor="middle" fontSize="11" fill="#03738C">{t}</text>
              </g>
            )
          })}
        </svg>
      </div>
    </div>
  )
}
