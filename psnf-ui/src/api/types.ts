// Espelho dos DTOs de resposta/requisição da psnf-api

export type UUID = string;

export interface Endereco {
  id: UUID;
  logradouro: string;
  numero?: string | null;
  complemento?: string | null;
  bairro?: string | null;
  cidade: string;
  estado?: string | null;
  cep: string;
  uf?: string | null;
}
export type EnderecoInput = Omit<Endereco, 'id'>;

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
}

export interface AnuncioInput {
  titulo: string;
  descricao: string;
  categoriaId: UUID;
  valores: { valorUnidade: number; quantidadeMinima: number }[];
  imagens: { caminho: string; principal: boolean }[];
  variacoes: { variacao: string }[];
}

export type StatusOrdem = 'AGUARDANDO_PAGAMENTO' | 'PAGO' | 'ENVIADO' | 'ENTREGUE' | 'CANCELADO';
export type TipoPagamento = 'PIX' | 'BOLETO' | 'TRANSFERENCIA';

export interface ItemPedido {
  id: UUID;
  anuncioId: UUID;
  tituloAnuncio: string;
  variacao: string | null;
  quantidade: number;
  valorUnidade: number;
  subtotal: number;
}
export interface Pagamento {
  id: UUID;
  tipoPagamento: TipoPagamento;
  valor: number;
  nomeReferencia: string | null;
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
  valorPago: number;
  valorRestante: number;
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
}

export interface NovaOrdemInput {
  compradorId: UUID;
  itens: { anuncioId: UUID; variacaoId?: UUID | null; quantidade: number; valorUnidade: number }[];
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
}
