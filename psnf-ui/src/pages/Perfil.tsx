import { useEffect, useState, type FormEvent } from 'react';
import { api } from '../api/endpoints';
import type { EnderecoInput } from '../api/types';
import { useAcao, useApi } from '../api/useApi';
import { ACEITA_IMAGENS, UploadArquivo } from '../components/UploadArquivo';
import { EnderecoForm } from '../components/EnderecoForm';
import { Botao, Campo, Carregando, controle, cx, dica, ErroBox, ImagemAnuncio, linhaForm, painel, Painel } from '../components/ui';

export function Perfil() {
  const usuario = useApi(() => api.usuarioLogado(), []);
  const salvar = useAcao();
  const endereco = useAcao();
  const [dados, setDados] = useState({ nome: '', telefone: '', imagem: '' });

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
          <Campo label="Telefone">
            <input className={controle} value={dados.telefone} onChange={(e) => setDados({ ...dados, telefone: e.target.value })} />
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
    </section>
  );
}
