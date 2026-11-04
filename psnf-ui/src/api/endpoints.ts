import { http } from './client';
import type {
  ArquivoEnviado, Anuncio, AnuncioInput, Categoria, EnderecoInput, NovaOrdemInput, OrdemDePedido,
  OrdemDePedidoResumo, PagamentoInput, Usuario, UUID,
} from './types';

const q = (params: Record<string, string | undefined | null>) => {
  const s = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => v && s.set(k, v));
  const str = s.toString();
  return str ? `?${str}` : '';
};

export const api = {
  // arquivos (MinIO/S3): devolve o caminho a ser usado em imagens, NF, mensagens...
  enviarArquivo: (arquivo: File) => http.upload<ArquivoEnviado>('/arquivos', 'arquivo', arquivo),

  // usuários
  usuarioLogado: () => http.get<Usuario>('/usuarios/me'),
  usuario: (id: UUID) => http.get<Usuario>(`/usuarios/${id}`),
  cadastrarUsuario: (body: { nome: string; email: string; senha: string; telefone: string }) =>
    http.post<Usuario>('/usuarios', body),
  atualizarUsuario: (body: { nome: string; telefone?: string; imagem?: string }) => http.put<Usuario>('/usuarios/me', body),
  adicionarEndereco: (body: EnderecoInput) => http.post<Usuario>('/usuarios/me/enderecos', body),
  removerEndereco: (id: UUID) => http.delete<Usuario>(`/usuarios/me/enderecos/${id}`),
  anunciosDoUsuario: (id: UUID) => http.get<Anuncio[]>(`/usuarios/${id}/anuncios`),

  // categorias
  categorias: () => http.get<Categoria[]>('/categorias'),
  cadastrarCategoria: (nome: string) => http.post<Categoria>('/categorias', { nome }),

  // anúncios
  anunciosAtivos: (categoriaId?: UUID) => http.get<Anuncio[]>(`/anuncios${q({ categoriaId })}`),
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
