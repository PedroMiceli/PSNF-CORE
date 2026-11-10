import { useState } from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { AlertasProvider, useAlertas } from '../context/AlertasContext';
import { NovidadesProvider } from '../context/NovidadesContext';
import { ChatProvider } from '../context/ChatContext';
import { AvisosMensagens } from './AvisosMensagens';
import { useUsuario } from '../context/UsuarioContext';
import { urlArquivo } from '../api/client';
import { ChatHub } from './ChatHub';
import { ChatJanela } from './ChatJanela';
import { cx } from './ui';

const itemMenu = ({ isActive }: { isActive: boolean }) =>
  cx('rounded-lg px-3 py-1.5 text-[0.95rem] text-slate-300 hover:bg-topo-claro hover:no-underline',
    isActive && 'bg-topo-claro text-white');

export function Layout() {
  const { usuarioId, usuario, sair } = useUsuario();

  return (
    // key: ao entrar com outro usuário o estado de chat/alertas recomeça do zero
    <NovidadesProvider key={usuarioId}>
    <AlertasProvider key={usuarioId}>
    <ChatProvider key={usuarioId} usuarioId={usuarioId}>
    <div className="flex min-h-screen flex-col">
      <header className="sticky top-0 z-10 bg-topo text-white">
        <div className="mx-auto flex max-w-[1200px] flex-wrap items-center gap-6 px-4 py-3 lg:h-16 lg:flex-nowrap lg:py-0">
          <NavLink to="/" className="text-xl font-extrabold tracking-wider text-white hover:no-underline">PSNF</NavLink>
          <nav className="flex flex-1 flex-wrap gap-1">
            <NavLink to="/" end className={itemMenu}>Vitrine</NavLink>
            <NavLink to="/meus-anuncios" className={itemMenu}>Meus anúncios</NavLink>
            <NavLink to="/negociacoes" className={itemMenu}>Negociações</NavLink>
            <NavLink to="/ordens" className={itemMenu}>Ordens de pedido <ContadorOrdens /></NavLink>
            <NavLink to="/perfil" className={itemMenu}>Perfil</NavLink>
          </nav>
          <div className="flex items-center gap-2">
            <NavLink to="/perfil" className="flex items-center gap-2 rounded-full py-1 pr-3 pl-1 text-sm text-slate-200 hover:bg-topo-claro hover:no-underline"
              title={usuario?.email}>
              <Avatar nome={usuario?.nome ?? ''} imagem={usuario?.imagem} />
              <span className="max-w-40 truncate font-semibold">{usuario?.nome}</span>
            </NavLink>
            <button type="button" onClick={() => sair()}
              className="cursor-pointer rounded-lg px-3 py-1.5 text-sm text-slate-300 hover:bg-topo-claro hover:text-white">
              Sair
            </button>
          </div>
        </div>
      </header>
      <div className="flex flex-1">
        <div className="flex min-w-0 flex-1 flex-col">
          <main className="mx-auto w-full max-w-[1200px] flex-1 px-4 pt-6 pb-12">
            {/* key: com outro usuário a página é remontada e busca os dados de novo */}
            <Outlet key={usuarioId} />
          </main>
        </div>
        {/* hub de negociações: barra à direita em todas as páginas */}
        <ChatHub />
      </div>
      <ChatJanela />
      <AvisosMensagens />
    </div>
    </ChatProvider>
    </AlertasProvider>
    </NovidadesProvider>
  );
}

/** Quantas ordens têm mensagens novas nos incidentes (no menu "Ordens de pedido"). */
function ContadorOrdens() {
  const { alertas } = useAlertas();
  if (!alertas.length) return null;
  return (
    <span className="ml-1 inline-flex min-w-5 items-center justify-center rounded-full bg-vermelho px-1.5 text-[0.68rem] font-bold text-white"
      title={`${alertas.length} ordem(ns) com mensagens novas`} aria-label={`${alertas.length} ordem(ns) com mensagens novas`}>
      {alertas.length}
    </span>
  );
}

/** Foto do usuário ou, sem foto (ou se ela não carregar), a inicial do nome. */
function Avatar({ nome, imagem }: { nome: string; imagem?: string | null }) {
  const [falhou, setFalhou] = useState(false);
  if (imagem && !falhou) {
    return <img src={urlArquivo(imagem)} alt="" onError={() => setFalhou(true)} className="size-8 rounded-full bg-topo-claro object-cover" />;
  }
  return (
    <span className="flex size-8 items-center justify-center rounded-full bg-primaria font-bold text-white" aria-hidden="true">
      {nome.charAt(0).toUpperCase()}
    </span>
  );
}
