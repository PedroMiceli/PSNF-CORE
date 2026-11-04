import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { Vazio } from './components/ui';
import { UsuarioProvider } from './context/UsuarioContext';
import { AnuncioDetalhe } from './pages/AnuncioDetalhe';
import { AnuncioForm } from './pages/AnuncioForm';
import { Categorias } from './pages/Categorias';
import { MeusAnuncios } from './pages/MeusAnuncios';
import { NovaOrdem } from './pages/NovaOrdem';
import { OrdemDetalhe } from './pages/OrdemDetalhe';
import { Ordens } from './pages/Ordens';
import { Perfil } from './pages/Perfil';
import { Vitrine } from './pages/Vitrine';

export function App() {
  return (
    <UsuarioProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<Layout />}>
            <Route index element={<Vitrine />} />
            <Route path="anuncios/novo" element={<AnuncioForm />} />
            <Route path="anuncios/:id" element={<AnuncioDetalhe />} />
            <Route path="anuncios/:id/editar" element={<AnuncioForm />} />
            <Route path="meus-anuncios" element={<MeusAnuncios />} />
            <Route path="ordens" element={<Ordens />} />
            <Route path="ordens/nova" element={<NovaOrdem />} />
            <Route path="ordens/:id" element={<OrdemDetalhe />} />
            <Route path="categorias" element={<Categorias />} />
            <Route path="perfil" element={<Perfil />} />
            <Route path="*" element={<Vazio>Página não encontrada.</Vazio>} />
          </Route>
        </Routes>
      </BrowserRouter>
    </UsuarioProvider>
  );
}
