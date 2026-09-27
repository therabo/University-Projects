(() => {
    'use strict';

    const number = new Intl.NumberFormat('it-IT');
    const decimal = new Intl.NumberFormat('it-IT', { maximumFractionDigits: 2 });
    const euro = new Intl.NumberFormat('it-IT', {
        style: 'currency', currency: 'EUR', notation: 'compact', maximumFractionDigits: 1
    });
    const svgNamespace = 'http://www.w3.org/2000/svg';
    const portraitFallback = './images/player-portrait-placeholder.svg';
    const byId = (id) => document.getElementById(id);
    const dateLabel = (value) => value
        ? new Intl.DateTimeFormat('it-IT', { dateStyle: 'medium', timeZone: 'UTC' }).format(new Date(`${value}T12:00:00Z`))
        : '—';
    const text = (id, value) => { byId(id).textContent = value ?? '—'; };
    const status = (id, message) => {
        const element = byId(id);
        element.textContent = message;
        element.hidden = !message;
    };

    async function readJson(url, options) {
        const response = await fetch(url, options);
        if (!response.ok) throw new Error(`Richiesta non riuscita (${response.status})`);
        return response.json();
    }

    function isTrustedPortraitUrl(value) {
        if (typeof value !== 'string') return false;
        try {
            const url = new URL(value);
            return url.protocol === 'https:' && url.hostname === 'img.a.transfermarkt.technology'
                && url.pathname.startsWith('/portrait/header/') && !url.username && !url.password;
        } catch (error) {
            return false;
        }
    }

    function renderProfile(profile) {
        text('player-name', profile.Name || 'Giocatore');
        text('player-subtitle', [profile.Position, profile.Team].filter(Boolean).join(' · ') || 'Profilo giocatore');
        document.title = `${profile.Name || 'Giocatore'} · Football Whispers`;
        const image = byId('player-image');
        image.alt = profile.Name ? `Ritratto di ${profile.Name}` : 'Ritratto giocatore';
        image.onerror = () => { image.onerror = null; image.src = portraitFallback; };
        image.src = isTrustedPortraitUrl(profile.ImageUrl) ? profile.ImageUrl : portraitFallback;
        text('player-team', profile.Team);
        text('player-position', profile.Position);
        text('player-nationality', profile.Nationality);
        text('player-birth', profile.DateOfBirth ? dateLabel(profile.DateOfBirth) : '—');
        text('player-age', Number.isInteger(profile.Age) ? `${profile.Age} anni` : '—');
        text('player-height', profile.Height ? `${profile.Height} cm` : '—');
        text('player-foot', profile.Foot === 'left' ? 'Sinistro' : profile.Foot === 'right' ? 'Destro' : profile.Foot === 'both' ? 'Entrambi' : '—');
    }

    function renderStats(data) {
        const seasonSelect = byId('player-season');
        const scopeSelect = byId('player-scope');
        seasonSelect.replaceChildren();
        if (!data.availableSeasons.length) {
            seasonSelect.add(new Option('Nessuna annata', ''));
            seasonSelect.disabled = true;
            scopeSelect.disabled = true;
        } else {
            for (const season of data.availableSeasons) seasonSelect.add(new Option(`${season}/${String(season + 1).slice(-2)}`, season));
            seasonSelect.value = String(data.season);
            seasonSelect.disabled = false;
            scopeSelect.disabled = false;
        }
        scopeSelect.value = data.scope;

        const summary = data.summary;
        text('stat-appearances', number.format(summary.appearances));
        text('stat-minutes', number.format(summary.minutes));
        text('stat-goals', number.format(summary.goals));
        text('stat-assists', number.format(summary.assists));
        text('stat-contributions', number.format(summary.goalContributions));
        text('stat-per90', summary.minutes ? decimal.format(summary.contributionsPer90) : '—');
        text('stat-goals-per90', summary.minutes ? decimal.format(summary.goalsPer90) : '—');
        text('stat-assists-per90', summary.minutes ? decimal.format(summary.assistsPer90) : '—');
        text('stat-yellow', number.format(summary.yellowCards));
        text('stat-red', number.format(summary.redCards));

        const rows = data.seasons.map((item) => {
            const row = document.createElement('tr');
            if (item.season === data.season) row.classList.add('is-selected');
            const cells = [
                `${item.season}/${String(item.season + 1).slice(-2)}`,
                number.format(item.appearances), number.format(item.minutes),
                number.format(item.goals), number.format(item.assists),
                item.minutes ? decimal.format(item.contributionsPer90) : '—'
            ];
            for (const value of cells) {
                const cell = document.createElement('td');
                cell.textContent = value;
                row.append(cell);
            }
            return row;
        });
        const body = byId('season-rows');
        if (rows.length) body.replaceChildren(...rows);
        else {
            const row = document.createElement('tr');
            const cell = document.createElement('td');
            cell.colSpan = 6;
            cell.textContent = 'Nessuna presenza nell’archivio.';
            row.append(cell);
            body.replaceChildren(row);
        }

        status('stats-status', data.season === null ? 'Nessuna partita registrata per questo giocatore.'
            : summary.appearances === 0 ? 'Nessuna presenza registrata per questo filtro.' : '');
    }

    function svgElement(tag, attributes = {}) {
        const element = document.createElementNS(svgNamespace, tag);
        for (const [name, value] of Object.entries(attributes)) element.setAttribute(name, String(value));
        return element;
    }

    function drawChart(history) {
        const container = byId('valuation-chart');
        container.replaceChildren();
        if (!history.length) return;
        const width = 960;
        const height = 260;
        const left = 72;
        const right = 24;
        const top = 24;
        const bottom = 210;
        const maxValue = Math.max(...history.map((point) => point.valueEur), 1);
        const timestamps = history.map((point) => Date.parse(`${point.date}T00:00:00Z`));
        const firstTimestamp = timestamps[0];
        const timespan = Math.max(timestamps.at(-1) - firstTimestamp, 1);
        const x = (index) => left + (timestamps[index] - firstTimestamp) * (width - left - right) / timespan;
        const y = (value) => bottom - value / maxValue * (bottom - top);
        const svg = svgElement('svg', { viewBox: `0 0 ${width} ${height}`, role: 'presentation', preserveAspectRatio: 'none' });
        const defs = svgElement('defs');
        const gradient = svgElement('linearGradient', { id: 'valuation-fill', x1: '0', x2: '0', y1: '0', y2: '1' });
        gradient.append(svgElement('stop', { offset: '0%', 'stop-color': '#e3c977', 'stop-opacity': '.38' }));
        gradient.append(svgElement('stop', { offset: '100%', 'stop-color': '#e3c977', 'stop-opacity': '0' }));
        defs.append(gradient);
        svg.append(defs);
        for (const fraction of [0, 0.5, 1]) {
            const ordinate = y(maxValue * fraction);
            svg.append(svgElement('line', { x1: left, x2: width - right, y1: ordinate, y2: ordinate, stroke: '#ffffff', 'stroke-opacity': '.15' }));
            const label = svgElement('text', { x: left - 12, y: ordinate + 5, 'text-anchor': 'end' });
            label.textContent = euro.format(maxValue * fraction);
            svg.append(label);
        }
        const points = history.map((item, index) => `${x(index)},${y(item.valueEur)}`);
        const area = `M ${left},${bottom} L ${points.join(' L ')} L ${x(history.length - 1)},${bottom} Z`;
        svg.append(svgElement('path', { d: area, fill: 'url(#valuation-fill)' }));
        svg.append(svgElement('polyline', {
            points: points.join(' '), fill: 'none', stroke: '#e3c977', 'stroke-width': 3.5,
            'stroke-linecap': 'round', 'stroke-linejoin': 'round'
        }));
        for (const index of [...new Set([0, history.length - 1])]) {
            const point = history[index];
            svg.append(svgElement('circle', { cx: x(index), cy: y(point.valueEur), r: 5, fill: '#e3c977' }));
            const label = svgElement('text', { x: x(index), y: 243, 'text-anchor': index ? 'end' : 'start' });
            label.textContent = dateLabel(point.date);
            svg.append(label);
        }
        container.append(svg);
        container.setAttribute('aria-label', `Valore di mercato: da ${euro.format(history[0].valueEur)} il ${dateLabel(history[0].date)} a ${euro.format(history.at(-1).valueEur)} il ${dateLabel(history.at(-1).date)}.`);
    }

    function renderValuations(data) {
        if (!data.summary) {
            status('valuation-status', 'Nessuna rilevazione di valore disponibile nel database.');
            return;
        }
        text('valuation-latest', euro.format(data.summary.latestValueEur));
        text('valuation-latest-date', dateLabel(data.summary.latestDate));
        text('valuation-peak', euro.format(data.summary.peakValueEur));
        text('valuation-peak-date', dateLabel(data.summary.peakDate));
        drawChart(data.history);
        status('valuation-status', '');
    }

    document.addEventListener('DOMContentLoaded', async () => {
        const query = new URLSearchParams(window.location.search);
        const playerName = query.get('player')?.trim();
        const playerId = query.get('playerId');
        const hasPlayerId = /^[1-9][0-9]*$/.test(playerId || '') && Number.isSafeInteger(Number(playerId));
        if (!hasPlayerId && !playerName) {
            text('player-name', 'Giocatore non specificato');
            text('player-error', 'Apri questa pagina dalla lista giocatori o da una scheda squadra.');
            byId('player-error').hidden = false;
            return;
        }
        try {
            const profile = hasPlayerId
                ? await readJson(`/api/players/${playerId}/profile`)
                : await readJson('/info_player', {
                    method: 'POST', headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ playerName })
                });
            if (!Number.isSafeInteger(profile.PlayerId) || profile.PlayerId <= 0) throw new Error('Giocatore non trovato nel catalogo');
            renderProfile(profile);
            let requestNumber = 0;
            async function loadStats() {
                const current = ++requestNumber;
                const params = new URLSearchParams({ scope: byId('player-scope').value });
                if (byId('player-season').value) params.set('season', byId('player-season').value);
                status('stats-status', 'Caricamento statistiche…');
                try {
                    const stats = await readJson(`/api/players/${profile.PlayerId}/stats?${params}`);
                    if (current === requestNumber) renderStats(stats);
                } catch (error) {
                    if (current === requestNumber) status('stats-status', 'Statistiche temporaneamente non disponibili.');
                    console.error(error);
                }
            }
            byId('player-season').addEventListener('change', loadStats);
            byId('player-scope').addEventListener('change', loadStats);
            await Promise.all([
                loadStats(),
                readJson(`/api/players/${profile.PlayerId}/valuations`).then(renderValuations).catch((error) => {
                    status('valuation-status', 'Storico valori temporaneamente non disponibile.');
                    console.error(error);
                })
            ]);
        } catch (error) {
            text('player-name', 'Profilo non disponibile');
            text('player-error', 'Non è stato possibile caricare questo giocatore. Torna alla lista e riprova.');
            byId('player-error').hidden = false;
            console.error(error);
        }
    });
})();
