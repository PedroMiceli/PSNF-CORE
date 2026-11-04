import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// O front chama /api/... e o Vite repassa para a API Spring Boot (evita configurar CORS no back).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
});
