document.addEventListener('DOMContentLoaded', () => {
    const registerForm = document.getElementById('registerForm');
    const messageDiv = document.getElementById('message');

    const showMessage = (message, isSuccess) => {
        messageDiv.textContent = message;
        messageDiv.style.display = 'block';
        messageDiv.className = 'message';
        messageDiv.classList.add(isSuccess ? 'success' : 'error');
    };

    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        messageDiv.style.display = 'none';

        const name = document.getElementById('registerName').value;
        const email = document.getElementById('registerEmail').value;
        const password = document.getElementById('registerPassword').value;
        const data = { name, email, pass: password };

        try {
            const response = await fetch('/api/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data),
            });

            if (response.ok) {
                showMessage('Registro bem-sucedido! Redirecionando para o login...', true);
                setTimeout(() => {
                    window.location.href = '/login';
                }, 2000);
            } else {
                const errorText = await response.text();
                showMessage(errorText || 'Não foi possível registrar.', false);
            }
        } catch (error) {
            showMessage('Erro de conexão. Tente novamente.', false);
        }
    });
});