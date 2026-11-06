import { useEffect, useRef, useState, type FormEvent, type KeyboardEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ApiError } from '../api/client';
import { api } from '../api/endpoints';
import type { MensagemNegociacao, Negociacao, ParticipanteNegociacao } from '../api/types';
import { useChat } from '../context/ChatContext';
import { useUsuario } from '../context/UsuarioContext';
import { formatarMoeda } from './format';
import { Botao, cx, ImagemAnuncio } from './ui';

/** Intervalo da atualização automática das mensagens (até existir WebSocket). */
const ATUALIZAR_A_CADA_MS = 4000;

const hora = (iso: string) => new Date(iso).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
const dia = (iso: string) => {
  const d = new Date(iso);
  const hoje = new Date();
  const ontem = new Date(); ontem.setDate(hoje.getDate() - 1);
  if (d.toDateString() === hoje.toDateString()) return 'Hoje';
  if (d.toDateString() === ontem.toDateString()) return 'Ontem';
  return d.toLocaleDateString('pt-BR', { day: '2-digit', month: 'long', year: d.getFullYear() === hoje.getFullYear() ? undefined : 'numeric' });
};
const iniciais = (nome: string) => nome.split(/\s+/).filter(Boolean).slice(0, 2).map((p) => p[0]?.toUpperCase()).join('');

/** Janela de chat da negociação, presa no canto inferior direito (tela cheia no celular). */
export function ChatJanela() {
  const { aberta, inicial, fecharChat } = useChat();
  const { usuarioId } = useUsuario();
  const [neg, setNeg] = useState<Negociacao | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [minimizada, setMinimizada] = useState(false);

  // carrega ao abrir outra negociação
  useEffect(() => {
    setErro(null);
    setMinimizada(false);
    if (!aberta) { setNeg(null); return; }
    if (inicial?.id === aberta) { setNeg(inicial); return; }
    setNeg(null);
    api.negociacao(aberta).then(setNeg).catch((e) => setErro(e instanceof ApiError ? e.message : String(e)));
  }, [aberta, inicial]);

  // atualização automática enquanto a janela estiver aberta e visível
  useEffect(() => {
    if (!aberta || minimizada) return;
    const t = setInterval(() => {
      if (document.hidden) return;
      api.negociacao(aberta)
        .then((n) => setNeg((atual) => (atual && atual.mensagens.length === n.mensagens.length ? atual : n)))
        .catch(() => { /* tenta de novo no próximo ciclo */ });
    }, ATUALIZAR_A_CADA_MS);
    return () => clearInterval(t);
  }, [aberta, minimizada]);

  if (!aberta) return null;

  const souVendedor = neg?.vendedor.id === usuarioId;
  const outro = neg ? (souVendedor ? neg.comprador : neg.vendedor) : null;

  return (
    <section role="dialog" aria-label="Chat da negociação"
      className={cx('fixed z-30 flex flex-col overflow-hidden border border-borda bg-white shadow-2xl',
        'sm:right-4 sm:bottom-4 sm:w-[400px] sm:rounded-card',
        minimizada ? 'right-2 bottom-2 left-2 rounded-card sm:left-auto' : 'inset-0 sm:inset-auto sm:h-[min(600px,calc(100vh-6rem))]')}>
      <Cabecalho neg={neg} outro={outro} souVendedor={souVendedor} minimizada={minimizada}
        onMinimizar={() => setMinimizada((m) => !m)} onFechar={fecharChat} />
      {!minimizada && (
        erro ? <p className="m-4 rounded-lg bg-red-50 p-3 text-sm text-vermelho">{erro}</p>
          : !neg ? <p className="m-4 text-sm text-suave">Carregando conversa...</p>
            : <Conversa neg={neg} usuarioId={usuarioId} onAtualizada={setNeg} onIrParaAnuncio={() => {
              if (window.innerWidth < 640) setMinimizada(true); // no celular o chat cobre a tela
            }} />
      )}
    </section>
  );
}

function Cabecalho({ neg, outro, souVendedor, minimizada, onMinimizar, onFechar }: {
  neg: Negociacao | null; outro: ParticipanteNegociacao | null; souVendedor: boolean; minimizada: boolean;
  onMinimizar: () => void; onFechar: () => void;
}) {
  const navigate = useNavigate();
  const botao = 'cursor-pointer rounded-md px-2 py-1 text-base leading-none text-slate-300 hover:bg-topo-claro hover:text-white';
  return (
    <header className="shrink-0">
      {/* produto em negociação */}
      <div className="flex items-center gap-3 bg-topo px-3 py-2.5 text-white">
        {neg && <ImagemAnuncio caminho={neg.anuncio.imagem} alt="" compacta className="size-11 shrink-0 rounded-md" />}
        <div className="min-w-0 flex-1">
          <p className="m-0 text-[0.7rem] tracking-wide text-slate-400 uppercase">Negociação</p>
          {neg ? (
            <Link to={`/anuncios/${neg.anuncio.id}`} title={neg.anuncio.titulo}
              className="block truncate text-sm font-semibold text-white">{neg.anuncio.titulo}</Link>
          ) : <span className="text-sm">...</span>}
          {neg?.anuncio.precoMenor != null && (
            <p className="m-0 text-xs text-slate-300">a partir de {formatarMoeda(neg.anuncio.precoMenor)} / un.</p>
          )}
        </div>
        <button type="button" className={botao} onClick={onMinimizar} aria-label={minimizada ? 'Expandir chat' : 'Minimizar chat'}
          aria-expanded={!minimizada}>{minimizada ? '▴' : '▾'}</button>
        <button type="button" className={botao} onClick={onFechar} aria-label="Fechar chat">✕</button>
      </div>

      {/* com quem estou falando */}
      {!minimizada && outro && neg && (
        <div className="flex items-center gap-2.5 border-b border-borda bg-gray-50 px-3 py-2">
          {outro.imagem
            ? <ImagemAnuncio caminho={outro.imagem} alt="" compacta className="size-8 shrink-0 rounded-full" />
            : <span className="flex size-8 shrink-0 items-center justify-center rounded-full bg-blue-100 text-xs font-bold text-primaria">{iniciais(outro.nome)}</span>}
          <div className="min-w-0 flex-1 text-xs leading-tight">
            <p className="m-0 truncate">
              <strong className="text-sm">{outro.nome}</strong>{' '}
              <span className="text-suave">· {souVendedor ? 'comprador' : 'vendedor'}</span>
            </p>
            <p className="m-0 truncate text-suave">
              {[outro.cidade && outro.uf ? `${outro.cidade}/${outro.uf}` : null,
                `na PSNF desde ${new Date(outro.membroDesde).getFullYear()}`].filter(Boolean).join(' · ')}
            </p>
          </div>
          {souVendedor && (
            <Botao tamanho="pequeno" variante="primario" title="Monta a ordem para este comprador com o produto da negociação"
              onClick={() => navigate(`/ordens/nova?anuncio=${neg.anuncio.id}&comprador=${neg.comprador.id}`)}>
              Gerar ordem
            </Botao>
          )}
        </div>
      )}
    </header>
  );
}

function Conversa({ neg, usuarioId, onAtualizada, onIrParaAnuncio }: {
  neg: Negociacao; usuarioId: string; onAtualizada: (n: Negociacao) => void; onIrParaAnuncio: () => void;
}) {
  const [texto, setTexto] = useState('');
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const fim = useRef<HTMLDivElement>(null);
  const campo = useRef<HTMLTextAreaElement>(null);

  // rola para a última mensagem quando chega uma nova
  useEffect(() => { fim.current?.scrollIntoView({ block: 'end' }); }, [neg.mensagens.length]);
  useEffect(() => { campo.current?.focus(); }, [neg.id]);

  const enviar = async (e?: FormEvent) => {
    e?.preventDefault();
    if (!texto.trim() || enviando) return;
    setEnviando(true);
    setErro(null);
    try {
      onAtualizada(await api.enviarMensagemNegociacao(neg.id, texto));
      setTexto('');
    } catch (err) {
      setErro(err instanceof ApiError ? err.message : String(err));
    } finally {
      setEnviando(false);
      campo.current?.focus();
    }
  };

  const teclas = (e: KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); void enviar(); }
  };

  const nomeDe = (id: string) => (id === neg.vendedor.id ? neg.vendedor.nome : neg.comprador.nome);

  return (
    <>
      <div className="flex-1 overflow-y-auto bg-slate-50 px-3 py-3" aria-live="polite">
        {neg.mensagens.map((m, i) => {
          const novoDia = i === 0 || dia(m.dataCadastro) !== dia(neg.mensagens[i - 1].dataCadastro);
          return (
            <div key={m.id}>
              {novoDia && <p className="my-2 text-center text-[0.7rem] font-semibold tracking-wide text-suave uppercase">{dia(m.dataCadastro)}</p>}
              <Bolha m={m} minha={m.autorId === usuarioId} autor={nomeDe(m.autorId)} neg={neg} onIrParaAnuncio={onIrParaAnuncio} />
            </div>
          );
        })}
        <div ref={fim} />
      </div>
      <form onSubmit={enviar} className="shrink-0 border-t border-borda bg-white p-2.5">
        {erro && <p className="mx-1 mt-0 mb-1.5 text-xs text-vermelho">{erro}</p>}
        <div className="flex items-end gap-2">
          <textarea ref={campo} value={texto} onChange={(e) => setTexto(e.target.value)} onKeyDown={teclas}
            rows={Math.min(4, Math.max(1, texto.split('\n').length))} maxLength={2000}
            placeholder="Escreva uma mensagem..." aria-label="Mensagem"
            className="max-h-28 min-h-10 flex-1 resize-none rounded-lg border border-gray-300 px-3 py-2 text-sm outline-none focus:border-primaria focus:ring-2 focus:ring-blue-200" />
          <Botao type="submit" variante="primario" disabled={!texto.trim() || enviando} className="h-10">
            {enviando ? '...' : 'Enviar'}
          </Botao>
        </div>
        <p className="m-0 mt-1 px-1 text-[0.68rem] text-suave">Enter envia · Shift+Enter quebra a linha</p>
      </form>
    </>
  );
}

function Bolha({ m, minha, autor, neg, onIrParaAnuncio }: {
  m: MensagemNegociacao; minha: boolean; autor: string; neg: Negociacao; onIrParaAnuncio: () => void;
}) {
  return (
    <div className={cx('mb-2 flex flex-col', minha ? 'items-end' : 'items-start')}>
      <div className={cx('max-w-[85%] px-3 py-2 text-sm break-words whitespace-pre-wrap shadow-sm',
        minha ? 'rounded-[12px_12px_2px_12px] bg-primaria text-white' : 'rounded-[12px_12px_12px_2px] border border-borda bg-white')}>
        {!minha && <p className="m-0 mb-0.5 text-[0.7rem] font-semibold text-primaria">{autor}</p>}
        {m.texto}
        {m.tipo === 'ABORDAGEM' && (
          // cartão do produto de interesse, para o vendedor saber de qual anúncio se trata
          <Link to={`/anuncios/${neg.anuncio.id}`} onClick={onIrParaAnuncio}
            className={cx('mt-2 flex items-center gap-2.5 rounded-lg p-2 whitespace-normal hover:no-underline',
              minha ? 'bg-white/15 text-white hover:bg-white/25' : 'bg-slate-100 text-texto hover:bg-slate-200')}>
            <ImagemAnuncio caminho={neg.anuncio.imagem} alt="" compacta className="size-10 shrink-0 rounded-md" />
            <span className="min-w-0 flex-1">
              <span className="block truncate font-semibold">{neg.anuncio.titulo}</span>
              <span className={cx('text-xs', minha ? 'text-blue-100' : 'text-primaria')}>Ver anúncio →</span>
            </span>
          </Link>
        )}
      </div>
      <span className="mt-0.5 px-1 text-[0.65rem] text-suave">{hora(m.dataCadastro)}</span>
    </div>
  );
}
