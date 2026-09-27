document.addEventListener('DOMContentLoaded', () => {
    'use strict';

    const socket = io();
    const roomList = document.getElementById('chat-room-list');
    const roomStatus = document.getElementById('room-list-status');
    const connectionStatus = document.getElementById('chat-connection-status');
    const roomTitle = document.getElementById('chat-room-title');
    const loginLink = document.getElementById('chat-login-link');
    const leaveButton = document.getElementById('chat-leave');
    const messages = document.getElementById('chat-messages');
    const users = document.getElementById('chat-users');
    const userCount = document.getElementById('chat-presence-count');
    const composer = document.getElementById('chat-composer');
    const messageInput = document.getElementById('chat-message');
    const sendButton = document.getElementById('chat-send');
    const generalRoom = { id: 'general', label: 'Chat generale', type: 'general' };
    let rooms = [generalRoom];
    let activeRoomId = null;
    let desiredRoomId = null;
    let joinSequence = 0;
    let loginUsername = '';

    try {
        loginUsername = (localStorage.getItem('chat_username') || '').trim();
    } catch (error) {
    }
    loginLink.hidden = Boolean(loginUsername);

    function setPresence(names) {
        const fragment = document.createDocumentFragment();
        names.forEach((name) => {
            const item = document.createElement('li');
            item.textContent = name;
            fragment.append(item);
        });
        users.replaceChildren(fragment);
        userCount.textContent = String(names.length);
    }

    function setSelectedRoom(roomId) {
        roomList.querySelectorAll('[data-room-id]').forEach((button) => {
            if (button.dataset.roomId === roomId) button.setAttribute('aria-current', 'true');
            else button.removeAttribute('aria-current');
        });
    }

    function resetConversation() {
        activeRoomId = null;
        roomTitle.textContent = 'Seleziona un canale';
        connectionStatus.textContent = !loginUsername ? '' : socket.connected ? 'Pronto a chattare' : 'Connessione interrotta';
        messages.replaceChildren();
        const placeholder = document.createElement('p');
        placeholder.id = 'chat-placeholder';
        placeholder.className = 'chat-placeholder';
        placeholder.textContent = 'Scegli un canale per iniziare a chattare.';
        messages.append(placeholder);
        setPresence([]);
        setSelectedRoom(null);
        messageInput.disabled = true;
        sendButton.disabled = true;
        leaveButton.disabled = true;
    }

    function appendMessage(element, shouldFollow = false) {
        const nearBottom = messages.scrollHeight - messages.scrollTop - messages.clientHeight < 90;
        document.getElementById('chat-placeholder')?.remove();
        messages.append(element);
        if (nearBottom || shouldFollow) messages.scrollTop = messages.scrollHeight;
    }

    function addSystemMessage(text) {
        const item = document.createElement('p');
        item.className = 'chat-system-message';
        item.textContent = text;
        appendMessage(item);
    }

    function addChatMessage(message) {
        const own = message.senderId === socket.id;
        const article = document.createElement('article');
        article.className = own ? 'chat-message chat-message--own' : 'chat-message';
        const meta = document.createElement('div');
        meta.className = 'chat-message__meta';
        const author = document.createElement('strong');
        author.textContent = own ? 'Tu' : message.username;
        const time = document.createElement('time');
        const date = new Date(message.createdAt);
        time.dateTime = message.createdAt;
        time.textContent = Number.isNaN(date.getTime()) ? '' : date.toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' });
        const text = document.createElement('p');
        text.className = 'chat-message__text';
        text.textContent = message.text;
        meta.append(author, time);
        article.append(meta, text);
        appendMessage(article, own);
    }

    function renderRooms() {
        const fragment = document.createDocumentFragment();
        rooms.forEach((room) => {
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'chat-room-button';
            button.dataset.roomId = room.id;
            button.textContent = room.label;
            button.disabled = !loginUsername;
            button.addEventListener('click', () => joinRoom(room.id));
            fragment.append(button);
        });
        roomList.replaceChildren(fragment);
        setSelectedRoom(activeRoomId);
    }

    function joinRoom(roomId) {
        if (!loginUsername) return;
        const room = rooms.find((entry) => entry.id === roomId);
        if (!room) return;
        desiredRoomId = roomId;
        if (!socket.connected) {
            roomStatus.textContent = 'Connessione in corso. Il canale si aprirà automaticamente.';
            return;
        }
        const sequence = ++joinSequence;
        roomStatus.textContent = `Accesso a ${room.label}…`;
        socket.timeout(5000).emit('chat:join', { roomId, username: loginUsername }, (error, response) => {
            if (sequence !== joinSequence) return;
            if (error || !response?.ok) {
                roomStatus.textContent = response?.error || 'Impossibile entrare nel canale. Riprova.';
                return;
            }
            const changedRoom = activeRoomId !== response.room.id;
            activeRoomId = response.room.id;
            roomTitle.textContent = response.room.label;
            connectionStatus.textContent = `Connesso come ${response.username}`;
            roomStatus.textContent = '';
            if (changedRoom) messages.replaceChildren();
            setPresence(response.users);
            setSelectedRoom(activeRoomId);
            messageInput.disabled = false;
            sendButton.disabled = false;
            leaveButton.disabled = false;
            messageInput.focus();
        });
    }

    async function loadRooms() {
        roomStatus.textContent = 'Caricamento dei campionati…';
        try {
            const response = await fetch('/api/chat/rooms');
            if (!response.ok) throw new Error('Chat room request failed');
            const data = await response.json();
            if (!Array.isArray(data)) throw new Error('Invalid chat room response');
            const valid = data.filter((room) => room && typeof room.id === 'string' && typeof room.label === 'string');
            rooms = [generalRoom, ...valid.filter((room) => room.id !== 'general')];
            renderRooms();
            roomStatus.textContent = '';
        } catch (error) {
            console.error('Impossibile caricare i canali dei campionati.', error);
            roomStatus.textContent = 'Campionati non disponibili. La chat generale rimane accessibile.';
        }
    }

    leaveButton.addEventListener('click', () => {
        joinSequence += 1;
        desiredRoomId = null;
        socket.emit('chat:leave');
        roomStatus.textContent = '';
        resetConversation();
    });

    composer.addEventListener('submit', (event) => {
        event.preventDefault();
        const text = messageInput.value.trim();
        if (!activeRoomId || !text) return;
        sendButton.disabled = true;
        socket.timeout(5000).emit('chat:send', { text }, (error, response) => {
            sendButton.disabled = !activeRoomId;
            if (error || !response?.ok) {
                connectionStatus.textContent = response?.error || 'Invio non riuscito. Riprova.';
                return;
            }
            if (messageInput.value.trim() === text) messageInput.value = '';
            messageInput.focus();
        });
    });

    socket.on('connect', () => {
        connectionStatus.textContent = loginUsername ? 'Pronto a chattare' : '';
        if (desiredRoomId) joinRoom(desiredRoomId);
    });
    socket.on('disconnect', () => {
        activeRoomId = null;
        connectionStatus.textContent = 'Connessione interrotta. Riconnessione in corso…';
        messageInput.disabled = true;
        sendButton.disabled = true;
    });
    socket.on('chat:presence', (payload) => {
        if (payload?.roomId === activeRoomId && Array.isArray(payload.users)) setPresence(payload.users);
    });
    socket.on('chat:system', (payload) => {
        if (payload?.roomId === activeRoomId && typeof payload.text === 'string') addSystemMessage(payload.text);
    });
    socket.on('chat:message', (payload) => {
        if (payload?.roomId === activeRoomId && typeof payload.text === 'string') addChatMessage(payload);
    });

    window.addEventListener('storage', (event) => {
        if (event.key === 'chat_username') window.location.reload();
    });

    renderRooms();
    if (!loginUsername) connectionStatus.textContent = '';
    loadRooms();
});
