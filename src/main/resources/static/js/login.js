document.getElementById('loginForm').addEventListener('submit', function(e) {
    e.preventDefault();
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    fetch('http://localhost:8080/api/Auth/Login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
    })
        .then(response => response.text()) // Cambia a .json() si el backend responde con JSON
        .then(msg => {
            alert('Login exitoso: ' + msg);
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error en el login');
        });
});