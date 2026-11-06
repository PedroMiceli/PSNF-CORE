import { api } from '../api/endpoints';
import { useApi } from '../api/useApi';
import { useChat } from '../context/ChatContext';
import { useUsuario } from '../context/UsuarioContext';
import { formatarData } from '../components/format';
import { Badge, Carregando, cx, dica, ErroBox, ImagemAnuncio, TituloPagina, Vazio } from '../components/ui';

/** Conversas do usuário logado, como comprador ou vendedor. Clicar abre o chat. */
export function Negociacoes() {
  const { usuarioId } = useUsuario();
  const { abrirChat, aberta } = useChat();
  const lista = useApi(() => api.negociacoes(), [aberta]); // recarrega ao fechar/abrir uma conversa

  if (lista.loading && !lista.data) return <Carregando />;

  return (
    <section className="max-w-[900px]">
      <TituloPagina><h1>Negociações</h1></TituloPagina>
      <p className={dica}>Conversas com compradores e vendedores. Para começar uma, abra um anúncio e clique em <strong>Iniciar negociação</strong>.</p>
      <ErroBox erro={lista.error} />
      {lista.data?.length === 0 ? (
        <Vazio>Nenhuma negociação ainda.</Vazio>
      ) : (
        <ul className="divide-y divide-borda overflow-hidden rounded-card border border-borda bg-white">
          {lista.data?.map((n) => {
            const minha = n.ultimaMensagemAutorId === usuarioId;
            return (
              <li key={n.id}>
                <button type="button" onClick={() => abrirChat(n.id)}
                  className={cx('flex w-full cursor-pointer items-center gap-3 px-4 py-3 text-left hover:bg-gray-50',
                    aberta === n.id && 'bg-blue-50')}>
                  <ImagemAnuncio caminho={n.imagemAnuncio} alt="" compacta className="size-14 shrink-0 rounded-lg" />
                  <span className="min-w-0 flex-1">
                    <span className="flex flex-wrap items-center gap-x-2">
                      <strong className="truncate">{n.tituloAnuncio}</strong>
                      <Badge className={n.papel === 'VENDEDOR' ? 'bg-blue-100 text-blue-800' : 'bg-green-100 text-green-800'}>
                        {n.papel === 'VENDEDOR' ? 'Você vende' : 'Você compra'}
                      </Badge>
                    </span>
                    <span className="block text-sm text-suave">com {n.outroParticipanteNome}</span>
                    {n.ultimaMensagem && (
                      <span className="block truncate text-sm">{minha && <span className="text-suave">Você: </span>}{n.ultimaMensagem}</span>
                    )}
                  </span>
                  <small className="shrink-0 self-start text-xs text-suave">{formatarData(n.ultimaMensagemEm)}</small>
                </button>
              </li>
            );
          })}
        </ul>
      )}
    </section>
  );
}
