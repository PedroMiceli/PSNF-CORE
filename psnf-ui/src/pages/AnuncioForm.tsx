import { useEffect, useState, type FormEvent } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../api/endpoints';
import type { AnuncioInput } from '../api/types';
import { useAcao, useApi } from '../api/useApi';
import { Campo, Carregando, ErroBox } from '../components/ui';

interface ValorForm { valorUnidade: string; quantidadeMinima: string }
interface ImagemForm { caminho: string; principal: boolean }

const vazio = {
  titulo: '',
  descricao: '',
  categoriaId: '',
  valores: [{ valorUnidade: '', quantidadeMinima: '1' }] as ValorForm[],
  imagens: [{ caminho: '', principal: true }] as ImagemForm[],
  variacoes: [] as string[],
};

export function AnuncioForm() {
  const { id } = useParams();
  const editando = !!id;
  const navigate = useNavigate();
  const categorias = useApi(() => api.categorias(), []);
  const existente = useApi(async () => (id ? api.anuncio(id) : null), [id]);
  const acao = useAcao();
  const [form, setForm] = useState(vazio);

  useEffect(() => {
    const a = existente.data;
    if (!a) return;
    setForm({
      titulo: a.titulo,
      descricao: a.descricao,
      categoriaId: a.categoriaId,
      valores: [...a.valores].sort((x, y) => x.quantidadeMinima - y.quantidadeMinima)
        .map((v) => ({ valorUnidade: String(v.valorUnidade), quantidadeMinima: String(v.quantidadeMinima) })),
      imagens: a.imagens.map((i) => ({ caminho: i.caminho, principal: i.principal })),
      variacoes: a.variacoes.map((v) => v.variacao),
    });
  }, [existente.data]);

  const set = <K extends keyof typeof form>(k: K, v: (typeof form)[K]) => setForm((f) => ({ ...f, [k]: v }));
  const erroCampo = (prefixo: string) =>
    Object.entries(acao.erro?.campos ?? {}).find(([k]) => k === prefixo || k.startsWith(prefixo + '['))?.[1];

  const salvar = async (e: FormEvent) => {
    e.preventDefault();
    const body: AnuncioInput = {
      titulo: form.titulo,
      descricao: form.descricao,
      categoriaId: form.categoriaId,
      valores: form.valores.map((v) => ({ valorUnidade: Number(v.valorUnidade), quantidadeMinima: Number(v.quantidadeMinima) })),
      imagens: form.imagens.filter((i) => i.caminho.trim()),
      variacoes: form.variacoes.filter((v) => v.trim()).map((variacao) => ({ variacao })),
    };
    const r = await acao.executar(() => (id ? api.atualizarAnuncio(id, body) : api.criarAnuncio(body)));
    if (r) navigate(`/anuncios/${r.id}`);
  };

  if (editando && existente.loading) return <Carregando />;

  return (
    <section className="formulario">
      <Link to="/meus-anuncios" className="voltar">← Meus anúncios</Link>
      <h1>{editando ? 'Editar anúncio' : 'Novo anúncio'}</h1>
      <ErroBox erro={existente.error ?? acao.erro} />

      <form onSubmit={salvar}>
        <div className="painel">
          <Campo label="Título" erro={erroCampo('titulo')}>
            <input value={form.titulo} onChange={(e) => set('titulo', e.target.value)} placeholder="Carrinho de metal" />
          </Campo>
          <Campo label="Descrição" erro={erroCampo('descricao')}>
            <textarea rows={4} value={form.descricao} onChange={(e) => set('descricao', e.target.value)} />
          </Campo>
          <Campo label="Categoria" erro={erroCampo('categoriaId')} dica={categorias.data?.length ? undefined : 'Cadastre uma categoria primeiro'}>
            <select value={form.categoriaId} onChange={(e) => set('categoriaId', e.target.value)}>
              <option value="">Selecione...</option>
              {categorias.data?.map((c) => <option key={c.id} value={c.id}>{c.nome}</option>)}
            </select>
          </Campo>
        </div>

        <div className="painel">
          <h3>Faixas de preço</h3>
          <p className="dica">A partir da quantidade mínima, cada unidade custa o valor informado.</p>
          {erroCampo('valores') && <p className="campo-msg">{erroCampo('valores')}</p>}
          {form.valores.map((v, i) => (
            <div key={i} className="linha-form">
              <Campo label="A partir de (un.)">
                <input type="number" min={1} value={v.quantidadeMinima}
                  onChange={(e) => set('valores', form.valores.map((x, j) => (j === i ? { ...x, quantidadeMinima: e.target.value } : x)))} />
              </Campo>
              <Campo label="Valor por unidade (R$)">
                <input type="number" step="0.01" min={0} value={v.valorUnidade}
                  onChange={(e) => set('valores', form.valores.map((x, j) => (j === i ? { ...x, valorUnidade: e.target.value } : x)))} />
              </Campo>
              <button type="button" className="btn pequeno perigo" disabled={form.valores.length === 1}
                onClick={() => set('valores', form.valores.filter((_, j) => j !== i))}>Remover</button>
            </div>
          ))}
          <button type="button" className="btn pequeno" onClick={() => set('valores', [...form.valores, { valorUnidade: '', quantidadeMinima: '' }])}>
            + Faixa de preço
          </button>
        </div>

        <div className="painel">
          <h3>Imagens</h3>
          <p className="dica">Informe URLs (ex.: https://picsum.photos/seed/carrinho/600/400). Marque qual é a principal.</p>
          {erroCampo('imagens') && <p className="campo-msg">{erroCampo('imagens')}</p>}
          {form.imagens.map((img, i) => (
            <div key={i} className="linha-form">
              <Campo label={`Imagem ${i + 1}`}>
                <input value={img.caminho} placeholder="https://..."
                  onChange={(e) => set('imagens', form.imagens.map((x, j) => (j === i ? { ...x, caminho: e.target.value } : x)))} />
              </Campo>
              <label className="radio">
                <input type="radio" name="principal" checked={img.principal}
                  onChange={() => set('imagens', form.imagens.map((x, j) => ({ ...x, principal: j === i })))} />
                principal
              </label>
              <button type="button" className="btn pequeno perigo" disabled={form.imagens.length === 1}
                onClick={() => set('imagens', form.imagens.filter((_, j) => j !== i))}>Remover</button>
            </div>
          ))}
          <button type="button" className="btn pequeno" onClick={() => set('imagens', [...form.imagens, { caminho: '', principal: false }])}>
            + Imagem
          </button>
        </div>

        <div className="painel">
          <h3>Variações <small>(opcional)</small></h3>
          {form.variacoes.map((v, i) => (
            <div key={i} className="linha-form">
              <Campo label={`Variação ${i + 1}`}>
                <input value={v} placeholder="BRANCO - P"
                  onChange={(e) => set('variacoes', form.variacoes.map((x, j) => (j === i ? e.target.value : x)))} />
              </Campo>
              <button type="button" className="btn pequeno perigo" onClick={() => set('variacoes', form.variacoes.filter((_, j) => j !== i))}>
                Remover
              </button>
            </div>
          ))}
          <button type="button" className="btn pequeno" onClick={() => set('variacoes', [...form.variacoes, ''])}>+ Variação</button>
        </div>

        <div className="acoes">
          <button type="submit" className="btn primario" disabled={acao.enviando}>
            {acao.enviando ? 'Salvando...' : editando ? 'Salvar alterações' : 'Publicar anúncio'}
          </button>
        </div>
      </form>
    </section>
  );
}
