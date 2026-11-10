import { useState, type FormEvent } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { ApiError } from '../../api/client';
import { api } from '../../api/endpoints';
import { Botao, Campo, classesBotao, controle, ErroBox } from '../../components/ui';
import { useUsuario } from '../../context/UsuarioContext';
import { AuthLayout } from './AuthLayout';

/** Destino do link de recuperação: /redefinir-senha?token=... */
export function RedefinirSenha() {
  const [params] = useSearchParams();
  const { estado, sair } = useUsuario();
  const [senha, setSenha] = useState('');
  const [confirmacao, setConfirmacao] = useState('');
  const [tentou, setTentou] = useState(false);
  const [erro, setErro] = useState<ApiError | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [pronto, setPronto] = useState(false);

  const erroSenha = senha.length < 8 ? 'Use pelo menos 8 caracteres' : senha.length > 72 ? 'Use no máximo 72 caracteres' : undefined;
  const erroConfirmacao = confirmacao !== senha ? 'As senhas não conferem' : undefined;

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    setTentou(true);
    if (erroSenha || erroConfirmacao) return;
    setEnviando(true);
    try {
      await api.redefinirSenha(params.get('token') ?? '', senha);
      setPronto(true);
    } catch (e) {
      setErro(e instanceof ApiError ? e : new ApiError(0, String(e)));
    } finally {
      setEnviando(false);
    }
  };

  if (pronto) {
    return (
      <AuthLayout titulo="Senha alterada">
        <p className="mb-5 rounded-lg border border-green-200 bg-green-50 px-4 py-3 text-sm" role="status">
          Pronto! Sua senha foi alterada e as sessões abertas em outros dispositivos foram encerradas.
        </p>
        {/* a API encerrou todas as sessões, inclusive a deste navegador */}
        <Link to="/login" onClick={() => estado.tipo === 'logado' && sair()} className={classesBotao('primario') + ' w-full justify-center'}>
          Entrar com a senha nova
        </Link>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout titulo="Escolha uma senha nova" rodape={<Link to="/login">Voltar para o login</Link>}>
      <ErroBox erro={erro && !erro.campos ? erro : null} simples />
      {erro && erro.status === 422 && <p className="-mt-2 mb-4 text-sm"><Link to="/esqueci-senha">Pedir um link novo</Link></p>}
      <form onSubmit={enviar} noValidate>
        <Campo label="Senha nova" erro={(tentou ? erroSenha : undefined) ?? erro?.campos?.novaSenha}
          dica="Pelo menos 8 caracteres. Uma frase é mais fácil de lembrar e mais difícil de adivinhar.">
          <input className={controle} type="password" autoComplete="new-password" autoFocus value={senha} onChange={(e) => setSenha(e.target.value)} />
        </Campo>
        <Campo label="Confirme a senha nova" erro={tentou ? erroConfirmacao : undefined}>
          <input className={controle} type="password" autoComplete="new-password" value={confirmacao} onChange={(e) => setConfirmacao(e.target.value)} />
        </Campo>
        <Botao type="submit" variante="primario" disabled={enviando} className="mt-2 w-full justify-center">
          {enviando ? 'Salvando...' : 'Salvar senha nova'}
        </Botao>
      </form>
    </AuthLayout>
  );
}
