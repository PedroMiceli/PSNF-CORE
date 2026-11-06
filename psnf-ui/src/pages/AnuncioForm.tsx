import { useEffect, useState, type FormEvent } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { api } from '../api/endpoints';
import type { AnuncioInput } from '../api/types';
import { useAcao, useApi } from '../api/useApi';
import { ACEITA_IMAGENS, UploadArquivo } from '../components/UploadArquivo';
import { acoes, Botao, Campo, Carregando, controle, cx, dica, ErroBox, ImagemAnuncio, linhaForm, Painel, Voltar } from '../components/ui';

interface ValorForm { valorUnidade: string; quantidadeMinima: string }
interface ImagemForm { caminho: string; principal: boolean }

/** Garante exatamente uma imagem principal (a primeira, se nenhuma estiver marcada). */
const comPrincipal = (imgs: ImagemForm[]) =>
  imgs.some((i) => i.principal) ? imgs : imgs.map((i, j) => ({ ...i, principal: j === 0 }));

const vazio = {
  titulo: '',
  descricao: '',
  categoriaId: '',
  valores: [{ valorUnidade: '', quantidadeMinima: '1' }] as ValorForm[],
  imagens: [] as ImagemForm[],
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
  const [urlExterna, setUrlExterna] = useState('');

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
      imagens: form.imagens,
      variacoes: form.variacoes.filter((v) => v.trim()).map((variacao) => ({ variacao })),
    };
    const r = await acao.executar(() => (id ? api.atualizarAnuncio(id, body) : api.criarAnuncio(body)));
    if (r) navigate(`/anuncios/${r.id}`);
  };

  if (editando && existente.loading) return <Carregando />;

  const erroGeral = (texto?: string) => texto && <p className="mb-2 text-xs text-vermelho">{texto}</p>;

  return (
    <section className="max-w-[960px]">
      <Voltar to="/meus-anuncios">Meus anúncios</Voltar>
      <h1>{editando ? 'Editar anúncio' : 'Novo anúncio'}</h1>
      <ErroBox erro={existente.error ?? acao.erro} />

      <form onSubmit={salvar}>
        <Painel>
          <Campo label="Título" erro={erroCampo('titulo')}>
            <input className={controle} value={form.titulo} onChange={(e) => set('titulo', e.target.value)} placeholder="Carrinho de metal" />
          </Campo>
          <Campo label="Descrição" erro={erroCampo('descricao')}>
            <textarea className={controle} rows={4} value={form.descricao} onChange={(e) => set('descricao', e.target.value)} />
          </Campo>
          <Campo label="Categoria" erro={erroCampo('categoriaId')} dica={categorias.data?.length ? undefined : 'Nenhuma categoria disponível. Verifique se a API está no ar.'}>
            <select className={controle} value={form.categoriaId} onChange={(e) => set('categoriaId', e.target.value)}>
              <option value="">Selecione...</option>
              {categorias.data?.map((c) => <option key={c.id} value={c.id}>{c.nome}</option>)}
            </select>
          </Campo>
        </Painel>

        <Painel>
          <h3>Faixas de preço</h3>
          <p className={dica}>A partir da quantidade mínima, cada unidade custa o valor informado.</p>
          {erroGeral(erroCampo('valores'))}
          {form.valores.map((v, i) => (
            <div key={i} className={linhaForm}>
              <Campo label="A partir de (un.)">
                <input className={controle} type="number" min={1} value={v.quantidadeMinima}
                  onChange={(e) => set('valores', form.valores.map((x, j) => (j === i ? { ...x, quantidadeMinima: e.target.value } : x)))} />
              </Campo>
              <Campo label="Valor por unidade (R$)">
                <input className={controle} type="number" step="0.01" min={0} value={v.valorUnidade}
                  onChange={(e) => set('valores', form.valores.map((x, j) => (j === i ? { ...x, valorUnidade: e.target.value } : x)))} />
              </Campo>
              <Botao variante="perigo" tamanho="pequeno" className="mb-3" disabled={form.valores.length === 1}
                onClick={() => set('valores', form.valores.filter((_, j) => j !== i))}>Remover</Botao>
            </div>
          ))}
          <Botao tamanho="pequeno" onClick={() => set('valores', [...form.valores, { valorUnidade: '', quantidadeMinima: '' }])}>
            + Faixa de preço
          </Botao>
        </Painel>

        <Painel>
          <h3>Imagens</h3>
          <p className={dica}>Envie as fotos do produto (JPG, PNG, WEBP ou GIF até 5 MB). Clique em ★ para escolher a principal.</p>
          {erroGeral(erroCampo('imagens'))}
          {form.imagens.length > 0 && (
            <div className="mb-3 grid grid-cols-[repeat(auto-fill,minmax(150px,1fr))] gap-3">
              {form.imagens.map((img, i) => (
                <div key={img.caminho + i}
                  className={cx('overflow-hidden rounded-card border-2 bg-white', img.principal ? 'border-amber-500' : 'border-borda')}>
                  <ImagemAnuncio caminho={img.caminho} alt={`Imagem ${i + 1}`} className="h-[110px] w-full" />
                  <div className="flex flex-wrap justify-between gap-1 p-1.5">
                    <Botao tamanho="pequeno" title="Definir como principal" aria-pressed={img.principal}
                      onClick={() => set('imagens', form.imagens.map((x, j) => ({ ...x, principal: j === i })))}>
                      {img.principal ? '★ Principal' : '☆'}
                    </Botao>
                    <Botao variante="perigo" tamanho="pequeno"
                      onClick={() => set('imagens', comPrincipal(form.imagens.filter((_, j) => j !== i)))}>Remover</Botao>
                  </div>
                </div>
              ))}
            </div>
          )}
          <UploadArquivo accept={ACEITA_IMAGENS} multiple rotulo="Enviar imagens"
            onEnviado={(a) => setForm((f) => ({ ...f, imagens: comPrincipal([...f.imagens, { caminho: a.caminho, principal: false }]) }))} />
          <div className={cx(linhaForm, 'mt-1.5')}>
            <Campo label="ou adicione por URL">
              <input className={controle} value={urlExterna} placeholder="https://..." onChange={(e) => setUrlExterna(e.target.value)} />
            </Campo>
            <Botao tamanho="pequeno" className="mb-3" disabled={!urlExterna.trim()}
              onClick={() => { set('imagens', comPrincipal([...form.imagens, { caminho: urlExterna.trim(), principal: false }])); setUrlExterna(''); }}>
              Adicionar URL
            </Botao>
          </div>
        </Painel>

        <Painel>
          <h3>Variações <small className="font-normal text-suave">(opcional)</small></h3>
          {form.variacoes.map((v, i) => (
            <div key={i} className={linhaForm}>
              <Campo label={`Variação ${i + 1}`}>
                <input className={controle} value={v} placeholder="BRANCO - P"
                  onChange={(e) => set('variacoes', form.variacoes.map((x, j) => (j === i ? e.target.value : x)))} />
              </Campo>
              <Botao variante="perigo" tamanho="pequeno" className="mb-3"
                onClick={() => set('variacoes', form.variacoes.filter((_, j) => j !== i))}>Remover</Botao>
            </div>
          ))}
          <Botao tamanho="pequeno" onClick={() => set('variacoes', [...form.variacoes, ''])}>+ Variação</Botao>
        </Painel>

        <div className={acoes}>
          <Botao type="submit" variante="primario" disabled={acao.enviando}>
            {acao.enviando ? 'Salvando...' : editando ? 'Salvar alterações' : 'Publicar anúncio'}
          </Botao>
        </div>
      </form>
    </section>
  );
}
