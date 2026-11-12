import { useEffect, useState } from 'react';
import { useDebounce } from '../api/useDebounce';
import { Link } from 'react-router-dom';
import { api } from '../api/endpoints';
import { useApi } from '../api/useApi';
import { CartaoAnuncio, Paginacao } from '../components/CartaoAnuncio';
import { Carregando, classesBotao, cx, ErroBox, Vazio } from '../components/ui';
import { useUsuario } from '../context/UsuarioContext';

const POR_PAGINA = 24;

export function Vitrine() {
  const { usuario } = useUsuario();
  // quem tem CNPJ negocia qualquer anúncio: o selo "Somente CNPJ" só aparece para quem não tem
  const temCnpj = !!usuario?.cnpj;
  // visitante clicou num cartão: aviso leve no rodapé (some sozinho); "Entrar" volta para este anúncio
  const [avisoLogin, setAvisoLogin] = useState<{ anuncioId: string; vez: number } | null>(null);
  const avisarLogin = (anuncioId: string) => setAvisoLogin((v) => ({ anuncioId, vez: (v?.vez ?? 0) + 1 }));
  useEffect(() => {
    if (!avisoLogin) return;
    const t = setTimeout(() => setAvisoLogin(null), 5000);
    return () => clearTimeout(t);
  }, [avisoLogin]);
  const [categoriaId, setCategoriaId] = useState<string>('');
  const [busca, setBusca] = useState('');
  const termo = useDebounce(busca.trim(), 300);

  // a página pertence ao filtro atual: mudou o termo ou a categoria, volta para a primeira página
  // (calculado no próprio render, sem efeito, para não disparar uma busca com a página antiga)
  const filtro = `${termo}|${categoriaId}`;
  const [paginacao, setPaginacao] = useState({ filtro, pagina: 0 });
  const pagina = paginacao.filtro === filtro ? paginacao.pagina : 0;
  const setPagina = (nova: number) => setPaginacao({ filtro, pagina: nova });

  const categorias = useApi(() => api.categorias(), []);
  const anuncios = useApi(
    () => api.pesquisarAnuncios({ termo, categoriaId: categoriaId || undefined, pagina, tamanho: POR_PAGINA }),
    [termo, categoriaId, pagina],
  );

  const nomeCategoria = (id: string) => categorias.data?.find((c) => c.id === id)?.nome ?? '';
  const resultado = anuncios.data;

  const escolherCategoria = (id: string) => {
    setCategoriaId(id);
    window.scrollTo({ top: 0 });
  };

  const itemCategoria = (ativo: boolean) => cx(
    'w-full cursor-pointer rounded-lg px-2.5 py-1.5 text-left text-sm whitespace-nowrap hover:bg-slate-100',
    'max-md:rounded-full max-md:border max-md:border-borda max-md:px-3',
    ativo ? 'bg-blue-50 font-bold text-primaria' : 'text-texto',
  );

  return (
    <section className="grid items-start gap-6 md:grid-cols-[240px_minmax(0,1fr)] max-md:gap-3">
      <h1 className="sr-only">Vitrine</h1>

      {/* coluna da esquerda: categorias (no celular vira uma faixa rolável) */}
      <aside aria-label="Categorias"
        className="rounded-card border border-borda bg-white p-2.5 md:sticky md:top-[76px] md:max-h-[calc(100vh-96px)] md:overflow-y-auto md:px-2.5 md:py-4 max-md:overflow-x-auto">
        <h2 className="mx-2 mb-2.5 font-bold max-md:hidden">Categorias</h2>
        <ul className="flex gap-0.5 md:flex-col max-md:gap-1.5">
          <li>
            <button className={itemCategoria(categoriaId === '')} aria-current={categoriaId === '' ? 'true' : undefined}
              onClick={() => escolherCategoria('')}>Todas</button>
          </li>
          {categorias.data?.map((c) => (
            <li key={c.id}>
              <button className={itemCategoria(categoriaId === c.id)} aria-current={categoriaId === c.id ? 'true' : undefined}
                onClick={() => escolherCategoria(c.id)}>{c.nome}</button>
            </li>
          ))}
        </ul>
      </aside>

      {/* conteúdo: barra de pesquisa no topo e os anúncios no meio */}
      <div className="min-w-0">
        {!usuario ? (
          <p className="mb-4 rounded-card border border-blue-200 bg-blue-50 px-4 py-3 text-sm" role="note">
            <Link to="/login">Entre</Link> ou <Link to="/cadastro">crie sua conta</Link> para ver os detalhes dos anúncios,
            negociar com os fornecedores e fazer pedidos.
          </p>
        ) : !usuario.cnpj && (
          <p className="mb-4 rounded-card border border-amber-200 bg-amber-50 px-4 py-3 text-sm" role="note">
            Anúncios marcados como <strong>Somente CNPJ</strong> são vendidos apenas para empresas: você pode vê-los, mas para
            negociar <Link to="/perfil">cadastre o CNPJ no seu perfil</Link>.
          </p>
        )}
        <div className="mb-4 flex items-center gap-2.5 rounded-card border border-gray-300 bg-white px-4 focus-within:border-primaria focus-within:ring-3 focus-within:ring-blue-200">
          <span aria-hidden="true">🔍</span>
          <input type="search" placeholder="Buscar produtos (ex.: carrinho, cabo usb)..." value={busca}
            onChange={(e) => setBusca(e.target.value)} aria-label="Buscar produtos"
            className="min-w-0 flex-1 bg-transparent py-3.5 text-base outline-none" />
        </div>

        <ErroBox erro={anuncios.error ?? categorias.error} />
        {resultado && (
          <p className="-mt-1.5 mb-4 text-sm text-suave [&_strong]:text-texto" aria-live="polite">
            {resultado.total === 0 ? 'Nenhum anúncio encontrado' : `${resultado.total.toLocaleString('pt-BR')} anúncio(s)`}
            {termo && <> para <strong>“{termo}”</strong></>}
            {categoriaId && <> em <strong>{nomeCategoria(categoriaId)}</strong></>}
          </p>
        )}

        {anuncios.loading && !resultado ? (
          <Carregando />
        ) : !resultado?.itens.length ? (
          <Vazio>
            {termo || categoriaId
              ? 'Tente outro termo ou outra categoria.'
              : !usuario
                ? 'Nenhum anúncio publicado ainda.'
                : <>Nenhum anúncio ativo. <Link to="/anuncios/novo">Cadastre um anúncio</Link> como vendedor.</>}
          </Vazio>
        ) : (
          <>
            <div className={cx('grid grid-cols-1 gap-4 transition-opacity sm:grid-cols-2 lg:grid-cols-3', anuncios.loading && 'opacity-55')}>
              {resultado.itens.map((a) => (
                <CartaoAnuncio key={a.id} anuncio={a} categoria={nomeCategoria(a.categoriaId)} mostrarSeloCnpj={!temCnpj}
                  aoClicarSemLogin={usuario ? undefined : () => avisarLogin(a.id)} />
              ))}
            </div>
            <Paginacao pagina={pagina} totalPaginas={resultado.totalPaginas} carregando={anuncios.loading} aoMudar={setPagina} />
          </>
        )}
      </div>
      {avisoLogin && (
        <div key={avisoLogin.vez} role="status"
          className="fixed inset-x-4 bottom-6 z-40 mx-auto flex max-w-[560px] flex-wrap items-center gap-x-4 gap-y-2 rounded-card bg-topo px-5 py-3.5 text-sm text-white shadow-xl motion-safe:animate-[surgir_0.2s_ease-out]">
          <span className="min-w-0 flex-1">Logue ou crie uma conta para visualizar mais detalhes deste produto.</span>
          <span className="flex items-center gap-2">
            <Link to="/login" state={{ de: `/anuncios/${avisoLogin.anuncioId}` }}
              className="rounded-lg px-3 py-1.5 font-semibold text-white hover:bg-topo-claro hover:no-underline">Entrar</Link>
            <Link to="/cadastro" className={classesBotao('primario', 'pequeno')}>Criar conta</Link>
            <button type="button" onClick={() => setAvisoLogin(null)} aria-label="Fechar aviso"
              className="cursor-pointer rounded px-1.5 text-slate-300 hover:bg-topo-claro hover:text-white">✕</button>
          </span>
        </div>
      )}
    </section>
  );
}
