import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://server:3000', // Adres Twojego backendu
        changeOrigin: true,
        secure: false
      },
      
      '/socket.io': {
        target: 'http://server:3000',
        ws: true,
        changeOrigin: true
      }
    }
  }
})
