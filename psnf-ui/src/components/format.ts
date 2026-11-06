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
