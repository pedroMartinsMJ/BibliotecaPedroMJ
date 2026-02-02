async function fetchJson(url) {
  const res = await fetch(url, { headers: { Accept: "application/json" } });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return await res.json();
}

function livroCard(livro) {
  const autor = livro?.autor?.nome ? `• ${livro.autor.nome}` : "";
  const tipo = livro?.tipoArquivo ? String(livro.tipoArquivo) : "ARQUIVO";

  return `
    <div class="livro-card" onclick="window.location.href='/livros/${livro.id}'">
      <div class="livro-image">
        <div class="livro-placeholder">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
          </svg>
        </div>
      </div>
      <div class="livro-info">
        <div class="livro-meta">
          <span class="badge">${tipo}</span>
          <span>${autor}</span>
        </div>
        <h3 class="livro-title">${livro.titulo ?? "Livro"}</h3>
        <button class="btn btn-secondary" type="button">Ver detalhes</button>
      </div>
    </div>
  `;
}

document.addEventListener("DOMContentLoaded", async () => {
  const root = document.getElementById("livros-destaque");
  if (!root) return;

  root.innerHTML = `<div class="livro-card"><div class="livro-info"><h3 class="livro-title">Carregando...</h3></div></div>`;

  try {
    const livros = await fetchJson("/api/livros");
    const destaque = Array.isArray(livros) ? livros.slice(0, 6) : [];

    if (destaque.length === 0) {
      root.innerHTML = `<div class="livro-card"><div class="livro-info"><h3 class="livro-title">Nenhum livro cadastrado ainda.</h3></div></div>`;
      return;
    }

    root.innerHTML = destaque.map(livroCard).join("");
  } catch (e) {
    root.innerHTML = `<div class="livro-card"><div class="livro-info"><h3 class="livro-title">Falha ao carregar livros.</h3></div></div>`;
  }
});

