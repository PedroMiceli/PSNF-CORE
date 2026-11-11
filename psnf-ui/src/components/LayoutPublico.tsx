import { Link, NavLink, Outlet, useLocation } from 'react-router-dom';
import { classesBotao } from './ui';

/**
 * Moldura da área pública (sem login): só a vitrine. Ver os detalhes de um anúncio, negociar e todo o
 * resto pedem login; o link de um anúncio leva ao login e, depois de entrar, volta para o anúncio.
 */
export function LayoutPublico() {
  const location = useLocation();
  const voltarPara = { de: location.pathname + location.search };
  return (
    <div className="flex min-h-screen flex-col">
      <header className="sticky top-0 z-10 bg-topo text-white">
        <div className="mx-auto flex max-w-[1200px] items-center gap-4 px-4 py-3 lg:h-16 lg:py-0">
          <NavLink to="/" className="text-xl font-extrabold tracking-wider text-white hover:no-underline">PSNF</NavLink>
          <span className="flex-1 text-sm text-slate-300 max-sm:hidden">Marketplace B2B: compre direto de fornecedores, com preço por quantidade.</span>
          <span className="flex-1 sm:hidden" />
          <Link to="/login" state={voltarPara} className="rounded-lg px-3 py-1.5 text-sm text-slate-200 hover:bg-topo-claro hover:no-underline">Entrar</Link>
          <Link to="/cadastro" className={classesBotao('primario', 'pequeno')}>Criar conta</Link>
        </div>
      </header>
      <main className="mx-auto w-full max-w-[1200px] flex-1 px-4 pt-6 pb-12">
        <Outlet />
      </main>
    </div>
  );
}
