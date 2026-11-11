import { useCallback, useRef, useState, type ReactNode } from 'react';
import { Dialogo } from './Dialogo';
import { Botao } from './ui';

export interface OpcoesConfirmacao {
  titulo: string;
  mensagem: ReactNode;
  /** texto do botão que confirma (ex.: "Cancelar ordem") */
  confirmar: string;
  /** ação destrutiva: botão vermelho */
  perigo?: boolean;
  /** texto do botão que desiste (padrão "Voltar") */
  voltar?: string;
}

/**
 * Substitui o window.confirm do navegador por um diálogo com o visual do sistema:
 *
 *   const [confirmar, dialogoConfirmacao] = useConfirmacao();
 *   if (await confirmar({ titulo: 'Cancelar a ordem?', mensagem: '...', confirmar: 'Cancelar ordem', perigo: true })) ...
 *   return <>... {dialogoConfirmacao}</>;
 */
export function useConfirmacao(): [(o: OpcoesConfirmacao) => Promise<boolean>, ReactNode] {
  const [opcoes, setOpcoes] = useState<OpcoesConfirmacao | null>(null);
  const resolver = useRef<((ok: boolean) => void) | null>(null);

  const confirmar = useCallback((o: OpcoesConfirmacao) => new Promise<boolean>((resolve) => {
    resolver.current = resolve;
    setOpcoes(o);
  }), []);

  const responder = useCallback((ok: boolean) => {
    resolver.current?.(ok);
    resolver.current = null;
    setOpcoes(null);
  }, []);
  const desistir = useCallback(() => responder(false), [responder]);

  const dialogo = (
    <Dialogo aberto={!!opcoes} titulo={opcoes?.titulo ?? ''} onFechar={desistir}
      rodape={opcoes && (
        <div className="flex flex-wrap justify-end gap-2">
          <Botao onClick={desistir}>{opcoes.voltar ?? 'Voltar'}</Botao>
          <Botao variante={opcoes.perigo ? 'perigoForte' : 'primario'} onClick={() => responder(true)}>
            {opcoes.confirmar}
          </Botao>
        </div>
      )}>
      <div className="text-[0.95rem] leading-relaxed">{opcoes?.mensagem}</div>
    </Dialogo>
  );
  return [confirmar, dialogo];
}
