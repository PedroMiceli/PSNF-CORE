const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

export const formatarMoeda = (v: number | null | undefined) => (v == null ? '—' : moeda.format(v));

export const formatarData = (iso: string | null | undefined) => {
  if (!iso) return '—';
  const d = new Date(iso.length === 10 ? `${iso}T00:00:00` : iso);
  return iso.length === 10 ? d.toLocaleDateString('pt-BR') : d.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
};

export const STATUS_LABEL: Record<string, string> = {
  AGUARDANDO_PAGAMENTO: 'Aguardando pagamento',
  PAGO: 'Pago',
  ENVIADO: 'Enviado',
  ENTREGUE: 'Entregue',
  CANCELADO: 'Cancelado',
};
