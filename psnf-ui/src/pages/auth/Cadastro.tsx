import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { ApiError } from '../../api/client';
import { api } from '../../api/endpoints';
import { cnpjValido, mascaraCnpj, mascaraTelefone, telefoneValido } from '../../components/format';
import { ResultadoConsultaCnpj, useConsultaCnpj } from '../../components/ConsultaCnpj';
import { Botao, Campo, controle, ErroBox } from '../../components/ui';
import { AuthLayout } from './AuthLayout';

const SENHA_MIN = 8;

export function Cadastro() {
  const [form, setForm] = useState({ nome: '', email: '', telefone: '', cnpj: '', senha: '', confirmacao: '' });
  const [erro, setErro] = useState<ApiError | null>(null);
  const [tentou, setTentou] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const [criado, setCriado] = useState(false);
  const [reenviado, setReenviado] = useState(false);
  const mudar = (campo: keyof typeof form) => (e: { target: { value: string } }) =>
    setForm((f) => ({ ...f, [campo]: campo === 'telefone' ? mascaraTelefone(e.target.value) : campo === 'cnpj' ? mascaraCnpj(e.target.value) : e.target.value }));

  // CNPJ completo: consulta a Receita e, se o nome ainda está vazio, preenche com o da empresa
  const consulta = useConsultaCnpj(form.cnpj, (e) =>
    setForm((f) => (f.nome.trim() ? f : { ...f, nome: e.nomeFantasia ?? e.razaoSocial ?? '' })));

  const erros: Partial<Record<keyof typeof form, string>> = {
    nome: !form.nome.trim() ? 'Informe o nome da empresa ou o seu' : undefined,
    email: !/^\S+@\S+\.\S+$/.test(form.email.trim()) ? 'Informe um e-mail válido' : undefined,
    telefone: !telefoneValido(form.telefone) ? 'Informe DDD + número' : undefined,
    cnpj: !cnpjValido(form.cnpj) ? 'CNPJ inválido: confira os números'
      : consulta.tipo === 'inativa' ? 'CNPJ inativo na Receita Federal' : undefined,
    senha: form.senha.length < SENHA_MIN ? `Use pelo menos ${SENHA_MIN} caracteres` : form.senha.length > 72 ? 'Use no máximo 72 caracteres' : undefined,
    confirmacao: form.confirmacao !== form.senha ? 'As senhas não conferem' : undefined,
  };
  const valido = Object.values(erros).every((v) => !v);
  const erroDe = (campo: keyof typeof form) => (tentou ? erros[campo] : undefined) ?? erro?.campos?.[campo];

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    setTentou(true);
    if (!valido) return;
    setEnviando(true);
    try {
      await api.cadastrar({ nome: form.nome.trim(), email: form.email.trim(), telefone: form.telefone, cnpj: form.cnpj || undefined, senha: form.senha });
      setCriado(true);
    } catch (e) {
      setErro(e instanceof ApiError ? e : new ApiError(0, String(e)));
    } finally {
      setEnviando(false);
    }
  };

  if (criado) {
    return (
      <AuthLayout titulo="Confirme seu e-mail" rodape={<Link to="/login">Ir para o login</Link>}>
        <p className="mb-4 rounded-lg border border-green-200 bg-green-50 px-4 py-3 text-sm" role="status">
          Conta criada! Enviamos um link para <strong>{form.email.trim()}</strong>. Abra-o para ativar a conta
          (vale por 24 horas) e depois entre com seu e-mail e senha.
        </p>
        <p className="text-sm text-suave">
          Não chegou? Confira a caixa de spam ou{' '}
          {reenviado ? 'pronto, enviamos de novo.' : (
            <button type="button" className="cursor-pointer font-semibold text-primaria hover:underline"
              onClick={async () => { await api.reenviarConfirmacao(form.email.trim()).catch(() => undefined); setReenviado(true); }}>
              envie de novo
            </button>
          )}
        </p>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout titulo="Criar conta" subtitulo="Uma conta serve para comprar e para vender."
      rodape={<>Já tem conta? <Link to="/login">Entrar</Link></>}>
      <ErroBox erro={erro && !erro.campos ? erro : null} simples />
      <form onSubmit={enviar} noValidate>
        <Campo label="CNPJ (opcional)" erro={erroDe('cnpj')}
          dica="Empresas: preenchemos os dados pela Receita e você vê todos os anúncios. Sem CNPJ, só os liberados para pessoa física.">
          <input className={controle} inputMode="text" autoFocus placeholder="00.000.000/0000-00" value={form.cnpj} onChange={mudar('cnpj')} />
        </Campo>
        <ResultadoConsultaCnpj resultado={consulta} />
        <Campo label="Nome ou razão social" erro={erroDe('nome')}>
          <input className={controle} autoComplete="organization" value={form.nome} onChange={mudar('nome')} />
        </Campo>
        <Campo label="E-mail" erro={erroDe('email')}>
          <input className={controle} type="email" autoComplete="email" value={form.email} onChange={mudar('email')} />
        </Campo>
        <Campo label="Telefone" erro={erroDe('telefone')}>
          <input className={controle} type="tel" autoComplete="tel" placeholder="(11) 99999-0001" value={form.telefone} onChange={mudar('telefone')} />
        </Campo>
        <Campo label="Senha" erro={erroDe('senha')} dica={`Pelo menos ${SENHA_MIN} caracteres. Uma frase é mais fácil de lembrar e mais difícil de adivinhar.`}>
          <input className={controle} type="password" autoComplete="new-password" value={form.senha} onChange={mudar('senha')} />
        </Campo>
        <Campo label="Confirme a senha" erro={erroDe('confirmacao')}>
          <input className={controle} type="password" autoComplete="new-password" value={form.confirmacao} onChange={mudar('confirmacao')} />
        </Campo>
        <Botao type="submit" variante="primario" disabled={enviando} className="mt-2 w-full justify-center">
          {enviando ? 'Criando conta...' : 'Criar conta'}
        </Botao>
      </form>
    </AuthLayout>
  );
}
