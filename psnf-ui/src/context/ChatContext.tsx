import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { ApiError } from '../api/client';
import { api } from '../api/endpoints';
import type { Negociacao, NegociacaoResumo, UUID } from '../api/types';
import { useAoMudar, useNovidades } from './NovidadesContext';


interface Ctx {
  /** negociação aberta na janela de chat */
  aberta: UUID | null;
  /** dados já carregados ao iniciar (evita uma segunda chamada) */
  inicial: Negociacao | null;
  abrirChat: (id: UUID) => void;
  /** muda a cada abrirChat: reabrir a mesma conversa (ex.: estava minimizada) expande e recarrega a janela */
  aberturas: number;
  fecharChat: () => void;
  /** comprador inicia (ou reabre) a negociação do anúncio e abre o chat */
  iniciarNegociacao: (anuncioId: UUID) => Promise<ApiError | null>;

  /** todas as conversas do usuário (hub de negociações) */
  lista: NegociacaoResumo[] | null;
  erroLista: ApiError | null;
  recarregarLista: () => void;
  /** mensagens dos outros ainda não vistas nesta conversa */
  naoLidas: (n: NegociacaoResumo) => number;
  totalNaoLidas: number;
  /** registra que o usuário já viu as N mensagens da conversa */
  marcarVista: (id: UUID, quantidadeMensagens: number) => void;

  /** barra lateral do hub aberta (larga) ou recolhida (só miniaturas) */
  hubExpandido: boolean;
  alternarHub: () => void;
}

const ChatContext = createContext<Ctx | null>(null);

/* ------------------------------------------------------------------ armazenamento local (por usuário) */
// a conversa aberta continua aberta ao recarregar a página (por aba do navegador);
// "vistas" e o estado da barra ficam no navegador. Tudo separado por usuário: ao trocar o
// usuário no mesmo navegador, nada da outra pessoa aparece.
const ler = <T,>(storage: () => Storage, chave: string, padrao: T): T => {
  try { const v = storage().getItem(chave); return v == null ? padrao : (JSON.parse(v) as T); } catch { return padrao; }
};
const gravar = (storage: () => Storage, chave: string, valor: unknown) => {
  try {
    if (valor == null) storage().removeItem(chave); else storage().setItem(chave, JSON.stringify(valor));
  } catch { /* sem storage: só não lembra */ }
};
const sessao = () => sessionStorage;
const local = () => localStorage;

export function ChatProvider({ usuarioId, children }: { usuarioId: string; children: ReactNode }) {
  const kAberta = `psnf.chatAberto.${usuarioId}`;
  const kVistas = `psnf.chatVistas.${usuarioId}`;
  const kHub = `psnf.hubExpandido.${usuarioId}`;

  const [aberta, setAbertaState] = useState<UUID | null>(() => ler<UUID | null>(sessao, kAberta, null));
  const setAberta = useCallback((id: UUID | null) => { setAbertaState(id); gravar(sessao, kAberta, id); }, [kAberta]);
  const [inicial, setInicial] = useState<Negociacao | null>(null);

  const [lista, setLista] = useState<NegociacaoResumo[] | null>(null);
  const [erroLista, setErroLista] = useState<ApiError | null>(null);
  const [vistas, setVistas] = useState<Record<UUID, number>>(() => ler(local, kVistas, {}));
  const [hubExpandido, setHubExpandido] = useState(() => ler(local, kHub, true));

  const recarregarLista = useCallback(() => {
    api.negociacoes()
      .then((l) => { setLista(l); setErroLista(null); })
      .catch((e) => setErroLista(e instanceof ApiError ? e : new ApiError(0, String(e))));
  }, []);

  // carrega ao entrar; depois só recarrega quando a consulta de novidades indica conversa nova ou mensagem nova
  // (antes: a lista inteira era buscada a cada 8 s, mesmo sem nada novo)
  const { novidades } = useNovidades();
  useEffect(() => { recarregarLista(); }, [recarregarLista]);
  useAoMudar(novidades?.negociacoes.versao, recarregarLista);

  const [aberturas, setAberturas] = useState(0);
  const abrirChat = useCallback((id: UUID) => { setInicial(null); setAberta(id); setAberturas((n) => n + 1); }, [setAberta]);
  const fecharChat = useCallback(() => { setAberta(null); setInicial(null); }, [setAberta]);
  const iniciarNegociacao = useCallback(async (anuncioId: UUID) => {
    try {
      const n = await api.iniciarNegociacao(anuncioId);
      setInicial(n);
      setAberta(n.id);
      recarregarLista();
      return null;
    } catch (e) {
      return e instanceof ApiError ? e : new ApiError(0, String(e));
    }
  }, [setAberta, recarregarLista]);

  const marcarVista = useCallback((id: UUID, quantidade: number) => {
    setVistas((v) => {
      if ((v[id] ?? 0) >= quantidade) return v;
      const novo = { ...v, [id]: quantidade };
      gravar(local, kVistas, novo);
      return novo;
    });
  }, [kVistas]);

  // não lidas = mensagens da outra pessoa ainda sem resposta (vem da API), descontando as que o
  // usuário já viu neste navegador; as próprias mensagens nunca contam
  const naoLidas = useCallback((n: NegociacaoResumo) => {
    if (n.id === aberta) return 0;
    const novasDesdeQueViu = Math.max(0, n.quantidadeMensagens - (vistas[n.id] ?? 0));
    return Math.min(n.mensagensSemResposta, novasDesdeQueViu);
  }, [vistas, aberta]);
  const totalNaoLidas = useMemo(() => (lista ?? []).reduce((s, n) => s + (naoLidas(n) > 0 ? 1 : 0), 0), [lista, naoLidas]);

  const alternarHub = useCallback(() => setHubExpandido((e) => { gravar(local, kHub, !e); return !e; }), [kHub]);

  const valor = useMemo(() => ({
    aberta, inicial, abrirChat, aberturas, fecharChat, iniciarNegociacao,
    lista, erroLista, recarregarLista, naoLidas, totalNaoLidas, marcarVista, hubExpandido, alternarHub,
  }), [aberta, inicial, abrirChat, aberturas, fecharChat, iniciarNegociacao, lista, erroLista, recarregarLista, naoLidas,
    totalNaoLidas, marcarVista, hubExpandido, alternarHub]);
  return <ChatContext.Provider value={valor}>{children}</ChatContext.Provider>;
}

export function useChat() {
  const ctx = useContext(ChatContext);
  if (!ctx) throw new Error('useChat precisa estar dentro de <ChatProvider>');
  return ctx;
}
