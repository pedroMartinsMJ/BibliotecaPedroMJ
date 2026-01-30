// Validação do formulário de cadastro de autor
document.addEventListener('DOMContentLoaded', function() {
    const form = document.querySelector('.register-form');

    if (form) {
        const senha = document.getElementById('senha');
        const confirmarSenha = document.getElementById('confirmarSenha');
        const dataNascimento = document.getElementById('dataNascimento');

        // Validar senhas em tempo real
        if (confirmarSenha) {
            confirmarSenha.addEventListener('input', function() {
                if (senha.value !== confirmarSenha.value) {
                    confirmarSenha.setCustomValidity('As senhas não conferem');
                    mostrarErroInput(confirmarSenha, 'As senhas não conferem');
                } else {
                    confirmarSenha.setCustomValidity('');
                    limparErroInput(confirmarSenha);
                }
            });
        }

        // Validar idade mínima (14 anos)
        if (dataNascimento) {
            dataNascimento.addEventListener('change', function() {
                const hoje = new Date();
                const dataNasc = new Date(this.value);
                const idade = hoje.getFullYear() - dataNasc.getFullYear();

                if (idade < 14) {
                    this.setCustomValidity('Você deve ter pelo menos 14 anos');
                    mostrarErroInput(this, 'Você deve ter pelo menos 14 anos');
                } else {
                    this.setCustomValidity('');
                    limparErroInput(this);
                }
            });
        }

        // Validação ao submeter
        form.addEventListener('submit', function(e) {
            // Verificar se as senhas conferem
            if (senha.value !== confirmarSenha.value) {
                e.preventDefault();
                mostrarErroInput(confirmarSenha, 'As senhas não conferem');
                confirmarSenha.focus();
                return false;
            }

            // Verificar checkbox de termos
            const termos = form.querySelector('input[type="checkbox"]');
            if (!termos.checked) {
                e.preventDefault();
                alert('Você deve aceitar os Termos de Uso e Política de Privacidade');
                return false;
            }

            // Mostrar loading no botão
            const btnSubmit = form.querySelector('button[type="submit"]');
            btnSubmit.disabled = true;
            btnSubmit.innerHTML = '<span>Criando conta...</span>';
        });

        // Remover erros ao digitar
        const inputs = form.querySelectorAll('input');
        inputs.forEach(input => {
            input.addEventListener('input', function() {
                limparErroInput(this);
            });
        });
    }

    // Fechar alertas após alguns segundos
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 300);
        }, 5000);
    });
});

function mostrarErroInput(input, mensagem) {
    const formGroup = input.closest('.form-group');
    const existingError = formGroup.querySelector('.error-message');

    if (existingError && !existingError.getAttribute('th:if')) {
        existingError.textContent = mensagem;
    } else if (!existingError) {
        const errorDiv = document.createElement('span');
        errorDiv.className = 'error-message';
        errorDiv.textContent = mensagem;
        formGroup.appendChild(errorDiv);
    }

    input.classList.add('input-error');
}

function limparErroInput(input) {
    const formGroup = input.closest('.form-group');
    const errors = formGroup.querySelectorAll('.error-message:not([th\\:if])');

    errors.forEach(error => error.remove());
    input.classList.remove('input-error');
}

// Máscara para data (opcional - se quiser formato BR)
function formatarData(input) {
    let valor = input.value.replace(/\D/g, '');

    if (valor.length >= 8) {
        valor = valor.substring(0, 2) + '/' + valor.substring(2, 4) + '/' + valor.substring(4, 8);
    } else if (valor.length >= 4) {
        valor = valor.substring(0, 2) + '/' + valor.substring(2, 4) + '/' + valor.substring(4);
    } else if (valor.length >= 2) {
        valor = valor.substring(0, 2) + '/' + valor.substring(2);
    }

    input.value = valor;
}

// Validação de força da senha
function verificarForcaSenha(senha) {
    let forca = 0;

    if (senha.length >= 8) forca++;
    if (senha.length >= 12) forca++;
    if (/[a-z]/.test(senha) && /[A-Z]/.test(senha)) forca++;
    if (/\d/.test(senha)) forca++;
    if (/[^a-zA-Z\d]/.test(senha)) forca++;

    return forca;
}

// Mostrar indicador de força da senha
const senhaInput = document.getElementById('senha');
if (senhaInput) {
    senhaInput.addEventListener('input', function() {
        const forca = verificarForcaSenha(this.value);
        const formGroup = this.closest('.form-group');

        let indicadorExistente = formGroup.querySelector('.senha-forca');
        if (!indicadorExistente && this.value.length > 0) {
            const indicador = document.createElement('div');
            indicador.className = 'senha-forca';

            const labels = ['Muito fraca', 'Fraca', 'Média', 'Forte', 'Muito forte'];
            const classes = ['muito-fraca', 'fraca', 'media', 'forte', 'muito-forte'];

            indicador.innerHTML = `
                <div class="senha-barra">
                    <div class="senha-progresso ${classes[forca - 1] || 'muito-fraca'}"
                         style="width: ${(forca / 5) * 100}%"></div>
                </div>
                <span class="senha-label">${labels[forca - 1] || 'Muito fraca'}</span>
            `;

            formGroup.appendChild(indicador);
        } else if (indicadorExistente) {
            if (this.value.length === 0) {
                indicadorExistente.remove();
            } else {
                const labels = ['Muito fraca', 'Fraca', 'Média', 'Forte', 'Muito forte'];
                const classes = ['muito-fraca', 'fraca', 'media', 'forte', 'muito-forte'];

                const progresso = indicadorExistente.querySelector('.senha-progresso');
                const label = indicadorExistente.querySelector('.senha-label');

                progresso.className = `senha-progresso ${classes[forca - 1] || 'muito-fraca'}`;
                progresso.style.width = `${(forca / 5) * 100}%`;
                label.textContent = labels[forca - 1] || 'Muito fraca';
            }
        }
    });
}