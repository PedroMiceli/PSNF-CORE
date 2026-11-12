import { BrowserRouter, Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { Layout } from './components/Layout';
import { LayoutPublico } from './components/LayoutPublico';
import { Vazio } from './components/ui';
import { UsuarioProvider, useUsuario } from './context/UsuarioContext';
import { Cadastro } from './pages/auth/Cadastro';
import { ConfirmarEmail } from './pages/auth/ConfirmarEmail';
import { EsqueciSenha } from './pages/auth/EsqueciSenha';
import { RedefinirSenha } from './pages/auth/RedefinirSenha';
import { Login } from './pages/auth/Login';
import { AnuncioDetalhe } from './pages/AnuncioDetalhe';
import { AnuncioForm } from './pages/AnuncioForm';
import { AnunciosVendedor } from './pages/AnunciosVendedor';
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

// links que chegam por e-mail: funcionam logado ou não (ex.: confirmou o e-mail em outra aba)
const rotasDeLink = [
  <Route key="confirmar" path="confirmar-email" element={<ConfirmarEmail />} />,
  <Route key="redefinir" path="redefinir-senha" element={<RedefinirSenha />} />,
];

function Rotas() {
  const { estado } = useUsuario();
  const location = useLocation();

  if (estado.tipo === 'carregando') {
    return <div className="flex min-h-screen items-center justify-center text-suave">Carregando...</div>;
  }

  // sem login: a vitrine (ver e pesquisar) e as telas de conta; qualquer outra (abrir um anúncio,
  // negociar...) vai para o login e volta depois para onde ia
  if (estado.tipo === 'anonimo') {
    return (
      <Routes>
        <Route element={<LayoutPublico />}>
          <Route index element={<Vitrine />} />
        </Route>
        <Route path="login" element={<Login />} />
        <Route path="cadastro" element={<Cadastro />} />
        <Route path="esqueci-senha" element={<EsqueciSenha />} />
        {rotasDeLink}
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
      <Route path="esqueci-senha" element={<EsqueciSenha />} />
      {rotasDeLink}
      <Route element={<Layout />}>
        <Route index element={<Vitrine />} />
        <Route path="anuncios/novo" element={<AnuncioForm />} />
        <Route path="anuncios/:id" element={<AnuncioDetalhe />} />
        <Route path="anuncios/:id/editar" element={<AnuncioForm />} />
        <Route path="meus-anuncios" element={<MeusAnuncios />} />
        {/* só logado: sem login, cai no "*" das rotas públicas, que leva ao login e volta para cá */}
        <Route path="vendedores/:id" element={<AnunciosVendedor />} />
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
