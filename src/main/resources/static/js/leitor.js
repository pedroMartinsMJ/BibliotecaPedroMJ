async function fetchJson(url) {
  const res = await fetch(url, { headers: { Accept: "application/json" } });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return await res.json();
}

function showError(msg) {
  const box = document.getElementById("leitor-erro");
  const span = document.getElementById("leitor-erro-msg");
  if (span) span.textContent = msg;
  if (box) box.hidden = false;
}

async function initPdf(url) {
  // pdfjsLib é global (carregado via CDN)
  // eslint-disable-next-line no-undef
  pdfjsLib.GlobalWorkerOptions.workerSrc =
    "https://cdnjs.cloudflare.com/ajax/libs/pdf.js/4.10.38/pdf.worker.min.js";

  const pdfRoot = document.getElementById("pdf-root");
  const canvas = document.getElementById("pdf-canvas");
  const pageEl = document.getElementById("pdf-page");
  const pagesEl = document.getElementById("pdf-pages");
  const btnPrev = document.getElementById("pdf-prev");
  const btnNext = document.getElementById("pdf-next");
  const btnZoomIn = document.getElementById("pdf-zoom-in");
  const btnZoomOut = document.getElementById("pdf-zoom-out");

  if (!pdfRoot || !canvas || !pageEl || !pagesEl) throw new Error("UI PDF incompleta");
  pdfRoot.hidden = false;

  // eslint-disable-next-line no-undef
  const loadingTask = pdfjsLib.getDocument(url);
  const pdf = await loadingTask.promise;

  let pageNum = 1;
  let scale = 1.2;

  pagesEl.textContent = String(pdf.numPages);

  async function render() {
    const page = await pdf.getPage(pageNum);
    const viewport = page.getViewport({ scale });
    const ctx = canvas.getContext("2d");
    canvas.height = viewport.height;
    canvas.width = viewport.width;
    pageEl.textContent = String(pageNum);

    await page.render({ canvasContext: ctx, viewport }).promise;
  }

  btnPrev?.addEventListener("click", async () => {
    if (pageNum <= 1) return;
    pageNum -= 1;
    await render();
  });

  btnNext?.addEventListener("click", async () => {
    if (pageNum >= pdf.numPages) return;
    pageNum += 1;
    await render();
  });

  btnZoomIn?.addEventListener("click", async () => {
    scale = Math.min(3.0, scale + 0.2);
    await render();
  });

  btnZoomOut?.addEventListener("click", async () => {
    scale = Math.max(0.6, scale - 0.2);
    await render();
  });

  await render();
}

async function initEpub(url) {
  const epubRoot = document.getElementById("epub-root");
  const viewer = document.getElementById("epub-viewer");
  const btnPrev = document.getElementById("epub-prev");
  const btnNext = document.getElementById("epub-next");

  if (!epubRoot || !viewer) throw new Error("UI EPUB incompleta");
  epubRoot.hidden = false;

  // ePub é global (carregado via CDN)
  // eslint-disable-next-line no-undef
  const book = ePub(url);
  const rendition = book.renderTo(viewer, { width: "100%", height: "100%" });
  await rendition.display();

  btnPrev?.addEventListener("click", () => rendition.prev());
  btnNext?.addEventListener("click", () => rendition.next());
}

document.addEventListener("DOMContentLoaded", async () => {
  const meta = document.querySelector('meta[name="livro-id"]');
  const livroId = meta?.getAttribute("content");
  const title = document.getElementById("leitor-title");
  const btnBaixar = document.getElementById("btn-baixar");

  if (!livroId) return;

  try {
    const livro = await fetchJson(`/api/livros/${livroId}`);
    if (title) title.textContent = `${livro?.titulo ?? "Livro"} — ${livro?.autor?.nome ?? ""}`.trim();

    const downloadUrl = `/api/livros/${livroId}/download`;
    const inlineUrl = `/api/livros/${livroId}/download?inline=true`;

    if (btnBaixar) btnBaixar.setAttribute("href", downloadUrl);

    const tipo = String(livro?.tipoArquivo ?? "").toUpperCase();
    if (!livro?.temArquivo) {
      showError("Este livro não possui arquivo disponível.");
      return;
    }

    if (tipo === "PDF") {
      await initPdf(inlineUrl);
      return;
    }

    if (tipo === "EPUB") {
      await initEpub(inlineUrl);
      return;
    }

    showError("Tipo de arquivo não suportado neste leitor.");
  } catch (e) {
    showError("Falha ao carregar o livro. Tente novamente.");
  }
});

