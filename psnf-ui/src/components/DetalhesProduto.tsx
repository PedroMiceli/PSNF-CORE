import type { Anuncio, InformacoesProduto } from '../api/types';
import { CONDICAO_LABEL, formatarNumero, ORIGENS, textoGarantia } from './produto';
import { tabela } from './ui';

/** Linhas "rótulo: valor" com apenas o que foi preenchido. */
export function linhasInformacoes(i: InformacoesProduto | null | undefined): [string, string][] {
  if (!i) return [];
  const medidas = [i.comprimentoCm, i.larguraCm, i.alturaCm].every((v) => v != null)
    ? `${formatarNumero(i.comprimentoCm, 1)} × ${formatarNumero(i.larguraCm, 1)} × ${formatarNumero(i.alturaCm, 1)} cm (C × L × A)`
    : [i.comprimentoCm && `C ${formatarNumero(i.comprimentoCm, 1)} cm`, i.larguraCm && `L ${formatarNumero(i.larguraCm, 1)} cm`,
      i.alturaCm && `A ${formatarNumero(i.alturaCm, 1)} cm`].filter(Boolean).join(' · ');
  const linhas: [string, string | null | undefined | false][] = [
    ['Marca', i.marca],
    ['Modelo', i.modelo],
    ['Condição', i.condicao && CONDICAO_LABEL[i.condicao]],
    ['Garantia', textoGarantia(i)],
    ['SKU', i.sku],
    ['GTIN / EAN', i.gtin],
    ['NCM', i.ncm],
    ['CEST', i.cest],
    ['Origem', i.origem != null && `${i.origem} — ${ORIGENS[i.origem] ?? ''}`],
    ['Unidade', i.unidadeComercial && (i.quantidadePorEmbalagem && i.quantidadePorEmbalagem > 1
      ? `${i.unidadeComercial} (${i.quantidadePorEmbalagem} itens por embalagem)` : i.unidadeComercial)],
    ['Peso líquido', i.pesoLiquidoKg != null && `${formatarNumero(i.pesoLiquidoKg)} kg`],
    ['Peso bruto', i.pesoBrutoKg != null && `${formatarNumero(i.pesoBrutoKg)} kg`],
    ['Dimensões', medidas],
    ['Certificações', i.certificacoes],
  ];
  return linhas.filter((l): l is [string, string] => !!l[1]);
}

/** Informações do produto e ficha técnica na página do anúncio (some quando nada foi preenchido). */
export function DetalhesProduto({ anuncio }: { anuncio: Anuncio }) {
  const linhas = linhasInformacoes(anuncio.informacoes);
  const ficha = anuncio.caracteristicas ?? [];
  if (linhas.length === 0 && ficha.length === 0) return null;
  return (
    <>
      {linhas.length > 0 && (
        <>
          <h4>Informações do produto</h4>
          <dl className="mb-5 grid grid-cols-[max-content_minmax(0,1fr)] gap-x-4 gap-y-1.5 text-sm">
            {linhas.map(([r, v]) => (
              <div key={r} className="contents">
                <dt className="text-suave">{r}</dt>
                <dd className="m-0 break-words whitespace-pre-wrap">{v}</dd>
              </div>
            ))}
          </dl>
        </>
      )}
      {ficha.length > 0 && (
        <>
          <h4>Ficha técnica</h4>
          <div className={tabela.caixa}>
            <table className={tabela.tabela}>
              <tbody>
                {ficha.map((c, i) => (
                  <tr key={i} className="even:bg-gray-50">
                    <th scope="row" className="w-2/5 px-3 py-2 text-left text-sm font-semibold text-gray-700">{c.nome}</th>
                    <td className="px-3 py-2 text-sm break-words">{c.valor}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </>
  );
}
