import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/LoginServlet': {
        target: 'http://localhost:8080/sghcp_inicio_sesion',
        changeOrigin: true,
      },
    },
  },
})