import { useEffect, useRef, type ReactNode } from 'react';
import { createPortal } from 'react-dom';

/**
 * Painel lateral (drawer) à direita, por cima da página. Fecha com Esc, com o botão ✕ ou clicando no fundo.
 * No celular ocupa a tela inteira.
 */
export function SidePanel({ aberto, titulo, subtitulo, onFechar, rodape, children }: {
  aberto: boolean;
  titulo: string;
  subtitulo?: ReactNode;
  onFechar: () => void;
  rodape?: ReactNode;
  children: ReactNode;
}) {
  const painel = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!aberto) return;
    const anterior = document.activeElement as HTMLElement | null;
    const tecla = (e: KeyboardEvent) => e.key === 'Escape' && onFechar();
    document.addEventListener('keydown', tecla);
    const overflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden'; // a página de trás não rola
    painel.current?.focus();
    return () => {
      document.removeEventListener('keydown', tecla);
      document.body.style.overflow = overflow;
      anterior?.focus();
    };
  }, [aberto, onFechar]);

  if (!aberto) return null;

  return createPortal(
    <div className="fixed inset-0 z-40 flex justify-end">
      <div className="absolute inset-0 bg-slate-900/40" onClick={onFechar} aria-hidden="true" />
      <div ref={painel} role="dialog" aria-modal="true" aria-labelledby="sidepanel-titulo" tabIndex={-1}
        className="relative flex h-full w-full max-w-[600px] flex-col bg-white shadow-2xl outline-none">
        <header className="flex items-start gap-3 border-b border-borda px-5 py-4">
          <div className="min-w-0 flex-1">
            <h2 id="sidepanel-titulo" className="m-0 text-lg">{titulo}</h2>
            {subtitulo && <p className="mt-1 mb-0 text-sm text-suave">{subtitulo}</p>}
          </div>
          <button type="button" onClick={onFechar} aria-label="Fechar"
            className="-mr-1 cursor-pointer rounded-lg px-2 py-1 text-xl leading-none text-suave hover:bg-gray-100">✕</button>
        </header>
        <div className="flex-1 overflow-y-auto px-5 py-4">{children}</div>
        {rodape && <footer className="border-t border-borda bg-gray-50 px-5 py-3">{rodape}</footer>}
      </div>
    </div>,
    document.body,
  );
}
