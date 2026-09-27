const PLAYER_PORTRAIT_FALLBACK = './images/player-portrait-placeholder.svg';
const CLUB_CREST_FALLBACK = './images/club-crest-placeholder.svg';
let rosterRequestToken = 0;

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

function redirectToPage(playerName, playerId) {
    localStorage.setItem('giocatore', playerName);
    window.location.href = Number.isSafeInteger(playerId) && playerId > 0
        ? './player.html?playerId=' + encodeURIComponent(playerId)
        : './player.html?player=' + encodeURIComponent(playerName);
}
function infoSquadra(url, squad) {
    axios.post(url, { squad: squad })
        .then(function (response) {
            const info = response.data[0];
            if (!info) throw new Error('Squadra non trovata nel catalogo');

            const display = function (id, value) {
                document.getElementById(id).textContent = value === null || value === undefined || value === ''
                    ? '—' : String(value);
            };
            display('Infostadio', info.stadium_name);
            display('Infoposti_stadio', info.stadium_seats);
            display('Infoallenatore', info.coach_name);
            display('Infoeta_media', info.average_age);
            display('Infodim_squadra', info.squad_size);

            const profileCell = document.getElementById('Infosito_ufficiale');
            profileCell.textContent = '—';
            if (typeof info.url === 'string') {
                try {
                    const profileUrl = new URL(info.url);
                    if (profileUrl.protocol === 'https:') {
                        const link = document.createElement('a');
                        link.href = profileUrl.href;
                        link.target = '_blank';
                        link.rel = 'noopener noreferrer';
                        link.textContent = 'Apri scheda';
                        profileCell.replaceChildren(link);
                    }
                } catch (error) {
                }
            }

            const crest = document.getElementById('team-crest');
            const clubId = Number(info.club_id);
            crest.alt = `Logo ${info.name || squad}`;
            crest.onerror = function () {
                crest.onerror = null;
                crest.src = CLUB_CREST_FALLBACK;
            };
            crest.src = Number.isSafeInteger(clubId) && clubId > 0
                ? `/clubs/${clubId}/crest` : CLUB_CREST_FALLBACK;

            if (Number.isSafeInteger(clubId) && clubId > 0) {
                const seasonSelect = document.getElementById('roster-season');
                seasonSelect.addEventListener('change', function () {
                    const season = Number(seasonSelect.value);
                    if (!Number.isInteger(season)) return;
                    const pageUrl = new URL(window.location.href);
                    pageUrl.searchParams.set('season', String(season));
                    window.history.replaceState(null, '', pageUrl);
                    loadRoster(clubId, season);
                });
                const seasonParam = new URLSearchParams(window.location.search).get('season');
                const requestedSeason = /^(19|20)[0-9]{2}$/.test(seasonParam || '')
                    ? Number(seasonParam) : null;
                loadRoster(clubId, requestedSeason);
            } else {
                showRosterMessage('Rosa non disponibile: ID squadra mancante.');
                window.TeamSeasonStats.unavailable('Statistiche non disponibili: ID squadra mancante.');
            }
        })
        .catch(function (error) {
            console.error('Impossibile caricare la squadra:', error);
            showRosterMessage('Rosa non disponibile.');
            window.TeamSeasonStats.unavailable('Statistiche non disponibili per questa squadra.');
        });
}

function showRosterMessage(message) {
    const tableBody = document.getElementById('team-players-body');
    const row = document.createElement('tr');
    const cell = document.createElement('td');
    cell.colSpan = 2;
    cell.className = 'team-roster-message';
    cell.textContent = message;
    row.append(cell);
    tableBody.replaceChildren(row);
}

async function loadRoster(clubId, requestedSeason) {
    const requestToken = ++rosterRequestToken;
    const tableBody = document.getElementById('team-players-body');
    const seasonSelect = document.getElementById('roster-season');
    const rosterNote = document.getElementById('roster-note');
    tableBody.setAttribute('aria-busy', 'true');
    seasonSelect.disabled = true;
    rosterNote.textContent = 'Caricamento della rosa…';
    showRosterMessage('Caricamento giocatori…');

    try {
        const response = await axios.get(`/clubs/${clubId}/roster`, {
            params: requestedSeason === null ? undefined : { season: requestedSeason }
        });
        if (requestToken !== rosterRequestToken) return;

        const roster = response.data;
        const seasons = Array.isArray(roster.availableSeasons)
            ? roster.availableSeasons.filter(Number.isInteger) : [];
        seasonSelect.replaceChildren(...seasons.map(function (year) {
            return new Option(`${year}/${String(year + 1).slice(-2)}`, String(year));
        }));
        seasonSelect.value = roster.season === null ? '' : String(roster.season);
        seasonSelect.disabled = seasons.length <= 1;
        window.TeamSeasonStats.load(clubId, roster.season);

        const players = Array.isArray(roster.players) ? roster.players : [];
        rosterNote.textContent = `${players.length} giocatori con presenze registrate`;
        if (players.length === 0) {
            showRosterMessage('Nessuna presenza registrata per questa annata.');
        } else {
            const rows = players.map(function (player, index) {
                const row = document.createElement('tr');
                const number = document.createElement('td');
                number.textContent = String(index + 1);
                const playerCell = document.createElement('td');
                const button = document.createElement('button');
                button.type = 'button';
                button.className = 'button-table';
                const portraitFrame = document.createElement('span');
                portraitFrame.className = 'team-roster-portrait';
                const portrait = document.createElement('img');
                portrait.src = isTrustedPortraitUrl(player.image_url)
                    ? player.image_url : PLAYER_PORTRAIT_FALLBACK;
                portrait.alt = '';
                portrait.width = 44;
                portrait.height = 44;
                portrait.loading = index < 6 ? 'eager' : 'lazy';
                portrait.decoding = 'async';
                portrait.onerror = function () {
                    portrait.onerror = null;
                    portrait.src = PLAYER_PORTRAIT_FALLBACK;
                };
                portraitFrame.append(portrait);
                const name = document.createElement('span');
                name.className = 'team-roster-name';
                const playerName = player.name;
                name.textContent = playerName || 'Giocatore senza nome';
                button.append(portraitFrame, name);
                button.addEventListener('click', function () { redirectToPage(name.textContent, player.playerId); });
                playerCell.append(button);
                row.append(number, playerCell);
                return row;
            });
            tableBody.replaceChildren(...rows);
        }
    } catch (error) {
        if (requestToken !== rosterRequestToken) return;
        if (requestedSeason !== null && error.response?.status === 400) {
            const pageUrl = new URL(window.location.href);
            pageUrl.searchParams.delete('season');
            window.history.replaceState(null, '', pageUrl);
            return loadRoster(clubId, null);
        }
        console.error('Impossibile caricare la rosa stagionale:', error);
        rosterNote.textContent = 'Rosa non disponibile';
        showRosterMessage('Impossibile caricare i giocatori.');
        window.TeamSeasonStats.unavailable('Statistiche non disponibili: annata non caricata.');
        seasonSelect.disabled = true;
    } finally {
        if (requestToken === rosterRequestToken) tableBody.removeAttribute('aria-busy');
    }
}

function onSubmit(){
    const squadra = localStorage.getItem('squadra');
    document.getElementById('nome_squadra').innerText = squadra || 'Squadra non selezionata';
    if (!squadra) {
        document.getElementById('team-players-body').querySelector('td').textContent =
            'Seleziona una squadra dalla pagina Squadre.';
        window.TeamSeasonStats.unavailable('Seleziona una squadra per vedere le statistiche.');
        return;
    }
    infoSquadra('/list_info_squad', squadra);
}

document.addEventListener('DOMContentLoaded', onSubmit);


