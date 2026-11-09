import { useEffect, useState, type FormEvent } from 'react';
import { ApiError, urlArquivo } from '../api/client';
import { api } from '../api/endpoints';
import type { Avaliacao, AvaliacoesAnuncio as Dados } from '../api/types';
import { useUsuario } from '../context/UsuarioContext';
import { formatarData } from './format';
import { EscolherEstrelas, Estrelas, formatarNota } from './Estrelas';
import { ACEITA_IMAGENS, UploadArquivo } from './UploadArquivo';
import { Botao, cx, dica, ImagemAnuncio, Painel } from './ui';

const TAMANHO_TEXTO = 250;
const MAXIMO_IMAGENS = 3;

/** Avaliações do produto: resumo, formulário (só para quem comprou e recebeu) e a lista. */
export function AvaliacoesAnuncio({ anuncioId, onAtualizado }: { anuncioId: string; onAtualizado?: (media: number | null, total: number) => void }) {
  const [dados, setDados] = useState<Dados | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    api.avaliacoes(anuncioId).then(setDados).catch((e) => setErro(e instanceof ApiError ? e.message : String(e)));
  }, [anuncioId]);

  // vindo do botão "Avaliar" da ordem (#avaliacoes): rola até aqui quando carregar
  useEffect(() => {
    if (dados && window.location.hash === '#avaliacoes') {
      setTimeout(() => document.getElementById('avaliacoes')?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 100);
    }
  }, [!!dados]); // eslint-disable-line react-hooks/exhaustive-deps

  const atualizar = (d: Dados) => { setDados(d); onAtualizado?.(d.media, d.total); };

  return (
    <Painel id="avaliacoes" className="mt-8 scroll-mt-20">
      <h3 className="mt-0">Avaliações dos compradores</h3>
      {erro && <p className="text-sm text-vermelho">{erro}</p>}
      {!dados && !erro && <p className={dica}>Carregando avaliações...</p>}
      {dados && (
        <>
          <Resumo d={dados} />
          {dados.podeAvaliar
            ? <Formulario anuncioId={anuncioId} minha={dados.minha} onSalvo={atualizar} />
            : dados.motivoNaoPode && <p className={cx(dica, 'rounded-lg bg-gray-50 px-3 py-2')}>⭐ {dados.motivoNaoPode}.</p>}
          <Lista d={dados} />
        </>
      )}
    </Painel>
  );
}

function Resumo({ d }: { d: Dados }) {
  if (d.total === 0) return <p className={dica}>Este produto ainda não tem avaliações.</p>;
  return (
    <div className="mb-4 flex flex-wrap items-center gap-6">
      <div className="text-center">
        <strong className="block text-4xl leading-none">{formatarNota(d.media)}</strong>
        <Estrelas nota={d.media ?? 0} tamanho="text-lg" />
        <span className="block text-xs text-suave">{d.total} avaliação(ões)</span>
      </div>
      <ul className="m-0 min-w-[200px] flex-1 list-none space-y-1 p-0" aria-label="Distribuição das notas">
        {[5, 4, 3, 2, 1].map((n) => {
          const qtd = d.porNota[n - 1] ?? 0;
          return (
            <li key={n} className="flex items-center gap-2 text-xs">
              <span className="w-6 text-right text-suave">{n} ★</span>
              <span className="h-2 flex-1 overflow-hidden rounded-full bg-gray-100">
                <span className="block h-full rounded-full bg-amber-400" style={{ width: `${(qtd / d.total) * 100}%` }} />
              </span>
              <span className="w-6 text-suave">{qtd}</span>
            </li>
          );
        })}
      </ul>
    </div>
  );
}

function Formulario({ anuncioId, minha, onSalvo }: { anuncioId: string; minha: Avaliacao | null; onSalvo: (d: Dados) => void }) {
  const [editando, setEditando] = useState(!minha);
  const [nota, setNota] = useState(minha?.nota ?? 0);
  const [texto, setTexto] = useState(minha?.texto ?? '');
  const [imagens, setImagens] = useState<string[]>(minha?.imagens ?? []);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    setNota(minha?.nota ?? 0); setTexto(minha?.texto ?? ''); setImagens(minha?.imagens ?? []); setEditando(!minha);
  }, [minha]);

  const executar = async (fn: () => Promise<Dados>) => {
    setEnviando(true); setErro(null);
    try { onSalvo(await fn()); return true; } catch (e) { setErro(e instanceof ApiError ? e.message : String(e)); return false; } finally { setEnviando(false); }
  };

  const publicar = async (e: FormEvent) => {
    e.preventDefault();
    if (!nota) { setErro('Escolha de 1 a 5 estrelas'); return; }
    if (await executar(() => api.avaliar(anuncioId, { nota, texto: texto.trim() || null, imagens }))) setEditando(false);
  };

  if (minha && !editando) {
    return (
      <div className="mb-4 flex flex-wrap items-center justify-between gap-2 rounded-lg border border-green-200 bg-green-50 px-3 py-2 text-sm">
        <span>✔ Você avaliou este produto com <strong>{minha.nota} estrela{minha.nota > 1 ? 's' : ''}</strong>.</span>
        <span className="flex gap-2">
          <Botao tamanho="pequeno" onClick={() => setEditando(true)}>Editar avaliação</Botao>
          <Botao tamanho="pequeno" variante="perigo" disabled={enviando}
            onClick={() => window.confirm('Excluir a sua avaliação?') && void executar(() => api.removerAvaliacao(anuncioId))}>Excluir</Botao>
        </span>
      </div>
    );
  }

  return (
    <form onSubmit={publicar} className="mb-5 rounded-lg border border-borda bg-gray-50 p-4" aria-label="Avaliar o produto">
      <h4 className="mt-0 mb-2">{minha ? 'Editar a sua avaliação' : 'Avalie este produto'}</h4>
      <p className={cx(dica, 'mt-0')}>Você comprou e recebeu este produto. Conte para outros compradores o que achou.</p>
      <EscolherEstrelas valor={nota} onChange={(n) => { setNota(n); setErro(null); }} />
      <label className="mt-3 block">
        <span className="mb-1 flex justify-between text-xs font-semibold text-gray-700">
          Comentário <span className={cx('font-normal', texto.length > TAMANHO_TEXTO - 20 ? 'text-laranja' : 'text-suave')}>{texto.length}/{TAMANHO_TEXTO}</span>
        </span>
        <textarea value={texto} onChange={(e) => setTexto(e.target.value.slice(0, TAMANHO_TEXTO))} rows={3} maxLength={TAMANHO_TEXTO}
          placeholder="Qualidade, embalagem, se veio como anunciado... (opcional)"
          className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm outline-none focus:border-primaria focus:ring-2 focus:ring-blue-200" />
      </label>
      <div className="mt-2">
        <span className="mb-1 block text-xs font-semibold text-gray-700">Fotos ({imagens.length}/{MAXIMO_IMAGENS})</span>
        <div className="flex flex-wrap items-center gap-2">
          {imagens.map((img, i) => (
            <div key={img + i} className="relative">
              <ImagemAnuncio caminho={img} alt={`Foto ${i + 1}`} compacta className="size-16 rounded-lg border border-borda" />
              <button type="button" onClick={() => setImagens(imagens.filter((_, j) => j !== i))} aria-label={`Remover foto ${i + 1}`}
                className="absolute -top-1.5 -right-1.5 flex size-5 cursor-pointer items-center justify-center rounded-full bg-vermelho text-xs text-white">✕</button>
            </div>
          ))}
          {imagens.length < MAXIMO_IMAGENS && (
            <UploadArquivo accept={ACEITA_IMAGENS} compacto rotulo="Adicionar foto"
              onEnviado={(a) => setImagens((atual) => (atual.length < MAXIMO_IMAGENS ? [...atual, a.caminho] : atual))} />
          )}
        </div>
      </div>
      {erro && <p className="mt-2 mb-0 text-sm text-vermelho" role="alert">{erro}</p>}
      <div className="mt-3 flex gap-2">
        <Botao type="submit" variante="primario" disabled={enviando}>{enviando ? 'Enviando...' : minha ? 'Salvar avaliação' : 'Publicar avaliação'}</Botao>
        {minha && <Botao onClick={() => setEditando(false)}>Cancelar</Botao>}
      </div>
    </form>
  );
}

function Lista({ d }: { d: Dados }) {
  const { usuarioId } = useUsuario();
  if (!d.avaliacoes.length) return null;
  return (
    <ul className="m-0 list-none divide-y divide-borda p-0">
      {d.avaliacoes.map((a) => (
        <li key={a.id} className="py-3">
          <div className="flex flex-wrap items-center gap-2">
            <Estrelas nota={a.nota} />
            <strong className="text-sm">{a.autorNome}</strong>
            {a.autorId === usuarioId && <span className="rounded-full bg-blue-100 px-2 text-[0.68rem] font-bold text-blue-800">Você</span>}
            <span className="rounded-full bg-green-100 px-2 text-[0.68rem] font-semibold text-green-800" title="Comprou e recebeu este produto">✔ Compra verificada</span>
            <small className="ml-auto text-xs text-suave">{formatarData(a.dataCadastro)}{a.dataAlteracao && ' · editada'}</small>
          </div>
          {a.texto && <p className="mt-1.5 mb-0 text-sm break-words whitespace-pre-wrap">{a.texto}</p>}
          {a.imagens.length > 0 && (
            <div className="mt-2 flex gap-2">
              {a.imagens.map((img, i) => (
                <a key={img + i} href={urlArquivo(img)} target="_blank" rel="noreferrer" aria-label={`Ver foto ${i + 1} da avaliação`}>
                  <ImagemAnuncio caminho={img} alt="" compacta className="size-20 rounded-lg border border-borda" />
                </a>
              ))}
            </div>
          )}
        </li>
      ))}
    </ul>
  );
}
