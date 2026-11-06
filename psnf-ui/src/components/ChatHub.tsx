import { useMemo, useState } from 'react';
import type { NegociacaoResumo } from '../api/types';
import { useChat } from '../context/ChatContext';
import { useUsuario } from '../context/UsuarioContext';
import { SidePanel } from './SidePanel';
import { cx, ImagemAnuncio } from './ui';

type Filtro = 'TODAS' | 'COMPRADOR' | 'VENDEDOR';

/** "14:32" se for hoje, "Ontem", ou "05/11". */
export const quando = (iso: string) => {
  const d = new Date(iso);
  const hoje = new Date();
  const ontem = new Date(); ontem.setDate(hoje.getDate() - 1);
  if (d.toDateString() === hoje.toDateString()) return d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  if (d.toDateString() === ontem.toDateString()) return 'Ontem';
  return d.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' });
};

const ContadorNaoLidas = ({ n, className }: { n: number; className?: string }) =>
  n > 0 ? (
    <span className={cx('inline-flex min-w-5 items-center justify-center rounded-full bg-vermelho px-1.5 text-[0.68rem] font-bold text-white', className)}
      aria-label={`${n} mensagem(ns) não lida(s)`}>{n > 99 ? '99+' : n}</span>
  ) : null;

/** Busca + filtro (todas / compras / vendas) + lista de conversas. Usada na barra, no celular e na página. */
export function ListaNegociacoes({ aoAbrir, grande }: { aoAbrir?: () => void; grande?: boolean }) {
  const { lista, erroLista, aberta, abrirChat, naoLidas } = useChat();
  const { usuarioId } = useUsuario();
  const [filtro, setFiltro] = useState<Filtro>('TODAS');
  const [busca, setBusca] = useState('');

  const visiveis = useMemo(() => {
    const t = busca.trim().toLowerCase();
    return (lista ?? []).filter((n) => (filtro === 'TODAS' || n.papel === filtro)
      && (!t || n.tituloAnuncio.toLowerCase().includes(t) || n.outroParticipanteNome.toLowerCase().includes(t)));
  }, [lista, filtro, busca]);

  const aba = (f: Filtro, rotulo: string) => (
    <button type="button" onClick={() => setFiltro(f)} aria-pressed={filtro === f}
      className={cx('flex-1 cursor-pointer rounded-md px-2 py-1 text-xs font-semibold',
        filtro === f ? 'bg-white text-texto shadow-sm' : 'text-suave hover:text-texto')}>{rotulo}</button>
  );

  return (
    <div className="flex min-h-0 flex-1 flex-col">
      <div className="space-y-2 border-b border-borda p-3">
        <input type="search" value={busca} onChange={(e) => setBusca(e.target.value)} placeholder="Buscar produto ou pessoa"
          aria-label="Buscar negociação"
          className="w-full rounded-lg border border-gray-300 px-3 py-1.5 text-sm outline-none focus:border-primaria focus:ring-2 focus:ring-blue-200" />
        <div className="flex gap-1 rounded-lg bg-gray-100 p-0.5" role="group" aria-label="Filtrar negociações">
          {aba('TODAS', 'Todas')}{aba('COMPRADOR', 'Compras')}{aba('VENDEDOR', 'Vendas')}
        </div>
      </div>

      <div className="min-h-0 flex-1 overflow-y-auto">
        {erroLista && !lista && <p className="m-3 text-sm text-vermelho">{erroLista.message}</p>}
        {!lista && !erroLista && <p className="m-3 text-sm text-suave">Carregando...</p>}
        {lista && visiveis.length === 0 && (
          <p className="m-4 text-center text-sm text-suave">
            {lista.length === 0
              ? <>Nenhuma negociação ainda. Abra um anúncio e clique em <strong>Iniciar negociação</strong>.</>
              : 'Nenhuma conversa encontrada.'}
          </p>
        )}
        <ul className="divide-y divide-borda">
          {visiveis.map((n) => (
            <ItemConversa key={n.id} n={n} ativa={aberta === n.id} naoLidas={naoLidas(n)} minha={n.ultimaMensagemAutorId === usuarioId}
              grande={grande} onClick={() => { abrirChat(n.id); aoAbrir?.(); }} />
          ))}
        </ul>
      </div>
    </div>
  );
}

function ItemConversa({ n, ativa, naoLidas, minha, grande, onClick }: {
  n: NegociacaoResumo; ativa: boolean; naoLidas: number; minha: boolean; grande?: boolean; onClick: () => void;
}) {
  return (
    <li>
      <button type="button" onClick={onClick} aria-current={ativa ? 'true' : undefined}
        className={cx('flex w-full cursor-pointer items-center gap-2.5 px-3 text-left hover:bg-gray-50',
          grande ? 'py-3' : 'py-2.5', ativa && 'bg-blue-50 hover:bg-blue-50')}>
        <ImagemAnuncio caminho={n.imagemAnuncio} alt="" compacta className={cx('shrink-0 rounded-lg', grande ? 'size-14' : 'size-11')} />
        <span className="min-w-0 flex-1">
          <span className="flex items-baseline gap-2">
            <span className={cx('min-w-0 flex-1 truncate text-sm', naoLidas > 0 ? 'font-bold' : 'font-semibold')}>{n.tituloAnuncio}</span>
            <small className={cx('shrink-0 text-[0.68rem]', naoLidas > 0 ? 'font-bold text-primaria' : 'text-suave')}>{quando(n.ultimaMensagemEm)}</small>
          </span>
          <span className="flex items-center gap-1.5 text-xs text-suave">
            <span className={cx('size-1.5 shrink-0 rounded-full', n.papel === 'VENDEDOR' ? 'bg-blue-500' : 'bg-green-600')} aria-hidden="true" />
            <span className="truncate">{n.papel === 'VENDEDOR' ? 'Venda para' : 'Compra de'} {n.outroParticipanteNome}</span>
          </span>
          <span className="flex items-center gap-2">
            <span className={cx('min-w-0 flex-1 truncate text-xs', naoLidas > 0 ? 'font-semibold text-texto' : 'text-suave')}>
              {minha && 'Você: '}{n.ultimaMensagem}
            </span>
            <ContadorNaoLidas n={naoLidas} />
          </span>
        </span>
      </button>
    </li>
  );
}

/**
 * Hub de negociações: barra fixa à direita (computador), que pode ser recolhida para só as miniaturas.
 * No celular vira um botão flutuante que abre a lista.
 */
export function ChatHub() {
  const { lista, aberta, abrirChat, naoLidas, totalNaoLidas, hubExpandido, alternarHub } = useChat();
  const [listaCelular, setListaCelular] = useState(false);

  return (
    <>
      <aside aria-label="Negociações"
        className={cx('sticky top-16 hidden h-[calc(100vh-4rem)] shrink-0 flex-col border-l border-borda bg-white lg:flex',
          hubExpandido ? 'w-80' : 'w-16')}>
        <div className={cx('flex items-center gap-2 border-b border-borda py-2.5', hubExpandido ? 'px-3' : 'flex-col px-1')}>
          {hubExpandido && (
            <h2 className="m-0 flex flex-1 items-center gap-2 text-base">
              💬 Negociações <ContadorNaoLidas n={totalNaoLidas} />
            </h2>
          )}
          <button type="button" onClick={alternarHub} aria-expanded={hubExpandido}
            aria-label={hubExpandido ? 'Recolher negociações' : 'Expandir negociações'}
            title={hubExpandido ? 'Recolher' : 'Expandir negociações'}
            className="relative cursor-pointer rounded-md px-2 py-1 text-suave hover:bg-gray-100 hover:text-texto">
            {hubExpandido ? '»' : <>💬<ContadorNaoLidas n={totalNaoLidas} className="absolute -top-1 -right-1" /></>}
          </button>
        </div>

        {hubExpandido ? <ListaNegociacoes /> : (
          <ul className="flex-1 space-y-2 overflow-y-auto py-2">
            {(lista ?? []).map((n) => (
              <li key={n.id} className="flex justify-center">
                <button type="button" onClick={() => abrirChat(n.id)} title={`${n.tituloAnuncio} — ${n.outroParticipanteNome}`}
                  aria-label={`${n.tituloAnuncio} com ${n.outroParticipanteNome}`}
                  className={cx('relative cursor-pointer rounded-lg p-0.5 ring-2', aberta === n.id ? 'ring-primaria' : 'ring-transparent hover:ring-borda')}>
                  <ImagemAnuncio caminho={n.imagemAnuncio} alt="" compacta className="size-11 rounded-md" />
                  <ContadorNaoLidas n={naoLidas(n)} className="absolute -top-1.5 -right-1.5" />
                </button>
              </li>
            ))}
          </ul>
        )}
      </aside>

      {/* celular/tablet: botão flutuante (escondido enquanto o chat ocupa a tela) */}
      {!aberta && (
        <button type="button" onClick={() => setListaCelular(true)} aria-label="Abrir negociações"
          className="fixed right-4 bottom-4 z-20 flex size-14 cursor-pointer items-center justify-center rounded-full bg-primaria text-2xl text-white shadow-lg hover:bg-primaria-escura lg:hidden">
          💬<ContadorNaoLidas n={totalNaoLidas} className="absolute -top-1 -right-1" />
        </button>
      )}
      <SidePanel aberto={listaCelular} onFechar={() => setListaCelular(false)} titulo="Negociações"
        subtitulo={lista ? `${lista.length} conversa(s)` : undefined}>
        <div className="-mx-5 -my-4 flex h-[calc(100%+2rem)] flex-col">
          <ListaNegociacoes aoAbrir={() => setListaCelular(false)} grande />
        </div>
      </SidePanel>
    </>
  );
}
