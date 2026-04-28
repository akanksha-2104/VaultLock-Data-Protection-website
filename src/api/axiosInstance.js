import axios from 'axios';

// No baseURL — Vite proxies /api/* → localhost:8080 (see vite.config.js)
const api = axios.create({
  baseURL: '',
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
});

// ── Credential store (in-memory, not localStorage for security) ──────────────
// Your Spring Boot backend uses HTTP Basic authentication via AuthenticationManager.
// The login endpoint manually authenticates but does not issue a JSESSIONID session.
// So every API request must carry the user's credentials in the Authorization header.
// We store them in memory (cleared on page close) and attach them via interceptor.
let _credentials = null; // { email, password } — set on login, cleared on logout

export function setCredentials(email, password) {
  _credentials = { email, password };
}

export function clearCredentials() {
  _credentials = null;
}

// Request interceptor — attaches HTTP Basic auth header to every API call.
// Spring Security's DaoAuthenticationProvider validates this on every request,
// so Authentication is always populated correctly in controllers.
api.interceptors.request.use((config) => {
  if (_credentials) {
    const encoded = btoa(`${_credentials.email}:${_credentials.password}`);
    config.headers['Authorization'] = `Basic ${encoded}`;
  }
  return config;
});

export default api;