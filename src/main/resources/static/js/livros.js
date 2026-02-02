async function fetchJson(url) {
  const res = await fetch(url, { headers: { Accept: "application/json" } });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return await res.json();
}

function normalize(s) {
  return String(s ?? "")
    .toLowerCase()
    .normalize("NFD")
    .replace(/\p{Diacritic}/gu, "");
}

function livroCard(livro) {
  const autor = livro?.autor?.nome ?? "Autor(a) desconhecido(a)";
  const idioma = livro?.idioma ? `• ${livro.idioma}` : "";
  const tipo = livro?.tipoArquivo ? String(livro.tipoArquivo) : "ARQUIVO";
  const tamanho = livro?.tamanhoFormatado ? `• ${livro.tamanhoFormatado}` : "";

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
          <span>${autor} ${idioma} ${tamanho}</span>
        </div>
        <h3 class="livro-title">${livro?.titulo ?? "Livro"}</h3>
        <button class="btn btn-secondary" type="button">Abrir</button>
      </div>
    </div>
  `;
}

document.addEventListener("DOMContentLoaded", async () => {
  const grid = document.getElementById("livros-grid");
  const q = document.getElementById("q");
  if (!grid || !q) return;

  grid.innerHTML = `<div class="livro-card"><div class="livro-info"><h3 class="livro-title">Carregando...</h3></div></div>`;

  let livros = [];
  try {
    const data = await fetchJson("/api/livros");
    livros = Array.isArray(data) ? data : [];
  } catch (e) {
    grid.innerHTML = `<div class="livro-card"><div class="livro-info"><h3 class="livro-title">Falha ao carregar o catálogo.</h3></div></div>`;
    return;
  }

  function render() {
    const term = normalize(q.value);
    const filtered = livros.filter((l) => {
      const hay = normalize(
        [
          l?.titulo,
          l?.descricao,
          l?.autor?.nome,
          l?.autor?.username,
          l?.idioma,
          l?.editora,
          l?.isbn,
        ].filter(Boolean).join(" ")
      );
      return hay.includes(term);
    });

    if (filtered.length === 0) {
      grid.innerHTML = `<div class="livro-card"><div class="livro-info"><h3 class="livro-title">Nenhum resultado.</h3></div></div>`;
      return;
    }

    grid.innerHTML = filtered.map(livroCard).join("");
  }

  q.addEventListener("input", render);
  render();
});

