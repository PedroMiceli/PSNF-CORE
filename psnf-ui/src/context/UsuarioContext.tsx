import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { definirUsuarioAtual } from '../api/client';

// Mesmos ids do UsuarioMock da API
export const VENDEDOR_MOCK = { id: '3c288400-f58f-4d28-905c-f7dba388d6c9', nome: 'Vendedor Mock' };
export const COMPRADOR_MOCK = { id: '7f1e2a5b-9c3d-4e8f-a6b7-1d2c3e4f5a6b', nome: 'Comprador Mock' };

export interface UsuarioConhecido {
  id: string;
  nome: string;
}

interface Ctx {
  usuarioId: string;
  trocarUsuario: (id: string) => void;
  usuarios: UsuarioConhecido[];
  lembrarUsuario: (u: UsuarioConhecido) => void;
}

const UsuarioContext = createContext<Ctx | null>(null);

const KEY_ATUAL = 'psnf.usuarioAtual';
const KEY_LISTA = 'psnf.usuariosConhecidos';

function ler<T>(key: string, padrao: T): T {
  try {
    const v = localStorage.getItem(key);
    return v ? (JSON.parse(v) as T) : padrao;
  } catch {
    return padrao;
  }
}
function gravar(key: string, valor: unknown) {
  try {
    localStorage.setItem(key, JSON.stringify(valor));
  } catch {
    /* sem storage: segue só em memória */
  }
}

/**
 * Simula o "login" enquanto a API não tem autenticação: o usuário escolhido vai no header X-Usuario-Id.
 */
export function UsuarioProvider({ children }: { children: ReactNode }) {
  const [usuarioId, setUsuarioId] = useState<string>(() => ler(KEY_ATUAL, VENDEDOR_MOCK.id));
  const [extras, setExtras] = useState<UsuarioConhecido[]>(() => ler(KEY_LISTA, []));

  // define antes do primeiro render dos filhos (as páginas já buscam dados no mount)
  definirUsuarioAtual(usuarioId);

  useEffect(() => gravar(KEY_ATUAL, usuarioId), [usuarioId]);
  useEffect(() => gravar(KEY_LISTA, extras), [extras]);

  const value = useMemo<Ctx>(() => ({
    usuarioId,
    trocarUsuario: (id) => {
      definirUsuarioAtual(id);
      setUsuarioId(id);
    },
    usuarios: [VENDEDOR_MOCK, COMPRADOR_MOCK, ...extras],
    lembrarUsuario: (u) => setExtras((lista) => [...lista.filter((x) => x.id !== u.id), u]),
  }), [usuarioId, extras]);

  return <UsuarioContext.Provider value={value}>{children}</UsuarioContext.Provider>;
}

export function useUsuario() {
  const ctx = useContext(UsuarioContext);
  if (!ctx) throw new Error('useUsuario fora do UsuarioProvider');
  return ctx;
}
