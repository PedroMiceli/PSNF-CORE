import { useState, type FormEvent } from 'react';
import { api } from '../api/endpoints';
import { useAcao, useApi } from '../api/useApi';
import { Campo, Carregando, ErroBox, Vazio } from '../components/ui';

export function Categorias() {
  const categorias = useApi(() => api.categorias(), []);
  const acao = useAcao();
  const [nome, setNome] = useState('');

  const salvar = async (e: FormEvent) => {
    e.preventDefault();
    const c = await acao.executar(() => api.cadastrarCategoria(nome));
    if (c) {
      setNome('');
      void categorias.reload();
    }
  };

  return (
    <section className="formulario">
      <h1>Categorias</h1>
      <form onSubmit={salvar} className="painel linha-form">
        <Campo label="Nova categoria" erro={acao.erro?.campos?.nome}>
          <input value={nome} onChange={(e) => setNome(e.target.value)} placeholder="Roupas, Utilidades..." />
        </Campo>
        <button className="btn primario" disabled={acao.enviando}>Cadastrar</button>
      </form>
      <ErroBox erro={categorias.error ?? (acao.erro?.campos ? null : acao.erro)} />
      {categorias.loading ? <Carregando /> : !categorias.data?.length ? <Vazio>Nenhuma categoria cadastrada.</Vazio> : (
        <div className="chips">
          {categorias.data.map((c) => <span key={c.id} className="chip estatico">{c.nome}</span>)}
        </div>
      )}
    </section>
  );
}
