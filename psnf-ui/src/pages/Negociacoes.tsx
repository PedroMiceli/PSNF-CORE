import { ListaNegociacoes } from '../components/ChatHub';
import { dica, TituloPagina } from '../components/ui';
import { useChat } from '../context/ChatContext';

/** Página com todas as conversas (as mesmas da barra lateral). Clicar abre o chat. */
export function Negociacoes() {
  const { lista } = useChat();
  return (
    <section className="max-w-[900px]">
      <TituloPagina><h1>Negociações</h1></TituloPagina>
      <p className={dica}>
        Conversas com compradores e vendedores{lista ? ` (${lista.length})` : ''}. Elas também ficam na barra à direita,
        em qualquer página. Para começar uma, abra um anúncio e clique em <strong>Iniciar negociação</strong>.
      </p>
      <div className="flex h-[min(70vh,720px)] flex-col overflow-hidden rounded-card border border-borda bg-white">
        <ListaNegociacoes grande />
      </div>
    </section>
  );
}
