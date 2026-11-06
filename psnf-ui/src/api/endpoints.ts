import { http } from './client';
import type {
  ArquivoEnviado, Anuncio, EnderecoConsultado, AnuncioInput, Categoria, EnderecoInput, Ncm, NovaOrdemInput, OrdemDePedido,
  OrdemDePedidoResumo, Pagina, PagamentoInput, Usuario, UUID,
} from './types';

const q = (params: Record<string, string | number | undefined | null>) => {
  const s = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => v != null && v !== '' && s.set(k, String(v)));
  const str = s.toString();
  return str ? `?${str}` : '';
};

export const api = {
  // arquivos (MinIO/S3): devolve o caminho a ser usado em imagens, NF, mensagens...
  enviarArquivo: (arquivo: File) => http.upload<ArquivoEnviado>('/arquivos', 'arquivo', arquivo),

  // usuários
  usuarioLogado: () => http.get<Usuario>('/usuarios/me'),
  usuario: (id: UUID) => http.get<Usuario>(`/usuarios/${id}`),
  /** SOMENTE DESENVOLVIMENTO: usuários para o seletor "Logado como" (login simulado). */
  usuariosDev: () => http.get<{ id: UUID; nome: string; email: string }[]>('/dev/usuarios'),
  atualizarUsuario: (body: { nome: string; telefone?: string; imagem?: string }) => http.put<Usuario>('/usuarios/me', body),
  adicionarEndereco: (body: EnderecoInput) => http.post<Usuario>('/usuarios/me/enderecos', body),
  removerEndereco: (id: UUID) => http.delete<Usuario>(`/usuarios/me/enderecos/${id}`),
  /** Busca o endereço pelo CEP (a API consulta a BrasilAPI). */
  consultarCep: (cep: string) => http.get<EnderecoConsultado>(`/cep/${cep.replace(/\D/g, '')}`),
  anunciosDoUsuario: (id: UUID) => http.get<Anuncio[]>(`/usuarios/${id}/anuncios`),

  // tabela NCM (a API consulta a BrasilAPI)
  pesquisarNcm: (busca: string) => http.get<Ncm[]>(`/ncm${q({ busca })}`),
  consultarNcm: (codigo: string) => http.get<Ncm>(`/ncm/${codigo.replace(/\D/g, '')}`),

  // categorias (fixas, somente leitura)
  categorias: () => http.get<Categoria[]>('/categorias'),

  // anúncios
  /** Vitrine/pesquisa: anúncios ativos, paginados; a busca de texto é feita no PostgreSQL. */
  pesquisarAnuncios: (filtro: { termo?: string; categoriaId?: UUID; pagina?: number; tamanho?: number }) =>
    http.get<Pagina<Anuncio>>(`/anuncios${q(filtro)}`),
  anuncio: (id: UUID) => http.get<Anuncio>(`/anuncios/${id}`),
  meusAnuncios: () => http.get<Anuncio[]>('/anuncios/meus'),
  criarAnuncio: (body: AnuncioInput) => http.post<Anuncio>('/anuncios', body),
  atualizarAnuncio: (id: UUID, body: AnuncioInput) => http.put<Anuncio>(`/anuncios/${id}`, body),
  ativarAnuncio: (id: UUID) => http.patch<Anuncio>(`/anuncios/${id}/ativar`),
  desativarAnuncio: (id: UUID) => http.patch<Anuncio>(`/anuncios/${id}/desativar`),

  // ordens de pedido
  criarOrdem: (body: NovaOrdemInput) => http.post<OrdemDePedido>('/ordens-de-pedido', body),
  ordem: (id: UUID) => http.get<OrdemDePedido>(`/ordens-de-pedido/${id}`),
  compras: () => http.get<OrdemDePedidoResumo[]>('/ordens-de-pedido/compras'),
  vendas: () => http.get<OrdemDePedidoResumo[]>('/ordens-de-pedido/vendas'),
  pagar: (id: UUID, body: PagamentoInput) => http.post<OrdemDePedido>(`/ordens-de-pedido/${id}/pagamentos`, body),
  adicionarNotaFiscal: (id: UUID, caminho: string) =>
    http.post<OrdemDePedido>(`/ordens-de-pedido/${id}/notas-fiscais`, { caminho }),
  enviarMensagem: (id: UUID, mensagem: string, imagem?: string) =>
    http.post<OrdemDePedido>(`/ordens-de-pedido/${id}/mensagens`, { mensagem, imagem: imagem || null }),
  despachar: (id: UUID, previsaoDeEntrega: string | null) =>
    http.patch<OrdemDePedido>(`/ordens-de-pedido/${id}/enviar`, { previsaoDeEntrega }),
  confirmarEntrega: (id: UUID) => http.patch<OrdemDePedido>(`/ordens-de-pedido/${id}/confirmar-entrega`),
  cancelar: (id: UUID) => http.patch<OrdemDePedido>(`/ordens-de-pedido/${id}/cancelar`),
};
