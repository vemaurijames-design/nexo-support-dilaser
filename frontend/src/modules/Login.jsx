import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import http from '../api/http'

export default function Login({ onOk }) {
  const nav = useNavigate()
  const [email, setEmail] = useState('soportetecnico2@dilaser.com.co')
  const [password, setPassword] = useState('')
  const [err, setErr] = useState('')
  const [msg, setMsg] = useState('')
  const [loading, setLoading] = useState(false)

  const entrar = async (e) => {
    e.preventDefault()
    setErr(''); setMsg(''); setLoading(true)
    try {
      const { data } = await http.post('/auth/login', { email, password })
      const token = data.token || data.accessToken || data.access_token
      if (!token) throw new Error('El servidor no devolvió token')
      localStorage.setItem('nexo_token', token)
      localStorage.setItem('nexo_nombre', data.nombre || data.nombres || email)
      localStorage.setItem('nexo_email', data.email || email)
      localStorage.setItem('nexo_rol', data.rol || '')
      if (onOk) onOk()
      else {
        nav('/')
        window.location.href = '/'
      }
    } catch (ex) {
      setErr(ex.response?.data?.message || ex.message || 'Correo o contraseña incorrectos')
    } finally { setLoading(false) }
  }

  const forgot = async () => {
    setErr(''); setMsg('')
    if (!email.includes('@')) { setErr('Escribe el correo'); return }
    try {
      await http.post('/auth/forgot', { email })
      setMsg('Si el correo existe, enviamos el enlace para restablecer la contraseña.')
    } catch (ex) {
      setErr(ex.response?.data?.message || 'No se pudo enviar el correo')
    }
  }

  return (
    <div style={s.page}>
      <form onSubmit={entrar} style={s.card}>
        <div style={s.logoBox}>
          <img src="/logodilaser.png" alt="Dilaser" style={s.logo} />
        </div>
        <h1 style={s.title}>Iniciar sesión</h1>
        <p style={s.lead}>Soporte técnico Dilaser · Nexo Support</p>
        {err && <div style={s.err}>{err}</div>}
        {msg && <div style={s.ok}>{msg}</div>}
        <label style={s.label}>Correo</label>
        <input style={s.input} type="email" value={email} onChange={e => setEmail(e.target.value)} required />
        <label style={s.label}>Contraseña</label>
        <input style={s.input} type="password" value={password} onChange={e => setPassword(e.target.value)} required />
        <button style={s.btn} disabled={loading}>{loading ? 'Ingresando…' : 'Iniciar sesión'}</button>
        <button type="button" style={s.link} onClick={forgot}>Olvidé mi contraseña</button>
        <p style={s.foot}>© 2026 Dilaser · Nexo Support</p>
      </form>
    </div>
  )
}

const s = {
  page: { minHeight: '100vh', display: 'grid', placeItems: 'center', background: 'linear-gradient(160deg,#026277,#03738C 55%,#0b8aa3)', fontFamily: 'Segoe UI, Arial, sans-serif' },
  card: { width: 420, background: '#fff', borderRadius: 18, padding: '28px 28px 20px', boxShadow: '0 18px 50px rgba(0,0,0,.18)' },
  logoBox: { background: '#03738C', borderRadius: 12, padding: '14px 16px', marginBottom: 18, display: 'flex', justifyContent: 'center' },
  logo: { height: 54, width: 'auto', maxWidth: '100%', objectFit: 'contain' },
  title: { margin: '4px 0 0', fontSize: 26, color: '#12363A' },
  lead: { margin: '4px 0 14px', color: '#6b8589', fontSize: 14 },
  label: { display: 'block', fontSize: 13, color: '#35575c', margin: '10px 0 4px' },
  input: { width: '100%', boxSizing: 'border-box', border: '1px solid #c5e0e4', borderRadius: 10, padding: '11px 12px', fontSize: 15 },
  btn: { width: '100%', marginTop: 18, background: '#03738C', color: '#fff', border: 0, borderRadius: 10, padding: '12px 14px', fontWeight: 700, cursor: 'pointer' },
  link: { width: '100%', marginTop: 10, background: '#fff', color: '#03738C', border: '1px solid #b7dbe0', borderRadius: 10, padding: '10px 14px', cursor: 'pointer' },
  err: { background: '#fde8e8', color: '#9b1c1c', padding: '8px 10px', borderRadius: 8, fontSize: 13 },
  ok: { background: '#e7f6ee', color: '#146c43', padding: '8px 10px', borderRadius: 8, fontSize: 13 },
  foot: { textAlign: 'center', color: '#8aa0a3', fontSize: 12, marginTop: 16 }
}
