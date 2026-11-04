// Cliente HTTP: envia o usuário "logado" no header X-Usuario-Id (autenticação temporária da API)

let usuarioAtualId: string | null = null;

export function definirUsuarioAtual(id: string) {
  usuarioAtualId = id;
}

export class ApiError extends Error {
  status: number;
  /** erros de validação por campo (HTTP 400 vindo do @Valid) */
  campos?: Record<string, string>;

  constructor(status: number, message: string, campos?: Record<string, string>) {
    super(message);
    this.status = status;
    this.campos = campos;
  }
}

const BASE = '/api';

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  const headers: Record<string, string> = {};
  const ehForm = body instanceof FormData;
  // FormData: o navegador define o Content-Type multipart com o boundary
  if (body !== undefined && !ehForm) headers['Content-Type'] = 'application/json';
  if (usuarioAtualId) headers['X-Usuario-Id'] = usuarioAtualId;

  let res: Response;
  try {
    res = await fetch(BASE + path, {
      method,
      headers,
      body: body === undefined ? undefined : ehForm ? body : JSON.stringify(body),
    });
  } catch {
    throw new ApiError(0, 'Não foi possível conectar à API. Ela está rodando em localhost:8080?');
  }

  const texto = await res.text();
  const json = texto ? safeParse(texto) : null;

  if (!res.ok) {
    if (json && typeof json === 'object') {
      const obj = json as Record<string, string>;
      if (typeof obj.erro === 'string') throw new ApiError(res.status, obj.erro);
      if (res.status === 400 && !('status' in obj)) {
        throw new ApiError(res.status, 'Verifique os campos destacados', obj);
      }
    }
    if (res.status === 502 || res.status === 504) {
      throw new ApiError(res.status, 'API indisponível. Ela está rodando em localhost:8080?');
    }
    throw new ApiError(res.status, `Erro ${res.status} ao chamar ${method} ${path}`);
  }
  return json as T;
}

function safeParse(texto: string): unknown {
  try {
    return JSON.parse(texto);
  } catch {
    return texto;
  }
}

export const http = {
  get: <T>(path: string) => request<T>('GET', path),
  post: <T>(path: string, body?: unknown) => request<T>('POST', path, body ?? {}),
  put: <T>(path: string, body: unknown) => request<T>('PUT', path, body),
  patch: <T>(path: string, body?: unknown) => request<T>('PATCH', path, body),
  delete: <T>(path: string) => request<T>('DELETE', path),
  upload: <T>(path: string, campo: string, arquivo: File) => {
    const form = new FormData();
    form.append(campo, arquivo);
    return request<T>('POST', path, form);
  },
};

/**
 * Converte o "caminho" salvo na API em uma URL que o navegador consegue abrir.
 * Arquivos enviados ao storage ficam em /arquivos/... e passam pelo proxy /api.
 */
export function urlArquivo(caminho: string | null | undefined): string | undefined {
  if (!caminho) return undefined;
  if (caminho.startsWith('/arquivos/')) return BASE + caminho;
  return caminho;
}
