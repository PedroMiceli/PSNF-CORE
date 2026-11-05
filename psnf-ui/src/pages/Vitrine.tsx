import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/endpoints';
import { useApi } from '../api/useApi';
import { formatarMoeda } from '../components/format';
import { Carregando, ErroBox, ImagemAnuncio, Vazio } from '../components/ui';

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

  return (
    <section className="vitrine">
      <h1 className="sr-only">Vitrine</h1>

      {/* coluna da esquerda: categorias */}
      <aside className="vitrine-categorias" aria-label="Categorias">
        <h2>Categorias</h2>
        <ul>
          <li>
            <button className={categoriaId === '' ? 'ativo' : ''} aria-current={categoriaId === '' ? 'true' : undefined}
              onClick={() => escolherCategoria('')}>Todas</button>
          </li>
          {categorias.data?.map((c) => (
            <li key={c.id}>
              <button className={categoriaId === c.id ? 'ativo' : ''} aria-current={categoriaId === c.id ? 'true' : undefined}
                onClick={() => escolherCategoria(c.id)}>{c.nome}</button>
            </li>
          ))}
        </ul>
      </aside>

      {/* conteúdo: barra de pesquisa no topo e os anúncios no meio */}
      <div className="vitrine-conteudo">
        <div className="vitrine-busca">
          <span aria-hidden="true">🔍</span>
          <input type="search" placeholder="Buscar produtos (ex.: carrinho, cabo usb)..." value={busca}
            onChange={(e) => setBusca(e.target.value)} aria-label="Buscar produtos" />
        </div>

        <ErroBox erro={anuncios.error ?? categorias.error} />
        {resultado && (
          <p className="contagem">
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
              : <>Nenhum anúncio ativo. <Link to="/anuncios/novo">Cadastre um anúncio</Link> como vendedor.</>}
          </Vazio>
        ) : (
          <>
            <div className={`grid-vitrine ${anuncios.loading ? 'atualizando' : ''}`}>
              {resultado.itens.map((a) => {
                const principal = a.imagens.find((i) => i.principal) ?? a.imagens[0];
                return (
                  <Link key={a.id} to={`/anuncios/${a.id}`} className="card-anuncio">
                    <ImagemAnuncio caminho={principal?.caminho} alt={a.titulo} className="card-img" />
                    <div className="card-corpo">
                      <span className="card-categoria">{nomeCategoria(a.categoriaId)}</span>
                      <h3>{a.titulo}</h3>
                      <p className="card-preco">
                        <small>a partir de</small> {formatarMoeda(a.precoMenor)}
                      </p>
                      <p className="card-meta">
                        {a.valores.length} faixa(s) de preço · {a.variacoes.length} variação(ões)
                      </p>
                    </div>
                  </Link>
                );
              })}
            </div>
            {resultado.totalPaginas > 1 && (
              <nav className="paginacao" aria-label="Páginas">
                <button className="btn" disabled={pagina === 0 || anuncios.loading}
                  onClick={() => { setPagina(pagina - 1); window.scrollTo({ top: 0 }); }}>← Anterior</button>
                <span>Página {pagina + 1} de {resultado.totalPaginas}</span>
                <button className="btn" disabled={pagina + 1 >= resultado.totalPaginas || anuncios.loading}
                  onClick={() => { setPagina(pagina + 1); window.scrollTo({ top: 0 }); }}>Próxima →</button>
              </nav>
            )}
          </>
        )}
      </div>
    </section>
  );
}
