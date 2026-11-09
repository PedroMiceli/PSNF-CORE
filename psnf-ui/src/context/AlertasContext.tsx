import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from 'react';
import { api } from '../api/endpoints';
import type { AlertaMensagem, UUID } from '../api/types';

/** Intervalo da consulta de mensagens novas nos incidentes (até existir WebSocket). */
const ATUALIZAR_A_CADA_MS = 15000;

/** Aviso que aparece no canto da tela quando chega uma mensagem nova. */
export interface Aviso {
  id: number;
  alerta: AlertaMensagem;
}

interface Ctx {
  /** ordens com mensagens de incidente que o usuário ainda não viu */
  alertas: AlertaMensagem[];
  naoLidasDaOrdem: (ordemId: UUID) => number;
  recarregar: () => void;
  avisos: Aviso[];
  dispensarAviso: (id: number) => void;
  /** ordem aberta na tela: não gera aviso no canto (a própria página já destaca) */
  definirOrdemAberta: (ordemId: UUID | null) => void;
}

const AlertasContext = createContext<Ctx | null>(null);

export function AlertasProvider({ children }: { children: ReactNode }) {
  const [alertas, setAlertas] = useState<AlertaMensagem[]>([]);
  const [avisos, setAvisos] = useState<Aviso[]>([]);
  const anteriores = useRef<Map<UUID, number> | null>(null);
  const ordemAberta = useRef<UUID | null>(null);
  const proximoId = useRef(1);

  const recarregar = useCallback(() => {
    api.alertasMensagens().then((lista) => {
      const antes = anteriores.current;
      // aviso no canto para cada ordem com mais mensagens novas do que na consulta anterior
      // (na primeira consulta também: assim quem entra no site já vê que tem mensagem esperando)
      const novos = lista.filter((a) => a.ordemId !== ordemAberta.current && a.naoLidas > (antes?.get(a.ordemId) ?? 0));
      if (novos.length) {
        setAvisos((atual) => [...atual.filter((v) => !novos.some((n) => n.ordemId === v.alerta.ordemId)),
          ...novos.map((alerta) => ({ id: proximoId.current++, alerta }))].slice(-3));
      }
      anteriores.current = new Map(lista.map((a) => [a.ordemId, a.naoLidas]));
      setAlertas(lista);
    }).catch(() => { /* tenta de novo no próximo ciclo */ });
  }, []);

  useEffect(() => {
    recarregar();
    const t = setInterval(() => { if (!document.hidden) recarregar(); }, ATUALIZAR_A_CADA_MS);
    return () => clearInterval(t);
  }, [recarregar]);

  const dispensarAviso = useCallback((id: number) => setAvisos((a) => a.filter((v) => v.id !== id)), []);
  const definirOrdemAberta = useCallback((id: UUID | null) => {
    ordemAberta.current = id;
    if (id) setAvisos((a) => a.filter((v) => v.alerta.ordemId !== id));
  }, []);
  const naoLidasDaOrdem = useCallback((id: UUID) => alertas.find((a) => a.ordemId === id)?.naoLidas ?? 0, [alertas]);

  const valor = useMemo(() => ({ alertas, naoLidasDaOrdem, recarregar, avisos, dispensarAviso, definirOrdemAberta }),
    [alertas, naoLidasDaOrdem, recarregar, avisos, dispensarAviso, definirOrdemAberta]);
  return <AlertasContext.Provider value={valor}>{children}</AlertasContext.Provider>;
}

export function useAlertas() {
  const ctx = useContext(AlertasContext);
  if (!ctx) throw new Error('useAlertas precisa estar dentro de <AlertasProvider>');
  return ctx;
}
