async function fetchJson(url) {
  const res = await fetch(url, { headers: { Accept: "application/json" } });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return await res.json();
}

function fmtDate(iso) {
  if (!iso) return "";
  try {
    const d = new Date(iso);
    return new Intl.DateTimeFormat("pt-BR").format(d);
  } catch {
    return "";
  }
}

document.addEventListener("DOMContentLoaded", async () => {
  const meta = document.querySelector('meta[name="livro-id"]');
  const livroId = meta?.getAttribute("content");
  const root = document.getElementById("livro-detalhe");
  if (!livroId || !root) return;

  root.innerHTML = `<h2>Carregando...</h2>`;

  try {
    const livro = await fetchJson(`/api/livros/${livroId}`);
    const autor = livro?.autor?.nome ?? "Autor(a) desconhecido(a)";
    const tipo = livro?.tipoArquivo ? String(livro.tipoArquivo) : "ARQUIVO";
    const tamanho = livro?.tamanhoFormatado ?? "";
    const publicado = fmtDate(livro?.dataPublicacao);

    const podeLer = !!livro?.temArquivo;
    const hrefLer = podeLer ? `/leitor/${livroId}` : "#";
    const hrefBaixar = podeLer ? `/api/livros/${livroId}/download` : "#";

    root.innerHTML = `
      <h2>${livro?.titulo ?? "Livro"}</h2>
      <div class="livro-meta">
        <span class="badge">${tipo}</span>
        <span>${autor}</span>
        ${livro?.idioma ? `<span class="badge">${livro.idioma}</span>` : ""}
        ${tamanho ? `<span class="badge">${tamanho}</span>` : ""}
        ${publicado ? `<span class="badge">Publicado: ${publicado}</span>` : ""}
      </div>

      ${livro?.descricao ? `<p>${livro.descricao}</p>` : `<p>Sem descrição.</p>`}

      <div class="livro-actions">
        <a class="btn btn-primary" href="${hrefLer}" ${podeLer ? "" : 'aria-disabled="true"'}>Ler agora</a>
        <a class="btn btn-secondary" href="${hrefBaixar}" ${podeLer ? "" : 'aria-disabled="true"'}>Baixar</a>
        <a class="btn btn-secondary" href="/livros">Voltar</a>
      </div>
    `;
  } catch (e) {
    root.innerHTML = `<h2>Livro não encontrado</h2><p>Não foi possível carregar os detalhes.</p><div class="livro-actions"><a class="btn btn-secondary" href="/livros">Voltar</a></div>`;
  }
});

