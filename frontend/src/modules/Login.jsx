import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import http from '../api/http'

export default function Login() {
  const nav = useNavigate()
  const [email, setEmail] = useState('soportetecnico2@dilaser.com.co')
  const [password, setPassword] = useState('admin123456')
  const [mode, setMode] = useState('login')
  const [msg, setMsg] = useState('')
  const [err, setErr] = useState('')

  const submit = async (e) => {
    e.preventDefault()
    setErr(''); setMsg('')
    try {
      if (mode === 'login') {
        const { data } = await http.post('/auth/login', { email, password })
        localStorage.setItem('nexo_token', data.accessToken)
        localStorage.setItem('nexo_nombre', data.nombre)
        localStorage.setItem('nexo_rol', data.rol)
        nav('/')
      } else {
        await http.post('/auth/forgot-password', { email })
        setMsg('Si el correo existe, enviaremos instrucciones.')
      }
    } catch (ex) {
      setErr(ex.response?.data?.message || 'No se pudo autenticar')
    }
  }

  return (
    <div className="login-wrap">
      <form className="login-card" onSubmit={submit}>
        <h2 style={{ color: '#03738C', marginTop: 0 }}>Nexo Support</h2>
        <p className="muted">Soporte técnico Dilaser</p>
        <label>Correo</label>
        <input value={email} onChange={(e) => setEmail(e.target.value)} type="email" required />
        {mode === 'login' && (
          <>
            <label>Contraseña</label>
            <input value={password} onChange={(e) => setPassword(e.target.value)} type="password" required />
          </>
        )}
        {err && <p className="err">{err}</p>}
        {msg && <p className="muted">{msg}</p>}
        <p>
          <button className="btn" type="submit">{mode === 'login' ? 'Ingresar' : 'Enviar enlace'}</button>
        </p>
        <button type="button" className="btn ghost" onClick={() => setMode(mode === 'login' ? 'forgot' : 'login')}>
          {mode === 'login' ? 'Olvidé mi contraseña' : 'Volver al login'}
        </button>
      </form>
    </div>
  )
}
