import { useEffect, useMemo, useRef, useState, type FormEvent, type KeyboardEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ApiError } from '../api/client';
import { api } from '../api/endpoints';
import type { Anuncio, AnuncioNegociacao, MensagemNegociacao, Negociacao, ParticipanteNegociacao } from '../api/types';
import { useChat } from '../context/ChatContext';
import { useUsuario } from '../context/UsuarioContext';
import { formatarMoeda } from './format';
import { Botao, cx, ImagemAnuncio, PagamentoPendenteBadge, StatusBadge } from './ui';

/**
 * Intervalo do chat aberto, conforme a conversa: rápido quando os dois estão conversando agora,
 * mais lento quando ela esfria. Cada consulta busca só as mensagens novas (chat por delta).
 */
export const intervaloChat = (ultimaMensagemEm: string | undefined, agora = Date.now()) => {
  const idade = ultimaMensagemEm ? agora - new Date(ultimaMensagemEm).getTime() : Infinity;
  if (idade < 2 * 60_000) return 3_000; //   conversa acontecendo agora
  if (idade < 10 * 60_000) return 10_000; // conversa recente
  return 30_000; //                          conversa parada
};

/** Mensagens que mudam o cabeçalho (produtos, ordens): pedem recarregar a conversa inteira. */
const MUDA_CABECALHO = new Set(['ANUNCIO_ADICIONADO', 'ANUNCIO_REMOVIDO', 'ORDEM_GERADA']);

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
  const { aberta, inicial, aberturas, fecharChat, marcarVista, recarregarLista, hubExpandido } = useChat();
  const { usuarioId } = useUsuario();
  const [neg, setNeg] = useState<Negociacao | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [minimizada, setMinimizada] = useState(false);
  const [escolhendo, setEscolhendo] = useState(false);

  // carrega ao abrir uma negociação (ou reabrir a mesma: expande e traz o que mudou)
  useEffect(() => {
    setErro(null);
    setMinimizada(false);
    setEscolhendo(false);
    if (!aberta) { setNeg(null); return; }
    if (inicial?.id === aberta) { setNeg(inicial); return; }
    setNeg((atual) => (atual?.id === aberta ? atual : null)); // reabrindo a mesma: mantém na tela enquanto atualiza
    api.negociacao(aberta).then(setNeg).catch((e) => setErro(e instanceof ApiError ? e.message : String(e)));
  }, [aberta, inicial, aberturas]);

  // mensagens novas enquanto a janela estiver aberta: pergunta só pelo que veio DEPOIS da última mensagem
  // que já está na tela (antes: a conversa inteira, com anúncios e ordens, a cada 4 s)
  const negRef = useRef(neg);
  negRef.current = neg;
  useEffect(() => {
    if (!aberta || minimizada) return;
    let timer: ReturnType<typeof setTimeout>;
    let ativo = true;
    const agendar = () => { if (ativo) timer = setTimeout(ciclo, intervaloChat(negRef.current?.mensagens.at(-1)?.dataCadastro)); };
    const ciclo = async () => {
      const atual = negRef.current;
      const ultima = atual?.mensagens.at(-1);
      if (!document.hidden && atual && ultima) {
        try {
          const novas = await api.mensagensNegociacao(atual.id, ultima.id);
          if (novas.length && ativo) {
            if (novas.some((m) => MUDA_CABECALHO.has(m.tipo))) {
              setNeg(await api.negociacao(atual.id)); // produto incluído ou ordem gerada: cabeçalho muda
            } else {
              setNeg((n) => (n && n.id === atual.id ? { ...n, mensagens: [...n.mensagens, ...novas.filter((m) => !n.mensagens.some((x) => x.id === m.id))] } : n));
            }
            recarregarLista();
          }
        } catch { /* tenta de novo no próximo ciclo */ }
      }
      agendar();
    };
    agendar();
    // voltou para a aba: confere na hora
    const aoVoltar = () => { if (!document.hidden) { clearTimeout(timer); void ciclo(); } };
    document.addEventListener('visibilitychange', aoVoltar);
    return () => { ativo = false; clearTimeout(timer); document.removeEventListener('visibilitychange', aoVoltar); };
    // neg?.id: recomeça quando a conversa termina de carregar (antes disso não há "última mensagem" para medir o ritmo)
  }, [aberta, minimizada, neg?.id]); // eslint-disable-line react-hooks/exhaustive-deps

  // o que está na tela já foi visto (inclui as mensagens que o próprio usuário enviou)
  useEffect(() => {
    if (neg && !minimizada) marcarVista(neg.id, neg.mensagens.length);
  }, [neg, minimizada, marcarVista]);

  if (!aberta) return null;

  // no celular o chat cobre a tela: minimiza para mostrar o anúncio
  const irParaAnuncio = () => { if (window.innerWidth < 640) setMinimizada(true); };

  const souVendedor = neg?.vendedor.id === usuarioId;
  const outro = neg ? (souVendedor ? neg.comprador : neg.vendedor) : null;
  const atualizada = (n: Negociacao) => { setNeg(n); recarregarLista(); };

  return (
    <section role="dialog" aria-label="Chat da negociação"
      className={cx('fixed z-30 flex flex-col overflow-hidden border border-borda bg-white shadow-2xl',
        'sm:right-4 sm:bottom-4 sm:w-[400px] sm:rounded-card',
        // no computador fica ao lado da barra de negociações
        hubExpandido ? 'lg:right-[21rem]' : 'lg:right-20',
        minimizada ? 'right-2 bottom-2 left-2 rounded-card sm:left-auto' : 'inset-0 sm:inset-auto sm:h-[min(600px,calc(100vh-6rem))]')}>
      <Cabecalho neg={neg} outro={outro} souVendedor={souVendedor} minimizada={minimizada}
        onMinimizar={() => setMinimizada((m) => !m)} onFechar={fecharChat} onGerarOrdem={() => setMinimizada(true)} />
      {!minimizada && (
        erro ? <p className="m-4 rounded-lg bg-red-50 p-3 text-sm text-vermelho">{erro}</p>
          : !neg ? <p className="m-4 text-sm text-suave">Carregando conversa...</p>
            : (
              <>
                <ProdutosNegociacao neg={neg} escolhendo={escolhendo} onEscolher={() => setEscolhendo((e) => !e)} onAtualizada={atualizada}
                  onIrParaAnuncio={irParaAnuncio} />
                <OrdensNegociacao neg={neg} onIrParaOrdem={irParaAnuncio} />
                {escolhendo
                  ? <SeletorAnuncios neg={neg} onAtualizada={(n) => { atualizada(n); setEscolhendo(false); }} onCancelar={() => setEscolhendo(false)} />
                  : <Conversa neg={neg} usuarioId={usuarioId} onAtualizada={atualizada} onIrParaAnuncio={irParaAnuncio} />}
              </>
            )
      )}
    </section>
  );
}

function Cabecalho({ neg, outro, souVendedor, minimizada, onMinimizar, onFechar, onGerarOrdem }: {
  neg: Negociacao | null; outro: ParticipanteNegociacao | null; souVendedor: boolean; minimizada: boolean;
  onMinimizar: () => void; onFechar: () => void; onGerarOrdem: () => void;
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
            <Botao tamanho="pequeno" variante="primario" title="Monta a ordem para este comprador com todos os produtos da negociação"
              // minimiza o chat para o formulário da ordem ficar visível
              onClick={() => { onGerarOrdem(); navigate(`/ordens/nova?negociacao=${neg.id}`); }}>
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
              {m.tipo === 'ORDEM_GERADA'
                ? <AvisoOrdem m={m} minha={m.autorId === usuarioId} autor={nomeDe(m.autorId)} neg={neg} onIrParaOrdem={onIrParaAnuncio} />
                : m.tipo === 'ANUNCIO_ADICIONADO' || m.tipo === 'ANUNCIO_REMOVIDO'
                ? <AvisoProduto m={m} minha={m.autorId === usuarioId} autor={nomeDe(m.autorId)} neg={neg} onIrParaAnuncio={onIrParaAnuncio} />
                : <Bolha m={m} minha={m.autorId === usuarioId} autor={nomeDe(m.autorId)} neg={neg} onIrParaAnuncio={onIrParaAnuncio} />}
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

/** Cartão de produto clicável (leva ao anúncio). */
function CartaoProduto({ anuncio, claro, onClick }: { anuncio: AnuncioNegociacao; claro?: boolean; onClick: () => void }) {
  return (
    <Link to={`/anuncios/${anuncio.id}`} onClick={onClick}
      className={cx('mt-2 flex items-center gap-2.5 rounded-lg p-2 text-left whitespace-normal hover:no-underline',
        claro ? 'bg-white/15 text-white hover:bg-white/25' : 'bg-slate-100 text-texto hover:bg-slate-200')}>
      <ImagemAnuncio caminho={anuncio.imagem} alt="" compacta className="size-10 shrink-0 rounded-md" />
      <span className="min-w-0 flex-1">
        <span className="block truncate font-semibold">{anuncio.titulo}</span>
        <span className={cx('text-xs', claro ? 'text-blue-100' : 'text-primaria')}>
          {anuncio.precoMenor != null && `a partir de ${formatarMoeda(anuncio.precoMenor)} · `}Ver anúncio →
        </span>
      </span>
    </Link>
  );
}

/** Aviso automático no meio da conversa: produto incluído ou retirado da negociação. */
function AvisoProduto({ m, minha, autor, neg, onIrParaAnuncio }: {
  m: MensagemNegociacao; minha: boolean; autor: string; neg: Negociacao; onIrParaAnuncio: () => void;
}) {
  const anuncio = neg.anuncios.find((a) => a.id === m.anuncioId);
  const quem = minha ? 'Você' : autor;
  if (m.tipo === 'ANUNCIO_REMOVIDO') {
    return (
      <p className="my-2 text-center text-xs text-suave">
        ➖ {quem} retirou {m.texto.replace(/^Retirou o produto /, '').replace(/ da negociação\.$/, '')} da negociação · {hora(m.dataCadastro)}
      </p>
    );
  }
  return (
    <div className="my-2 flex flex-col items-center">
      <div className="w-full max-w-[90%] rounded-lg border border-dashed border-green-300 bg-green-50 px-3 py-2 text-center text-xs text-green-900">
        🛒 <strong>{quem}</strong> adicionou um produto à negociação · {hora(m.dataCadastro)}
        {anuncio
          ? <CartaoProduto anuncio={anuncio} onClick={onIrParaAnuncio} />
          : <p className="m-0 mt-1 text-suave">{m.texto.replace(/^Adicionou o produto /, '').replace(/ à negociação\.$/, '')} (retirado depois)</p>}
      </div>
    </div>
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
          <CartaoProduto anuncio={neg.anuncio} claro={minha} onClick={onIrParaAnuncio} />
        )}
      </div>
      <span className="mt-0.5 px-1 text-[0.65rem] text-suave">{hora(m.dataCadastro)}</span>
    </div>
  );
}

/** Faixa com os produtos da negociação: o que iniciou a conversa e os incluídos depois. */
function ProdutosNegociacao({ neg, escolhendo, onEscolher, onAtualizada, onIrParaAnuncio }: {
  neg: Negociacao; escolhendo: boolean; onEscolher: () => void; onAtualizada: (n: Negociacao) => void; onIrParaAnuncio: () => void;
}) {
  const [removendo, setRemovendo] = useState<string | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  const remover = async (a: AnuncioNegociacao) => {
    if (!window.confirm(`Retirar "${a.titulo}" da negociação?`)) return;
    setRemovendo(a.id);
    setErro(null);
    try { onAtualizada(await api.removerAnuncioNegociacao(neg.id, a.id)); }
    catch (e) { setErro(e instanceof ApiError ? e.message : String(e)); }
    finally { setRemovendo(null); }
  };

  return (
    <div className="shrink-0 border-b border-borda bg-white px-3 py-2">
      <div className="mb-1.5 flex items-center justify-between gap-2">
        <span className="text-[0.7rem] font-semibold tracking-wide text-suave uppercase">
          Produtos na negociação ({neg.anuncios.length})
        </span>
        <button type="button" onClick={onEscolher} aria-expanded={escolhendo}
          className="cursor-pointer rounded-md px-1.5 py-0.5 text-xs font-semibold text-primaria hover:bg-blue-50">
          {escolhendo ? 'Voltar à conversa' : '+ Adicionar produto'}
        </button>
      </div>
      <ul className="-mx-1 flex gap-1.5 overflow-x-auto px-1 pb-1" aria-label="Produtos na negociação">
        {neg.anuncios.map((a, i) => (
          <li key={a.id} className={cx('flex max-w-[170px] shrink-0 items-center gap-1.5 rounded-full border py-0.5 pr-1 pl-0.5',
            i === 0 ? 'border-blue-200 bg-blue-50' : 'border-borda bg-gray-50', !a.ativo && 'opacity-60')}>
            <ImagemAnuncio caminho={a.imagem} alt="" compacta className="size-6 shrink-0 rounded-full" />
            <Link to={`/anuncios/${a.id}`} onClick={onIrParaAnuncio} title={i === 0 ? `${a.titulo} (iniciou a negociação)` : a.titulo}
              className="min-w-0 truncate text-xs text-texto">{a.titulo}</Link>
            {i > 0 && (
              <button type="button" onClick={() => remover(a)} disabled={removendo === a.id} aria-label={`Retirar ${a.titulo}`}
                className="cursor-pointer rounded-full px-1 text-xs leading-none text-suave hover:bg-red-50 hover:text-vermelho">✕</button>
            )}
          </li>
        ))}
      </ul>
      {erro && <p className="m-0 text-xs text-vermelho">{erro}</p>}
    </div>
  );
}

/** Lista os outros anúncios ativos do vendedor para incluir na negociação. */
function SeletorAnuncios({ neg, onAtualizada, onCancelar }: {
  neg: Negociacao; onAtualizada: (n: Negociacao) => void; onCancelar: () => void;
}) {
  const [anuncios, setAnuncios] = useState<Anuncio[] | null>(null);
  const [busca, setBusca] = useState('');
  const [adicionando, setAdicionando] = useState<string | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    api.anunciosDoUsuario(neg.vendedor.id).then(setAnuncios).catch((e) => setErro(e instanceof ApiError ? e.message : String(e)));
  }, [neg.vendedor.id]);

  const disponiveis = useMemo(() => {
    const t = busca.trim().toLowerCase();
    return (anuncios ?? []).filter((a) => a.ativo && !neg.anuncios.some((x) => x.id === a.id)
      && (!t || a.titulo.toLowerCase().includes(t)));
  }, [anuncios, neg.anuncios, busca]);

  const adicionar = async (a: Anuncio) => {
    setAdicionando(a.id);
    setErro(null);
    try { onAtualizada(await api.adicionarAnuncioNegociacao(neg.id, a.id)); }
    catch (e) { setErro(e instanceof ApiError ? e.message : String(e)); setAdicionando(null); }
  };

  return (
    <div className="flex min-h-0 flex-1 flex-col bg-slate-50">
      <div className="border-b border-borda bg-white p-3">
        <p className="m-0 mb-2 text-sm">
          Outros produtos de <strong>{neg.vendedor.nome}</strong>. Os produtos incluídos entram juntos na ordem de pedido.
        </p>
        <input type="search" value={busca} onChange={(e) => setBusca(e.target.value)} autoFocus placeholder="Buscar produto do vendedor"
          aria-label="Buscar produto do vendedor"
          className="w-full rounded-lg border border-gray-300 px-3 py-1.5 text-sm outline-none focus:border-primaria focus:ring-2 focus:ring-blue-200" />
        {erro && <p className="m-0 mt-1.5 text-xs text-vermelho">{erro}</p>}
      </div>
      <ul className="min-h-0 flex-1 divide-y divide-borda overflow-y-auto bg-white">
        {!anuncios && !erro && <li className="p-3 text-sm text-suave">Carregando produtos...</li>}
        {anuncios && disponiveis.length === 0 && (
          <li className="p-4 text-center text-sm text-suave">
            {busca ? 'Nenhum produto encontrado.' : 'Todos os produtos ativos deste vendedor já estão na negociação.'}
          </li>
        )}
        {disponiveis.map((a) => (
          <li key={a.id} className="flex items-center gap-2.5 px-3 py-2">
            <ImagemAnuncio caminho={(a.imagens.find((i) => i.principal) ?? a.imagens[0])?.caminho} alt="" compacta className="size-11 shrink-0 rounded-md" />
            <span className="min-w-0 flex-1">
              <span className="block truncate text-sm font-semibold">{a.titulo}</span>
              {a.precoMenor != null && <span className="text-xs text-suave">a partir de {formatarMoeda(a.precoMenor)}</span>}
            </span>
            <Botao tamanho="pequeno" disabled={adicionando != null} onClick={() => adicionar(a)}>
              {adicionando === a.id ? '...' : 'Adicionar'}
            </Botao>
          </li>
        ))}
      </ul>
      <div className="border-t border-borda bg-white p-2.5 text-right">
        <Botao tamanho="pequeno" onClick={onCancelar}>Voltar à conversa</Botao>
      </div>
    </div>
  );
}

/**
 * Ordens de pedido entre comprador e vendedor: as geradas nesta conversa e as demais entre os dois,
 * para ninguém esquecer o que já foi fechado. Fica recolhida para não tomar espaço do chat.
 */
function OrdensNegociacao({ neg, onIrParaOrdem }: { neg: Negociacao; onIrParaOrdem: () => void }) {
  const [aberta, setAberta] = useState(false);
  if (!neg.ordens.length) return null;
  const daConversa = neg.ordens.filter((o) => o.daNegociacao).length;
  return (
    <div className="shrink-0 border-b border-borda bg-white px-3 py-1.5">
      <button type="button" onClick={() => setAberta((a) => !a)} aria-expanded={aberta}
        className="flex w-full cursor-pointer items-center gap-2 py-0.5 text-left">
        <span className="text-[0.7rem] font-semibold tracking-wide text-suave uppercase">📄 Ordens de pedido ({neg.ordens.length})</span>
        {daConversa > 0 && <span className="text-[0.68rem] text-suave">· {daConversa} desta conversa</span>}
        <span className="ml-auto text-xs font-semibold text-primaria">{aberta ? 'Ocultar' : 'Ver'}</span>
      </button>
      {aberta && (
        <ul className="mt-1 max-h-40 space-y-1 overflow-y-auto pb-1" aria-label="Ordens de pedido">
          {neg.ordens.map((o) => (
            <li key={o.id}>
              <Link to={`/ordens/${o.id}`} onClick={onIrParaOrdem}
                className="flex flex-wrap items-center gap-x-2 gap-y-1 rounded-lg border border-borda px-2 py-1.5 text-xs text-texto hover:bg-gray-50 hover:no-underline">
                <strong className="font-mono">{o.codigo}</strong>
                <StatusBadge status={o.status} />
                <PagamentoPendenteBadge status={o.status} pendente={o.pagamentoPendente} />
                <span className="ml-auto text-suave">{formatarMoeda(o.valorTotal)} · {new Date(o.dataCadastro).toLocaleDateString('pt-BR')}</span>
                {!o.daNegociacao && <span className="w-full text-[0.65rem] text-suave">criada fora desta conversa</span>}
              </Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

/** Aviso no meio da conversa: o vendedor gerou uma ordem a partir dela. */
function AvisoOrdem({ m, minha, autor, neg, onIrParaOrdem }: {
  m: MensagemNegociacao; minha: boolean; autor: string; neg: Negociacao; onIrParaOrdem: () => void;
}) {
  const ordem = neg.ordens.find((o) => o.id === m.ordemId);
  return (
    <div className="my-2 flex flex-col items-center">
      <div className="w-full max-w-[90%] rounded-lg border border-blue-200 bg-blue-50 px-3 py-2 text-center text-xs text-blue-900">
        📄 <strong>{minha ? 'Você' : autor}</strong> gerou uma ordem de pedido · {hora(m.dataCadastro)}
        {ordem ? (
          <Link to={`/ordens/${ordem.id}`} onClick={onIrParaOrdem}
            className="mt-2 flex flex-wrap items-center justify-center gap-2 rounded-lg bg-white px-2 py-1.5 text-texto hover:bg-blue-100 hover:no-underline">
            <strong className="font-mono">{ordem.codigo}</strong>
            <StatusBadge status={ordem.status} />
            <span>{formatarMoeda(ordem.valorTotal)}</span>
            <span className="font-semibold text-primaria">Ver ordem →</span>
          </Link>
        ) : <p className="m-0 mt-1">{m.texto}</p>}
      </div>
    </div>
  );
}
