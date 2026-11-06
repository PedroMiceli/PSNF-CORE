import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { api } from '../api/endpoints';
import { useAcao, useApi } from '../api/useApi';
import { DetalhesProduto } from '../components/DetalhesProduto';
import { formatarMoeda } from '../components/format';
import { acoes, Badge, BADGE_INATIVO, Botao, Carregando, chip, cx, dica, ErroBox, ImagemAnuncio, Voltar } from '../components/ui';
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
    <section>
      <Voltar to="/">Vitrine</Voltar>
      <div className="grid gap-8 md:grid-cols-[minmax(280px,1fr)_1.2fr]">
        {/* galeria */}
        <div>
          <ImagemAnuncio caminho={principal} alt={a.titulo} className="h-[380px] w-full rounded-card border border-borda max-md:h-[260px]" />
          <div className="mt-2.5 flex flex-wrap gap-2">
            {a.imagens.map((img) => (
              <button key={img.id} onClick={() => setImagemSelecionada(img.caminho)} aria-label="Ver imagem"
                className={cx('cursor-pointer rounded-lg border-2 p-0', img.caminho === principal ? 'border-primaria' : 'border-transparent')}>
                <ImagemAnuncio caminho={img.caminho} alt="" compacta className="h-14 w-18 rounded-md" />
              </button>
            ))}
          </div>
        </div>

        {/* informações */}
        <div className="min-w-0">
          <p className="text-sm tracking-wider text-suave uppercase">{vendedor.data?.nome ?? 'Fornecedor'}</p>
          <h1>
            {a.titulo} {!a.ativo && <Badge className={BADGE_INATIVO}>Inativo</Badge>}
          </h1>

          {a.variacoes.length > 0 && (
            <>
              <h4>Variações</h4>
              <div className="mb-5 flex flex-wrap gap-2">
                {a.variacoes.map((v) => (
                  <span key={v.id} className={chip} title={[v.sku && `SKU ${v.sku}`, v.gtin && `GTIN ${v.gtin}`].filter(Boolean).join(' · ') || undefined}>
                    {v.variacao}
                  </span>
                ))}
              </div>
            </>
          )}

          <h4>Preço por quantidade</h4>
          <div className="mb-5 grid grid-cols-[repeat(auto-fill,minmax(140px,1fr))] gap-2">
            {faixas.map((f, i) => {
              const prox = faixas[i + 1];
              return (
                <div key={f.id} className="flex flex-col rounded-lg border border-green-200 bg-green-50 px-3 py-2.5">
                  <small className="text-xs text-suave">
                    {prox ? `${f.quantidadeMinima} – ${prox.quantidadeMinima - 1} un.` : `a partir de ${f.quantidadeMinima} un.`}
                  </small>
                  <strong className="text-lg text-verde">{formatarMoeda(f.valorUnidade)}</strong>
                </div>
              );
            })}
          </div>

          <ErroBox erro={acao.erro} />
          {ehDono ? (
            <div className={acoes}>
              <Botao variante="primario" onClick={() => navigate(`/ordens/nova?anuncio=${a.id}`)}>Gerar ordem de pedido</Botao>
              <Botao onClick={() => navigate(`/anuncios/${a.id}/editar`)}>Editar</Botao>
              <Botao disabled={acao.enviando} onClick={alternarStatus}>{a.ativo ? 'Desativar' : 'Ativar'}</Botao>
            </div>
          ) : (
            <div>
              <Botao variante="primario" disabled title="O chat ainda não existe na API">Iniciar chat</Botao>
              <p className={dica}>
                O chat ainda não foi implementado. No fluxo previsto, comprador e vendedor negociam no chat e o
                <strong> vendedor</strong> gera a ordem de pedido para o comprador.
              </p>
            </div>
          )}

          <h4>Descrição do produto</h4>
          <p className="leading-relaxed whitespace-pre-wrap">{a.descricao}</p>

          <DetalhesProduto anuncio={a} />
        </div>
      </div>
    </section>
  );
}
