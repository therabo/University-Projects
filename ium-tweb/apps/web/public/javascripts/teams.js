document.addEventListener('DOMContentLoaded', function () {
    'use strict';

    const teamsBody = document.getElementById('teams-table-body');
    const playersBody = document.getElementById('players-table-body');
    const leagueLogo = document.getElementById('competition-logo');
    const leagueFallback = document.getElementById('league-fallback');
    const seasonSelect = document.getElementById('club-season');
    const logoPath = /^\/competitions\/[A-Z0-9]{1,12}\/logo$/;
    const crestPath = /^\/clubs\/[0-9]+\/crest$/;
    const crestFallback = 'images/club-crest-placeholder.svg';
    const portraitFallback = 'images/player-portrait-placeholder.svg';
    let selectedCompetitionId = null;

    function isTrustedPortraitUrl(value) {
        if (typeof value !== 'string') return false;
        try {
            const url = new URL(value);
            return url.protocol === 'https:'
                && url.hostname === 'img.a.transfermarkt.technology'
                && url.pathname.startsWith('/portrait/header/')
                && !url.username
                && !url.password;
        } catch (error) {
            return false;
        }
    }

    function showMessage(tableBody, message) {
        const row = document.createElement('tr');
        const cell = document.createElement('td');
        cell.colSpan = 2;
        cell.className = 'table-message';
        cell.textContent = message;
        row.append(cell);
        tableBody.replaceChildren(row);
    }

    function createRow(index, label, onOpen, imageUrl, imageType) {
        const row = document.createElement('tr');
        const number = document.createElement('td');
        number.textContent = String(index + 1);
        const name = document.createElement('td');
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'table-entry';
        button.addEventListener('click', onOpen);

        if (imageType) {
            const isPortrait = imageType === 'portrait';
            const fallback = isPortrait ? portraitFallback : crestFallback;
            const slot = document.createElement('span');
            slot.className = isPortrait ? 'portrait-slot' : 'crest-slot';
            const image = document.createElement('img');
            image.className = isPortrait ? 'player-portrait' : 'team-crest';
            image.alt = '';
            image.loading = index < 8 ? 'eager' : 'lazy';
            image.decoding = 'async';
            image.addEventListener('error', function () {
                if (!image.src.endsWith(fallback)) image.src = fallback;
            });
            const trusted = isPortrait
                ? isTrustedPortraitUrl(imageUrl)
                : typeof imageUrl === 'string' && crestPath.test(imageUrl);
            image.src = trusted ? imageUrl : fallback;
            slot.append(image);
            button.append(slot);
        }

        const text = document.createElement('span');
        text.textContent = label;
        button.append(text);
        name.append(button);
        row.append(number, name);
        return row;
    }

    function renderTeams(teams) {
        if (!Array.isArray(teams) || !teams.length) {
            showMessage(teamsBody, 'Nessuna squadra disponibile per questo campionato.');
            return;
        }
        const fragment = document.createDocumentFragment();
        teams.forEach(function (team, index) {
            fragment.append(createRow(index, team.name, function () {
                localStorage.setItem('squadra', team.name);
                window.location.href = 'team.html?season=' + encodeURIComponent(seasonSelect.value);
            }, team.crestUrl, 'crest'));
        });
        teamsBody.replaceChildren(fragment);
    }

    function renderPlayers(players) {
        if (!Array.isArray(players) || !players.length) {
            showMessage(playersBody, 'Nessun giocatore disponibile per questo campionato.');
            return;
        }
        const fragment = document.createDocumentFragment();
        players.forEach(function (player, index) {
            const name = [player.firstName, player.lastName].filter(Boolean).join(' ').trim();
            if (!name) return;
            fragment.append(createRow(index, name, function () {
                localStorage.setItem('giocatore', name);
                window.location.href = Number.isSafeInteger(player.playerId) && player.playerId > 0
                    ? 'player.html?playerId=' + encodeURIComponent(player.playerId)
                    : 'player.html?player=' + encodeURIComponent(name);
            }, player.imageUrl, 'portrait'));
        });
        playersBody.replaceChildren(fragment);
    }

    function renderCompetition(competition) {
        const displayName = competition.displayName || competition.name || 'Campionato';
        document.getElementById('league-title').textContent = displayName;
        document.getElementById('country').textContent = competition.countryName || '—';
        document.getElementById('data-season').textContent = Number.isInteger(competition.dataSeason)
            ? String(competition.dataSeason) : '—';
        document.getElementById('club-count').textContent = Number.isInteger(competition.clubCount)
            ? String(competition.clubCount) : '—';
        document.getElementById('league-tier').textContent = competition.tier === 'first_tier'
            ? '1ª divisione' : (competition.tier || '—');
        document.getElementById('competition-type').textContent = competition.competitionType === 'domestic_league'
            ? 'Campionato nazionale' : (competition.competitionType || '—');
        document.getElementById('confederazione').textContent = competition.confederation === 'europa'
            ? 'Europa' : (competition.confederation || '—');
        leagueFallback.textContent = displayName;
        if (logoPath.test(competition.logoUrl || '')) {
            leagueLogo.onload = function () {
                leagueLogo.hidden = false;
                leagueFallback.hidden = true;
            };
            leagueLogo.onerror = function () {
                leagueLogo.hidden = true;
                leagueFallback.hidden = false;
            };
            leagueLogo.alt = 'Logo ' + displayName;
            leagueLogo.src = competition.logoUrl;
        }
    }

    function renderSeasonOptions(competition) {
        const seasons = Array.isArray(competition.availableSeasons)
            ? competition.availableSeasons.filter(Number.isInteger) : [];
        seasonSelect.replaceChildren();
        if (!seasons.length) {
            seasonSelect.add(new Option('—', ''));
            seasonSelect.disabled = true;
            return;
        }
        seasons.forEach(function (year) {
            seasonSelect.add(new Option(String(year), String(year)));
        });
        seasonSelect.value = String(competition.dataSeason);
        seasonSelect.disabled = false;
    }

    async function loadTeams(competitionId, season) {
        seasonSelect.disabled = true;
        teamsBody.setAttribute('aria-busy', 'true');
        showMessage(teamsBody, 'Caricamento delle squadre…');
        try {
            const response = await axios.post('/list_teamsbycompetition', {
                comp: competitionId,
                season: season
            });
            const teams = response.data;
            renderTeams(teams);
            document.getElementById('data-season').textContent = String(season);
            document.getElementById('club-count').textContent = String(teams.length);
        } catch (error) {
            console.error('Errore durante il caricamento delle squadre.', error);
            showMessage(teamsBody, 'Impossibile caricare le squadre. Riprova più tardi.');
            document.getElementById('club-count').textContent = '—';
        } finally {
            teamsBody.removeAttribute('aria-busy');
            seasonSelect.disabled = seasonSelect.options.length <= 1;
        }
    }

    async function loadCompetition() {
        const selected = localStorage.getItem('campionato') || 'serie-a';
        try {
            const response = await axios.post('/info_competition', { comp: selected });
            const competition = response.data;
            if (!competition || !competition.id) throw new Error('ID campionato assente');
            selectedCompetitionId = competition.id;
            renderCompetition(competition);
            renderSeasonOptions(competition);
            const results = await Promise.allSettled([
                Number.isInteger(competition.dataSeason)
                    ? loadTeams(competition.id, competition.dataSeason)
                    : Promise.resolve(showMessage(teamsBody, 'Nessuna stagione disponibile per questo campionato.')),
                axios.post('/comp_players', { comp: competition.id })
            ]);
            if (results[1].status === 'fulfilled') {
                renderPlayers(results[1].value.data);
            } else {
                console.error('Errore durante il caricamento dei giocatori.', results[1].reason);
                showMessage(playersBody, 'Impossibile caricare i giocatori. Riprova più tardi.');
            }
        } catch (error) {
            console.error('Errore durante il caricamento del campionato.', error);
            leagueFallback.textContent = 'Campionato non disponibile';
            showMessage(teamsBody, 'Impossibile caricare le squadre.');
            showMessage(playersBody, 'Impossibile caricare i giocatori.');
        }
    }

    seasonSelect.addEventListener('change', function () {
        const season = Number(seasonSelect.value);
        if (selectedCompetitionId && Number.isInteger(season)) {
            loadTeams(selectedCompetitionId, season);
        }
    });

    loadCompetition();
});
