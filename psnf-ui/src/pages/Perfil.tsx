import { useEffect, useState, type FormEvent } from 'react';
import { api } from '../api/endpoints';
import type { EnderecoInput } from '../api/types';
import { useAcao, useApi } from '../api/useApi';
import { ACEITA_IMAGENS, UploadArquivo } from '../components/UploadArquivo';
import { Campo, Carregando, ErroBox, ImagemAnuncio } from '../components/ui';
import { useUsuario } from '../context/UsuarioContext';

const enderecoVazio: EnderecoInput = { logradouro: '', numero: '', complemento: '', bairro: '', cidade: '', estado: '', cep: '', uf: '' };

export function Perfil() {
  const usuario = useApi(() => api.usuarioLogado(), []);
  const salvar = useAcao();
  const endereco = useAcao();
  const [dados, setDados] = useState({ nome: '', telefone: '', imagem: '' });
  const [novoEnd, setNovoEnd] = useState<EnderecoInput>(enderecoVazio);

  useEffect(() => {
    if (usuario.data) setDados({ nome: usuario.data.nome, telefone: usuario.data.telefone ?? '', imagem: usuario.data.imagem ?? '' });
  }, [usuario.data]);

  if (usuario.loading) return <Carregando />;
  if (!usuario.data) return <ErroBox erro={usuario.error} />;
  const u = usuario.data;

  const salvarDados = async (e: FormEvent) => {
    e.preventDefault();
    const r = await salvar.executar(() => api.atualizarUsuario(dados));
    if (r) usuario.setData(r);
  };
  const adicionarEndereco = async (e: FormEvent) => {
    e.preventDefault();
    const r = await endereco.executar(() => api.adicionarEndereco(novoEnd));
    if (r) {
      usuario.setData(r);
      setNovoEnd(enderecoVazio);
    }
  };
  const removerEndereco = async (id: string) => {
    const r = await endereco.executar(() => api.removerEndereco(id));
    if (r) usuario.setData(r);
  };
  const campoEnd = (k: keyof EnderecoInput, label: string, placeholder = '') => (
    <Campo label={label} erro={endereco.erro?.campos?.[k]}>
      <input value={novoEnd[k] ?? ''} placeholder={placeholder} onChange={(e) => setNovoEnd({ ...novoEnd, [k]: e.target.value })} />
    </Campo>
  );

  return (
    <section className="formulario">
      <h1>Perfil</h1>
      <p className="dica">Id: <code>{u.id}</code> · {u.email}</p>

      <form className="painel" onSubmit={salvarDados}>
        <h3>Dados</h3>
        <ErroBox erro={salvar.erro} />
        <div className="linha-form">
          <Campo label="Nome" erro={salvar.erro?.campos?.nome}><input value={dados.nome} onChange={(e) => setDados({ ...dados, nome: e.target.value })} /></Campo>
          <Campo label="Telefone"><input value={dados.telefone} onChange={(e) => setDados({ ...dados, telefone: e.target.value })} /></Campo>
        </div>
        <div className="foto-perfil">
          {dados.imagem ? <ImagemAnuncio caminho={dados.imagem} alt="Foto do perfil" className="avatar" /> : <div className="avatar avatar-vazio">👤</div>}
          <div>
            <UploadArquivo accept={ACEITA_IMAGENS} compacto rotulo={dados.imagem ? 'Trocar foto' : 'Enviar foto'}
              onEnviado={(a) => setDados((d) => ({ ...d, imagem: a.caminho }))} />
            <p className="dica">A foto é salva ao clicar em "Salvar".</p>
          </div>
        </div>
        <button className="btn primario" disabled={salvar.enviando}>Salvar</button>
      </form>

      <div className="painel">
        <h3>Endereços de entrega</h3>
        <ErroBox erro={endereco.erro?.campos ? null : endereco.erro} />
        {u.enderecos.length === 0 ? <p className="dica">Nenhum endereço cadastrado.</p> : (
          <ul className="lista">
            {u.enderecos.map((e) => (
              <li key={e.id}>
                {e.logradouro}, {e.numero} {e.complemento && `- ${e.complemento}`} · {e.bairro} · {e.cidade}/{e.uf} · CEP {e.cep}
                <button className="btn pequeno perigo direita" disabled={endereco.enviando} onClick={() => removerEndereco(e.id)}>Remover</button>
              </li>
            ))}
          </ul>
        )}
        <form onSubmit={adicionarEndereco} className="sub-form">
          <h4>Adicionar endereço</h4>
          <div className="linha-form">{campoEnd('logradouro', 'Logradouro', 'Rua...')}{campoEnd('numero', 'Número')}{campoEnd('complemento', 'Complemento')}</div>
          <div className="linha-form">{campoEnd('bairro', 'Bairro')}{campoEnd('cidade', 'Cidade')}{campoEnd('estado', 'Estado')}{campoEnd('uf', 'UF', 'SP')}{campoEnd('cep', 'CEP', '00000-000')}</div>
          <button className="btn" disabled={endereco.enviando}>Adicionar</button>
        </form>
      </div>

      <NovoUsuario />
    </section>
  );
}

/** Cadastro de usuários extras para testar (eles entram no seletor "Logado como"). */
function NovoUsuario() {
  const { lembrarUsuario, trocarUsuario } = useUsuario();
  const acao = useAcao();
  const [form, setForm] = useState({ nome: '', email: '', senha: '123456', telefone: '' });
  const [criado, setCriado] = useState<{ id: string; nome: string } | null>(null);

  const criar = async (e: FormEvent) => {
    e.preventDefault();
    const u = await acao.executar(() => api.cadastrarUsuario(form));
    if (u) {
      lembrarUsuario({ id: u.id, nome: u.nome });
      setCriado({ id: u.id, nome: u.nome });
      setForm({ nome: '', email: '', senha: '123456', telefone: '' });
    }
  };
  const campo = (k: keyof typeof form, label: string, type = 'text') => (
    <Campo label={label} erro={acao.erro?.campos?.[k]}>
      <input type={type} value={form[k]} onChange={(e) => setForm({ ...form, [k]: e.target.value })} />
    </Campo>
  );

  return (
    <form className="painel" onSubmit={criar}>
      <h3>Cadastrar outro usuário (para testes)</h3>
      <ErroBox erro={acao.erro?.campos ? null : acao.erro} />
      <div className="linha-form">{campo('nome', 'Nome')}{campo('email', 'Email', 'email')}</div>
      <div className="linha-form">{campo('telefone', 'Telefone')}{campo('senha', 'Senha', 'password')}</div>
      <button className="btn" disabled={acao.enviando}>Cadastrar</button>
      {criado && (
        <p className="ok">
          ✔ {criado.nome} cadastrado e adicionado ao seletor "Logado como".{' '}
          <button type="button" className="btn pequeno" onClick={() => trocarUsuario(criado.id)}>Entrar como {criado.nome}</button>
        </p>
      )}
    </form>
  );
}
