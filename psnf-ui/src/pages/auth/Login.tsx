import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import type { ApiError } from '../../api/client';
import { api } from '../../api/endpoints';
import { Botao, Campo, controle, ErroBox } from '../../components/ui';
import { useUsuario } from '../../context/UsuarioContext';
import { AuthLayout } from './AuthLayout';

export function Login() {
  const { entrar, estado } = useUsuario();
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [erro, setErro] = useState<ApiError | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [reenviado, setReenviado] = useState(false);
  const motivo = estado.tipo === 'anonimo' ? estado.motivo : undefined;

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    setEnviando(true);
    setReenviado(false);
    const falha = await entrar(email.trim(), senha);
    setEnviando(false);
    // sem erro, as rotas de usuário logado assumem e levam à página que ele tentou abrir (App.tsx)
    if (falha) setErro(falha);
  };

  return (
    <AuthLayout titulo="Entrar" subtitulo="Acesse sua conta para comprar e vender."
      rodape={<>Ainda não tem conta? <Link to="/cadastro">Cadastre sua empresa</Link></>}>
      {motivo === 'expirou' && !erro && (
        <p className="mb-4 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm" role="status">
          Sua sessão expirou. Entre novamente para continuar.
        </p>
      )}
      {motivo === 'saiu' && !erro && (
        <p className="mb-4 rounded-lg border border-green-200 bg-green-50 px-4 py-3 text-sm" role="status">Você saiu da sua conta.</p>
      )}
      <ErroBox erro={erro} simples />
      {erro?.codigo === 'EMAIL_NAO_CONFIRMADO' && (
        <p className="-mt-2 mb-4 text-sm">
          {reenviado ? 'Enviamos um novo link de confirmação. Confira também a caixa de spam.' : (
            <button type="button" className="cursor-pointer font-semibold text-primaria hover:underline"
              onClick={async () => { await api.reenviarConfirmacao(email.trim()).catch(() => undefined); setReenviado(true); }}>
              Reenviar o e-mail de confirmação
            </button>
          )}
        </p>
      )}
      <form onSubmit={enviar} noValidate>
        <Campo label="E-mail">
          <input className={controle} type="email" autoComplete="username" autoFocus required
            value={email} onChange={(e) => setEmail(e.target.value)} />
        </Campo>
        <Campo label="Senha">
          <input className={controle} type="password" autoComplete="current-password" required
            value={senha} onChange={(e) => setSenha(e.target.value)} />
        </Campo>
        <Botao type="submit" variante="primario" disabled={enviando || !email || !senha} className="mt-2 w-full justify-center">
          {enviando ? 'Entrando...' : 'Entrar'}
        </Botao>
        <p className="mt-4 text-center text-sm"><Link to="/esqueci-senha">Esqueci minha senha</Link></p>
      </form>
    </AuthLayout>
  );
}
