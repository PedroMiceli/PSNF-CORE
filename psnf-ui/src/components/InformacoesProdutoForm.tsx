import { useEffect, useRef, useState, type ReactNode } from 'react';
import { ApiError } from '../api/client';
import { api } from '../api/endpoints';
import type { Caracteristica, Ncm } from '../api/types';
import {
  CONDICAO_LABEL, mascaraCest, mascaraNcm, ORIGENS, RESPONSAVEL_LABEL, somenteDigitos, UNIDADES, validarGtin, validarInfo,
  type InfoForm,
} from './produto';
import { Botao, Campo, controle, cx, dica, linhaForm } from './ui';

export interface VariacaoForm { variacao: string; sku: string; gtin: string }

interface Props {
  info: InfoForm;
  onInfo: (campo: keyof InfoForm, valor: string) => void;
  caracteristicas: Caracteristica[];
  onCaracteristicas: (lista: Caracteristica[]) => void;
  variacoes: VariacaoForm[];
  onVariacoes: (lista: VariacaoForm[]) => void;
  /** erros por campo vindos da API (informacoes.ncm, caracteristicas[0].nome, variacoes[1].gtin...) */
  errosApi?: Record<string, string>;
  /** mostra os erros detectados no navegador (depois de tentar salvar) */
  mostrarErros: boolean;
}

const SUGESTOES_FICHA = ['Material', 'Cor', 'Voltagem', 'Potência', 'Capacidade', 'Composição', 'Acabamento', 'Itens inclusos'];

/** Conteúdo do painel "Informe mais sobre o produto": todos os campos são opcionais. */
export function InformacoesProdutoForm({ info, onInfo, caracteristicas, onCaracteristicas, variacoes, onVariacoes, errosApi, mostrarErros }: Props) {
  const errosLocais = mostrarErros ? validarInfo(info) : {};
  const erro = (k: keyof InfoForm) => errosLocais[k] ?? errosApi?.[`informacoes.${k}`];

  const texto = (k: keyof InfoForm, label: string, extra: { placeholder?: string; className?: string; dica?: string; maxLength?: number; maiusculas?: boolean } = {}) => (
    <Campo label={label} erro={erro(k)} dica={extra.dica} className={extra.className}>
      <input className={controle} value={info[k]} placeholder={extra.placeholder} maxLength={extra.maxLength}
        autoCapitalize={extra.maiusculas ? 'characters' : undefined}
        onChange={(e) => onInfo(k, extra.maiusculas ? e.target.value.toUpperCase() : e.target.value)} />
    </Campo>
  );
  const numero = (k: keyof InfoForm, label: string, extra: { step?: string; className?: string; placeholder?: string } = {}) => (
    <Campo label={label} erro={erro(k)} className={extra.className ?? 'min-w-[110px]'}>
      <input className={controle} type="number" inputMode="decimal" min={0} step={extra.step ?? 'any'} value={info[k]}
        placeholder={extra.placeholder} onChange={(e) => onInfo(k, e.target.value)} />
    </Campo>
  );

  const preenchidos = (...ks: (keyof InfoForm)[]) => ks.filter((k) => info[k].trim()).length;

  return (
    <div>
      <p className={cx(dica, 'mt-0')}>
        Tudo aqui é <strong>opcional</strong>. O que você informar aparece no anúncio e é copiado para a ordem de pedido,
        ajudando a emitir a nota fiscal e deixando claro para o comprador o que está sendo vendido.
      </p>

      <Secao titulo="Identificação" contagem={preenchidos('sku', 'gtin', 'marca', 'modelo', 'condicao')} aberta>
        <div className={linhaForm}>
          {texto('sku', 'SKU (seu código)', { placeholder: 'CAR-MET-01', maxLength: 60, maiusculas: true, dica: 'Vai como "código do produto" na NF' })}
          {texto('gtin', 'GTIN / EAN (código de barras)', { placeholder: '7891234567895', dica: 'Deixe vazio se não tiver ("SEM GTIN")' })}
        </div>
        <div className={linhaForm}>
          {texto('marca', 'Marca', { maxLength: 120 })}
          {texto('modelo', 'Modelo', { maxLength: 120 })}
          <Campo label="Condição" erro={erro('condicao')} className="min-w-[150px]">
            <select className={controle} value={info.condicao} onChange={(e) => onInfo('condicao', e.target.value)}>
              <option value="">Não informar</option>
              {Object.entries(CONDICAO_LABEL).map(([v, l]) => <option key={v} value={v}>{l}</option>)}
            </select>
          </Campo>
        </div>
      </Secao>

      <Secao titulo="Fiscal" subtitulo="classificação usada na NF-e"
        contagem={preenchidos('ncm', 'cest', 'origem', 'unidadeComercial', 'quantidadePorEmbalagem')}>
        <NcmCampo valor={info.ncm} erro={erro('ncm')} onChange={(v) => onInfo('ncm', v)} />
        <div className={linhaForm}>
          <Campo label="CEST (se houver ST)" erro={erro('cest')} className="min-w-[150px]">
            <input className={controle} value={info.cest} inputMode="numeric" placeholder="00.000.00"
              onChange={(e) => onInfo('cest', mascaraCest(e.target.value))} />
          </Campo>
          <Campo label="Unidade comercial" erro={erro('unidadeComercial')} className="min-w-[120px]">
            <input className={controle} value={info.unidadeComercial} list="unidades-comerciais" maxLength={6} placeholder="UN"
              onChange={(e) => onInfo('unidadeComercial', e.target.value.toUpperCase())} />
            <datalist id="unidades-comerciais">{UNIDADES.map((u) => <option key={u} value={u} />)}</datalist>
          </Campo>
          {numero('quantidadePorEmbalagem', 'Itens por embalagem', { step: '1', placeholder: '1' })}
        </div>
        <Campo label="Origem da mercadoria" erro={erro('origem')}>
          <select className={controle} value={info.origem} onChange={(e) => onInfo('origem', e.target.value)}>
            <option value="">Não informar</option>
            {Object.entries(ORIGENS).map(([v, l]) => <option key={v} value={v}>{v} — {l}</option>)}
          </select>
        </Campo>
      </Secao>

      <Secao titulo="Logística" subtitulo="por unidade, com embalagem"
        contagem={preenchidos('pesoLiquidoKg', 'pesoBrutoKg', 'comprimentoCm', 'larguraCm', 'alturaCm')}>
        <div className={linhaForm}>
          {numero('pesoLiquidoKg', 'Peso líquido (kg)', { placeholder: '0,000' })}
          {numero('pesoBrutoKg', 'Peso bruto (kg)', { placeholder: '0,000' })}
        </div>
        <div className={linhaForm}>
          {numero('comprimentoCm', 'Comprimento (cm)')}
          {numero('larguraCm', 'Largura (cm)')}
          {numero('alturaCm', 'Altura (cm)')}
        </div>
      </Secao>

      <Secao titulo="Garantia e conformidade" contagem={preenchidos('garantiaMeses', 'garantiaResponsavel', 'certificacoes')}>
        <div className={linhaForm}>
          {numero('garantiaMeses', 'Garantia (meses)', { step: '1', placeholder: '12' })}
          <Campo label="Garantia dada por" erro={erro('garantiaResponsavel')} className="min-w-[150px]">
            <select className={controle} value={info.garantiaResponsavel} onChange={(e) => onInfo('garantiaResponsavel', e.target.value)}>
              <option value="">Não informar</option>
              {Object.entries(RESPONSAVEL_LABEL).map(([v, l]) => <option key={v} value={v}>{l}</option>)}
            </select>
          </Campo>
        </div>
        <Campo label="Certificações e registros" erro={erro('certificacoes')} dica="INMETRO, ANVISA, CA (EPI), ANATEL... com os números de registro">
          <textarea className={controle} rows={3} maxLength={1000} value={info.certificacoes}
            onChange={(e) => onInfo('certificacoes', e.target.value)} />
        </Campo>
      </Secao>

      <Secao titulo="Ficha técnica" contagem={caracteristicas.length}>
        <FichaTecnica lista={caracteristicas} onChange={onCaracteristicas} errosApi={errosApi} />
      </Secao>

      {variacoes.length > 0 && (
        <Secao titulo="Códigos por variação" subtitulo="quando cada variação tem SKU/código de barras próprio"
          contagem={variacoes.filter((v) => v.sku.trim() || v.gtin.trim()).length}>
          <p className={cx(dica, 'mt-0')}>Se ficar vazio, a variação usa o SKU e o GTIN do produto.</p>
          {variacoes.map((v, i) => {
            const set = (campo: 'sku' | 'gtin', valor: string) =>
              onVariacoes(variacoes.map((x, j) => (j === i ? { ...x, [campo]: valor } : x)));
            const erroGtin = (mostrarErros ? validarGtin(v.gtin) : undefined) ?? errosApi?.[`variacoes[${i}].gtin`];
            return (
              <div key={i} className={linhaForm}>
                <p className="mb-3 min-w-[110px] flex-1 self-center font-semibold break-words">{v.variacao || `Variação ${i + 1}`}</p>
                <Campo label="SKU" erro={errosApi?.[`variacoes[${i}].sku`]}>
                  <input className={controle} value={v.sku} maxLength={60} placeholder={info.sku || undefined} autoCapitalize="characters"
                    onChange={(e) => set('sku', e.target.value.toUpperCase())} />
                </Campo>
                <Campo label="GTIN / EAN" erro={erroGtin}>
                  <input className={controle} value={v.gtin} inputMode="numeric" placeholder={info.gtin || undefined}
                    onChange={(e) => set('gtin', e.target.value)} />
                </Campo>
              </div>
            );
          })}
        </Secao>
      )}
    </div>
  );
}

function Secao({ titulo, subtitulo, contagem, aberta, children }: {
  titulo: string; subtitulo?: string; contagem: number; aberta?: boolean; children: ReactNode;
}) {
  // começa aberta se for a primeira ou se já tiver algo preenchido
  const [inicial] = useState(aberta || contagem > 0);
  return (
    <details open={inicial} className="group mb-3 rounded-card border border-borda">
      <summary className="flex cursor-pointer list-none items-center gap-2 px-4 py-3 select-none [&::-webkit-details-marker]:hidden">
        <span className="text-suave transition-transform group-open:rotate-90">▸</span>
        <strong>{titulo}</strong>
        {subtitulo && <small className="text-suave max-sm:hidden">· {subtitulo}</small>}
        {contagem > 0 && (
          <span className="ml-auto rounded-full bg-blue-100 px-2 py-0.5 text-xs font-bold text-primaria">{contagem}</span>
        )}
      </summary>
      <div className="border-t border-borda px-4 pt-3">{children}</div>
    </details>
  );
}

/** NCM com busca na tabela oficial: digite parte da descrição ("parafuso") ou do código. */
function NcmCampo({ valor, erro, onChange }: { valor: string; erro?: string; onChange: (v: string) => void }) {
  const [resultados, setResultados] = useState<Ncm[]>([]);
  const [aberto, setAberto] = useState(false);
  const [destaque, setDestaque] = useState(0);
  const [buscando, setBuscando] = useState(false);
  const [aviso, setAviso] = useState<string | null>(null);
  const [descricao, setDescricao] = useState<string | null>(null);
  const ultimaBusca = useRef('');

  const digitos = somenteDigitos(valor);
  const codigoCompleto = /^[\d.\s]*$/.test(valor) && digitos.length === 8;

  useEffect(() => {
    const termo = valor.trim();
    setAviso(null);
    if (codigoCompleto) {
      // código completo: confere na tabela e mostra a descrição
      setAberto(false);
      setDescricao(null);
      const alvo = digitos;
      ultimaBusca.current = alvo;
      api.consultarNcm(alvo)
        .then((n) => ultimaBusca.current === alvo && setDescricao(n.descricao))
        .catch((e) => ultimaBusca.current === alvo && setAviso(e instanceof ApiError && e.status === 404
          ? 'Este código não está na tabela NCM vigente. Confira o número.'
          : 'Não foi possível conferir o NCM agora (o código será salvo assim mesmo).'));
      return;
    }
    setDescricao(null);
    if (termo.length < 3) { setResultados([]); setAberto(false); return; }
    const t = setTimeout(() => {
      ultimaBusca.current = termo;
      setBuscando(true);
      api.pesquisarNcm(termo)
        .then((r) => { if (ultimaBusca.current !== termo) return; setResultados(r); setDestaque(0); setAberto(true); })
        .catch((e) => ultimaBusca.current === termo && setAviso(e instanceof ApiError ? e.message : 'Falha ao buscar o NCM'))
        .finally(() => ultimaBusca.current === termo && setBuscando(false));
    }, 400);
    return () => clearTimeout(t);
  }, [valor]);

  const escolher = (n: Ncm) => {
    ultimaBusca.current = '';
    onChange(n.codigo);
    setAberto(false);
  };

  const alterar = (v: string) => onChange(/^[\d.\s]*$/.test(v) ? mascaraNcm(v) : v);

  return (
    <Campo label="NCM" erro={erro}
      dica={buscando ? '⏳ Buscando na tabela NCM...' : descricao ? undefined : 'Digite o código ou parte da descrição (ex.: "parafuso") para buscar'}>
      <div className="relative">
        <input className={controle} value={valor} placeholder="0000.00.00 ou descrição" autoComplete="off"
          role="combobox" aria-expanded={aberto} aria-controls="ncm-opcoes" aria-autocomplete="list"
          onChange={(e) => alterar(e.target.value)}
          onFocus={() => resultados.length > 0 && !codigoCompleto && setAberto(true)}
          onBlur={() => setTimeout(() => setAberto(false), 150)}
          onKeyDown={(e) => {
            if (!aberto || resultados.length === 0) return;
            if (e.key === 'ArrowDown') { e.preventDefault(); setDestaque((d) => Math.min(d + 1, resultados.length - 1)); }
            else if (e.key === 'ArrowUp') { e.preventDefault(); setDestaque((d) => Math.max(d - 1, 0)); }
            else if (e.key === 'Enter') { e.preventDefault(); escolher(resultados[destaque]); }
            else if (e.key === 'Escape') { e.stopPropagation(); setAberto(false); }
          }} />
        {aberto && (
          <ul id="ncm-opcoes" role="listbox"
            className="absolute inset-x-0 top-full z-10 mt-1 max-h-72 overflow-y-auto rounded-lg border border-borda bg-white shadow-lg">
            {resultados.length === 0 && <li className="px-3 py-2.5 text-sm text-suave">Nenhum NCM encontrado para "{valor}".</li>}
            {resultados.map((n, i) => (
              <li key={n.codigo} role="option" aria-selected={i === destaque}
                onMouseDown={(e) => { e.preventDefault(); escolher(n); }} onMouseEnter={() => setDestaque(i)}
                className={cx('cursor-pointer border-b border-borda px-3 py-2 text-sm last:border-0', i === destaque && 'bg-blue-50')}>
                <strong className="mr-2 font-mono text-primaria">{n.codigo}</strong>{n.descricao}
              </li>
            ))}
          </ul>
        )}
      </div>
      {descricao && <span className="text-xs text-verde">✔ {descricao}</span>}
      {aviso && <span className="text-xs text-laranja">⚠ {aviso}</span>}
    </Campo>
  );
}

function FichaTecnica({ lista, onChange, errosApi }: {
  lista: Caracteristica[]; onChange: (l: Caracteristica[]) => void; errosApi?: Record<string, string>;
}) {
  const set = (i: number, campo: keyof Caracteristica, valor: string) =>
    onChange(lista.map((c, j) => (j === i ? { ...c, [campo]: valor } : c)));
  const usadas = new Set(lista.map((c) => c.nome.trim().toLowerCase()));
  return (
    <div>
      <p className={cx(dica, 'mt-0')}>Características em formato nome/valor, ex.: Material → Aço carbono.</p>
      {lista.map((c, i) => (
        <div key={i} className={linhaForm}>
          <Campo label="Nome" erro={errosApi?.[`caracteristicas[${i}].nome`]} className="basis-32">
            <input className={controle} value={c.nome} maxLength={60} onChange={(e) => set(i, 'nome', e.target.value)} />
          </Campo>
          <Campo label="Valor" erro={errosApi?.[`caracteristicas[${i}].valor`]} className="grow-2 basis-48">
            <input className={controle} value={c.valor} maxLength={300} onChange={(e) => set(i, 'valor', e.target.value)} />
          </Campo>
          <Botao variante="perigo" tamanho="pequeno" className="mb-3" aria-label={`Remover ${c.nome || 'característica'}`}
            onClick={() => onChange(lista.filter((_, j) => j !== i))}>✕</Botao>
        </div>
      ))}
      <div className="mb-3 flex flex-wrap items-center gap-1.5">
        <Botao tamanho="pequeno" disabled={lista.length >= 50} onClick={() => onChange([...lista, { nome: '', valor: '' }])}>
          + Característica
        </Botao>
        {SUGESTOES_FICHA.filter((s) => !usadas.has(s.toLowerCase())).slice(0, 5).map((s) => (
          <button key={s} type="button" onClick={() => onChange([...lista, { nome: s, valor: '' }])}
            className="cursor-pointer rounded-full border border-dashed border-borda px-2.5 py-0.5 text-xs text-suave hover:border-primaria hover:text-primaria">
            + {s}
          </button>
        ))}
      </div>
    </div>
  );
}
