import { NavLink, Outlet } from 'react-router-dom';
import { ChatProvider } from '../context/ChatContext';
import { COMPRADOR_MOCK, VENDEDOR_MOCK, useUsuario } from '../context/UsuarioContext';
import { ChatHub } from './ChatHub';
import { ChatJanela } from './ChatJanela';
import { cx } from './ui';

const itemMenu = ({ isActive }: { isActive: boolean }) =>
  cx('rounded-lg px-3 py-1.5 text-[0.95rem] text-slate-300 hover:bg-topo-claro hover:no-underline',
    isActive && 'bg-topo-claro text-white');

export function Layout() {
  const { usuarioId, trocarUsuario, usuarios } = useUsuario();
  const corPapel = usuarioId === VENDEDOR_MOCK.id ? 'bg-blue-900' : usuarioId === COMPRADOR_MOCK.id ? 'bg-green-800' : 'bg-orange-900';

  return (
    // key: ao trocar de usuário o chat aberto é fechado
    <ChatProvider key={usuarioId} usuarioId={usuarioId}>
    <div className="flex min-h-screen flex-col">
      <header className="sticky top-0 z-10 bg-topo text-white">
        <div className="mx-auto flex max-w-[1200px] flex-wrap items-center gap-6 px-4 py-3 lg:h-16 lg:flex-nowrap lg:py-0">
          <NavLink to="/" className="text-xl font-extrabold tracking-wider text-white hover:no-underline">PSNF</NavLink>
          <nav className="flex flex-1 flex-wrap gap-1">
            <NavLink to="/" end className={itemMenu}>Vitrine</NavLink>
            <NavLink to="/meus-anuncios" className={itemMenu}>Meus anúncios</NavLink>
            <NavLink to="/negociacoes" className={itemMenu}>Negociações</NavLink>
            <NavLink to="/ordens" className={itemMenu}>Ordens de pedido</NavLink>
            <NavLink to="/perfil" className={itemMenu}>Perfil</NavLink>
          </nav>
          <label className={cx('flex items-center gap-2 rounded-full py-1 pr-1 pl-3 text-xs text-slate-300', corPapel)}
            title="Simula o login (header X-Usuario-Id)">
            <span>Logado como</span>
            <select value={usuarioId} onChange={(e) => trocarUsuario(e.target.value)}
              className="max-w-52 rounded-full border-0 bg-white px-3 py-1.5 text-sm font-semibold text-texto outline-none">
              {usuarios.map((u) => (
                <option key={u.id} value={u.id} title={u.email}>{u.nome}</option>
              ))}
            </select>
          </label>
        </div>
      </header>
      <div className="flex flex-1">
        <div className="flex min-w-0 flex-1 flex-col">
          <main className="mx-auto w-full max-w-[1200px] flex-1 px-4 pt-6 pb-12">
            {/* key: ao trocar de usuário a página é remontada e busca os dados de novo */}
            <Outlet key={usuarioId} />
          </main>
          <footer className="p-4 text-center text-xs text-suave">
            Protótipo PSNF — o usuário logado é simulado pelo header <code>X-Usuario-Id</code>.
          </footer>
        </div>
        {/* hub de negociações: barra à direita em todas as páginas */}
        <ChatHub />
      </div>
      <ChatJanela />
    </div>
    </ChatProvider>
  );
}
