import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api/client.js'

export default function Login() {
  const nav = useNavigate()
  const [email, setEmail] = useState('soportetecnico2@dilaser.com.co')
  const [password, setPassword] = useState('')
  const [err, setErr] = useState('')
  const [msg, setMsg] = useState('')
  const [mode, setMode] = useState('login')
  const [brand, setBrand] = useState({ nombreComercial: 'Nexo Support', logoUrl: '', colorPrimario: '#03738C' })

  useEffect(() => {
    api.get('/api/empresa/public').then(r => setBrand(r.data)).catch(() => {})
  }, [])

  const submit = async (e) => {
    e.preventDefault()
    setErr(''); setMsg('')
    try {
      if (mode === 'forgot') {
        await api.post('/api/auth/forgot-password', { email })
        setMsg('Si el correo existe, enviaremos instrucciones.')
        return
      }
      const { data } = await api.post('/api/auth/login', { email, password })
      localStorage.setItem('nexo_token', data.accessToken || data.token)
      localStorage.setItem('nexo_nombre', data.nombre || data.nombres || email)
      localStorage.setItem('nexo_rol', data.rol || '')
      nav('/')
    } catch (ex) {
      setErr(ex.response?.data?.message || 'No se pudo autenticar')
    }
  }

  const primary = brand.colorPrimario || '#03738C'

  return (
    <div className="login-wrap" style={{ background: `linear-gradient(155deg, ${primary} 0%, #1F736A 100%)` }}>
      <form className="login-card pro" onSubmit={submit}>
        <div className="login-brand">
          {brand.logoUrl
            ? <img className="login-logo" src={(api.defaults.baseURL || '') + brand.logoUrl} alt="Logo" />
            : <div className="login-logo-placeholder">D</div>}
          <div>
            <h1>{brand.nombreComercial || 'Nexo Support'}</h1>
            <p className="muted">Soporte técnico Dilaser</p>
          </div>
        </div>

        {mode === 'login' ? (
          <>
            <label>Correo</label>
            <input type="email" required value={email} onChange={e => setEmail(e.target.value)} autoComplete="username" />
            <label>Contraseña</label>
            <input type="password" required value={password} onChange={e => setPassword(e.target.value)} autoComplete="current-password" />
          </>
        ) : (
          <>
            <p className="muted">Ingresa tu correo y te enviaremos el enlace de restablecimiento.</p>
            <label>Correo</label>
            <input type="email" required value={email} onChange={e => setEmail(e.target.value)} />
          </>
        )}

        {err && <p className="err">{err}</p>}
        {msg && <p className="ok">{msg}</p>}

        <button className="btn full" type="submit" style={{ background: primary }}>
          {mode === 'login' ? 'Ingresar' : 'Enviar instrucciones'}
        </button>
        <button type="button" className="btn ghost full" onClick={() => { setMode(mode === 'login' ? 'forgot' : 'login'); setErr(''); setMsg('') }}>
          {mode === 'login' ? 'Olvidé mi contraseña' : 'Volver al login'}
        </button>
        <p className="login-foot muted">© {new Date().getFullYear()} Dilaser · Nexo Support</p>
      </form>
    </div>
  )
}
