import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../api/endpoints';
import { useAcao, useApi } from '../api/useApi';
import { AvaliacoesAnuncio } from '../components/AvaliacoesAnuncio';
import { DetalhesProduto } from '../components/DetalhesProduto';
import { Estrelas, formatarNota } from '../components/Estrelas';
import { formatarMoeda } from '../components/format';
import { acoes, Badge, BADGE_CNPJ, BADGE_INATIVO, BADGE_PF, Botao, Carregando, chip, cx, dica, ErroBox, ImagemAnuncio, Voltar } from '../components/ui';
import { useChat } from '../context/ChatContext';
import { useUsuario } from '../context/UsuarioContext';

export function AnuncioDetalhe() {
  const { id = '' } = useParams();
  const { usuarioId } = useUsuario();
  const navigate = useNavigate();
  const anuncio = useApi(() => api.anuncio(id), [id]);
  const vendedor = useApi(async () => (anuncio.data ? api.usuario(anuncio.data.vendedorId) : null), [anuncio.data?.vendedorId]);
  const acao = useAcao();
  const { iniciarNegociacao } = useChat();
  const [iniciando, setIniciando] = useState(false);
  const [imagemSelecionada, setImagemSelecionada] = useState<string | null>(null);
  // nota no topo: vem do anúncio e acompanha quando o usuário avalia na própria página
  const [notaLocal, setNota] = useState<{ media: number | null; total: number } | null>(null);

  if (anuncio.loading) return <Carregando />;
  if (anuncio.error || !anuncio.data) return <ErroBox erro={anuncio.error} />;

  const a = anuncio.data;
  const nota = notaLocal ?? { media: a.notaMedia, total: a.quantidadeAvaliacoes };
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
          <p className="text-sm tracking-wider text-suave uppercase">
            {vendedor.data?.nome ?? 'Fornecedor'}
            {vendedor.data?.cnpjVerificado && (
              <span className="ml-2 align-middle text-xs font-semibold tracking-normal text-verde normal-case"
                title={`CNPJ ativo na Receita Federal: ${vendedor.data.razaoSocial ?? ''}`}>✓ Empresa verificada</span>
            )}
          </p>
          <h1>
            {a.titulo} {!a.ativo && <Badge className={BADGE_INATIVO}>Inativo</Badge>}{' '}
            {a.exigeCnpj
              ? <Badge className={BADGE_CNPJ}>Venda somente para CNPJ</Badge>
              : <Badge className={BADGE_PF}>Aceita pessoa física</Badge>}
          </h1>
          <a href="#avaliacoes" className="-mt-2 mb-4 inline-flex items-center gap-2 text-sm hover:no-underline"
            onClick={(e) => { e.preventDefault(); document.getElementById('avaliacoes')?.scrollIntoView({ behavior: 'smooth' }); }}>
            {nota.total > 0 ? (
              <><Estrelas nota={nota.media ?? 0} /> <strong className="text-texto">{formatarNota(nota.media)}</strong>
                <span className="text-suave">({nota.total} avaliação(ões))</span></>
            ) : <span className="text-suave">☆ Ainda sem avaliações</span>}
          </a>

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
            <div>
              <div className={acoes}>
                <Botao onClick={() => navigate(`/anuncios/${a.id}/editar`)}>Editar</Botao>
                <Botao disabled={acao.enviando} onClick={alternarStatus}>{a.ativo ? 'Desativar' : 'Ativar'}</Botao>
              </div>
              <p className={dica}>
                Compradores interessados iniciam uma negociação por aqui. Acompanhe as conversas em{' '}
                <Link to="/negociacoes">Negociações</Link> e gere a ordem de pedido direto pelo chat.
              </p>
            </div>
          ) : (
            <div>
              <Botao variante="primario" disabled={iniciando || !a.ativo} onClick={async () => {
                setIniciando(true);
                acao.setErro(await iniciarNegociacao(a.id));
                setIniciando(false);
              }}>
                💬 {iniciando ? 'Abrindo...' : 'Iniciar negociação'}
              </Botao>
              <p className={dica}>
                {a.ativo
                  ? <>Converse com o fornecedor sobre quantidades, valores e entrega. Fechado o acordo, o <strong>vendedor</strong> gera a ordem de pedido para você.</>
                  : 'Este anúncio está inativo e não aceita novas negociações.'}
              </p>
            </div>
          )}

          <h4>Descrição do produto</h4>
          <p className="leading-relaxed whitespace-pre-wrap">{a.descricao}</p>

          <DetalhesProduto anuncio={a} />
        </div>
      </div>
      <AvaliacoesAnuncio anuncioId={a.id} onAtualizado={(media, total) => setNota({ media, total })} />
    </section>
  );
}
