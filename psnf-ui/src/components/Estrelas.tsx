import { useState } from 'react';
import { cx } from './ui';

/** Nota em estrelas (aceita fração, ex.: 4,3 pinta 4 estrelas e 30% da quinta). */
export function Estrelas({ nota, tamanho = 'text-base', className }: { nota: number; tamanho?: string; className?: string }) {
  const n = Math.max(0, Math.min(5, nota));
  return (
    <span className={cx('relative inline-block leading-none whitespace-nowrap', tamanho, className)} role="img"
      aria-label={`${n.toLocaleString('pt-BR', { maximumFractionDigits: 1 })} de 5 estrelas`}>
      <span className="text-gray-300" aria-hidden="true">★★★★★</span>
      <span className="absolute inset-y-0 left-0 overflow-hidden text-amber-400" style={{ width: `${(n / 5) * 100}%` }} aria-hidden="true">★★★★★</span>
    </span>
  );
}

const ROTULOS = ['Péssimo', 'Ruim', 'Regular', 'Bom', 'Excelente'];

/** Escolha da nota (1 a 5) com o mouse ou o teclado. */
export function EscolherEstrelas({ valor, onChange }: { valor: number; onChange: (n: number) => void }) {
  const [hover, setHover] = useState(0);
  const mostrar = hover || valor;
  return (
    <div className="flex items-center gap-2">
      <div role="radiogroup" aria-label="Nota do produto" className="flex" onMouseLeave={() => setHover(0)}>
        {[1, 2, 3, 4, 5].map((n) => (
          <button key={n} type="button" role="radio" aria-checked={valor === n} aria-label={`${n} estrela${n > 1 ? 's' : ''} — ${ROTULOS[n - 1]}`}
            onClick={() => onChange(n)} onMouseEnter={() => setHover(n)}
            className={cx('cursor-pointer px-0.5 text-3xl leading-none transition-transform hover:scale-110',
              n <= mostrar ? 'text-amber-400' : 'text-gray-300')}>
            ★
          </button>
        ))}
      </div>
      <span className="text-sm text-suave">{mostrar ? ROTULOS[mostrar - 1] : 'Escolha de 1 a 5 estrelas'}</span>
    </div>
  );
}

/** "4,3" */
export const formatarNota = (n: number | null | undefined) =>
  n == null ? '—' : n.toLocaleString('pt-BR', { minimumFractionDigits: 1, maximumFractionDigits: 1 });
