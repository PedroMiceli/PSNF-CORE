import { useState } from 'react';
import { cx } from './ui';

/** Copia o texto para a área de transferência (com alternativa para navegadores sem a API moderna). */
export async function copiarTexto(texto: string) {
  try {
    await navigator.clipboard.writeText(texto);
    return true;
  } catch {
    const area = document.createElement('textarea');
    area.value = texto;
    area.style.position = 'fixed';
    area.style.opacity = '0';
    document.body.appendChild(area);
    area.select();
    const ok = document.execCommand('copy');
    area.remove();
    return ok;
  }
}

/**
 * Valor somente leitura com cara de campo: um clique copia o conteúdo (chave PIX, código do boleto,
 * dados bancários) e mostra "Copiado".
 */
export function CampoCopiavel({ rotulo, valor, mono }: { rotulo: string; valor: string; mono?: boolean }) {
  const [estado, setEstado] = useState<'normal' | 'copiado' | 'erro'>('normal');
  const copiar = async () => {
    setEstado((await copiarTexto(valor)) ? 'copiado' : 'erro');
    setTimeout(() => setEstado('normal'), 2000);
  };
  return (
    <div className="mb-3">
      <span className="mb-1 block text-xs font-semibold text-gray-700">{rotulo}</span>
      <button type="button" onClick={copiar} title="Clique para copiar" aria-label={`${rotulo}: ${valor}. Clique para copiar`}
        className={cx('group flex w-full cursor-copy items-center gap-2 rounded-lg border px-3 py-2 text-left text-[0.92rem] transition-colors',
          estado === 'copiado' ? 'border-green-300 bg-green-50' : 'border-gray-300 bg-gray-50 hover:border-primaria hover:bg-blue-50')}>
        <span className={cx('min-w-0 flex-1 break-words', mono && 'font-mono')}>{valor}</span>
        <span className={cx('shrink-0 text-xs font-semibold', estado === 'copiado' ? 'text-verde' : estado === 'erro' ? 'text-vermelho' : 'text-suave group-hover:text-primaria')}
          aria-live="polite">
          {estado === 'copiado' ? '✔ Copiado' : estado === 'erro' ? 'Não copiou' : '📋 Copiar'}
        </span>
      </button>
    </div>
  );
}
