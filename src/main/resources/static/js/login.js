document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const messageDiv = document.getElementById('message');

    const showMessage = (message, isSuccess) => {
        messageDiv.textContent = message;
        messageDiv.style.display = 'block';
        messageDiv.className = 'message';
        messageDiv.classList.add(isSuccess ? 'success' : 'error');
    };

    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        messageDiv.style.display = 'none';

        const email = document.getElementById('loginEmail').value;
        const password = document.getElementById('loginPassword').value;
        const data = { email, pass: password };

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data),
            });

            if (response.ok) {
                const result = await response.json();
                localStorage.setItem('jwtToken', result.accessToken);
                showMessage('Login bem-sucedido! Redirecionando...', true);
                setTimeout(() => {
                    // Redireciona para sua página principal
                    window.location.href = '/home';
                }, 1500);
            } else {
                const errorText = await response.text();
                showMessage(errorText || 'Credenciais inválidas.', false);
            }
        } catch (error) {
            showMessage('Erro de conexão. Tente novamente.', false);
        }
    });
});