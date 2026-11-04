import { NavLink, Outlet } from 'react-router-dom';
import { COMPRADOR_MOCK, VENDEDOR_MOCK, useUsuario } from '../context/UsuarioContext';

export function Layout() {
  const { usuarioId, trocarUsuario, usuarios } = useUsuario();
  const papel = usuarioId === VENDEDOR_MOCK.id ? 'vendedor' : usuarioId === COMPRADOR_MOCK.id ? 'comprador' : 'outro';

  return (
    <div className="app">
      <header className="topo">
        <div className="topo-inner">
          <NavLink to="/" className="logo">PSNF</NavLink>
          <nav className="menu">
            <NavLink to="/" end>Vitrine</NavLink>
            <NavLink to="/meus-anuncios">Meus anúncios</NavLink>
            <NavLink to="/ordens">Ordens de pedido</NavLink>
            <NavLink to="/categorias">Categorias</NavLink>
            <NavLink to="/perfil">Perfil</NavLink>
          </nav>
          <label className={`troca-usuario papel-${papel}`} title="Simula o login (header X-Usuario-Id)">
            <span>Logado como</span>
            <select
              value={usuarioId}
              onChange={(e) => trocarUsuario(e.target.value)}
            >
              {usuarios.map((u) => (
                <option key={u.id} value={u.id}>{u.nome}</option>
              ))}
            </select>
          </label>
        </div>
      </header>
      <main className="conteudo">
        {/* key: ao trocar de usuário a página é remontada e busca os dados de novo */}
        <Outlet key={usuarioId} />
      </main>
      <footer className="rodape">
        Protótipo PSNF — o usuário logado é simulado pelo header <code>X-Usuario-Id</code>.
      </footer>
    </div>
  );
}
