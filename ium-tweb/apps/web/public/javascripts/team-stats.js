(function () {
    'use strict';

    const number = new Intl.NumberFormat('it-IT', { maximumFractionDigits: 2 });
    const date = new Intl.DateTimeFormat('it-IT', {
        day: '2-digit', month: 'short', year: 'numeric'
    });
    let requestToken = 0;
    let activeClubId = null;
    let activeSeason = null;

    function element(tag, className, value) {
        const node = document.createElement(tag);
        if (className) node.className = className;
        if (value !== undefined) node.textContent = value;
        return node;
    }

    function formatDate(value) {
        return value ? date.format(new Date(`${value}T12:00:00`)) : '—';
    }

    function metric(label, value) {
        const row = element('div', 'team-stats-metric');
        row.append(element('span', '', label), element('strong', '', number.format(value)));
        return row;
    }

    function renderSummary(summary) {
        const fields = [
            ['Partite', summary.matches], ['Vittorie', summary.wins],
            ['Pareggi', summary.draws], ['Sconfitte', summary.losses],
            ['Gol fatti', summary.goalsFor], ['Gol subiti', summary.goalsAgainst]
        ];
        const cards = fields.map(function ([label, value]) {
            const card = element('div', 'team-stats-kpi');
            card.append(element('span', 'team-stats-kpi-label', label),
                element('strong', 'team-stats-kpi-value', number.format(value)));
            return card;
        });
        document.getElementById('stats-summary').replaceChildren(...cards);
    }

    function renderTrend(matches) {
        const maxGoals = Math.max(3, ...matches.flatMap((match) => [match.goalsFor, match.goalsAgainst]));
        const rows = matches.map(function (match) {
            const outcomeLabel = { win: 'V', draw: 'P', loss: 'S' }[match.outcome] || '—';
            const row = element('div', 'team-stats-match');
            row.setAttribute('aria-label', `${formatDate(match.date)}: ${match.venue === 'home' ? 'in casa' : 'in trasferta'} `
                + `contro ${match.opponent}, ${match.goalsFor} a ${match.goalsAgainst}`);
            const result = element('span', `team-stats-outcome team-stats-outcome--${match.outcome}`, outcomeLabel);
            const identity = element('div', 'team-stats-match-identity');
            identity.append(element('strong', '', match.opponent || 'Avversario'),
                element('small', '', `${formatDate(match.date)} · ${match.venue === 'home' ? 'Casa' : 'Trasferta'}`));
            const bars = element('div', 'team-stats-bars');
            for (const [kind, count] of [['for', match.goalsFor], ['against', match.goalsAgainst]]) {
                const track = element('span', 'team-stats-bar-track');
                const bar = element('span', `team-stats-bar team-stats-bar--${kind}`);
                bar.style.width = `${count / maxGoals * 100}%`;
                track.append(bar);
                bars.append(track);
            }
            row.append(result, identity, bars,
                element('strong', 'team-stats-score', `${match.goalsFor}–${match.goalsAgainst}`));
            return row;
        });
        document.getElementById('stats-trend').replaceChildren(...rows);
    }

    function renderDetails(data) {
        const attack = document.getElementById('stats-attack');
        const leaders = element('div', 'team-stats-leaders');
        leaders.append(element('h4', '', 'Migliori marcatori'));
        if (data.attack.topScorers.length) {
            data.attack.topScorers.forEach((player) => leaders.append(metric(player.name, player.goals)));
        } else {
            leaders.append(element('p', 'team-stats-muted', 'Nessun marcatore registrato.'));
        }
        attack.replaceChildren(
            metric('Gol per partita', data.summary.goalsPerMatch),
            metric('Assist registrati', data.attack.assists),
            metric('Giocatori impiegati', data.attack.playersUsed),
            leaders
        );
        document.getElementById('stats-defense').replaceChildren(
            metric('Reti inviolate', data.summary.cleanSheets),
            metric('Subiti per partita', data.summary.concededPerMatch),
            metric('Cartellini gialli', data.defense.yellowCards),
            metric('Cartellini rossi', data.defense.redCards)
        );
    }

    async function load(clubId, season) {
        activeClubId = clubId;
        activeSeason = season;
        const token = ++requestToken;
        const status = document.getElementById('stats-status');
        const content = document.getElementById('stats-content');
        const scopeSelect = document.getElementById('stats-scope');
        content.hidden = true;
        status.hidden = false;
        status.textContent = 'Caricamento statistiche…';
        scopeSelect.disabled = true;
        if (season === null) {
            status.textContent = 'Nessuna partita registrata per questa squadra.';
            return;
        }

        try {
            const response = await axios.get(`/clubs/${clubId}/stats`, {
                params: { season, scope: scopeSelect.value }
            });
            if (token !== requestToken) return;
            const data = response.data;
            scopeSelect.disabled = false;
            if (data.summary.matches === 0) {
                status.textContent = data.coverage.missingResults
                    ? 'Risultati non disponibili per le partite registrate.'
                    : 'Nessuna partita registrata in questa competizione e annata.';
                return;
            }
            renderSummary(data.summary);
            renderTrend(data.recentMatches);
            renderDetails(data);
            status.hidden = true;
            content.hidden = false;
        } catch (error) {
            if (token !== requestToken) return;
            console.error('Impossibile caricare le statistiche stagionali:', error);
            status.textContent = 'Statistiche non disponibili. Riprova cambiando competizione o annata.';
            scopeSelect.disabled = false;
        }
    }

    function unavailable(message) {
        ++requestToken;
        activeClubId = null;
        activeSeason = null;
        document.getElementById('stats-content').hidden = true;
        document.getElementById('stats-scope').disabled = true;
        const status = document.getElementById('stats-status');
        status.hidden = false;
        status.textContent = message;
    }

    document.addEventListener('DOMContentLoaded', function () {
        document.getElementById('stats-scope').addEventListener('change', function () {
            if (activeClubId !== null && activeSeason !== null) load(activeClubId, activeSeason);
        });
    });

    window.TeamSeasonStats = { load, unavailable };
}());
