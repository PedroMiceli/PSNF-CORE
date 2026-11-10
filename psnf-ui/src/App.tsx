import { BrowserRouter, Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { Layout } from './components/Layout';
import { Vazio } from './components/ui';
import { UsuarioProvider, useUsuario } from './context/UsuarioContext';
import { Cadastro } from './pages/auth/Cadastro';
import { Login } from './pages/auth/Login';
import { AnuncioDetalhe } from './pages/AnuncioDetalhe';
import { AnuncioForm } from './pages/AnuncioForm';
import { MeusAnuncios } from './pages/MeusAnuncios';
import { Negociacoes } from './pages/Negociacoes';
import { NovaOrdem } from './pages/NovaOrdem';
import { OrdemDetalhe } from './pages/OrdemDetalhe';
import { Ordens } from './pages/Ordens';
import { Perfil } from './pages/Perfil';
import { Vitrine } from './pages/Vitrine';

export function App() {
  return (
    <UsuarioProvider>
      <BrowserRouter>
        <Rotas />
      </BrowserRouter>
    </UsuarioProvider>
  );
}

function Rotas() {
  const { estado } = useUsuario();
  const location = useLocation();

  if (estado.tipo === 'carregando') {
    return <div className="flex min-h-screen items-center justify-center text-suave">Carregando...</div>;
  }

  // sem login: só as telas públicas; qualquer outra vai para o login e volta depois para onde ia
  if (estado.tipo === 'anonimo') {
    return (
      <Routes>
        <Route path="login" element={<Login />} />
        <Route path="cadastro" element={<Cadastro />} />
        <Route path="*" element={<Navigate to="/login" replace state={{ de: location.pathname + location.search + location.hash }} />} />
      </Routes>
    );
  }

  // acabou de entrar (ou já estava logado): vai para a página que tentou abrir antes do login
  const destino = (location.state as { de?: string } | null)?.de ?? '/';
  return (
    <Routes>
      <Route path="login" element={<Navigate to={destino} replace />} />
      <Route path="cadastro" element={<Navigate to={destino} replace />} />
      <Route element={<Layout />}>
        <Route index element={<Vitrine />} />
        <Route path="anuncios/novo" element={<AnuncioForm />} />
        <Route path="anuncios/:id" element={<AnuncioDetalhe />} />
        <Route path="anuncios/:id/editar" element={<AnuncioForm />} />
        <Route path="meus-anuncios" element={<MeusAnuncios />} />
        <Route path="negociacoes" element={<Negociacoes />} />
        <Route path="ordens" element={<Ordens />} />
        <Route path="ordens/nova" element={<NovaOrdem />} />
        <Route path="ordens/:id" element={<OrdemDetalhe />} />
        <Route path="perfil" element={<Perfil />} />
        <Route path="*" element={<Vazio>Página não encontrada.</Vazio>} />
      </Route>
    </Routes>
  );
}
