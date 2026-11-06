import { useState } from 'react';
import type { ItemPedido, OrdemDePedido } from '../api/types';
import { formatarMoeda } from './format';
import { CONDICAO_LABEL, formatarNumero, textoGarantia } from './produto';
import { Botao, cx, dica, painel, tabela } from './ui';

const descricaoItem = (i: ItemPedido) => (i.variacao ? `${i.tituloAnuncio} - ${i.variacao}` : i.tituloAnuncio);

/** "Novo · 12 meses (fabricante) · SKU CAM-01": resumo do produto mostrado abaixo do item. */
export function resumoItem(i: ItemPedido) {
  const info = i.informacoes;
  if (!info) return null;
  const partes = [info.condicao && CONDICAO_LABEL[info.condicao], textoGarantia(info) && `Garantia: ${textoGarantia(info)}`,
    info.marca, info.sku && `SKU ${info.sku}`].filter(Boolean);
  return partes.length ? partes.join(' · ') : null;
}

const COLUNAS = ['Código', 'Descrição', 'NCM', 'CEST', 'Origem', 'Unid.', 'Qtd.', 'Valor unit.', 'Valor total', 'GTIN/EAN'] as const;

const linha = (i: ItemPedido) => {
  const info = i.informacoes ?? {};
  return [
    info.sku ?? '',
    descricaoItem(i),
    info.ncm ?? '',
    info.cest ?? '',
    info.origem != null ? String(info.origem) : '',
    info.unidadeComercial ?? 'UN',
    String(i.quantidade),
    i.valorUnidade,
    i.subtotal,
    info.gtin ?? 'SEM GTIN',
  ] as const;
};

/** Valores em formato brasileiro para colar em planilhas/emissores (vírgula decimal, sem R$). */
const numeroBr = (v: number, casas = 2) => v.toLocaleString('pt-BR', { minimumFractionDigits: casas, maximumFractionDigits: Math.max(casas, 4), useGrouping: false });

const textoTabela = (o: OrdemDePedido, separador: string) => {
  const escapar = (v: string) => (separador === ';' && /[";\n]/.test(v) ? `"${v.replace(/"/g, '""')}"` : v.replace(/[\t\n]/g, ' '));
  const linhas = o.itens.map((i) => linha(i).map((v) => (typeof v === 'number' ? numeroBr(v) : escapar(v))).join(separador));
  return [COLUNAS.join(separador), ...linhas].join('\n');
};

/** Soma peso × quantidade; null se algum item não tiver o peso. */
const pesoTotal = (o: OrdemDePedido, campo: 'pesoLiquidoKg' | 'pesoBrutoKg') => {
  let total = 0;
  for (const i of o.itens) {
    const p = i.informacoes?.[campo];
    if (p == null) return null;
    total += p * i.quantidade;
  }
  return total;
};

/**
 * Bloco com os dados dos itens no formato da NF-e, copiados do anúncio no momento da compra.
 * Serve para o vendedor emitir a nota em outra plataforma (copiar/colar ou CSV).
 */
export function DadosNotaFiscal({ ordem, aberto }: { ordem: OrdemDePedido; aberto: boolean }) {
  const [copiado, setCopiado] = useState(false);
  const faltaNcm = ordem.itens.filter((i) => !i.informacoes?.ncm).length;
  const liquido = pesoTotal(ordem, 'pesoLiquidoKg');
  const bruto = pesoTotal(ordem, 'pesoBrutoKg');

  const copiar = async () => {
    try {
      await navigator.clipboard.writeText(textoTabela(ordem, '\t'));
      setCopiado(true);
      setTimeout(() => setCopiado(false), 2500);
    } catch {
      window.alert('Não foi possível copiar. Use "Baixar CSV".');
    }
  };

  const baixar = () => {
    const blob = new Blob(['﻿' + textoTabela(ordem, ';')], { type: 'text/csv;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `itens-nf-${ordem.codigo}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  };

  return (
    <details open={aberto} className={cx(painel, 'group')}>
      <summary className="flex cursor-pointer list-none flex-wrap items-center gap-2 select-none [&::-webkit-details-marker]:hidden">
        <span className="text-suave transition-transform group-open:rotate-90">▸</span>
        <h3 className="m-0">Dados para emissão da NF</h3>
        {faltaNcm > 0 && (
          <span className="rounded-full bg-amber-100 px-2.5 py-0.5 text-xs font-bold text-amber-800">
            {faltaNcm === ordem.itens.length ? 'NCM não informado' : `${faltaNcm} item(ns) sem NCM`}
          </span>
        )}
      </summary>
      <p className={cx(dica, 'mt-3')}>
        Informações copiadas do anúncio no momento da compra, para facilitar a emissão da nota em outra plataforma.
        Confira antes de emitir: CFOP, CST e alíquotas dependem do regime tributário do emitente.
      </p>
      <div className={cx(tabela.caixa, 'mb-3')}>
        <table className={cx(tabela.tabela, 'text-sm')}>
          <thead>
            <tr>
              {COLUNAS.map((c, k) => <th key={c} className={cx(tabela.th, k >= 6 && k <= 8 && 'text-right')}>{c}</th>)}
            </tr>
          </thead>
          <tbody>
            {ordem.itens.map((i) => {
              const l = linha(i);
              return (
                <tr key={i.id}>
                  {l.map((v, k) => (
                    <td key={k} className={cx(tabela.td, k >= 6 && k <= 8 ? tabela.num : k !== 1 && 'font-mono whitespace-nowrap',
                      v === '' && 'text-suave')}>
                      {typeof v === 'number' ? formatarMoeda(v) : v || '—'}
                    </td>
                  ))}
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
      <div className="flex flex-wrap items-center gap-x-6 gap-y-2">
        <span className="text-sm"><span className="text-suave">Valor total dos produtos:</span> <strong>{formatarMoeda(ordem.valorTotal)}</strong></span>
        {liquido != null && <span className="text-sm"><span className="text-suave">Peso líquido:</span> <strong>{formatarNumero(liquido)} kg</strong></span>}
        {bruto != null && <span className="text-sm"><span className="text-suave">Peso bruto:</span> <strong>{formatarNumero(bruto)} kg</strong></span>}
        <div className="ml-auto flex flex-wrap gap-2">
          <Botao tamanho="pequeno" onClick={copiar}>{copiado ? '✔ Copiado' : '📋 Copiar tabela'}</Botao>
          <Botao tamanho="pequeno" onClick={baixar}>⬇ Baixar CSV</Botao>
        </div>
      </div>
    </details>
  );
}
