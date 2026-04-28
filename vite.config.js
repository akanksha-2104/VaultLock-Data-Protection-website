import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      // Every request starting with /api is forwarded to Spring Boot.
      // Because the browser sees it as localhost:5173 → same origin →
      // session cookies are sent automatically.
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        // Rewrites the cookie domain from 8080 to 5173 so the browser
        // stores and resends it correctly on every subsequent request.
        cookieDomainRewrite: 'localhost',
      },
    },
  },
});
