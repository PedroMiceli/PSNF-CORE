// Espelho dos DTOs de resposta/requisição da psnf-api

export type UUID = string;

/** Resposta paginada da API (pagina começa em 0). */
export interface Pagina<T> {
  itens: T[];
  pagina: number;
  tamanho: number;
  total: number;
  totalPaginas: number;
}

export interface Endereco {
  id: UUID;
  logradouro: string;
  numero?: string | null;
  complemento?: string | null;
  bairro?: string | null;
  cidade: string;
  cep: string;
  uf: string;
}
export type EnderecoInput = Omit<Endereco, 'id'>;

/** Endereço encontrado pelo CEP (logradouro e bairro podem faltar em CEPs gerais de cidade). */
export interface EnderecoConsultado {
  cep: string;
  logradouro: string | null;
  bairro: string | null;
  cidade: string | null;
  uf: string | null;
}

export interface Usuario {
  id: UUID;
  nome: string;
  email: string;
  imagem?: string | null;
  telefone?: string | null;
  dataCadastro: string;
  enderecos: Endereco[];
}

export interface UsuarioResumo {
  id: UUID;
  nome: string;
  email: string;
  telefone?: string | null;
}

export interface Categoria {
  id: UUID;
  nome: string;
}

export interface Valor {
  id: UUID;
  valorUnidade: number;
  quantidadeMinima: number;
}
export interface Imagem {
  id: UUID;
  caminho: string;
  principal: boolean;
}
export interface Variacao {
  id: UUID;
  variacao: string;
  sku?: string | null;
  gtin?: string | null;
}

export type CondicaoProduto = 'NOVO' | 'USADO' | 'RECONDICIONADO';
export type ResponsavelGarantia = 'FABRICANTE' | 'FORNECEDOR';

/** Informações complementares do produto: todas opcionais; vão para o item da ordem na compra. */
export interface InformacoesProduto {
  sku?: string | null;
  gtin?: string | null;
  marca?: string | null;
  modelo?: string | null;
  condicao?: CondicaoProduto | null;
  ncm?: string | null;
  cest?: string | null;
  /** origem da mercadoria (tabela da NF-e, 0 a 8) */
  origem?: number | null;
  unidadeComercial?: string | null;
  quantidadePorEmbalagem?: number | null;
  pesoLiquidoKg?: number | null;
  pesoBrutoKg?: number | null;
  comprimentoCm?: number | null;
  larguraCm?: number | null;
  alturaCm?: number | null;
  garantiaMeses?: number | null;
  garantiaResponsavel?: ResponsavelGarantia | null;
  certificacoes?: string | null;
}

/** Linha da ficha técnica. */
export interface Caracteristica {
  nome: string;
  valor: string;
}

export interface Ncm {
  codigo: string;
  descricao: string;
}

export interface Anuncio {
  id: UUID;
  titulo: string;
  descricao: string;
  ativo: boolean;
  categoriaId: UUID;
  vendedorId: UUID;
  precoMenor: number | null;
  dataCadastro: string;
  valores: Valor[];
  imagens: Imagem[];
  variacoes: Variacao[];
  informacoes: InformacoesProduto | null;
  caracteristicas: Caracteristica[];
}

export interface AnuncioInput {
  titulo: string;
  descricao: string;
  categoriaId: UUID;
  valores: { valorUnidade: number; quantidadeMinima: number }[];
  imagens: { caminho: string; principal: boolean }[];
  variacoes: { variacao: string; sku?: string | null; gtin?: string | null }[];
  informacoes?: InformacoesProduto | null;
  caracteristicas?: Caracteristica[];
}

export type StatusOrdem = 'AGUARDANDO_CONFIRMACAO' | 'ACEITO' | 'AGUARDANDO_PAGAMENTO' | 'PAGO' | 'ENVIADO' | 'ENTREGUE' | 'CANCELADO';
export type TipoPagamento = 'PIX' | 'BOLETO' | 'TRANSFERENCIA';

export interface ItemPedido {
  id: UUID;
  anuncioId: UUID;
  tituloAnuncio: string;
  variacao: string | null;
  quantidade: number;
  valorUnidade: number;
  subtotal: number;
  /** cópia das informações do produto no momento da compra */
  informacoes: InformacoesProduto | null;
}
export interface Pagamento {
  id: UUID;
  tipoPagamento: TipoPagamento;
  valor: number;
  nomeReferencia: string | null;
  /** data do pagamento informada por quem registrou (yyyy-MM-dd) */
  dataReferencia: string | null;
  chavePix: string | null;
  boleto: string | null;
  codigoBanco: string | null;
  agencia: string | null;
  conta: string | null;
  /** comprovante anexado pelo comprador (caminho do arquivo) */
  comprovante: string | null;
  dataComprovante: string | null;
  /** quando foi registrado no sistema */
  dataCadastro: string;
}
export interface NotaFiscal {
  id: UUID;
  caminho: string;
  dataCadastro: string;
}
export interface Mensagem {
  id: UUID;
  mensagem: string;
  imagem: string | null;
  usuarioCriadorId: UUID;
  dataCadastro: string;
}

export interface OrdemDePedido {
  id: UUID;
  codigo: string;
  status: StatusOrdem;
  dataCadastro: string;
  previsaoDeEntrega: string | null;
  comprador: UsuarioResumo;
  vendedor: UsuarioResumo;
  valorTotal: number;
  /** soma das formas de pagamento cadastradas pelo vendedor */
  valorCadastrado: number;
  /** soma dos pagamentos com comprovante */
  valorPago: number;
  /** quanto falta o vendedor cadastrar em formas de pagamento */
  valorRestante: number;
  /** ainda falta pagar (algum pagamento sem comprovante), mesmo com o pedido enviado */
  pagamentoPendente: boolean;
  /** mensagens de incidente do outro lado que o usuário logado ainda não viu */
  mensagensNaoLidas: number;
  /** descrição e código de envio informados pelo vendedor; null se ainda não informou */
  entrega: Entrega | null;
  itens: ItemPedido[];
  pagamentos: Pagamento[];
  notasFiscais: NotaFiscal[];
  mensagensIncidente: Mensagem[];
}

export interface OrdemDePedidoResumo {
  id: UUID;
  codigo: string;
  status: StatusOrdem;
  dataCadastro: string;
  previsaoDeEntrega: string | null;
  compradorNome: string;
  vendedorNome: string;
  quantidadeItens: number;
  valorTotal: number;
  /** ainda falta pagar (mesmo com o pedido enviado ou entregue) */
  pagamentoPendente: boolean;
  /** mensagens de incidente do outro lado que o usuário logado ainda não viu */
  mensagensNaoLidas: number;
}

/** Detalhes da entrega informados pelo vendedor (os dois campos são opcionais). */
export interface Entrega {
  id: UUID;
  descricao: string | null;
  codigoRastreio: string | null;
  dataCadastro: string;
  dataAlteracao: string | null;
}
export interface EntregaInput {
  descricao: string | null;
  codigoRastreio: string | null;
}

/** Ordem com mensagens de incidente novas para o usuário logado. */
export interface AlertaMensagem {
  ordemId: UUID;
  codigo: string;
  papel: 'COMPRADOR' | 'VENDEDOR';
  outroParticipanteNome: string;
  naoLidas: number;
  ultimaMensagem: string | null;
  ultimaMensagemEm: string | null;
}

export interface NovaOrdemInput {
  compradorId: UUID;
  itens: { anuncioId: UUID; variacaoId?: UUID | null; quantidade: number; valorUnidade: number }[];
}

export interface ArquivoEnviado {
  nome: string;
  caminho: string;
  nomeOriginal: string;
  contentType: string;
  tamanho: number;
}

export interface PagamentoInput {
  tipoPagamento: TipoPagamento;
  valor: number;
  agencia?: string;
  codigoBanco?: string;
  conta?: string;
  nomeReferencia?: string;
  chavePix?: string;
  boleto?: string;
  /** yyyy-MM-dd; se vazio, a API usa a data de hoje */
  dataReferencia?: string;
}

/* ------------------------------------------------------------------ negociação (chat) */

export type TipoMensagemNegociacao = 'ABORDAGEM' | 'TEXTO' | 'ANUNCIO_ADICIONADO' | 'ANUNCIO_REMOVIDO';

export interface ParticipanteNegociacao {
  id: UUID;
  nome: string;
  email: string;
  telefone: string | null;
  imagem: string | null;
  cidade: string | null;
  uf: string | null;
  membroDesde: string;
}

export interface MensagemNegociacao {
  id: UUID;
  autorId: UUID;
  texto: string;
  tipo: TipoMensagemNegociacao;
  /** produto citado (abordagem, anúncio incluído/retirado) */
  anuncioId: UUID | null;
  dataCadastro: string;
}

export interface AnuncioNegociacao {
  id: UUID;
  titulo: string;
  imagem: string | null;
  precoMenor: number | null;
  quantidadeMinima: number;
  ativo: boolean;
}

export interface Negociacao {
  id: UUID;
  dataCadastro: string;
  /** anúncio de abordagem (o que iniciou a conversa) */
  anuncio: AnuncioNegociacao;
  /** todos os produtos da negociação, o de abordagem primeiro */
  anuncios: AnuncioNegociacao[];
  comprador: ParticipanteNegociacao;
  vendedor: ParticipanteNegociacao;
  mensagens: MensagemNegociacao[];
}

export interface NegociacaoResumo {
  id: UUID;
  /** papel do usuário logado nesta conversa */
  papel: 'COMPRADOR' | 'VENDEDOR';
  anuncioId: UUID;
  tituloAnuncio: string;
  imagemAnuncio: string | null;
  outroParticipanteId: UUID;
  outroParticipanteNome: string;
  ultimaMensagem: string | null;
  ultimaMensagemAutorId: UUID | null;
  ultimaMensagemEm: string;
  quantidadeMensagens: number;
  /** produtos na negociação */
  quantidadeAnuncios: number;
  /** mensagens da outra pessoa depois da última mensagem do usuário logado */
  mensagensSemResposta: number;
}
