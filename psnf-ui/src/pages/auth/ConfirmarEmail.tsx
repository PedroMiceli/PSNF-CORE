import { useEffect, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { ApiError } from '../../api/client';
import { api } from '../../api/endpoints';
import { classesBotao, ErroBox } from '../../components/ui';
import { useUsuario } from '../../context/UsuarioContext';
import { AuthLayout } from './AuthLayout';

/** Destino do link enviado no cadastro: /confirmar-email?token=... */
export function ConfirmarEmail() {
  const [params] = useSearchParams();
  const { estado } = useUsuario();
  const [resultado, setResultado] = useState<'confirmando' | 'ok' | ApiError>('confirmando');
  const enviado = useRef(false);

  useEffect(() => {
    if (enviado.current) return; // o link é de uso único: não pode ser enviado duas vezes (StrictMode)
    enviado.current = true;
    api.confirmarEmail(params.get('token') ?? '')
      .then(() => setResultado('ok'))
      .catch((e) => setResultado(e instanceof ApiError ? e : new ApiError(0, String(e))));
  }, [params]);

  const logado = estado.tipo === 'logado';
  return (
    <AuthLayout titulo="Confirmação de e-mail">
      {resultado === 'confirmando' && <p className="text-suave">Confirmando...</p>}
      {resultado === 'ok' && (
        <>
          <p className="mb-5 rounded-lg border border-green-200 bg-green-50 px-4 py-3" role="status">
            E-mail confirmado! Sua conta está ativa.
          </p>
          <Link to={logado ? '/' : '/login'} className={classesBotao('primario') + ' w-full justify-center'}>
            {logado ? 'Ir para a vitrine' : 'Entrar'}
          </Link>
        </>
      )}
      {resultado instanceof ApiError && (
        <>
          <ErroBox erro={resultado} simples />
          <p className="text-sm text-suave">
            Se você já confirmou, é só <Link to="/login">entrar</Link>. Senão, tente entrar: a tela de login
            oferece o envio de um link novo.
          </p>
        </>
      )}
    </AuthLayout>
  );
}
