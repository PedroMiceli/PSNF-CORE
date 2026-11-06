import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { api } from '../api/endpoints';
import type { Anuncio, Usuario } from '../api/types';
import { useAcao, useApi } from '../api/useApi';
import { formatarMoeda } from '../components/format';
import { acoes, Botao, Campo, Carregando, controle, cx, dica, ErroBox, InputMoeda, linhaForm, Painel, tabela, Vazio, Voltar } from '../components/ui';

interface ItemForm {
  anuncioId: string;
  variacaoId: string;
  quantidade: string;
  valorUnidade: string;
}

/** Sugestão de preço: a faixa do anúncio correspondente à quantidade (o vendedor pode negociar outro valor). */
function precoSugerido(anuncio: Anuncio | undefined, quantidade: number): number | null {
  if (!anuncio) return null;
  const faixa = [...anuncio.valores]
    .filter((v) => v.quantidadeMinima <= quantidade)
    .sort((a, b) => b.quantidadeMinima - a.quantidadeMinima)[0];
  return faixa?.valorUnidade ?? null;
}

const novoItem = (anuncio?: Anuncio): ItemForm => {
  const qtd = anuncio ? Math.min(...anuncio.valores.map((v) => v.quantidadeMinima)) : 1;
  return {
    anuncioId: anuncio?.id ?? '',
    variacaoId: '',
    quantidade: String(qtd),
    valorUnidade: precoSugerido(anuncio, qtd)?.toFixed(2) ?? '',
  };
};

/**
 * Tela do VENDEDOR: após fechar o acordo no chat, ele monta a ordem para o comprador
 * com as quantidades e os valores negociados.
 */
export function NovaOrdem() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const meus = useApi(() => api.meusAnuncios(), []);
  const acao = useAcao();
  const verificacao = useAcao();

  const [compradorId, setCompradorId] = useState(params.get('comprador') ?? '');
  const [comprador, setComprador] = useState<Usuario | null>(null);
  const [itens, setItens] = useState<ItemForm[]>([]);

  const ativos = useMemo(() => (meus.data ?? []).filter((a) => a.ativo), [meus.data]);
  const porId = (id: string) => ativos.find((a) => a.id === id);

  // primeiro item: o anúncio vindo da tela de detalhe (?anuncio=) ou vazio
  useEffect(() => {
    if (meus.data && itens.length === 0) setItens([novoItem(porId(params.get('anuncio') ?? ''))]);
  }, [meus.data]); // eslint-disable-line react-hooks/exhaustive-deps

  const verificarComprador = async (id = compradorId) => {
    setComprador(null);
    if (!id.trim()) return;
    const u = await verificacao.executar(() => api.usuario(id.trim()));
    if (u) setComprador(u);
  };

  // vindo do chat (?comprador=): já confere o comprador da negociação
  useEffect(() => {
    const id = params.get('comprador');
    if (id) void verificarComprador(id);
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const atualizar = (i: number, campo: keyof ItemForm, valor: string) =>
    setItens((lista) => lista.map((it, j) => {
      if (j !== i) return it;
      if (campo === 'anuncioId') return novoItem(porId(valor));
      const novo = { ...it, [campo]: valor };
      if (campo === 'quantidade') {
        const sugerido = precoSugerido(porId(it.anuncioId), Number(valor));
        if (sugerido != null) novo.valorUnidade = sugerido.toFixed(2);
      }
      return novo;
    }));

  const total = itens.reduce((s, it) => s + (Number(it.quantidade) || 0) * (Number(it.valorUnidade) || 0), 0);

  const criar = async (e: FormEvent) => {
    e.preventDefault();
    const ordem = await acao.executar(() => api.criarOrdem({
      compradorId: compradorId.trim(),
      itens: itens.map((it) => ({
        anuncioId: it.anuncioId,
        variacaoId: it.variacaoId || null,
        quantidade: Number(it.quantidade),
        valorUnidade: Number(it.valorUnidade),
      })),
    }));
    if (ordem) navigate(`/ordens/${ordem.id}`);
  };

  if (meus.loading) return <Carregando />;

  const celula = cx(tabela.td, 'px-2 py-1.5');
  return (
    <section className="max-w-[960px]">
      <Voltar to="/ordens">Ordens de pedido</Voltar>
      <h1>Gerar ordem de pedido</h1>
      <p className={dica}>
        Após negociar com o comprador no chat, monte a ordem com as quantidades e os <strong>valores acordados</strong>.
        O valor unitário vem preenchido com a faixa de preço do anúncio, mas pode ser alterado.
      </p>
      <ErroBox erro={meus.error ?? acao.erro} />

      {ativos.length === 0 ? (
        <Vazio>Você não tem anúncios ativos. <Link to="/anuncios/novo">Cadastre um anúncio</Link> primeiro.</Vazio>
      ) : (
        <form onSubmit={criar}>
          <Painel>
            <h3>Comprador</h3>
            <div className={linhaForm}>
              <Campo label="Código do comprador" erro={acao.erro?.campos?.compradorId}>
                <input className={controle} value={compradorId} placeholder="Informe o código do comprador"
                  onChange={(e) => { setCompradorId(e.target.value); setComprador(null); }} />
              </Campo>
              <Botao tamanho="pequeno" className="mb-3" disabled={!compradorId.trim() || verificacao.enviando}
                onClick={() => verificarComprador()}>Verificar</Botao>
            </div>
            {comprador && <p className="font-semibold text-verde">✔ {comprador.nome} — {comprador.email}</p>}
            {verificacao.erro && <p className="text-xs text-vermelho">{verificacao.erro.message}</p>}
          </Painel>

          <Painel>
            <h3>Itens</h3>
            <div className="overflow-x-auto">
              <table className={tabela.tabela}>
                <thead>
                  <tr>
                    {['Anúncio', 'Variação', 'Quantidade', 'Valor unitário', 'Subtotal', ''].map((t, i) => <th key={i} className={tabela.th}>{t}</th>)}
                  </tr>
                </thead>
                <tbody>
                  {itens.map((it, i) => {
                    const anuncio = porId(it.anuncioId);
                    return (
                      <tr key={i}>
                        <td className={celula}>
                          <select className={cx(controle, 'min-w-[220px]')} value={it.anuncioId} onChange={(e) => atualizar(i, 'anuncioId', e.target.value)}>
                            <option value="">Selecione...</option>
                            {ativos.map((a) => <option key={a.id} value={a.id}>{a.titulo}</option>)}
                          </select>
                        </td>
                        <td className={celula}>
                          <select className={cx(controle, 'min-w-[150px]')} value={it.variacaoId} disabled={!anuncio?.variacoes.length}
                            onChange={(e) => atualizar(i, 'variacaoId', e.target.value)}>
                            <option value="">{anuncio?.variacoes.length ? 'Sem variação' : '—'}</option>
                            {anuncio?.variacoes.map((v) => <option key={v.id} value={v.id}>{v.variacao}</option>)}
                          </select>
                        </td>
                        <td className={celula}>
                          <input className={cx(controle, 'min-w-[90px]')} type="number" min={1} value={it.quantidade}
                            onChange={(e) => atualizar(i, 'quantidade', e.target.value)} />
                        </td>
                        <td className={celula}>
                          <InputMoeda className="min-w-[110px]" valor={it.valorUnidade} aria-label="Valor unitário"
                            onValor={(v) => atualizar(i, 'valorUnidade', v)} />
                        </td>
                        <td className={cx(celula, tabela.num)}>{formatarMoeda((Number(it.quantidade) || 0) * (Number(it.valorUnidade) || 0))}</td>
                        <td className={celula}>
                          <Botao variante="perigo" tamanho="pequeno" disabled={itens.length === 1}
                            onClick={() => setItens(itens.filter((_, j) => j !== i))}>Remover</Botao>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
                <tfoot>
                  <tr>
                    <td colSpan={4} className={celula}>
                      <Botao tamanho="pequeno" onClick={() => setItens([...itens, novoItem()])}>+ Adicionar produto</Botao>
                    </td>
                    <td className={cx(celula, tabela.num, 'text-[1.05rem] font-extrabold')}>{formatarMoeda(total)}</td>
                    <td className={celula}></td>
                  </tr>
                </tfoot>
              </table>
            </div>
            {acao.erro?.campos && Object.keys(acao.erro.campos).some((k) => k.startsWith('itens')) && (
              <p className="mt-2 text-xs text-vermelho">Revise os itens: anúncio, quantidade e valor unitário são obrigatórios.</p>
            )}
          </Painel>

          <div className={acoes}>
            <Botao type="submit" variante="primario" disabled={acao.enviando}>
              {acao.enviando ? 'Enviando...' : 'Enviar para o comprador'}
            </Botao>
          </div>
        </form>
      )}
    </section>
  );
}
