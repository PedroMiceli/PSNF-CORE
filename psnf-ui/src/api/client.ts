// Cliente HTTP da API.
//
// Autenticação: os tokens ficam em cookies HttpOnly que o navegador envia sozinho (o JavaScript não
// consegue lê-los, então um XSS não rouba a sessão). Este módulo cuida de três coisas:
//  1. CSRF: copia o cookie XSRF-TOKEN para o header X-XSRF-TOKEN nas requisições que alteram dados;
//  2. sessão vencida (401): renova uma única vez (POST /auth/refresh) e repete a requisição;
//  3. sem sessão: avisa o UsuarioContext, que mostra a tela de login.

export class ApiError extends Error {
  status: number;
  /** erros de validação por campo (HTTP 400 vindo do @Valid) */
  campos?: Record<string, string>;
  /** identificador estável do erro, quando a API manda um (ex.: EMAIL_NAO_CONFIRMADO) */
  codigo?: string;

  constructor(status: number, message: string, campos?: Record<string, string>, codigo?: string) {
    super(message);
    this.status = status;
    this.campos = campos;
    this.codigo = codigo;
  }
}

const BASE = '/api';
const METODOS_SEGUROS = new Set(['GET', 'HEAD', 'OPTIONS']);
/** rotas em que um 401 é a resposta final (credencial errada, sessão encerrada), sem tentar renovar */
const SEM_RENOVAR = ['/auth/login', '/auth/refresh', '/auth/logout', '/auth/csrf'];

let aoPerderSessao: (() => void) | null = null;
/** chamado quando não há como renovar a sessão: o app volta para a tela de login */
export function definirAoPerderSessao(fn: (() => void) | null) {
  aoPerderSessao = fn;
}

function lerCookie(nome: string): string | null {
  const par = document.cookie.split('; ').find((c) => c.startsWith(nome + '='));
  return par ? decodeURIComponent(par.slice(nome.length + 1)) : null;
}

/** garante o cookie XSRF-TOKEN (a API o cria em GET /auth/csrf) */
async function tokenCsrf(forcar = false): Promise<string | null> {
  if (!forcar) {
    const atual = lerCookie('XSRF-TOKEN');
    if (atual) return atual;
  }
  await fetch(BASE + '/auth/csrf', { credentials: 'same-origin' }).catch(() => undefined);
  return lerCookie('XSRF-TOKEN');
}

// várias requisições podem receber 401 ao mesmo tempo (o token venceu): todas esperam a MESMA renovação
let renovando: Promise<boolean> | null = null;
export function renovarSessao(): Promise<boolean> {
  renovando ??= (async () => {
    try {
      const csrf = await tokenCsrf();
      const res = await fetch(BASE + '/auth/refresh', {
        method: 'POST', credentials: 'same-origin', headers: csrf ? { 'X-XSRF-TOKEN': csrf } : {},
      });
      return res.ok;
    } catch {
      return false;
    } finally {
      // libera depois que todos os que esperavam já receberam o resultado
      setTimeout(() => { renovando = null; }, 0);
    }
  })();
  return renovando;
}

async function request<T>(method: string, path: string, body?: unknown, tentativa = 0): Promise<T> {
  const headers: Record<string, string> = {};
  const ehForm = body instanceof FormData;
  // FormData: o navegador define o Content-Type multipart com o boundary
  if (body !== undefined && !ehForm) headers['Content-Type'] = 'application/json';
  if (!METODOS_SEGUROS.has(method)) {
    const csrf = await tokenCsrf(tentativa > 0);
    if (csrf) headers['X-XSRF-TOKEN'] = csrf;
  }

  let res: Response;
  try {
    res = await fetch(BASE + path, {
      method,
      headers,
      credentials: 'same-origin',
      body: body === undefined ? undefined : ehForm ? body : JSON.stringify(body),
    });
  } catch {
    throw new ApiError(0, 'Não foi possível conectar à API. Ela está rodando em localhost:8080?');
  }

  const texto = await res.text();
  const json = texto ? safeParse(texto) : null;
  const obj = json && typeof json === 'object' ? (json as Record<string, string>) : null;

  if (res.status === 401 && tentativa === 0 && !SEM_RENOVAR.some((p) => path.startsWith(p))) {
    if (await renovarSessao()) return request<T>(method, path, body, 1);
    aoPerderSessao?.();
  }
  // CSRF recusado (cookie trocado ou apagado): busca um token novo e tenta uma vez mais
  if (res.status === 403 && tentativa === 0 && obj?.erro?.includes('CSRF')) {
    return request<T>(method, path, body, 1);
  }

  if (!res.ok) {
    if (obj) {
      if (typeof obj.erro === 'string') throw new ApiError(res.status, obj.erro, undefined, obj.codigo);
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
 * Arquivos enviados ao storage ficam em /arquivos/... e passam pelo proxy /api
 * (o cookie de sessão vai junto, como em qualquer requisição da mesma origem).
 */
export function urlArquivo(caminho: string | null | undefined): string | undefined {
  if (!caminho) return undefined;
  if (caminho.startsWith('/arquivos/')) return BASE + caminho;
  return caminho;
}
