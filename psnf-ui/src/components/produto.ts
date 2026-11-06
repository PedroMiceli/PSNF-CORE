import type { CondicaoProduto, InformacoesProduto, ResponsavelGarantia } from '../api/types';

/** Tabela "origem da mercadoria" da NF-e. */
export const ORIGENS: Record<number, string> = {
  0: 'Nacional',
  1: 'Estrangeira — importação direta',
  2: 'Estrangeira — adquirida no mercado interno',
  3: 'Nacional, com mais de 40% de conteúdo importado',
  4: 'Nacional, produzida conforme processos produtivos básicos',
  5: 'Nacional, com até 40% de conteúdo importado',
  6: 'Estrangeira — importação direta, sem similar nacional (lista CAMEX)',
  7: 'Estrangeira — mercado interno, sem similar nacional (lista CAMEX)',
  8: 'Nacional, com mais de 70% de conteúdo importado',
};

export const CONDICAO_LABEL: Record<CondicaoProduto, string> = { NOVO: 'Novo', USADO: 'Usado', RECONDICIONADO: 'Recondicionado' };
export const RESPONSAVEL_LABEL: Record<ResponsavelGarantia, string> = { FABRICANTE: 'Fabricante', FORNECEDOR: 'Fornecedor' };

/** Unidades comerciais mais usadas na NF-e (o campo aceita qualquer sigla de até 6 letras). */
export const UNIDADES = ['UN', 'PC', 'CX', 'PCT', 'KIT', 'PAR', 'DZ', 'KG', 'G', 'L', 'ML', 'M', 'M2', 'M3', 'RL', 'FD', 'SC'];

/** O formulário trabalha com textos; números são convertidos ao salvar. */
export type InfoForm = Record<keyof InformacoesProduto, string>;

const CAMPOS_NUMERICOS = new Set<keyof InformacoesProduto>([
  'origem', 'quantidadePorEmbalagem', 'pesoLiquidoKg', 'pesoBrutoKg', 'comprimentoCm', 'larguraCm', 'alturaCm', 'garantiaMeses',
]);

export const INFO_VAZIA: InfoForm = {
  sku: '', gtin: '', marca: '', modelo: '', condicao: '', ncm: '', cest: '', origem: '', unidadeComercial: '',
  quantidadePorEmbalagem: '', pesoLiquidoKg: '', pesoBrutoKg: '', comprimentoCm: '', larguraCm: '', alturaCm: '',
  garantiaMeses: '', garantiaResponsavel: '', certificacoes: '',
};

export const infoParaForm = (i: InformacoesProduto | null | undefined): InfoForm => {
  const f = { ...INFO_VAZIA };
  if (i) (Object.keys(f) as (keyof InfoForm)[]).forEach((k) => { f[k] = i[k] == null ? '' : String(i[k]); });
  return f;
};

/** Campos vazios viram null; devolve null quando nada foi preenchido. */
export const formParaInfo = (f: InfoForm): InformacoesProduto | null => {
  const r: Record<string, unknown> = {};
  let algum = false;
  (Object.keys(f) as (keyof InfoForm)[]).forEach((k) => {
    const v = f[k].trim();
    if (!v) { r[k] = null; return; }
    algum = true;
    r[k] = CAMPOS_NUMERICOS.has(k) ? Number(v.replace(',', '.')) : v;
  });
  return algum ? (r as InformacoesProduto) : null;
};

export const contarPreenchidos = (f: InfoForm) => Object.values(f).filter((v) => v.trim()).length;

/* ------------------------------------------------------------------ validações (as mesmas da API) */

export const somenteDigitos = (v: string) => v.replace(/\D/g, '');

/** GTIN/EAN: 8, 12, 13 ou 14 dígitos com dígito verificador. Devolve a mensagem de erro ou undefined. */
export const validarGtin = (valor: string): string | undefined => {
  const v = valor.trim();
  if (!v || v.toUpperCase() === 'SEM GTIN') return undefined;
  const d = v.replace(/\s/g, '');
  if (!/^(\d{8}|\d{12}|\d{13}|\d{14})$/.test(d)) return 'Deve ter 8, 12, 13 ou 14 dígitos';
  let soma = 0;
  for (let i = d.length - 2, peso = 3; i >= 0; i--, peso = peso === 3 ? 1 : 3) soma += Number(d[i]) * peso;
  return (10 - (soma % 10)) % 10 === Number(d[d.length - 1]) ? undefined : 'Dígito verificador não confere';
};

export const mascaraNcm = (valor: string) => {
  const d = somenteDigitos(valor).slice(0, 8);
  return [d.slice(0, 4), d.slice(4, 6), d.slice(6)].filter(Boolean).join('.');
};

export const mascaraCest = (valor: string) => {
  const d = somenteDigitos(valor).slice(0, 7);
  return [d.slice(0, 2), d.slice(2, 5), d.slice(5)].filter(Boolean).join('.');
};

/** Erros de preenchimento detectados no navegador (antes de enviar). */
export const validarInfo = (f: InfoForm): Partial<Record<keyof InfoForm, string>> => {
  const e: Partial<Record<keyof InfoForm, string>> = {};
  const gtin = validarGtin(f.gtin);
  if (gtin) e.gtin = gtin;
  if (f.ncm && somenteDigitos(f.ncm).length !== 8) e.ncm = 'O NCM tem 8 dígitos (ex.: 7318.15.00)';
  if (f.cest && somenteDigitos(f.cest).length !== 7) e.cest = 'O CEST tem 7 dígitos (ex.: 01.003.00)';
  const liq = Number(f.pesoLiquidoKg.replace(',', '.'));
  const bru = Number(f.pesoBrutoKg.replace(',', '.'));
  if (f.pesoLiquidoKg && f.pesoBrutoKg && liq > bru) e.pesoLiquidoKg = 'Não pode ser maior que o peso bruto';
  return e;
};

/** Texto da garantia para o comprador: "12 meses (fabricante)". */
export const textoGarantia = (i: InformacoesProduto) => {
  if (i.garantiaMeses == null && !i.garantiaResponsavel) return null;
  const meses = i.garantiaMeses == null ? 'Garantia' : i.garantiaMeses === 0 ? 'Sem garantia' : `${i.garantiaMeses} ${i.garantiaMeses === 1 ? 'mês' : 'meses'}`;
  return i.garantiaResponsavel ? `${meses} (${RESPONSAVEL_LABEL[i.garantiaResponsavel].toLowerCase()})` : meses;
};

export const formatarNumero = (v: number | null | undefined, casas = 3) =>
  v == null ? '' : v.toLocaleString('pt-BR', { maximumFractionDigits: casas });
