import type { ReactNode } from 'react';

/** Moldura das telas sem login (entrar, cadastro, recuperação de senha). */
export function AuthLayout({ titulo, subtitulo, children, rodape }: {
  titulo: string; subtitulo?: ReactNode; children: ReactNode; rodape?: ReactNode;
}) {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-fundo px-4 py-10">
      <div className="mb-6 text-3xl font-extrabold tracking-wider text-topo">PSNF</div>
      <div className="w-full max-w-[420px] rounded-card border border-borda bg-white p-6 shadow-sm sm:p-8">
        <h1 className="mb-1 text-2xl">{titulo}</h1>
        {subtitulo && <p className="mb-5 text-sm text-suave">{subtitulo}</p>}
        {children}
      </div>
      {rodape && <div className="mt-5 text-sm text-suave">{rodape}</div>}
    </div>
  );
}
