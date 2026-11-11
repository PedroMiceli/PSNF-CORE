const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

export const formatarMoeda = (v: number | null | undefined) => (v == null ? '—' : moeda.format(v));

export const formatarData = (iso: string | null | undefined) => {
  if (!iso) return '—';
  const d = new Date(iso.length === 10 ? `${iso}T00:00:00` : iso);
  return iso.length === 10 ? d.toLocaleDateString('pt-BR') : d.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
};

/** Siglas das 27 unidades federativas (as mesmas validadas pela API). */
export const UFS = [
  'AC', 'AL', 'AM', 'AP', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MG', 'MS', 'MT', 'PA',
  'PB', 'PE', 'PI', 'PR', 'RJ', 'RN', 'RO', 'RR', 'RS', 'SC', 'SE', 'SP', 'TO',
] as const;

export const STATUS_LABEL: Record<string, string> = {
  AGUARDANDO_CONFIRMACAO: 'Aguardando confirmação',
  ACEITO: 'Aceito',
  AGUARDANDO_PAGAMENTO: 'Aguardando pagamento',
  PAGO: 'Pago',
  ENVIADO: 'Enviado',
  ENTREGUE: 'Entregue',
  CANCELADO: 'Cancelado',
};

/* ------------------------------------------------------------------ máscaras */

/** "11999990001" -> "(11) 99999-0001"; fixo com 10 dígitos -> "(11) 3333-0001". Aceita digitação parcial. */
export const mascaraTelefone = (valor: string) => {
  const d = valor.replace(/\D/g, '').slice(0, 11);
  if (d.length <= 2) return d.length ? `(${d}` : '';
  const ddd = `(${d.slice(0, 2)}) `;
  const resto = d.slice(2);
  const corte = d.length === 11 ? 5 : 4; // celular tem 9 dígitos depois do DDD
  return resto.length <= corte ? ddd + resto : `${ddd}${resto.slice(0, corte)}-${resto.slice(corte)}`;
};

/**
 * CNPJ: "11222333000181" -> "11.222.333/0001-81". Aceita o CNPJ alfanumérico (desde 2026): letras nos 12
 * primeiros caracteres, sempre em maiúsculas; os 2 últimos são números. Aceita digitação parcial.
 */
export const mascaraCnpj = (valor: string) => {
  const c = valor.toUpperCase().replace(/[^0-9A-Z]/g, '').slice(0, 14);
  const partes = [c.slice(0, 2), c.slice(2, 5), c.slice(5, 8), c.slice(8, 12), c.slice(12, 14)];
  let r = partes[0];
  if (partes[1]) r += '.' + partes[1];
  if (partes[2]) r += '.' + partes[2];
  if (partes[3]) r += '/' + partes[3];
  if (partes[4]) r += '-' + partes[4];
  return r;
};

/** Mesmo cálculo da API (módulo 11, valor de cada caractere = código ASCII - 48). Vazio conta como válido (é opcional). */
export const cnpjValido = (valor: string) => {
  const c = valor.toUpperCase().replace(/[^0-9A-Z]/g, '');
  if (!c) return true;
  if (!/^[0-9A-Z]{12}[0-9]{2}$/.test(c) || new Set(c).size === 1) return false;
  const dv = (n: number) => {
    const pesos = n === 12 ? [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2] : [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];
    const resto = pesos.reduce((s, p, i) => s + (c.charCodeAt(i) - 48) * p, 0) % 11;
    return resto < 2 ? 0 : 11 - resto;
  };
  return dv(12) === Number(c[12]) && dv(13) === Number(c[13]);
};

/** Telefone completo tem DDD + 8 (fixo) ou 9 (celular) dígitos. */
export const telefoneValido = (valor: string) => [10, 11].includes(valor.replace(/\D/g, '').length);

/**
 * Máscara de dinheiro digitada da direita para a esquerda (como em app de banco):
 * "8" -> R$ 0,08, "89" -> R$ 0,89, "890" -> R$ 8,90. Devolve o valor com ponto ("8.90") ou '' se vazio.
 */
export const digitosParaValor = (texto: string) => {
  const d = texto.replace(/\D/g, '').slice(0, 13);
  return d ? (Number(d) / 100).toFixed(2) : '';
};
