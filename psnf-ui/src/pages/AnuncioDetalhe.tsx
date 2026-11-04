import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../api/endpoints';
import { useAcao, useApi } from '../api/useApi';
import { formatarMoeda } from '../components/format';
import { Carregando, ErroBox, ImagemAnuncio } from '../components/ui';
import { useUsuario } from '../context/UsuarioContext';

export function AnuncioDetalhe() {
  const { id = '' } = useParams();
  const { usuarioId } = useUsuario();
  const navigate = useNavigate();
  const anuncio = useApi(() => api.anuncio(id), [id]);
  const vendedor = useApi(async () => (anuncio.data ? api.usuario(anuncio.data.vendedorId) : null), [anuncio.data?.vendedorId]);
  const acao = useAcao();
  const [imagemSelecionada, setImagemSelecionada] = useState<string | null>(null);

  if (anuncio.loading) return <Carregando />;
  if (anuncio.error || !anuncio.data) return <ErroBox erro={anuncio.error} />;

  const a = anuncio.data;
  const ehDono = a.vendedorId === usuarioId;
  const faixas = [...a.valores].sort((x, y) => x.quantidadeMinima - y.quantidadeMinima);
  const principal = imagemSelecionada ?? (a.imagens.find((i) => i.principal) ?? a.imagens[0])?.caminho;

  const alternarStatus = async () => {
    const r = await acao.executar(() => (a.ativo ? api.desativarAnuncio(a.id) : api.ativarAnuncio(a.id)));
    if (r) anuncio.setData(r);
  };

  return (
    <section className="detalhe-anuncio">
      <Link to="/" className="voltar">← Vitrine</Link>
      <div className="detalhe-grid">
        <div className="galeria">
          <ImagemAnuncio caminho={principal} alt={a.titulo} className="galeria-principal" />
          <div className="galeria-thumbs">
            {a.imagens.map((img) => (
              <button key={img.id} onClick={() => setImagemSelecionada(img.caminho)} className={img.caminho === principal ? 'ativo' : ''}>
                <ImagemAnuncio caminho={img.caminho} alt="" className="thumb" />
              </button>
            ))}
          </div>
        </div>

        <div className="detalhe-info">
          <p className="fornecedor">{vendedor.data?.nome ?? 'Fornecedor'}</p>
          <h1>
            {a.titulo} {!a.ativo && <span className="badge status-CANCELADO">Inativo</span>}
          </h1>

          {a.variacoes.length > 0 && (
            <>
              <h4>Variações</h4>
              <div className="chips">
                {a.variacoes.map((v) => <span key={v.id} className="chip estatico">{v.variacao}</span>)}
              </div>
            </>
          )}

          <h4>Preço por quantidade</h4>
          <div className="faixas">
            {faixas.map((f, i) => {
              const prox = faixas[i + 1];
              return (
                <div key={f.id} className="faixa">
                  <small>{prox ? `${f.quantidadeMinima} – ${prox.quantidadeMinima - 1} un.` : `a partir de ${f.quantidadeMinima} un.`}</small>
                  <strong>{formatarMoeda(f.valorUnidade)}</strong>
                </div>
              );
            })}
          </div>

          <ErroBox erro={acao.erro} />
          {ehDono ? (
            <div className="acoes">
              <button className="btn primario" onClick={() => navigate(`/ordens/nova?anuncio=${a.id}`)}>Gerar ordem de pedido</button>
              <button className="btn" onClick={() => navigate(`/anuncios/${a.id}/editar`)}>Editar</button>
              <button className="btn" disabled={acao.enviando} onClick={alternarStatus}>{a.ativo ? 'Desativar' : 'Ativar'}</button>
            </div>
          ) : (
            <div className="acoes">
              <button className="btn primario" disabled title="O chat ainda não existe na API">Iniciar chat</button>
              <p className="dica">
                O chat ainda não foi implementado. No fluxo previsto, comprador e vendedor negociam no chat e o
                <strong> vendedor</strong> gera a ordem de pedido para o comprador.
              </p>
            </div>
          )}

          <h4>Descrição do produto</h4>
          <p className="descricao">{a.descricao}</p>
        </div>
      </div>
    </section>
  );
}
