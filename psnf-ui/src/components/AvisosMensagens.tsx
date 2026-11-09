import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAlertas, type Aviso } from '../context/AlertasContext';

/** Avisos no canto inferior esquerdo: "Nova mensagem no pedido PED-X". Somem sozinhos em 10 s. */
export function AvisosMensagens() {
  const { avisos, dispensarAviso } = useAlertas();
  if (!avisos.length) return null;
  return (
    <div className="pointer-events-none fixed bottom-4 left-4 z-40 flex w-[min(360px,calc(100vw-2rem))] flex-col gap-2" aria-live="polite">
      {avisos.map((a) => <AvisoMensagem key={a.id} aviso={a} onFechar={() => dispensarAviso(a.id)} />)}
    </div>
  );
}

function AvisoMensagem({ aviso: { alerta: a }, onFechar }: { aviso: Aviso; onFechar: () => void }) {
  const navigate = useNavigate();
  useEffect(() => { const t = setTimeout(onFechar, 10000); return () => clearTimeout(t); }, [onFechar]);
  return (
    <div role="alert" className="pointer-events-auto flex items-start gap-3 rounded-card border border-orange-300 bg-white p-3 shadow-xl">
      <span className="text-xl" aria-hidden="true">💬</span>
      <button type="button" className="min-w-0 flex-1 cursor-pointer text-left"
        onClick={() => { onFechar(); navigate(`/ordens/${a.ordemId}#mensagens`); }}>
        <strong className="block text-sm">
          {a.naoLidas === 1 ? 'Nova mensagem' : `${a.naoLidas} mensagens novas`} no pedido {a.codigo}
        </strong>
        <span className="block text-xs text-suave">
          {a.outroParticipanteNome} ({a.papel === 'VENDEDOR' ? 'comprador' : 'vendedor'}) em Incidentes e mensagens
        </span>
        {a.ultimaMensagem && <span className="mt-1 block truncate text-sm">“{a.ultimaMensagem}”</span>}
        <span className="mt-1 block text-xs font-semibold text-primaria">Ver mensagem ›</span>
      </button>
      <button type="button" onClick={onFechar} aria-label="Fechar aviso"
        className="cursor-pointer rounded px-1 text-suave hover:bg-gray-100">✕</button>
    </div>
  );
}
