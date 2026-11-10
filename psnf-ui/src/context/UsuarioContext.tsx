import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { ApiError, definirAoPerderSessao } from '../api/client';
import { api } from '../api/endpoints';
import type { Usuario } from '../api/types';

type Estado =
  | { tipo: 'carregando' }
  | { tipo: 'anonimo'; motivo?: 'expirou' | 'saiu' }
  | { tipo: 'logado'; usuario: Usuario };

interface Ctx {
  estado: Estado;
  /** id do usuário logado ('' fora do login: as telas internas só aparecem logado) */
  usuarioId: string;
  usuario: Usuario | null;
  entrar: (email: string, senha: string) => Promise<ApiError | null>;
  sair: (todosDispositivos?: boolean) => Promise<void>;
  /** depois de editar o perfil, atualiza nome/foto no topo */
  atualizarUsuario: (u: Usuario) => void;
}

const UsuarioContext = createContext<Ctx | null>(null);

/**
 * Sessão do usuário. Ao abrir o app pergunta à API quem está logado (GET /usuarios/me): se o access token
 * venceu, o cliente HTTP renova sozinho com o refresh token; se não houver sessão, mostra o login.
 */
export function UsuarioProvider({ children }: { children: ReactNode }) {
  const [estado, setEstado] = useState<Estado>({ tipo: 'carregando' });

  useEffect(() => {
    // qualquer requisição que descubra a sessão perdida (refresh recusado) leva de volta ao login
    definirAoPerderSessao(() => setEstado((e) => (e.tipo === 'logado' ? { tipo: 'anonimo', motivo: 'expirou' } : e)));
    api.usuarioLogado()
      .then((usuario) => setEstado({ tipo: 'logado', usuario }))
      .catch(() => setEstado({ tipo: 'anonimo' }));
    return () => definirAoPerderSessao(null);
  }, []);

  const entrar = useCallback(async (email: string, senha: string) => {
    try {
      setEstado({ tipo: 'logado', usuario: await api.entrar(email, senha) });
      return null;
    } catch (e) {
      return e instanceof ApiError ? e : new ApiError(0, String(e));
    }
  }, []);

  const sair = useCallback(async (todos = false) => {
    await (todos ? api.sairDeTodos() : api.sair()).catch(() => undefined);
    setEstado({ tipo: 'anonimo', motivo: 'saiu' });
  }, []);

  const atualizarUsuario = useCallback((usuario: Usuario) => setEstado({ tipo: 'logado', usuario }), []);

  const value = useMemo<Ctx>(() => ({
    estado,
    usuario: estado.tipo === 'logado' ? estado.usuario : null,
    usuarioId: estado.tipo === 'logado' ? estado.usuario.id : '',
    entrar, sair, atualizarUsuario,
  }), [estado, entrar, sair, atualizarUsuario]);

  return <UsuarioContext.Provider value={value}>{children}</UsuarioContext.Provider>;
}

export function useUsuario() {
  const ctx = useContext(UsuarioContext);
  if (!ctx) throw new Error('useUsuario fora do UsuarioProvider');
  return ctx;
}
