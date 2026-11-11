import { Estrelas, formatarNota } from '../components/Estrelas';
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/endpoints';
import { useApi } from '../api/useApi';
import { formatarMoeda } from '../components/format';
import { Botao, Carregando, cx, ErroBox, ImagemAnuncio, Vazio } from '../components/ui';
import { useUsuario } from '../context/UsuarioContext';

const POR_PAGINA = 24;

/** Atualiza o valor só depois que o usuário para de digitar. */
function useDebounce<T>(valor: T, ms: number): T {
  const [atrasado, setAtrasado] = useState(valor);
  useEffect(() => {
    const t = setTimeout(() => setAtrasado(valor), ms);
    return () => clearTimeout(t);
  }, [valor, ms]);
  return atrasado;
}

export function Vitrine() {
  const { usuario } = useUsuario();
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
            <Link to="/login">Entre</Link> ou <Link to="/cadastro">crie sua conta</Link> para ver os detalhes, negociar
            com os fornecedores e fazer pedidos. Empresas com CNPJ cadastrado veem todos os anúncios; aqui aparecem
            os liberados para qualquer comprador.
          </p>
        ) : !usuario.cnpj && (
          <p className="mb-4 rounded-card border border-amber-200 bg-amber-50 px-4 py-3 text-sm" role="note">
            Você está vendo só os anúncios liberados para pessoa física. Muitos fornecedores vendem apenas
            para empresas: <Link to="/perfil">cadastre o CNPJ no seu perfil</Link> para ver todos.
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
                ? <>Nenhum anúncio aberto ao público por enquanto. <Link to="/cadastro">Crie sua conta</Link> com CNPJ para ver os anúncios para empresas.</>
                : !usuario.cnpj
                ? <>Nenhum anúncio liberado para pessoa física por enquanto. Com o <Link to="/perfil">CNPJ cadastrado</Link>, você vê os anúncios para empresas.</>
                : <>Nenhum anúncio ativo. <Link to="/anuncios/novo">Cadastre um anúncio</Link> como vendedor.</>}
          </Vazio>
        ) : (
          <>
            <div className={cx('grid grid-cols-1 gap-4 transition-opacity sm:grid-cols-2 lg:grid-cols-3', anuncios.loading && 'opacity-55')}>
              {resultado.itens.map((a) => {
                const principal = a.imagens.find((i) => i.principal) ?? a.imagens[0];
                return (
                  <Link key={a.id} to={`/anuncios/${a.id}`}
                    className="overflow-hidden rounded-card border border-borda bg-white text-inherit transition hover:-translate-y-0.5 hover:no-underline hover:shadow-[0_8px_24px_rgba(15,23,42,0.08)]">
                    <ImagemAnuncio caminho={principal?.caminho} alt={a.titulo} className="h-[170px] w-full" />
                    <div className="p-3.5">
                      <span className="text-[0.72rem] tracking-wider text-suave uppercase">{nomeCategoria(a.categoriaId)}</span>
                      <h3 className="mt-1 mb-1 text-base">{a.titulo}</h3>
                      {a.quantidadeAvaliacoes > 0 && (
                        <p className="mb-1 flex items-center gap-1 text-xs">
                          <Estrelas nota={a.notaMedia ?? 0} tamanho="text-sm" />
                          <strong>{formatarNota(a.notaMedia)}</strong> <span className="text-suave">({a.quantidadeAvaliacoes})</span>
                        </p>
                      )}
                      <p className="text-lg font-extrabold text-verde">
                        <small className="text-[0.72rem] font-medium text-suave">a partir de</small> {formatarMoeda(a.precoMenor)}
                      </p>
                      <p className="mt-1 text-xs text-suave">
                        {a.valores.length} faixa(s) de preço · {a.variacoes.length} variação(ões)
                      </p>
                    </div>
                  </Link>
                );
              })}
            </div>
            {resultado.totalPaginas > 1 && (
              <nav className="mt-6 flex items-center justify-center gap-4 text-suave" aria-label="Páginas">
                <Botao disabled={pagina === 0 || anuncios.loading}
                  onClick={() => { setPagina(pagina - 1); window.scrollTo({ top: 0 }); }}>← Anterior</Botao>
                <span>Página {pagina + 1} de {resultado.totalPaginas}</span>
                <Botao disabled={pagina + 1 >= resultado.totalPaginas || anuncios.loading}
                  onClick={() => { setPagina(pagina + 1); window.scrollTo({ top: 0 }); }}>Próxima →</Botao>
              </nav>
            )}
          </>
        )}
      </div>
    </section>
  );
}
