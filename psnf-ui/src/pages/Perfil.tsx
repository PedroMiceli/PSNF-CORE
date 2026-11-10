import { useEffect, useState, type FormEvent } from 'react';
import { api } from '../api/endpoints';
import type { EnderecoInput } from '../api/types';
import { useAcao, useApi } from '../api/useApi';
import { ACEITA_IMAGENS, UploadArquivo } from '../components/UploadArquivo';
import { mascaraTelefone, telefoneValido } from '../components/format';
import { EnderecoForm } from '../components/EnderecoForm';
import { Botao, Campo, Carregando, controle, cx, dica, ErroBox, ImagemAnuncio, linhaForm, painel, Painel } from '../components/ui';
import { useUsuario } from '../context/UsuarioContext';

export function Perfil() {
  const usuario = useApi(() => api.usuarioLogado(), []);
  const { atualizarUsuario, sair } = useUsuario();
  const atividade = useApi(() => api.atividade(), []);
  const [saindo, setSaindo] = useState(false);
  const salvar = useAcao();
  const endereco = useAcao();
  const [dados, setDados] = useState({ nome: '', telefone: '', imagem: '' });
  const [tentouSalvar, setTentouSalvar] = useState(false);

  useEffect(() => {
    if (usuario.data) setDados({ nome: usuario.data.nome, telefone: mascaraTelefone(usuario.data.telefone ?? ''), imagem: usuario.data.imagem ?? '' });
  }, [usuario.data]);

  if (usuario.loading) return <Carregando />;
  if (!usuario.data) return <ErroBox erro={usuario.error} />;
  const u = usuario.data;

  const salvarDados = async (e: FormEvent) => {
    e.preventDefault();
    setTentouSalvar(true);
    if (dados.telefone && !telefoneValido(dados.telefone)) return;
    // a API guarda só os dígitos; a máscara é apenas visual
    const r = await salvar.executar(() => api.atualizarUsuario({ ...dados, telefone: dados.telefone.replace(/\D/g, '') }));
    if (r) { usuario.setData(r); atualizarUsuario(r); }
  };
  const adicionarEndereco = async (novo: EnderecoInput) => {
    const r = await endereco.executar(() => api.adicionarEndereco(novo));
    if (r) usuario.setData(r);
    return !!r;
  };
  const removerEndereco = async (id: string) => {
    const r = await endereco.executar(() => api.removerEndereco(id));
    if (r) usuario.setData(r);
  };

  return (
    <section className="max-w-[960px]">
      <h1>Perfil</h1>
      <p className={cx(dica, 'break-all')}>Id: <code>{u.id}</code> · {u.email}</p>

      <form className={painel} onSubmit={salvarDados}>
        <h3>Dados</h3>
        <ErroBox erro={salvar.erro} />
        <div className={linhaForm}>
          <Campo label="Nome" erro={salvar.erro?.campos?.nome}>
            <input className={controle} value={dados.nome} onChange={(e) => setDados({ ...dados, nome: e.target.value })} />
          </Campo>
          <Campo label="Telefone" erro={dados.telefone && !telefoneValido(dados.telefone) && tentouSalvar
            ? 'Informe o DDD e o número (ex.: (11) 99999-0001)' : salvar.erro?.campos?.telefone}>
            <input className={controle} value={dados.telefone} inputMode="tel" autoComplete="tel" placeholder="(11) 99999-0001"
              onChange={(e) => setDados({ ...dados, telefone: mascaraTelefone(e.target.value) })} />
          </Campo>
        </div>
        <div className="mb-4 flex items-center gap-4">
          {dados.imagem
            ? <ImagemAnuncio caminho={dados.imagem} alt="Foto do perfil" compacta className="size-21 shrink-0 rounded-full border border-borda" />
            : <div className="flex size-21 shrink-0 items-center justify-center rounded-full border border-borda bg-slate-100 text-3xl">👤</div>}
          <div>
            <UploadArquivo accept={ACEITA_IMAGENS} compacto rotulo={dados.imagem ? 'Trocar foto' : 'Enviar foto'}
              onEnviado={(a) => setDados((d) => ({ ...d, imagem: a.caminho }))} />
            <p className={cx(dica, 'mb-0')}>A foto é salva ao clicar em "Salvar".</p>
          </div>
        </div>
        <Botao type="submit" variante="primario" disabled={salvar.enviando}>Salvar</Botao>
      </form>

      <Painel>
        <h3>Endereços de entrega</h3>
        <ErroBox erro={endereco.erro?.campos ? null : endereco.erro} />
        {u.enderecos.length === 0 ? <p className={dica}>Nenhum endereço cadastrado.</p> : (
          <ul className="divide-y divide-borda">
            {u.enderecos.map((e) => (
              <li key={e.id} className="flex flex-wrap items-center gap-2 py-2.5">
                <span className="min-w-0 flex-1">
                  {[
                    [e.logradouro, e.numero].filter(Boolean).join(', ') + (e.complemento ? ` - ${e.complemento}` : ''),
                    e.bairro,
                    `${e.cidade}/${e.uf}`,
                    `CEP ${e.cep}`,
                  ].filter(Boolean).join(' · ')}
                </span>
                <Botao variante="perigo" tamanho="pequeno" disabled={endereco.enviando} onClick={() => removerEndereco(e.id)}>Remover</Botao>
              </li>
            ))}
          </ul>
        )}
        <EnderecoForm enviando={endereco.enviando} erros={endereco.erro?.campos} onSalvar={adicionarEndereco} />
      </Painel>

      <Painel>
        <h3>Segurança</h3>
        <p className={dica}>
          Esqueceu a sessão aberta em outro computador ou perdeu o celular? Encerre o acesso em todos os
          dispositivos, inclusive neste: será preciso entrar de novo em cada um.
        </p>
        <Botao variante="perigo" disabled={saindo} onClick={async () => { setSaindo(true); await sair(true); }}>
          {saindo ? 'Encerrando...' : 'Sair de todos os dispositivos'}
        </Botao>

        <h4 className="mt-6">Atividade recente</h4>
        <p className={dica}>Não reconhece algum acesso? Troque a senha e saia de todos os dispositivos.</p>
        {atividade.data && atividade.data.length > 0 ? (
          <ul className="divide-y divide-borda text-sm">
            {atividade.data.map((a, i) => (
              <li key={i} className="flex flex-wrap items-baseline gap-x-3 gap-y-0.5 py-2">
                <span className={cx('font-semibold', ALERTA.has(a.tipo) && 'text-vermelho')}>{a.descricao}</span>
                <span className="text-suave">{new Date(a.quando).toLocaleString('pt-BR')}</span>
                <span className="w-full truncate text-xs text-suave" title={a.navegador ?? ''}>
                  {[a.ip && `IP ${a.ip}`, navegador(a.navegador)].filter(Boolean).join(' · ')}
                </span>
              </li>
            ))}
          </ul>
        ) : <p className={dica}>{atividade.loading ? 'Carregando...' : 'Nenhuma atividade registrada.'}</p>}
      </Painel>
    </section>
  );
}

/** eventos que merecem atenção do usuário */
const ALERTA = new Set(['LOGIN_FALHOU', 'LOGIN_BLOQUEADO', 'SESSAO_ROUBADA']);

/** "Mozilla/5.0 (...) Chrome/131..." -> "Chrome no Windows" (o texto completo fica no title) */
function navegador(ua: string | null): string | null {
  if (!ua) return null;
  const nome = /Edg\//.test(ua) ? 'Edge' : /OPR\//.test(ua) ? 'Opera' : /Firefox\//.test(ua) ? 'Firefox'
    : /Chrome\//.test(ua) ? 'Chrome' : /Safari\//.test(ua) ? 'Safari' : null;
  const so = /Windows/.test(ua) ? 'Windows' : /Android/.test(ua) ? 'Android' : /iPhone|iPad/.test(ua) ? 'iOS'
    : /Mac OS X/.test(ua) ? 'macOS' : /Linux/.test(ua) ? 'Linux' : null;
  if (nome && so) return `${nome} no ${so}`;
  return nome ?? so ?? ua.slice(0, 40);
}
