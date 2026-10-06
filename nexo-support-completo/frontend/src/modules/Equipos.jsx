import { useEffect, useState } from 'react'
import api from '../api/client.js'

const empty = {
  serial: '', modeloId: '', clienteId: '', propiedad: 'CLIENTE', estado: 'ACTIVO',
  nombreEquipo: '', institucionNombre: '', servicioUbicacion: '', codigoInterno: '',
  versionFicha: '1', fechaFicha: new Date().toISOString().slice(0, 10),
  paisOrigen: '', voltageAlimentacion: '', corrienteOperacion: '', potenciaVa: '',
  frecuenciaHz: '', presion: '', capacidad: '',
  anioFabricacion: '', fechaAdquisicion: '', fechaInstalacion: '', fechaPuestaFuncionamiento: '',
  fechaImportacion: '', pesoDeclarado: '', registroSanitario: '', numeroActaEntrega: '',
  garantiaInicio: '', garantiaFin: '', ciudadUbicacion: '', direccionUbicacion: '',
  representante: '', representanteDireccion: '', representanteTelefono: '', representanteEmail: '',
  tecnologiaPredominante: '', fuenteAlimentacion: '', clasificacionBiomedica: '',
  nivelRiesgo: '', usoClinico: '', requiereCalibracion: false,
  periodicidadCalibracion: 'Anual', periodicidadMantenimiento: 'Semestral',
  manuales: '', planos: '', recomendacionesFabricante: '', observaciones: '',
  pulsosActuales: '', potenciaSalidaHp: ''
}

export default function Equipos() {
  const [rows, setRows] = useState([])
  const [clientes, setClientes] = useState([])
  const [modelos, setModelos] = useState([])
  const [empresa, setEmpresa] = useState(null)
  const [q, setQ] = useState('')
  const [open, setOpen] = useState(false)
  const [form, setForm] = useState(empty)
  const [err, setErr] = useState('')

  const load = async () => {
    try {
      const [eq, cl, cat, em] = await Promise.all([
        api.get('/api/equipos', { params: { q: q || undefined } }),
        api.get('/api/clientes'),
        api.get('/api/catalogo/modelos').catch(() => api.get('/api/catalogos/modelos')).catch(() => ({ data: [] })),
        api.get('/api/empresa').catch(() => ({ data: null }))
      ])
      setRows(eq.data)
      setClientes(cl.data || [])
      setModelos(cat.data || [])
      setEmpresa(em.data)
    } catch (e) {
      setErr(e.response?.data?.message || 'Error al cargar')
    }
  }
  useEffect(() => { load() }, [])

  const save = async (e) => {
    e.preventDefault()
    setErr('')
    try {
      const body = {
        ...form,
        modelo: form.modeloId ? { id: form.modeloId } : undefined,
        cliente: form.clienteId ? { id: form.clienteId } : null,
        anioFabricacion: form.anioFabricacion ? Number(form.anioFabricacion) : null,
        pulsosActuales: form.pulsosActuales ? Number(form.pulsosActuales) : null
      }
      if (form.id) await api.put(`/api/equipos/${form.id}`, body)
      else await api.post('/api/equipos', body)
      setOpen(false)
      setForm(empty)
      load()
    } catch (ex) {
      setErr(ex.response?.data?.message || 'No se pudo guardar')
    }
  }

  const f = (k, v) => setForm(prev => ({ ...prev, [k]: v }))

  return (
    <div>
      <div className="page-head">
        <div>
          <h2>Hojas de vida</h2>
          <p className="muted">Ficha técnica biomédica · mantenimiento, verificación y calibración</p>
        </div>
        <button className="btn" onClick={() => { setForm(empty); setOpen(true) }}>+ Nueva hoja de vida</button>
      </div>
      {err && !open && <div className="alert error">{err}</div>}
      <div className="toolbar">
        <input className="search" placeholder="Buscar por serial o nombre…" value={q}
          onChange={e => setQ(e.target.value)} onKeyDown={e => e.key === 'Enter' && load()} />
        <button className="btn ghost" onClick={load}>Buscar</button>
      </div>
      <div className="card table-wrap">
        <table>
          <thead>
            <tr>
              <th>Serial</th><th>Equipo</th><th>Cliente / Institución</th>
              <th>Reg. sanitario</th><th>Ciudad</th><th>Estado</th><th></th>
            </tr>
          </thead>
          <tbody>
            {rows.map(r => (
              <tr key={r.id}>
                <td><strong>{r.serial}</strong></td>
                <td>{r.nombreEquipo || r.modelo?.nombre || '—'}</td>
                <td>{r.cliente?.razonSocial || r.institucionNombre || '—'}</td>
                <td>{r.registroSanitario}</td>
                <td>{r.ciudadUbicacion}</td>
                <td><span className="tag">{r.estado}</span></td>
                <td>
                  <button className="btn ghost sm" onClick={() => {
                    setForm({
                      ...empty, ...r,
                      modeloId: r.modelo?.id || '',
                      clienteId: r.cliente?.id || ''
                    })
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
                <h1>Hoja de vida</h1>
                <p className="sub">Equipo biomédico</p>
                <div className="bars"><span /><span /></div>
              </div>
              <div className="logo-slot">
                {empresa?.logoRuta && <img src={(api.defaults.baseURL || '') + '/api/empresa/logo'} alt="Logo" />}
              </div>
              <div className="meta">
                <strong>{empresa?.razonSocial || 'DILASER S.A.'}</strong>
                NIT {empresa?.nit || '811.046.078-4'}<br />
                {empresa?.telefonoMedellin || '(4) 311 2280'} · {empresa?.telefonoBogota || '(601) 622 3358'}
              </div>
            </div>
            <div className="form-title-bar">Historial de mantenimiento, verificación y calibración</div>
            <div className="form-body">
              {err && <div className="alert error">{err}</div>}

              <div className="form-section">
                <h3 className="form-section-title">Entidad / propietario</h3>
                <div className="form-row cols-2">
                  <div className="field">
                    <label>Cliente</label>
                    <select value={form.clienteId || ''} onChange={e => f('clienteId', e.target.value)}>
                      <option value="">— Dilaser / sin asignar —</option>
                      {clientes.map(c => <option key={c.id} value={c.id}>{c.razonSocial}</option>)}
                    </select>
                  </div>
                  <div className="field">
                    <label>Institución / nombre</label>
                    <input value={form.institucionNombre || ''} onChange={e => f('institucionNombre', e.target.value)} />
                  </div>
                </div>
                <div className="form-row cols-3">
                  <div className="field">
                    <label>Servicio / ubicación</label>
                    <input value={form.servicioUbicacion || ''} onChange={e => f('servicioUbicacion', e.target.value)} placeholder="Ej. Odontología" />
                  </div>
                  <div className="field">
                    <label>Ciudad</label>
                    <input value={form.ciudadUbicacion || ''} onChange={e => f('ciudadUbicacion', e.target.value)} />
                  </div>
                  <div className="field">
                    <label>Dirección ubicación</label>
                    <input value={form.direccionUbicacion || ''} onChange={e => f('direccionUbicacion', e.target.value)} />
                  </div>
                </div>
              </div>

              <div className="form-section">
                <h3 className="form-section-title">Datos del equipo</h3>
                <div className="form-row cols-2">
                  <div className="field">
                    <label>Nombre del equipo</label>
                    <input value={form.nombreEquipo || ''} onChange={e => f('nombreEquipo', e.target.value)} placeholder="Ej. AUTOCLAVE" />
                  </div>
                  <div className="field">
                    <label>Modelo *</label>
                    <select required value={form.modeloId || ''} onChange={e => f('modeloId', e.target.value)}>
                      <option value="">Seleccione marca · modelo</option>
                      {modelos.map(m => (
                        <option key={m.id} value={m.id}>{m.marca?.nombre || m.marcaNombre || ''} · {m.nombre}</option>
                      ))}
                    </select>
                  </div>
                </div>
                <div className="form-row cols-3">
                  <div className="field">
                    <label>Serie (S/N) *</label>
                    <input required value={form.serial || ''} onChange={e => f('serial', e.target.value)} />
                  </div>
                  <div className="field">
                    <label>Código interno</label>
                    <input value={form.codigoInterno || ''} onChange={e => f('codigoInterno', e.target.value)} />
                  </div>
                  <div className="field">
                    <label>Registro sanitario (INVIMA)</label>
                    <input value={form.registroSanitario || ''} onChange={e => f('registroSanitario', e.target.value)} />
                  </div>
                </div>
                <div className="form-row cols-3">
                  <div className="field">
                    <label>Año fabricación</label>
                    <input type="number" value={form.anioFabricacion || ''} onChange={e => f('anioFabricacion', e.target.value)} />
                  </div>
                  <div className="field">
                    <label>Fecha adquisición</label>
                    <input type="date" value={form.fechaAdquisicion || ''} onChange={e => f('fechaAdquisicion', e.target.value)} />
                  </div>
                  <div className="field">
                    <label>Fecha instalación</label>
                    <input type="date" value={form.fechaInstalacion || ''} onChange={e => f('fechaInstalacion', e.target.value)} />
                  </div>
                </div>
                <div className="form-row cols-3">
                  <div className="field">
                    <label>Puesta en funcionamiento</label>
                    <input type="date" value={form.fechaPuestaFuncionamiento || ''} onChange={e => f('fechaPuestaFuncionamiento', e.target.value)} />
                  </div>
                  <div className="field">
                    <label>Fecha importación</label>
                    <input type="date" value={form.fechaImportacion || ''} onChange={e => f('fechaImportacion', e.target.value)} />
                  </div>
                  <div className="field">
                    <label>País de origen</label>
                    <input value={form.paisOrigen || ''} onChange={e => f('paisOrigen', e.target.value)} />
                  </div>
                </div>
                <div className="form-row cols-3">
                  <div className="field">
                    <label>Propiedad</label>
                    <select value={form.propiedad || 'CLIENTE'} onChange={e => f('propiedad', e.target.value)}>
                      <option value="CLIENTE">Cliente</option>
                      <option value="DILASER_ALQUILER">Dilaser alquiler</option>
                      <option value="DILASER_DEMO">Dilaser demo</option>
                      <option value="DILASER_STOCK">Dilaser stock</option>
                    </select>
                  </div>
                  <div className="field">
                    <label>Garantía inicio</label>
                    <input type="date" value={form.garantiaInicio || ''} onChange={e => f('garantiaInicio', e.target.value)} />
                  </div>
                  <div className="field">
                    <label>Garantía fin</label>
                    <input type="date" value={form.garantiaFin || ''} onChange={e => f('garantiaFin', e.target.value)} />
                  </div>
                </div>
              </div>

              <div className="form-section">
                <h3 className="form-section-title">Registro técnico</h3>
                <div className="form-row cols-3">
                  <div className="field">
                    <label>Voltaje de operación</label>
                    <input value={form.voltageAlimentacion || ''} onChange={e => f('voltageAlimentacion', e.target.value)} placeholder="110-120 V" />
                  </div>
                  <div className="field">
                    <label>Corriente</label>
                    <input value={form.corrienteOperacion || ''} onChange={e => f('corrienteOperacion', e.target.value)} />
                  </div>
                  <div className="field">
                    <label>Potencia (VA)</label>
                    <input value={form.potenciaVa || ''} onChange={e => f('potenciaVa', e.target.value)} />
                  </div>
                </div>
                <div className="form-row cols-3">
                  <div className="field">
                    <label>Frecuencia (Hz)</label>
                    <input value={form.frecuenciaHz || ''} onChange={e => f('frecuenciaHz', e.target.value)} />
                  </div>
                  <div className="field">
                    <label>Presión</label>
                    <input value={form.presion || ''} onChange={e => f('presion', e.target.value)} />
                  </div>
                  <div className="field">
                    <label>Capacidad / peso</label>
                    <input value={form.capacidad || form.pesoDeclarado || ''} onChange={e => { f('capacidad', e.target.value); f('pesoDeclarado', e.target.value) }} />
                  </div>
                </div>
                <div className="form-row cols-2">
                  <div className="field">
                    <label>Tecnología predominante</label>
                    <select value={form.tecnologiaPredominante || ''} onChange={e => f('tecnologiaPredominante', e.target.value)}>
                      <option value="">—</option>
                      <option>Eléctrico</option><option>Electrónico</option><option>Mecánico</option>
                      <option>Hidráulico</option><option>Vapor</option><option>Neumático</option>
                      <option>Láser</option><option>Radiofrecuencia</option><option>Luz pulsada</option>
                      <option>Otro</option>
                    </select>
                  </div>
                  <div className="field">
                    <label>Fuente de alimentación</label>
                    <select value={form.fuenteAlimentacion || ''} onChange={e => f('fuenteAlimentacion', e.target.value)}>
                      <option value="">—</option>
                      <option>Eléctrico</option><option>Batería</option><option>Manual</option><option>Otro</option>
                    </select>
                  </div>
                </div>
              </div>

              <div className="form-section">
                <h3 className="form-section-title">Clasificación biomédica y uso</h3>
                <div className="form-row cols-3">
                  <div className="field">
                    <label>Clasificación</label>
                    <select value={form.clasificacionBiomedica || ''} onChange={e => f('clasificacionBiomedica', e.target.value)}>
                      <option value="">—</option>
                      <option>Diagnóstico</option>
                      <option>Tratamiento y mantenimiento de la vida</option>
                      <option>Rehabilitación</option>
                      <option>Prevención</option>
                      <option>Análisis de laboratorio</option>
                    </select>
                  </div>
                  <div className="field">
                    <label>Nivel de riesgo (clase)</label>
                    <select value={form.nivelRiesgo || ''} onChange={e => f('nivelRiesgo', e.target.value)}>
                      <option value="">—</option>
                      <option>Clase I</option>
                      <option>Clase IIa</option>
                      <option>Clase IIb</option>
                      <option>Clase III</option>
                    </select>
                  </div>
                  <div className="field">
                    <label>Uso</label>
                    <select value={form.usoClinico || ''} onChange={e => f('usoClinico', e.target.value)}>
                      <option value="">—</option>
                      <option>Médico</option><option>Básico</option><option>Apoyo</option>
                    </select>
                  </div>
                </div>
                <div className="form-row cols-3">
                  <div className="field">
                    <label>Periodicidad calibración</label>
                    <input value={form.periodicidadCalibracion || ''} onChange={e => f('periodicidadCalibracion', e.target.value)} placeholder="Anual" />
                  </div>
                  <div className="field">
                    <label>Periodicidad mantenimiento</label>
                    <input value={form.periodicidadMantenimiento || ''} onChange={e => f('periodicidadMantenimiento', e.target.value)} placeholder="Semestral" />
                  </div>
                  <div className="field" style={{ justifyContent: 'flex-end' }}>
                    <label className="check-item" style={{ marginTop: 22 }}>
                      <input type="checkbox" checked={!!form.requiereCalibracion}
                        onChange={e => f('requiereCalibracion', e.target.checked)} />
                      Requiere calibración
                    </label>
                  </div>
                </div>
              </div>

              <div className="form-section">
                <h3 className="form-section-title">Representante / proveedor</h3>
                <div className="form-row cols-2">
                  <div className="field">
                    <label>Representante</label>
                    <input value={form.representante || ''} onChange={e => f('representante', e.target.value)} />
                  </div>
                  <div className="field">
                    <label>Email</label>
                    <input value={form.representanteEmail || ''} onChange={e => f('representanteEmail', e.target.value)} />
                  </div>
                </div>
                <div className="form-row cols-2">
                  <div className="field">
                    <label>Dirección</label>
                    <input value={form.representanteDireccion || ''} onChange={e => f('representanteDireccion', e.target.value)} />
                  </div>
                  <div className="field">
                    <label>Teléfono</label>
                    <input value={form.representanteTelefono || ''} onChange={e => f('representanteTelefono', e.target.value)} />
                  </div>
                </div>
              </div>

              <div className="form-section">
                <h3 className="form-section-title">Manuales, planos y recomendaciones</h3>
                <div className="form-row cols-2">
                  <div className="field">
                    <label>Manuales</label>
                    <textarea rows={2} value={form.manuales || ''} onChange={e => f('manuales', e.target.value)} />
                  </div>
                  <div className="field">
                    <label>Planos</label>
                    <textarea rows={2} value={form.planos || ''} onChange={e => f('planos', e.target.value)} />
                  </div>
                </div>
                <div className="field">
                  <label>Recomendaciones del fabricante</label>
                  <textarea rows={2} value={form.recomendacionesFabricante || ''} onChange={e => f('recomendacionesFabricante', e.target.value)} />
                </div>
                <div className="field" style={{ marginTop: 10 }}>
                  <label>Observaciones</label>
                  <textarea rows={2} value={form.observaciones || ''} onChange={e => f('observaciones', e.target.value)} />
                </div>
              </div>

              <div className="form-actions">
                <button type="button" className="btn ghost" onClick={() => setOpen(false)}>Cancelar</button>
                <button className="btn" type="submit">Guardar hoja de vida</button>
              </div>
            </div>
          </form>
        </div>
      )}
    </div>
  )
}
