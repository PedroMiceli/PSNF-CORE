import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { ApiError } from '../../api/client';
import { api } from '../../api/endpoints';
import { Botao, Campo, controle, ErroBox } from '../../components/ui';
import { AuthLayout } from './AuthLayout';

export function EsqueciSenha() {
  const [email, setEmail] = useState('');
  const [enviado, setEnviado] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<ApiError | null>(null);

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    setEnviando(true);
    try {
      await api.esqueciSenha(email.trim());
      setEnviado(true);
    } catch (e) {
      setErro(e instanceof ApiError ? e : new ApiError(0, String(e)));
    } finally {
      setEnviando(false);
    }
  };

  return (
    <AuthLayout titulo="Esqueci minha senha" subtitulo="Enviaremos um link para você escolher uma senha nova."
      rodape={<Link to="/login">Voltar para o login</Link>}>
      {enviado ? (
        // a mesma mensagem para qualquer e-mail: a tela não revela quem tem conta
        <p className="rounded-lg border border-green-200 bg-green-50 px-4 py-3 text-sm" role="status">
          Se houver uma conta com <strong>{email.trim()}</strong>, enviamos um link para redefinir a senha.
          Ele vale por 1 hora. Confira também a caixa de spam.
        </p>
      ) : (
        <form onSubmit={enviar} noValidate>
          <ErroBox erro={erro} simples />
          <Campo label="E-mail da conta" erro={erro?.campos?.email}>
            <input className={controle} type="email" autoComplete="email" autoFocus value={email} onChange={(e) => setEmail(e.target.value)} />
          </Campo>
          <Botao type="submit" variante="primario" disabled={enviando || !email.trim()} className="mt-2 w-full justify-center">
            {enviando ? 'Enviando...' : 'Enviar link'}
          </Botao>
        </form>
      )}
    </AuthLayout>
  );
}
