import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      // 1. Proxy dla zapytań API (fetch)
      '/api': {
        target: 'http://localhost:3000', // Adres Twojego backendu
        changeOrigin: true,
        secure: false,
        // Opcjonalnie: jeśli na backendzie nie masz prefixu '/api' w endpointach,
        // użyj rewrite, aby go wyciąć przed wysłaniem do serwera:
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      
      // 2. Proxy dla Socket.io (ważne!)
      '/socket.io': {
        target: 'http://localhost:3000',
        ws: true, // Włącza wsparcie dla WebSocket
        changeOrigin: true
      }
    }
  }
})
