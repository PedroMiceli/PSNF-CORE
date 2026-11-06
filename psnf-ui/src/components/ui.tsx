import { useState, type ButtonHTMLAttributes, type HTMLAttributes, type InputHTMLAttributes, type ReactNode } from 'react';
import { Link, type LinkProps } from 'react-router-dom';
import { urlArquivo, type ApiError } from '../api/client';
import type { StatusOrdem } from '../api/types';
import { digitosParaValor, formatarMoeda, STATUS_LABEL } from './format';

/** Junta classes ignorando valores vazios: cx('a', cond && 'b'). */
export const cx = (...classes: (string | false | null | undefined)[]) => classes.filter(Boolean).join(' ');

/* ------------------------------------------------------------------ classes compartilhadas */

/** Inputs, selects e textareas. */
export const controle =
  'w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-[0.92rem] outline-none ' +
  'focus:border-primaria focus:ring-2 focus:ring-blue-200 disabled:bg-gray-50 disabled:text-suave';

/** Tabelas. */
export const tabela = {
  /** caixa com borda que rola na horizontal no celular (a página nunca rola de lado) */
  caixa: 'overflow-x-auto rounded-card border border-borda bg-white',
  tabela: 'w-full border-collapse',
  th: 'bg-gray-50 px-3 py-2.5 text-left text-xs font-semibold tracking-wide text-suave uppercase',
  td: 'border-t border-borda px-3 py-2.5 align-middle',
  num: 'text-right whitespace-nowrap',
};

/** Linha de campos de formulário que quebra no celular. */
export const linhaForm = 'flex flex-wrap items-end gap-3';

/** Grupo de botões/ações. */
export const acoes = 'flex flex-wrap items-end gap-2.5';

/** Etiqueta arredondada (variações, filtros). */
export const chip = 'inline-block rounded-full border border-borda bg-white px-3.5 py-1.5 text-sm';

/** Texto de ajuda cinza. */
export const dica = 'mt-1 mb-3 text-sm text-suave';

/**
 * Campo de valor em reais: mostra "R$ 8,90" e devolve "8.90" (texto com ponto, pronto para Number()).
 * Os dígitos entram pela direita: para R$ 8,90 digite 8, 9, 0.
 */
export function InputMoeda({ valor, onValor, className, ...props }: Omit<InputHTMLAttributes<HTMLInputElement>, 'value' | 'onChange' | 'type'> & {
  valor: string; onValor: (valor: string) => void;
}) {
  const texto = valor === '' || isNaN(Number(valor)) ? '' : formatarMoeda(Number(valor));
  return (
    <input {...props} className={cx(controle, 'text-right', className)} inputMode="numeric" placeholder={props.placeholder ?? 'R$ 0,00'}
      value={texto} onChange={(e) => onValor(digitosParaValor(e.target.value))}
      // os dígitos entram pela direita: depois do clique o cursor vai para o fim, a não ser que o
      // usuário tenha selecionado o texto (para apagar tudo ou substituir)
      onMouseUp={(e) => { const el = e.currentTarget; if (el.selectionStart === el.selectionEnd) el.setSelectionRange(el.value.length, el.value.length); }} />
  );
}

/* ------------------------------------------------------------------ botões */

type Variante = 'padrao' | 'primario' | 'perigo';
type Tamanho = 'normal' | 'pequeno';

const BOTAO_BASE =
  'inline-flex cursor-pointer items-center justify-center gap-1.5 rounded-lg border font-semibold whitespace-nowrap ' +
  'transition-colors hover:no-underline disabled:pointer-events-none disabled:opacity-50';
const BOTAO_VARIANTE: Record<Variante, string> = {
  padrao: 'border-borda bg-white text-texto hover:bg-gray-100',
  primario: 'border-primaria bg-primaria text-white hover:bg-primaria-escura',
  perigo: 'border-red-200 bg-white text-vermelho hover:bg-red-50',
};
const BOTAO_TAMANHO: Record<Tamanho, string> = {
  normal: 'px-4 py-2 text-sm',
  pequeno: 'px-2.5 py-1 text-xs',
};

export const classesBotao = (variante: Variante = 'padrao', tamanho: Tamanho = 'normal') =>
  cx(BOTAO_BASE, BOTAO_VARIANTE[variante], BOTAO_TAMANHO[tamanho]);

type BotaoProps = ButtonHTMLAttributes<HTMLButtonElement> & { variante?: Variante; tamanho?: Tamanho };

export function Botao({ variante, tamanho, className, type = 'button', ...props }: BotaoProps) {
  return <button type={type} className={cx(classesBotao(variante, tamanho), className)} {...props} />;
}

/** Link com cara de botão. */
export function LinkBotao({ variante, tamanho, className, ...props }: LinkProps & { variante?: Variante; tamanho?: Tamanho }) {
  return <Link className={cx(classesBotao(variante, tamanho), className)} {...props} />;
}

/* ------------------------------------------------------------------ blocos */

/** Caixa branca com borda (use em <form>, <section>...; para <div> use <Painel>). */
export const painel = 'mb-4 rounded-card border border-borda bg-white p-5';

export function Painel({ className, children, ...props }: HTMLAttributes<HTMLDivElement>) {
  return <div className={cx(painel, className)} {...props}>{children}</div>;
}

/** Título da página à esquerda e ações à direita. */
export function TituloPagina({ children }: { children: ReactNode }) {
  return <div className="mb-4 flex flex-wrap items-center justify-between gap-4 [&>h1]:m-0">{children}</div>;
}

export function Voltar({ to, children }: { to: string; children: ReactNode }) {
  return <Link to={to} className="mb-3 inline-block text-sm">← {children}</Link>;
}

const STATUS_COR: Record<StatusOrdem, string> = {
  AGUARDANDO_PAGAMENTO: 'bg-amber-100 text-amber-800',
  PAGO: 'bg-green-100 text-green-800',
  ENVIADO: 'bg-blue-100 text-blue-800',
  ENTREGUE: 'bg-indigo-100 text-indigo-800',
  CANCELADO: 'bg-red-100 text-red-800',
};

export function Badge({ className, children }: { className?: string; children: ReactNode }) {
  return (
    <span className={cx('inline-block rounded-full px-2.5 py-0.5 align-middle text-xs font-bold', className ?? 'bg-gray-200 text-gray-700')}>
      {children}
    </span>
  );
}

export function StatusBadge({ status }: { status: StatusOrdem }) {
  return <Badge className={STATUS_COR[status]}>{STATUS_LABEL[status] ?? status}</Badge>;
}

export const BADGE_ATIVO = STATUS_COR.PAGO;
export const BADGE_INATIVO = STATUS_COR.CANCELADO;

export function Vazio({ children }: { children: ReactNode }) {
  return <div className="rounded-card border border-dashed border-borda bg-white p-10 text-center text-suave">{children}</div>;
}

export function Carregando({ texto = 'Carregando...' }: { texto?: string }) {
  return <Vazio>{texto}</Vazio>;
}

export function ErroBox({ erro }: { erro: ApiError | null | undefined }) {
  if (!erro) return null;
  return (
    <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-vermelho" role="alert">
      <strong>{erro.status ? `Erro ${erro.status}: ` : ''}</strong>
      {erro.message}
      {erro.campos && (
        <ul className="mt-1.5 list-disc pl-5">
          {Object.entries(erro.campos).map(([campo, msg]) => (
            <li key={campo}>
              <code>{campo}</code>: {msg}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

/** Rótulo + controle + dica/erro. Use a classe `controle` no input/select; com erro ele fica vermelho. */
export function Campo({ label, erro, children, dica: textoDica, className }: {
  label: string; erro?: string; dica?: string; className?: string; children: ReactNode;
}) {
  return (
    <label className={cx(
      'mb-3 flex min-w-[140px] flex-1 flex-col gap-1',
      erro && '[&_:is(input,select,textarea)]:border-vermelho',
      className,
    )}>
      <span className="text-xs font-semibold text-gray-700">{label}</span>
      {children}
      {textoDica && !erro && <span className="text-xs text-suave">{textoDica}</span>}
      {erro && <span className="text-xs text-vermelho">{erro}</span>}
    </label>
  );
}

/** Mostra a imagem do anúncio; se o caminho não for uma URL válida, mostra um placeholder com o nome. */
export function ImagemAnuncio({ caminho, alt, className, compacta }: {
  caminho?: string | null; alt: string; className?: string;
  /** placeholder só com o ícone (miniaturas) */
  compacta?: boolean;
}) {
  const [falhou, setFalhou] = useState(false);
  const ehUrl = !!caminho && /^(https?:|data:|\/)/.test(caminho);
  if (!caminho || !ehUrl || falhou) {
    return (
      <div
        className={cx('flex flex-col items-center justify-center gap-1 overflow-hidden bg-linear-135 from-indigo-100 to-slate-100 text-suave', className)}
        title={caminho ?? ''}
      >
        <span className={compacta ? 'text-base' : 'text-3xl'}>📦</span>
        {!compacta && <small className="max-w-[90%] truncate text-[0.7rem]">{caminho || 'sem imagem'}</small>}
      </div>
    );
  }
  return <img className={cx('object-cover', className)} src={urlArquivo(caminho)} alt={alt} onError={() => setFalhou(true)} />;
}
