import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { definirUsuarioAtual } from '../api/client';
import { api } from '../api/endpoints';

// Mesmos ids do UsuarioMock da API
export const VENDEDOR_MOCK = { id: '3c288400-f58f-4d28-905c-f7dba388d6c9', nome: 'Vendedor Mock' };
export const COMPRADOR_MOCK = { id: '7f1e2a5b-9c3d-4e8f-a6b7-1d2c3e4f5a6b', nome: 'Comprador Mock' };

export interface UsuarioConhecido {
  id: string;
  nome: string;
  email?: string;
}

interface Ctx {
  usuarioId: string;
  trocarUsuario: (id: string) => void;
  /** usuários disponíveis no seletor "Logado como" */
  usuarios: UsuarioConhecido[];
}

const UsuarioContext = createContext<Ctx | null>(null);

const KEY_ATUAL = 'psnf.usuarioAtual';

function lerAtual(): string {
  try {
    const v = localStorage.getItem(KEY_ATUAL);
    return v ? (JSON.parse(v) as string) : VENDEDOR_MOCK.id;
  } catch {
    return VENDEDOR_MOCK.id;
  }
}

/**
 * Simula o "login" enquanto a API não tem autenticação: o usuário escolhido vai no header X-Usuario-Id.
 * A lista de usuários vem da API (GET /dev/usuarios, endpoint de desenvolvimento); os mocks ficam
 * sempre disponíveis, mesmo que a lista não carregue.
 */
export function UsuarioProvider({ children }: { children: ReactNode }) {
  const [usuarioId, setUsuarioId] = useState<string>(lerAtual);
  const [daApi, setDaApi] = useState<UsuarioConhecido[]>([]);

  // define antes do primeiro render dos filhos (as páginas já buscam dados no mount)
  definirUsuarioAtual(usuarioId);

  useEffect(() => {
    try {
      localStorage.setItem(KEY_ATUAL, JSON.stringify(usuarioId));
    } catch {
      /* sem storage: segue só em memória */
    }
  }, [usuarioId]);

  useEffect(() => {
    api.usuariosDev().then(setDaApi).catch(() => setDaApi([]));
  }, []);

  const usuarios = useMemo(() => {
    const mocks = [VENDEDOR_MOCK, COMPRADOR_MOCK];
    const outros = daApi.filter((u) => !mocks.some((m) => m.id === u.id));
    const lista: UsuarioConhecido[] = [...mocks, ...outros];
    // usuário salvo no navegador que não está na lista (ex.: banco recriado): mantém visível no seletor
    if (!lista.some((u) => u.id === usuarioId)) lista.push({ id: usuarioId, nome: 'Usuário atual (não encontrado)' });
    return lista;
  }, [daApi, usuarioId]);

  const value = useMemo<Ctx>(() => ({
    usuarioId,
    trocarUsuario: (id) => {
      definirUsuarioAtual(id);
      setUsuarioId(id);
    },
    usuarios,
  }), [usuarioId, usuarios]);

  return <UsuarioContext.Provider value={value}>{children}</UsuarioContext.Provider>;
}

export function useUsuario() {
  const ctx = useContext(UsuarioContext);
  if (!ctx) throw new Error('useUsuario fora do UsuarioProvider');
  return ctx;
}
