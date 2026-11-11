import { useEffect, useRef, type ReactNode } from 'react';
import { createPortal } from 'react-dom';

/** Janela modal centralizada. Fecha com Esc, com o ✕ ou clicando no fundo. */
export function Dialogo({ aberto, titulo, onFechar, rodape, children, largura = 'max-w-[480px]' }: {
  aberto: boolean; titulo: ReactNode; onFechar: () => void; rodape?: ReactNode; children: ReactNode;
  /** classe de largura máxima (padrão max-w-[480px]) */
  largura?: string;
}) {
  const caixa = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!aberto) return;
    const anterior = document.activeElement as HTMLElement | null;
    const tecla = (e: KeyboardEvent) => e.key === 'Escape' && onFechar();
    document.addEventListener('keydown', tecla);
    caixa.current?.focus();
    return () => { document.removeEventListener('keydown', tecla); anterior?.focus(); };
  }, [aberto, onFechar]);

  if (!aberto) return null;
  return createPortal(
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-slate-900/40" onClick={onFechar} aria-hidden="true" />
      <div ref={caixa} role="dialog" aria-modal="true" aria-labelledby="dialogo-titulo" tabIndex={-1}
        className={`relative flex max-h-[calc(100vh-2rem)] w-full ${largura} flex-col overflow-hidden rounded-card bg-white shadow-2xl outline-none`}>
        <header className="flex items-center gap-3 border-b border-borda px-5 py-3.5">
          <h2 id="dialogo-titulo" className="m-0 flex-1 text-lg">{titulo}</h2>
          <button type="button" onClick={onFechar} aria-label="Fechar"
            className="cursor-pointer rounded-lg px-2 py-1 text-xl leading-none text-suave hover:bg-gray-100">✕</button>
        </header>
        <div className="overflow-y-auto px-5 py-4">{children}</div>
        {rodape && <footer className="border-t border-borda bg-gray-50 px-5 py-3">{rodape}</footer>}
      </div>
    </div>,
    document.body,
  );
}
