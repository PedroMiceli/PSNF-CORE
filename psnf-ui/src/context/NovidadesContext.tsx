import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from 'react';
import { api } from '../api/endpoints';
import type { Novidades } from '../api/types';

/**
 * Intervalo da consulta de novidades. É a ÚNICA consulta periódica do site (fora o chat aberto):
 * hub de negociações, alertas de incidentes e a página da ordem só recarregam quando a versão muda.
 */
export const INTERVALO_NOVIDADES_MS = 30000;

interface Ctx {
  novidades: Novidades | null;
  /** consulta já (ex.: o usuário voltou para a aba) */
  verificarAgora: () => void;
}

const NovidadesContext = createContext<Ctx | null>(null);

export function NovidadesProvider({ children }: { children: ReactNode }) {
  const [novidades, setNovidades] = useState<Novidades | null>(null);
  const ultimaConsulta = useRef(0);

  const verificarAgora = useCallback(() => {
    // evita rajadas (foco + visibilidade disparam juntos)
    if (Date.now() - ultimaConsulta.current < 2000) return;
    ultimaConsulta.current = Date.now();
    api.novidades()
      // só troca o objeto se algo mudou: quem depende da versão não é avisado à toa
      .then((n) => setNovidades((atual) => (atual && JSON.stringify(atual) === JSON.stringify(n) ? atual : n)))
      .catch(() => { /* tenta de novo no próximo ciclo */ });
  }, []);

  useEffect(() => {
    verificarAgora();
    const t = setInterval(() => { if (!document.hidden) verificarAgora(); }, INTERVALO_NOVIDADES_MS);
    // voltou para a aba/janela: confere na hora em vez de esperar o próximo ciclo
    const aoVoltar = () => { if (!document.hidden) verificarAgora(); };
    document.addEventListener('visibilitychange', aoVoltar);
    window.addEventListener('focus', aoVoltar);
    return () => { clearInterval(t); document.removeEventListener('visibilitychange', aoVoltar); window.removeEventListener('focus', aoVoltar); };
  }, [verificarAgora]);

  const valor = useMemo(() => ({ novidades, verificarAgora }), [novidades, verificarAgora]);
  return <NovidadesContext.Provider value={valor}>{children}</NovidadesContext.Provider>;
}

export function useNovidades() {
  const ctx = useContext(NovidadesContext);
  if (!ctx) throw new Error('useNovidades precisa estar dentro de <NovidadesProvider>');
  return ctx;
}

/**
 * Executa `fn` quando a versão muda (não na primeira vez: a tela já carregou os dados ao abrir).
 * Ex.: useAoMudar(novidades?.ordens.versao, () => recarregarOrdem())
 */
export function useAoMudar(versao: string | undefined, fn: () => void) {
  const anterior = useRef<string | undefined>(undefined);
  const fnRef = useRef(fn);
  fnRef.current = fn;
  useEffect(() => {
    if (versao === undefined) return;
    if (anterior.current !== undefined && anterior.current !== versao) fnRef.current();
    anterior.current = versao;
  }, [versao]);
}
