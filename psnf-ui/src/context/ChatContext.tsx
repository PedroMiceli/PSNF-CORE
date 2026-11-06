import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react';
import { ApiError } from '../api/client';
import { api } from '../api/endpoints';
import type { Negociacao, UUID } from '../api/types';

interface Ctx {
  /** negociação aberta na janela de chat */
  aberta: UUID | null;
  /** dados já carregados ao iniciar (evita uma segunda chamada) */
  inicial: Negociacao | null;
  abrirChat: (id: UUID) => void;
  fecharChat: () => void;
  /** comprador inicia (ou reabre) a negociação do anúncio e abre o chat */
  iniciarNegociacao: (anuncioId: UUID) => Promise<ApiError | null>;
}

const ChatContext = createContext<Ctx | null>(null);

// a conversa aberta continua aberta ao recarregar a página (por aba do navegador)
// (guardada por usuário: ao trocar o "Logado como", não reabre a conversa de outra pessoa)
const chave = (usuarioId: string) => `psnf.chatAberto.${usuarioId}`;
const lerAberta = (usuarioId: string) => { try { return sessionStorage.getItem(chave(usuarioId)); } catch { return null; } };
const gravarAberta = (usuarioId: string, id: UUID | null) => {
  try {
    if (id) sessionStorage.setItem(chave(usuarioId), id); else sessionStorage.removeItem(chave(usuarioId));
  } catch { /* sem storage: só não lembra */ }
};

/** Janela de chat única, disponível em todas as páginas (fica aberta ao navegar). */
export function ChatProvider({ usuarioId, children }: { usuarioId: string; children: ReactNode }) {
  const [aberta, setAbertaState] = useState<UUID | null>(() => lerAberta(usuarioId));
  const setAberta = useCallback((id: UUID | null) => { setAbertaState(id); gravarAberta(usuarioId, id); }, [usuarioId]);
  const [inicial, setInicial] = useState<Negociacao | null>(null);

  const abrirChat = useCallback((id: UUID) => { setInicial(null); setAberta(id); }, [setAberta]);
  const fecharChat = useCallback(() => { setAberta(null); setInicial(null); }, [setAberta]);
  const iniciarNegociacao = useCallback(async (anuncioId: UUID) => {
    try {
      const n = await api.iniciarNegociacao(anuncioId);
      setInicial(n);
      setAberta(n.id);
      return null;
    } catch (e) {
      return e instanceof ApiError ? e : new ApiError(0, String(e));
    }
  }, [setAberta]);

  const valor = useMemo(() => ({ aberta, inicial, abrirChat, fecharChat, iniciarNegociacao }),
    [aberta, inicial, abrirChat, fecharChat, iniciarNegociacao]);
  return <ChatContext.Provider value={valor}>{children}</ChatContext.Provider>;
}

export function useChat() {
  const ctx = useContext(ChatContext);
  if (!ctx) throw new Error('useChat precisa estar dentro de <ChatProvider>');
  return ctx;
}
