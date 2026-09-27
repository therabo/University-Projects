document.addEventListener('DOMContentLoaded', function() {

    localStorage.setItem('campionato', 'serie-a');

    const mostraDiv2 = document.getElementById('mostra-div-2');
    const div1 = document.getElementById('div-1');
    const div2 = document.getElementById('div-2');
    const mostraDiv3 = document.getElementById('mostra-div-3')
    const div3 = document.getElementById('div-3')
    const mostraDiv1 = document.getElementById('mostra-div-1')
    const loginForm = document.getElementById('login-form');
    const usernameInput = document.getElementById('username');

    mostraDiv2.addEventListener('click', function(event) {
        event.preventDefault();

        if (!div1.classList.contains('hidden')) {
            div1.classList.add('hidden');
            div1.classList.remove('visible');
            div2.classList.remove('hidden');
            div2.classList.add('visible');
        } else {
            div2.classList.add('hidden');
            div2.classList.remove('visible');
            div1.classList.remove('hidden');
            div1.classList.add('visible');
        }
    });

    mostraDiv3.addEventListener('click', function(event) {
        event.preventDefault();

        if (!div2.classList.contains('hidden')) {
            div2.classList.add('hidden');
            div2.classList.remove('visible');
            div3.classList.remove('hidden');
            div3.classList.add('visible');
        } else {
            div3.classList.add('hidden');
            div3.classList.remove('visible');
            div2.classList.remove('hidden');
            div2.classList.add('visible');
        }
    });

    mostraDiv1.addEventListener('click', function(event) {
        event.preventDefault();

        if (!div3.classList.contains('hidden')) {
            div3.classList.add('hidden');
            div3.classList.remove('visible');
            div1.classList.remove('hidden');
            div1.classList.add('visible');
        } else {
            div1.classList.add('hidden');
            div1.classList.remove('visible');
            div3.classList.remove('hidden');
            div3.classList.add('visible');
        }
    });

    usernameInput.addEventListener('input', function() {
        usernameInput.setCustomValidity('');
    });

    loginForm.addEventListener('submit', function(event) {
        event.preventDefault();
        const username = usernameInput.value.replace(/[\u0000-\u001f\u007f<>]/g, '').replace(/\s+/g, ' ').trim();
        if (!username) {
            usernameInput.setCustomValidity('Inserisci un nome utente valido.');
            usernameInput.reportValidity();
            return;
        }
        try {
            localStorage.setItem('chat_username', username);
        } catch (error) {
            usernameInput.setCustomValidity('Impossibile salvare il nome utente in questo browser.');
            usernameInput.reportValidity();
            return;
        }
        const next = new URLSearchParams(window.location.search).get('next');
        window.location.href = next === 'channel.html' ? 'channel.html' : 'index.html';
    });

});
