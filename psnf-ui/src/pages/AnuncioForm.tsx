import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { api } from '../api/endpoints';
import type { AnuncioInput, Caracteristica } from '../api/types';
import { useAcao, useApi } from '../api/useApi';
import { InformacoesProdutoForm, type VariacaoForm } from '../components/InformacoesProdutoForm';
import {
  CONDICAO_LABEL, contarPreenchidos, formParaInfo, INFO_VAZIA, infoParaForm, validarGtin, validarInfo, type InfoForm,
} from '../components/produto';
import { SidePanel } from '../components/SidePanel';
import { ACEITA_IMAGENS, UploadArquivo } from '../components/UploadArquivo';
import { acoes, Botao, Campo, Carregando, controle, cx, dica, ErroBox, ImagemAnuncio, InputMoeda, linhaForm, Painel, Voltar } from '../components/ui';

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
  variacoes: [] as VariacaoForm[],
  informacoes: INFO_VAZIA as InfoForm,
  caracteristicas: [] as Caracteristica[],
};

/** Erros da API que pertencem ao painel de informações do produto. */
const ehErroDoPainel = (campo: string) =>
  campo.startsWith('informacoes.') || campo.startsWith('caracteristicas') || /^variacoes\[\d+\]\.(sku|gtin)$/.test(campo);

export function AnuncioForm() {
  const { id } = useParams();
  const editando = !!id;
  const navigate = useNavigate();
  const categorias = useApi(() => api.categorias(), []);
  const existente = useApi(async () => (id ? api.anuncio(id) : null), [id]);
  const acao = useAcao();
  const [form, setForm] = useState(vazio);
  const [urlExterna, setUrlExterna] = useState('');
  const [painelAberto, setPainelAberto] = useState(false);
  const [mostrarErrosPainel, setMostrarErrosPainel] = useState(false);
  const fecharPainel = useCallback(() => setPainelAberto(false), []);

  useEffect(() => {
    const a = existente.data;
    if (!a) return;
    setForm({
      titulo: a.titulo,
      descricao: a.descricao,
      categoriaId: a.categoriaId,
      valores: [...a.valores].sort((x, y) => x.quantidadeMinima - y.quantidadeMinima)
        .map((v) => ({ valorUnidade: v.valorUnidade.toFixed(2), quantidadeMinima: String(v.quantidadeMinima) })),
      imagens: a.imagens.map((i) => ({ caminho: i.caminho, principal: i.principal })),
      variacoes: a.variacoes.map((v) => ({ variacao: v.variacao, sku: v.sku ?? '', gtin: v.gtin ?? '' })),
      informacoes: infoParaForm(a.informacoes),
      caracteristicas: a.caracteristicas ?? [],
    });
  }, [existente.data]);

  // a API recusou algum campo do painel: abre o painel para mostrar onde está o erro
  useEffect(() => {
    if (Object.keys(acao.erro?.campos ?? {}).some(ehErroDoPainel)) setPainelAberto(true);
  }, [acao.erro]);

  const set = <K extends keyof typeof form>(k: K, v: (typeof form)[K]) => setForm((f) => ({ ...f, [k]: v }));
  const erroCampo = (prefixo: string) =>
    Object.entries(acao.erro?.campos ?? {}).find(([k]) => k === prefixo || k.startsWith(prefixo + '['))?.[1];

  const salvar = async (e: FormEvent) => {
    e.preventDefault();
    // erros que o navegador já consegue apontar (GTIN, NCM, pesos...): abre o painel em vez de enviar
    const comErro = Object.keys(validarInfo(form.informacoes)).length > 0 || form.variacoes.some((v) => validarGtin(v.gtin));
    if (comErro) {
      setMostrarErrosPainel(true);
      setPainelAberto(true);
      return;
    }
    const variacoes = form.variacoes.filter((v) => v.variacao.trim());
    const body: AnuncioInput = {
      titulo: form.titulo,
      descricao: form.descricao,
      categoriaId: form.categoriaId,
      valores: form.valores.map((v) => ({ valorUnidade: Number(v.valorUnidade), quantidadeMinima: Number(v.quantidadeMinima) })),
      imagens: form.imagens,
      variacoes: variacoes.map((v) => ({ variacao: v.variacao, sku: v.sku.trim() || null, gtin: v.gtin.trim() || null })),
      informacoes: formParaInfo(form.informacoes),
      caracteristicas: form.caracteristicas
        .map((c) => ({ nome: c.nome.trim(), valor: c.valor.trim() }))
        .filter((c) => c.nome || c.valor),
    };
    const r = await acao.executar(() => (id ? api.atualizarAnuncio(id, body) : api.criarAnuncio(body)));
    if (r) navigate(`/anuncios/${r.id}`);
  };

  if (editando && existente.loading) return <Carregando />;

  const totalPreenchidos = contarPreenchidos(form.informacoes) + form.caracteristicas.filter((c) => c.nome.trim()).length
    + form.variacoes.filter((v) => v.sku.trim() || v.gtin.trim()).length;
  const resumo = [
    form.informacoes.sku && `SKU ${form.informacoes.sku}`,
    /^\d{4}\.\d{2}\.\d{2}$/.test(form.informacoes.ncm) && `NCM ${form.informacoes.ncm}`,
    form.informacoes.condicao && CONDICAO_LABEL[form.informacoes.condicao as keyof typeof CONDICAO_LABEL],
    form.informacoes.marca,
    form.caracteristicas.length > 0 && `${form.caracteristicas.length} na ficha técnica`,
    totalPreenchidos > 0 && `${totalPreenchidos} campo(s) preenchido(s)`,
  ].filter(Boolean) as string[];

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
              <Campo label="Valor por unidade">
                <InputMoeda valor={v.valorUnidade}
                  onValor={(valor) => set('valores', form.valores.map((x, j) => (j === i ? { ...x, valorUnidade: valor } : x)))} />
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
              <Campo label={`Variação ${i + 1}`} erro={erroCampo(`variacoes[${i}].variacao`)}>
                <input className={controle} value={v.variacao} placeholder="BRANCO - P"
                  onChange={(e) => set('variacoes', form.variacoes.map((x, j) => (j === i ? { ...x, variacao: e.target.value } : x)))} />
              </Campo>
              <Botao variante="perigo" tamanho="pequeno" className="mb-3"
                onClick={() => set('variacoes', form.variacoes.filter((_, j) => j !== i))}>Remover</Botao>
            </div>
          ))}
          <Botao tamanho="pequeno" onClick={() => set('variacoes', [...form.variacoes, { variacao: '', sku: '', gtin: '' }])}>+ Variação</Botao>
        </Painel>

        <Painel className="flex flex-wrap items-center gap-4">
          <div className="min-w-[220px] flex-1">
            <h3 className="mt-0 mb-1">Informações do produto <small className="font-normal text-suave">(opcional)</small></h3>
            <p className="m-0 text-sm text-suave">
              {resumo.length > 0 ? resumo.join(' · ') : 'SKU, código de barras, NCM, pesos, garantia, ficha técnica... ajudam a emitir a NF e informam o comprador.'}
            </p>
          </div>
          <Botao onClick={() => setPainelAberto(true)} aria-haspopup="dialog">
            📋 Informe mais sobre o produto
            {totalPreenchidos > 0 && (
              <span className="rounded-full bg-primaria px-1.5 text-[0.7rem] text-white">{totalPreenchidos}</span>
            )}
          </Botao>
        </Painel>

        <SidePanel aberto={painelAberto} onFechar={fecharPainel} titulo="Informe mais sobre o produto"
          subtitulo={form.titulo || 'Novo anúncio'}
          rodape={
            <div className="flex flex-wrap items-center justify-between gap-2">
              <small className="text-suave">Salvo junto com o anúncio.</small>
              <Botao variante="primario" onClick={fecharPainel}>Concluir</Botao>
            </div>
          }>
          <InformacoesProdutoForm
            info={form.informacoes}
            onInfo={(k, v) => setForm((f) => ({ ...f, informacoes: { ...f.informacoes, [k]: v } }))}
            caracteristicas={form.caracteristicas}
            onCaracteristicas={(l) => set('caracteristicas', l)}
            variacoes={form.variacoes.filter((v) => v.variacao.trim())}
            onVariacoes={(l) => {
              // o painel só mostra variações com nome: devolve os códigos para as posições originais
              let k = 0;
              set('variacoes', form.variacoes.map((v) => (v.variacao.trim() ? l[k++] : v)));
            }}
            errosApi={acao.erro?.campos}
            mostrarErros={mostrarErrosPainel}
          />
        </SidePanel>

        <div className={acoes}>
          <Botao type="submit" variante="primario" disabled={acao.enviando}>
            {acao.enviando ? 'Salvando...' : editando ? 'Salvar alterações' : 'Publicar anúncio'}
          </Botao>
        </div>
      </form>
    </section>
  );
}
