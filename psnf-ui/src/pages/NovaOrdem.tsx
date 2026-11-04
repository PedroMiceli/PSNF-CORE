import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { api } from '../api/endpoints';
import type { Anuncio, Usuario } from '../api/types';
import { useAcao, useApi } from '../api/useApi';
import { formatarMoeda } from '../components/format';
import { Campo, Carregando, ErroBox, Vazio } from '../components/ui';
import { COMPRADOR_MOCK } from '../context/UsuarioContext';

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
    valorUnidade: String(precoSugerido(anuncio, qtd) ?? ''),
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

  const [compradorId, setCompradorId] = useState(COMPRADOR_MOCK.id);
  const [comprador, setComprador] = useState<Usuario | null>(null);
  const [itens, setItens] = useState<ItemForm[]>([]);

  const ativos = useMemo(() => (meus.data ?? []).filter((a) => a.ativo), [meus.data]);
  const porId = (id: string) => ativos.find((a) => a.id === id);

  // primeiro item: o anúncio vindo da tela de detalhe (?anuncio=) ou vazio
  useEffect(() => {
    if (meus.data && itens.length === 0) setItens([novoItem(porId(params.get('anuncio') ?? ''))]);
  }, [meus.data]); // eslint-disable-line react-hooks/exhaustive-deps

  const verificarComprador = async () => {
    setComprador(null);
    const u = await verificacao.executar(() => api.usuario(compradorId.trim()));
    if (u) setComprador(u);
  };
  // verifica o comprador padrão ao abrir a tela
  useEffect(() => { void verificarComprador(); }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const atualizar = (i: number, campo: keyof ItemForm, valor: string) =>
    setItens((lista) => lista.map((it, j) => {
      if (j !== i) return it;
      if (campo === 'anuncioId') return novoItem(porId(valor));
      const novo = { ...it, [campo]: valor };
      if (campo === 'quantidade') {
        const sugerido = precoSugerido(porId(it.anuncioId), Number(valor));
        if (sugerido != null) novo.valorUnidade = String(sugerido);
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

  return (
    <section className="formulario">
      <Link to="/ordens" className="voltar">← Ordens de pedido</Link>
      <h1>Gerar ordem de pedido</h1>
      <p className="dica">
        Após negociar com o comprador no chat, monte a ordem com as quantidades e os <strong>valores acordados</strong>.
        O valor unitário vem preenchido com a faixa de preço do anúncio, mas pode ser alterado.
      </p>
      <ErroBox erro={meus.error ?? acao.erro} />

      {ativos.length === 0 ? (
        <Vazio>Você não tem anúncios ativos. <Link to="/anuncios/novo">Cadastre um anúncio</Link> primeiro.</Vazio>
      ) : (
        <form onSubmit={criar}>
          <div className="painel">
            <h3>Comprador</h3>
            <div className="linha-form">
              <Campo label="Id do comprador" erro={acao.erro?.campos?.compradorId}>
                <input value={compradorId} onChange={(e) => { setCompradorId(e.target.value); setComprador(null); }} />
              </Campo>
              <button type="button" className="btn pequeno" onClick={verificarComprador}>Verificar</button>
              <button type="button" className="btn pequeno" onClick={() => { setCompradorId(COMPRADOR_MOCK.id); setComprador(null); }}>
                Usar comprador mock
              </button>
            </div>
            {comprador && <p className="ok">✔ {comprador.nome} — {comprador.email}</p>}
            {verificacao.erro && <p className="campo-msg">{verificacao.erro.message}</p>}
          </div>

          <div className="painel">
            <h3>Itens</h3>
            <table className="tabela tabela-form">
              <thead>
                <tr><th>Anúncio</th><th>Variação</th><th>Quantidade</th><th>Valor unitário (R$)</th><th>Subtotal</th><th></th></tr>
              </thead>
              <tbody>
                {itens.map((it, i) => {
                  const anuncio = porId(it.anuncioId);
                  return (
                    <tr key={i}>
                      <td>
                        <select value={it.anuncioId} onChange={(e) => atualizar(i, 'anuncioId', e.target.value)}>
                          <option value="">Selecione...</option>
                          {ativos.map((a) => <option key={a.id} value={a.id}>{a.titulo}</option>)}
                        </select>
                      </td>
                      <td>
                        <select value={it.variacaoId} disabled={!anuncio?.variacoes.length} onChange={(e) => atualizar(i, 'variacaoId', e.target.value)}>
                          <option value="">{anuncio?.variacoes.length ? 'Sem variação' : '—'}</option>
                          {anuncio?.variacoes.map((v) => <option key={v.id} value={v.id}>{v.variacao}</option>)}
                        </select>
                      </td>
                      <td><input type="number" min={1} value={it.quantidade} onChange={(e) => atualizar(i, 'quantidade', e.target.value)} /></td>
                      <td><input type="number" step="0.01" min={0} value={it.valorUnidade} onChange={(e) => atualizar(i, 'valorUnidade', e.target.value)} /></td>
                      <td className="num">{formatarMoeda((Number(it.quantidade) || 0) * (Number(it.valorUnidade) || 0))}</td>
                      <td>
                        <button type="button" className="btn pequeno perigo" disabled={itens.length === 1}
                          onClick={() => setItens(itens.filter((_, j) => j !== i))}>Remover</button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
              <tfoot>
                <tr>
                  <td colSpan={4}>
                    <button type="button" className="btn pequeno" onClick={() => setItens([...itens, novoItem()])}>+ Adicionar produto</button>
                  </td>
                  <td className="num total">{formatarMoeda(total)}</td>
                  <td></td>
                </tr>
              </tfoot>
            </table>
            {acao.erro?.campos && Object.keys(acao.erro.campos).some((k) => k.startsWith('itens')) && (
              <p className="campo-msg">Revise os itens: anúncio, quantidade e valor unitário são obrigatórios.</p>
            )}
          </div>

          <div className="acoes">
            <button type="submit" className="btn primario" disabled={acao.enviando}>
              {acao.enviando ? 'Enviando...' : 'Enviar para o comprador'}
            </button>
          </div>
        </form>
      )}
    </section>
  );
}
