document.addEventListener('DOMContentLoaded', function () {
    'use strict';

    const form = document.getElementById('advanced-search-form');
    const resultsBody = document.getElementById('players-results-body');
    const resultsCount = document.getElementById('results-count');
    const feedback = document.getElementById('search-feedback');
    const submitButton = document.getElementById('advanced_searchbtn');
    const resultsScroll = document.querySelector('.players-results-scroll');
    const filterSources = [
        { id: 'seasons_menu', url: '/seasons' },
        { id: 'country_menu', url: '/country' },
        { id: 'championships_menu', url: '/list_competitions' },
        { id: 'years_birth_menu', url: '/get_birth_years' },
        { id: 'club_menu', url: '/all_teams' },
        { id: 'role_menu', url: '/get_role' }
    ];
    let searchVersion = 0;

    function setFeedback(message) {
        feedback.textContent = message;
    }

    function showResultsMessage(message) {
        const row = document.createElement('tr');
        const cell = document.createElement('td');
        cell.colSpan = 6;
        cell.className = 'players-results-message';
        cell.textContent = message;
        row.append(cell);
        resultsBody.replaceChildren(row);
    }

    function clearResults() {
        resultsBody.replaceChildren();
        resultsCount.hidden = true;
        resultsCount.textContent = '';
        resultsScroll.scrollTop = 0;
        resultsScroll.scrollLeft = 0;
    }

    function renderResults(players) {
        if (!players.length) {
            showResultsMessage('Nessun giocatore trovato per i filtri selezionati.');
            resultsCount.textContent = '0 giocatori';
            resultsCount.hidden = false;
            return;
        }

        const fragment = document.createDocumentFragment();
        players.forEach(function (player, index) {
            const row = document.createElement('tr');
            const playerId = Number(player.PlayerId);
            const playerHref = Number.isSafeInteger(playerId) && playerId > 0
                ? 'player.html?playerId=' + encodeURIComponent(playerId)
                : 'player.html?player=' + encodeURIComponent(player.Name || '');
            row.className = 'players-result-row';
            row.addEventListener('click', function (event) {
                if (!event.target.closest('a')) window.location.href = playerHref;
            });
            const values = [index + 1, player.Name, player.Nationality, player.Birth, player.Team, player.Position];
            values.forEach(function (value, column) {
                const cell = document.createElement('td');
                if (column === 1 && value) {
                    const link = document.createElement('a');
                    link.href = playerHref;
                    link.textContent = String(value);
                    cell.append(link);
                } else {
                    cell.textContent = value == null || value === '' ? '—' : String(value);
                }
                row.append(cell);
            });
            fragment.append(row);
        });
        resultsBody.replaceChildren(fragment);
        resultsCount.textContent = players.length + (players.length === 1 ? ' giocatore' : ' giocatori');
        resultsCount.hidden = false;
        resultsScroll.scrollTop = 0;
        resultsScroll.scrollLeft = 0;
    }

    async function loadFilters() {
        const responses = await Promise.allSettled(filterSources.map(function (source) {
            return axios.get(source.url);
        }));
        let failures = 0;
        responses.forEach(function (result, index) {
            if (result.status !== 'fulfilled' || !Array.isArray(result.value.data)) {
                failures += 1;
                return;
            }
            const select = document.getElementById(filterSources[index].id);
            const fragment = document.createDocumentFragment();
            result.value.data.forEach(function (entry) {
                const label = typeof entry === 'object' && entry !== null ? entry.Name : entry;
                if (label == null || String(label).trim() === '') return;
                fragment.append(new Option(String(label), String(label)));
            });
            select.append(fragment);
        });
        if (failures) setFeedback('Alcuni filtri non sono disponibili. Ricarica la pagina per riprovare.');
    }

    form.addEventListener('submit', async function (event) {
        event.preventDefault();
        const values = Object.fromEntries(new FormData(form).entries());
        if (!Object.values(values).some(Boolean)) {
            setFeedback('Seleziona almeno un filtro prima di cercare.');
            return;
        }

        const version = ++searchVersion;
        setFeedback('');
        resultsCount.hidden = true;
        submitButton.disabled = true;
        resultsBody.setAttribute('aria-busy', 'true');
        showResultsMessage('Ricerca in corso…');
        try {
            const response = await axios.post('/advanced_search', {
                season: values.season || null,
                country: values.country || null,
                competition: values.competition || null,
                year_birth: values.year_birth || null,
                team: values.team || null,
                role: values.role || null
            });
            if (version !== searchVersion) return;
            if (!Array.isArray(response.data)) throw new Error('Invalid search result');
            renderResults(response.data);
        } catch (error) {
            if (version !== searchVersion) return;
            console.error('Impossibile completare la ricerca dei giocatori.', error);
            showResultsMessage('Impossibile completare la ricerca. Riprova più tardi.');
        } finally {
            if (version === searchVersion) {
                resultsBody.removeAttribute('aria-busy');
                submitButton.disabled = false;
            }
        }
    });

    form.addEventListener('reset', function () {
        searchVersion += 1;
        submitButton.disabled = false;
        resultsBody.removeAttribute('aria-busy');
        setFeedback('');
        clearResults();
    });

    loadFilters().catch(function (error) {
        console.error('Impossibile caricare i filtri dei giocatori.', error);
        setFeedback('Filtri non disponibili. Ricarica la pagina per riprovare.');
    });
});
