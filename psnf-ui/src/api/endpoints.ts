import { http } from './client';
import type {
  AlertaMensagem, ArquivoEnviado, AvaliacaoInput, AvaliacoesAnuncio, EntregaInput, Anuncio, EnderecoConsultado, AnuncioInput, Categoria, EnderecoInput, Ncm, Negociacao, NegociacaoResumo, NovaOrdemInput, OrdemDePedido,
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

  // negociações (chat entre comprador e vendedor sobre um anúncio)
  /** O comprador logado inicia (ou reabre) a negociação do anúncio. */
  iniciarNegociacao: (anuncioId: UUID) => http.post<Negociacao>('/negociacoes', { anuncioId }),
  negociacoes: () => http.get<NegociacaoResumo[]>('/negociacoes'),
  negociacao: (id: UUID) => http.get<Negociacao>(`/negociacoes/${id}`),
  enviarMensagemNegociacao: (id: UUID, texto: string) => http.post<Negociacao>(`/negociacoes/${id}/mensagens`, { texto }),
  /** inclui outro anúncio do mesmo vendedor na negociação */
  adicionarAnuncioNegociacao: (id: UUID, anuncioId: UUID) => http.post<Negociacao>(`/negociacoes/${id}/anuncios`, { anuncioId }),
  removerAnuncioNegociacao: (id: UUID, anuncioId: UUID) => http.delete<Negociacao>(`/negociacoes/${id}/anuncios/${anuncioId}`),

  // avaliações dos produtos (só quem comprou e recebeu avalia)
  avaliacoes: (anuncioId: UUID) => http.get<AvaliacoesAnuncio>(`/anuncios/${anuncioId}/avaliacoes`),
  /** publica ou edita a avaliação do usuário logado */
  avaliar: (anuncioId: UUID, body: AvaliacaoInput) => http.put<AvaliacoesAnuncio>(`/anuncios/${anuncioId}/avaliacoes/minha`, body),
  removerAvaliacao: (anuncioId: UUID) => http.delete<AvaliacoesAnuncio>(`/anuncios/${anuncioId}/avaliacoes/minha`),

  // ordens de pedido
  criarOrdem: (body: NovaOrdemInput) => http.post<OrdemDePedido>('/ordens-de-pedido', body),
  ordem: (id: UUID) => http.get<OrdemDePedido>(`/ordens-de-pedido/${id}`),
  compras: () => http.get<OrdemDePedidoResumo[]>('/ordens-de-pedido/compras'),
  vendas: () => http.get<OrdemDePedidoResumo[]>('/ordens-de-pedido/vendas'),
  pagar: (id: UUID, body: PagamentoInput) => http.post<OrdemDePedido>(`/ordens-de-pedido/${id}/pagamentos`, body),
  /** o comprador confere e aceita a ordem criada pelo vendedor */
  aceitarOrdem: (id: UUID) => http.patch<OrdemDePedido>(`/ordens-de-pedido/${id}/aceitar`),
  /** o comprador anexa o comprovante de um pagamento (caminho vindo de enviarArquivo) */
  anexarComprovante: (id: UUID, pagamentoId: UUID, caminho: string) =>
    http.post<OrdemDePedido>(`/ordens-de-pedido/${id}/pagamentos/${pagamentoId}/comprovante`, { caminho }),
  /** o vendedor exclui uma forma de pagamento cadastrada por engano (antes do comprovante) */
  removerPagamento: (id: UUID, pagamentoId: UUID) => http.delete<OrdemDePedido>(`/ordens-de-pedido/${id}/pagamentos/${pagamentoId}`),
  adicionarNotaFiscal: (id: UUID, caminho: string) =>
    http.post<OrdemDePedido>(`/ordens-de-pedido/${id}/notas-fiscais`, { caminho }),
  /** ordens com mensagens de incidente novas para o usuário logado */
  alertasMensagens: () => http.get<AlertaMensagem[]>('/ordens-de-pedido/mensagens-nao-lidas'),
  /** o usuário viu as mensagens de incidente da ordem */
  marcarMensagensLidas: (id: UUID) => http.patch<OrdemDePedido>(`/ordens-de-pedido/${id}/mensagens/lidas`),
  enviarMensagem: (id: UUID, mensagem: string, imagem?: string) =>
    http.post<OrdemDePedido>(`/ordens-de-pedido/${id}/mensagens`, { mensagem, imagem: imagem || null }),
  /** descrição da entrega e código de envio são opcionais */
  despachar: (id: UUID, previsaoDeEntrega: string | null, entrega?: EntregaInput) =>
    http.patch<OrdemDePedido>(`/ordens-de-pedido/${id}/enviar`, {
      previsaoDeEntrega, descricaoEntrega: entrega?.descricao || null, codigoRastreio: entrega?.codigoRastreio || null,
    }),
  /** o vendedor informa ou corrige os dados da entrega (com os dois vazios, a entrega é removida) */
  registrarEntrega: (id: UUID, entrega: EntregaInput) => http.put<OrdemDePedido>(`/ordens-de-pedido/${id}/entrega`, entrega),
  confirmarEntrega: (id: UUID) => http.patch<OrdemDePedido>(`/ordens-de-pedido/${id}/confirmar-entrega`),
  cancelar: (id: UUID) => http.patch<OrdemDePedido>(`/ordens-de-pedido/${id}/cancelar`),
};
