import { useEffect, useState } from 'react'
import api from '../api/client.js'

export default function Configuracion() {
  const [form, setForm] = useState({})
  const [msg, setMsg] = useState('')
  const [err, setErr] = useState('')
  const [logoPreview, setLogoPreview] = useState('')

  const load = async () => {
    const { data } = await api.get('/api/empresa')
    setForm(data)
    if (data.logoRuta) setLogoPreview(api.defaults.baseURL + '/api/empresa/logo?t=' + Date.now())
  }
  useEffect(() => { load().catch(e => setErr(e.message)) }, [])

  const save = async (e) => {
    e.preventDefault()
    setMsg(''); setErr('')
    try {
      const { data } = await api.put('/api/empresa', form)
      setForm(data)
      setMsg('Configuración guardada. Se usará en login, formularios y PDFs.')
    } catch (ex) {
      setErr(ex.response?.data?.message || 'Error al guardar')
    }
  }

  const onLogo = async (e) => {
    const file = e.target.files?.[0]
    if (!file) return
    const fd = new FormData()
    fd.append('file', file)
    try {
      await api.post('/api/empresa/logo', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
      setLogoPreview(api.defaults.baseURL + '/api/empresa/logo?t=' + Date.now())
      setMsg('Logo actualizado')
      load()
    } catch (ex) {
      setErr(ex.response?.data?.message || 'Error al subir logo')
    }
  }

  return (
    <div>
      <div className="page-head">
        <div>
          <h2>Configuración de empresa</h2>
          <p className="muted">Logo, datos fiscales y sedes · solo SUPERADMIN</p>
        </div>
      </div>
      {msg && <div className="alert ok">{msg}</div>}
      {err && <div className="alert error">{err}</div>}

      <form className="form-sheet" onSubmit={save}>
        <div className="form-sheet-header">
          <div className="brand-block">
            <h1>Empresa</h1>
            <p className="sub">Configuración general</p>
            <div className="bars"><span /><span /></div>
          </div>
          <div className="logo-slot">
            {logoPreview ? <img src={logoPreview} alt="Logo" onError={() => setLogoPreview('')} /> : null}
          </div>
          <div className="meta">
            <strong>Nexo Support</strong>
            Datos que aparecen en login, remisiones y PDFs
          </div>
        </div>
        <div className="form-title-bar">Identidad corporativa</div>
        <div className="form-body">
          <div className="form-section">
            <h3 className="form-section-title">Logo</h3>
            <div className="logo-row">
              <div className="logo-box">
                {logoPreview
                  ? <img src={logoPreview} alt="Logo" onError={() => setLogoPreview('')} />
                  : <span className="muted">Sin logo</span>}
              </div>
              <div>
                <label className="btn ghost">
                  Adjuntar logo
                  <input type="file" accept="image/*" hidden onChange={onLogo} />
                </label>
                <p className="muted sm">PNG o JPG · fondo transparente recomendado</p>
              </div>
            </div>
          </div>

          <div className="form-section">
            <h3 className="form-section-title">Datos legales</h3>
            <div className="form-row cols-2">
              <div className="field">
                <label>Razón social</label>
                <input value={form.razonSocial || ''} onChange={e => setForm({ ...form, razonSocial: e.target.value })} />
              </div>
              <div className="field">
                <label>Nombre comercial</label>
                <input value={form.nombreComercial || ''} onChange={e => setForm({ ...form, nombreComercial: e.target.value })} />
              </div>
            </div>
            <div className="form-row cols-3">
              <div className="field">
                <label>NIT</label>
                <input value={form.nit || ''} onChange={e => setForm({ ...form, nit: e.target.value })} />
              </div>
              <div className="field">
                <label>Email</label>
                <input value={form.email || ''} onChange={e => setForm({ ...form, email: e.target.value })} />
              </div>
              <div className="field">
                <label>Web</label>
                <input value={form.web || ''} onChange={e => setForm({ ...form, web: e.target.value })} />
              </div>
            </div>
            <div className="form-row cols-2">
              <div className="field">
                <label>Color primario</label>
                <input type="color" value={form.colorPrimario || '#03738C'}
                  onChange={e => setForm({ ...form, colorPrimario: e.target.value })} />
              </div>
              <div className="field">
                <label>Color secundario</label>
                <input type="color" value={form.colorSecundario || '#1F736A'}
                  onChange={e => setForm({ ...form, colorSecundario: e.target.value })} />
              </div>
            </div>
          </div>

          <div className="form-section">
            <h3 className="form-section-title">Sede Medellín</h3>
            <div className="form-row cols-2">
              <div className="field">
                <label>Dirección</label>
                <input value={form.direccionMedellin || ''} onChange={e => setForm({ ...form, direccionMedellin: e.target.value })} />
              </div>
              <div className="field">
                <label>Ciudad</label>
                <input value={form.ciudadMedellin || ''} onChange={e => setForm({ ...form, ciudadMedellin: e.target.value })} />
              </div>
            </div>
            <div className="form-row cols-2">
              <div className="field">
                <label>Teléfono</label>
                <input value={form.telefonoMedellin || ''} onChange={e => setForm({ ...form, telefonoMedellin: e.target.value })} />
              </div>
            </div>
          </div>

          <div className="form-section">
            <h3 className="form-section-title">Sede Bogotá</h3>
            <div className="form-row cols-2">
              <div className="field">
                <label>Dirección</label>
                <input value={form.direccionBogota || ''} onChange={e => setForm({ ...form, direccionBogota: e.target.value })} />
              </div>
              <div className="field">
                <label>Ciudad</label>
                <input value={form.ciudadBogota || ''} onChange={e => setForm({ ...form, ciudadBogota: e.target.value })} />
              </div>
            </div>
            <div className="form-row cols-2">
              <div className="field">
                <label>Teléfono</label>
                <input value={form.telefonoBogota || ''} onChange={e => setForm({ ...form, telefonoBogota: e.target.value })} />
              </div>
            </div>
          </div>

          <div className="form-actions">
            <button className="btn" type="submit">Guardar configuración</button>
          </div>
        </div>
      </form>
    </div>
  )
}
