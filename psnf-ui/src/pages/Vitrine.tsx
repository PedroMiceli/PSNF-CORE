import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/endpoints';
import { useApi } from '../api/useApi';
import { formatarMoeda } from '../components/format';
import { Carregando, ErroBox, ImagemAnuncio, Vazio } from '../components/ui';

export function Vitrine() {
  const [categoriaId, setCategoriaId] = useState<string>('');
  const [busca, setBusca] = useState('');
  const categorias = useApi(() => api.categorias(), []);
  const anuncios = useApi(() => api.anunciosAtivos(categoriaId || undefined), [categoriaId]);

  const filtrados = useMemo(() => {
    const termo = busca.trim().toLowerCase();
    const lista = anuncios.data ?? [];
    return termo ? lista.filter((a) => `${a.titulo} ${a.descricao}`.toLowerCase().includes(termo)) : lista;
  }, [anuncios.data, busca]);

  const nomeCategoria = (id: string) => categorias.data?.find((c) => c.id === id)?.nome ?? '';

  return (
    <section>
      <div className="titulo-pagina">
        <h1>Vitrine</h1>
        <input className="busca" placeholder="Buscar por título ou descrição..." value={busca} onChange={(e) => setBusca(e.target.value)} />
      </div>

      <div className="chips">
        <button className={`chip ${categoriaId === '' ? 'ativo' : ''}`} onClick={() => setCategoriaId('')}>Todas</button>
        {categorias.data?.map((c) => (
          <button key={c.id} className={`chip ${categoriaId === c.id ? 'ativo' : ''}`} onClick={() => setCategoriaId(c.id)}>
            {c.nome}
          </button>
        ))}
      </div>

      <ErroBox erro={anuncios.error ?? categorias.error} />
      {anuncios.loading ? (
        <Carregando />
      ) : filtrados.length === 0 ? (
        <Vazio>
          Nenhum anúncio ativo encontrado. <Link to="/anuncios/novo">Cadastre um anúncio</Link> como vendedor.
        </Vazio>
      ) : (
        <div className="grid-cards">
          {filtrados.map((a) => {
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
      )}
    </section>
  );
}
