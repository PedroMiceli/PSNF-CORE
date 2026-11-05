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
