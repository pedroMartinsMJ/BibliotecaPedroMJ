async function fetchJson(url) {
  const res = await fetch(url, { headers: { Accept: "application/json" } });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return await res.json();
}

function showError(msg) {
  const box = document.getElementById("postagem-erro");
  const span = document.getElementById("postagem-erro-msg");
  if (span) span.textContent = msg;
  if (box) box.hidden = false;
}

function clearError() {
  const box = document.getElementById("postagem-erro");
  const span = document.getElementById("postagem-erro-msg");
  if (box) box.hidden = true;
  if (span) span.textContent = "";
}

// Atualização na renderização
async function render() {
  const filtered = livros.filter(...);

  // Para cada livro, fazemos duas requisições separadas para economizar tráfego
  filtered.forEach(async (livro) => {
    // 1. Metadados (Rápido)
    const meta = await fetchJson(`/api/livros/${livro.id}`);

    // 2. Capa (Rápida, Cacheável)
    const capaUrl = await fetchJson(`/api/livros/${livro.id}/capa`);

    // Renderiza o card com metadados e imagem da capa
    livroCard(livro, meta, capaUrl);
  });
}


document.addEventListener("DOMContentLoaded", async () => {
  const form = document.querySelector('.postagem-form');
  const fileInput = document.getElementById('arquivo');
  const titleInput = document.getElementById('titulo');
  const authorInput = document.getElementById('autor');
  const descInput = document.getElementById('descricao');

  if (form) {
    // Validação em tempo real
    const inputs = form.querySelectorAll('input[type="text"], textarea');
    inputs.forEach(input => {
      input.addEventListener('input', function() {
        clearError();
      });
    });

    // Validação de arquivo
    if (fileInput) {
      fileInput.addEventListener('change', function() {
        clearError();
      });
    }

    // Validação de checkbox
    const checkbox = form.querySelector('input[type="checkbox"]');
    if (checkbox) {
      checkbox.addEventListener('change', function() {
        clearError();
      });
    }

    // Submissão do Formulário
    form.addEventListener('submit', async (e) => {
      e.preventDefault();

      // Verificar campos obrigatórios
      let isValid = true;

      if (!titleInput.value.trim()) {
        showError("Por favor, insira o título do livro.");
        titleInput.classList.add('input-error');
        isValid = false;
      } else {
        titleInput.classList.remove('input-error');
      }

      if (!authorInput.value.trim()) {
        showError("Por favor, insira seu nome.");
        authorInput.classList.add('input-error');
        isValid = false;
      } else {
        authorInput.classList.remove('input-error');
      }

      if (!fileInput.files.length) {
        showError("Por favor, selecione um arquivo (PDF ou EPUB).");
        fileInput.classList.add('input-error');
        isValid = false;
      } else {
        fileInput.classList.remove('input-error');
      }

      if (!checkbox.checked) {
        showError("Você deve aceitar os Termos de Uso.");
        isValid = false;
      }

      if (!isValid) return;

      // Simulação de Upload
      const btnSubmit = form.querySelector('button[type="submit"]');
      btnSubmit.disabled = true;
      btnSubmit.innerHTML = '<span>Enviando livro...</span>';

      const formData = new FormData();
      formData.append('titulo', titleInput.value);
      formData.append('autor', authorInput.value);
      formData.append('descricao', descInput.value);
      formData.append('arquivo', fileInput.files[0]);

      try {
        // Supondo que a API aceita POST em /api/livros/upload
        const res = await fetch('/api/livros/upload', {
          method: 'POST',
          body: formData
        });

        if (res.ok) {
          const data = await res.json();
          // Sucesso
          const successAlert = document.createElement('div');
          successAlert.className = 'alert alert-success';
          successAlert.innerHTML = `
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
            <span>Seu livro foi publicado com sucesso!</span>
          `;
          form.insertBefore(successAlert, btnSubmit);

          // Redirecionar
          setTimeout(() => {
            window.location.href = '/livros';
          }, 2000);
        } else {
          throw new Error("Falha ao enviar arquivo.");
        }
      } catch (e) {
        // Erro
        showError("Erro ao enviar livro. Tente novamente.");
        btnSubmit.disabled = false;
        btnSubmit.innerHTML = '<span>Publicar Livro</span>';
      }
    });
  }
});
