import { useCallback, useEffect, useState } from 'react';
import { ApiError } from './client';

/** Executa uma chamada da API e expõe dados, erro, carregamento e recarregar. */
export function useApi<T>(fn: () => Promise<T>, deps: unknown[]) {
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<ApiError | null>(null);
  const [loading, setLoading] = useState(true);

  const executar = useCallback(fn, deps);

  const reload = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setData(await executar());
    } catch (e) {
      setError(e instanceof ApiError ? e : new ApiError(0, String(e)));
    } finally {
      setLoading(false);
    }
  }, [executar]);

  useEffect(() => {
    void reload();
  }, [reload]);

  return { data, setData, error, loading, reload };
}

/** Para ações (POST/PUT/PATCH): controla "enviando" e erro. */
export function useAcao() {
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<ApiError | null>(null);

  const executar = useCallback(async <T,>(fn: () => Promise<T>): Promise<T | undefined> => {
    setEnviando(true);
    setErro(null);
    try {
      return await fn();
    } catch (e) {
      setErro(e instanceof ApiError ? e : new ApiError(0, String(e)));
      return undefined;
    } finally {
      setEnviando(false);
    }
  }, []);

  return { enviando, erro, setErro, executar };
}
