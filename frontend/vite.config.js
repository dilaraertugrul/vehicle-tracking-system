import { defineConfig } from 'vite'
// Vite ayarlarını tanımlamak için.

import react from '@vitejs/plugin-react'
// React'in Vite ile çalışmasını sağlayan plugin.

export default defineConfig({

  plugins: [react()],
  // React desteğini aktif ediyoruz.

  server: {

    proxy: {

      '/api': {
        // Frontend'den /api ile başlayan bir istek gelirse:

        target: 'http://localhost:8081',
        // İsteği Spring Boot backend'e yönlendir.

        changeOrigin: true,
        // Proxy isteğinin origin bilgisini hedef sunucuya göre düzenler.
      },

    },

  },

})