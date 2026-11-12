import { useState } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import { api } from '../api/endpoints';
import { useApi } from '../api/useApi';
import { useDebounce } from '../api/useDebounce';
import { CartaoAnuncio, Paginacao } from '../components/CartaoAnuncio';
import { Carregando, cx, ErroBox, ImagemAnuncio, Vazio, Voltar } from '../components/ui';
import { useUsuario } from '../context/UsuarioContext';

const POR_PAGINA = 24;

/**
 * Página de um vendedor: todos os anúncios ativos dele, com pesquisa, filtro pelas categorias em que ele
 * vende e paginação. Só existe para quem está logado (a rota fica fora das rotas públicas e a API exige login).
 */
export function AnunciosVendedor() {
  const { id = '' } = useParams();
  const { usuario, usuarioId } = useUsuario();
  // veio do detalhe de um anúncio: "voltar" leva de volta a ele
  const voltarPara = (useLocation().state as { anuncioId?: string } | null)?.anuncioId;
  const [categoriaId, setCategoriaId] = useState('');
  const [busca, setBusca] = useState('');
  const termo = useDebounce(busca.trim(), 300);

  // mudou o filtro: volta para a primeira página (calculado no render, como na vitrine)
  const filtro = `${id}|${termo}|${categoriaId}`;
  const [paginacao, setPaginacao] = useState({ filtro, pagina: 0 });
  const pagina = paginacao.filtro === filtro ? paginacao.pagina : 0;
  const setPagina = (nova: number) => setPaginacao({ filtro, pagina: nova });

  const vendedor = useApi(() => api.usuario(id), [id]);
  const categorias = useApi(() => api.categoriasDoVendedor(id), [id]);
  const anuncios = useApi(
    () => api.anunciosDoVendedor(id, { termo, categoriaId: categoriaId || undefined, pagina, tamanho: POR_PAGINA }),
    [id, termo, categoriaId, pagina],
  );

  if (vendedor.loading) return <Carregando />;
  if (vendedor.error || !vendedor.data) return <ErroBox erro={vendedor.error} />;

  const v = vendedor.data;
  const souEu = v.id === usuarioId;
  const totalAtivos = categorias.data?.reduce((s, c) => s + c.quantidade, 0) ?? 0;
  const nomeCategoria = (cid: string) => categorias.data?.find((c) => c.categoriaId === cid)?.nome ?? '';
  const resultado = anuncios.data;
  const desde = new Date(v.dataCadastro).toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });

  const itemCategoria = (ativo: boolean) => cx(
    'flex w-full cursor-pointer items-center justify-between gap-2 rounded-lg px-2.5 py-1.5 text-left text-sm whitespace-nowrap hover:bg-slate-100',
    'max-md:rounded-full max-md:border max-md:border-borda max-md:px-3',
    ativo ? 'bg-blue-50 font-bold text-primaria' : 'text-texto',
  );
  const escolher = (cid: string) => { setCategoriaId(cid); window.scrollTo({ top: 0 }); };

  return (
    <section>
      {voltarPara ? <Voltar to={`/anuncios/${voltarPara}`}>Voltar ao anúncio</Voltar> : <Voltar to="/">Vitrine</Voltar>}

      {/* cabeçalho do vendedor */}
      <header className="mb-5 flex flex-wrap items-center gap-4 rounded-card border border-borda bg-white p-5">
        {v.imagem
          ? <ImagemAnuncio caminho={v.imagem} alt={`Foto de ${v.nome}`} compacta className="size-16 shrink-0 rounded-full border border-borda" />
          : <div aria-hidden="true" className="flex size-16 shrink-0 items-center justify-center rounded-full border border-borda bg-slate-100 text-2xl font-bold text-suave">
              {v.nome.trim().charAt(0).toUpperCase()}
            </div>}
        <div className="min-w-0 flex-1">
          <h1 className="m-0 text-2xl">{v.nome}</h1>
          <p className="mt-1 flex flex-wrap items-center gap-x-3 gap-y-1 text-sm text-suave">
            {v.cnpjVerificado && (
              <span className="font-semibold text-verde" title={`CNPJ ativo na Receita Federal: ${v.razaoSocial ?? ''}`}>
                ✓ Empresa verificada{v.razaoSocial && v.razaoSocial !== v.nome ? ` · ${v.razaoSocial}` : ''}
              </span>
            )}
            <span>Na plataforma desde {desde}</span>
            {categorias.data && <span>{totalAtivos} anúncio(s) ativo(s)</span>}
          </p>
        </div>
        {souEu && <Link to="/meus-anuncios" className="text-sm">Gerenciar meus anúncios →</Link>}
      </header>

      <div className="grid items-start gap-6 md:grid-cols-[240px_minmax(0,1fr)] max-md:gap-3">
        {/* só as categorias em que este vendedor tem anúncios */}
        <aside aria-label="Categorias do vendedor"
          className="rounded-card border border-borda bg-white p-2.5 md:sticky md:top-[76px] md:max-h-[calc(100vh-96px)] md:overflow-y-auto md:px-2.5 md:py-4 max-md:overflow-x-auto">
          <h2 className="mx-2 mb-2.5 font-bold max-md:hidden">Categorias</h2>
          <ul className="flex gap-0.5 md:flex-col max-md:gap-1.5">
            <li>
              <button className={itemCategoria(categoriaId === '')} aria-current={categoriaId === '' ? 'true' : undefined}
                onClick={() => escolher('')}>Todas <span className="font-normal text-suave">{totalAtivos}</span></button>
            </li>
            {categorias.data?.map((c) => (
              <li key={c.categoriaId}>
                <button className={itemCategoria(categoriaId === c.categoriaId)} aria-current={categoriaId === c.categoriaId ? 'true' : undefined}
                  onClick={() => escolher(c.categoriaId)}>{c.nome} <span className="font-normal text-suave">{c.quantidade}</span></button>
              </li>
            ))}
          </ul>
        </aside>

        <div className="min-w-0">
          <div className="mb-4 flex items-center gap-2.5 rounded-card border border-gray-300 bg-white px-4 focus-within:border-primaria focus-within:ring-3 focus-within:ring-blue-200">
            <span aria-hidden="true">🔍</span>
            <input type="search" placeholder={`Buscar nos anúncios de ${v.nome}...`} value={busca}
              onChange={(e) => setBusca(e.target.value)} aria-label="Buscar nos anúncios do vendedor"
              className="min-w-0 flex-1 bg-transparent py-3.5 text-base outline-none" />
          </div>

          <ErroBox erro={anuncios.error ?? categorias.error} />
          {resultado && (termo || categoriaId) && (
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
                : souEu
                  ? <>Você não tem anúncios ativos. <Link to="/anuncios/novo">Cadastre um anúncio</Link>.</>
                  : 'Este vendedor não tem anúncios ativos no momento.'}
            </Vazio>
          ) : (
            <>
              <div className={cx('grid grid-cols-1 gap-4 transition-opacity sm:grid-cols-2 lg:grid-cols-3', anuncios.loading && 'opacity-55')}>
                {resultado.itens.map((a) => (
                  <CartaoAnuncio key={a.id} anuncio={a} categoria={nomeCategoria(a.categoriaId)} mostrarSeloCnpj={!usuario?.cnpj} />
                ))}
              </div>
              <Paginacao pagina={pagina} totalPaginas={resultado.totalPaginas} carregando={anuncios.loading} aoMudar={setPagina} />
            </>
          )}
        </div>
      </div>
    </section>
  );
}
