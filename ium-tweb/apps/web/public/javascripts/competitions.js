document.addEventListener('DOMContentLoaded', function () {
    'use strict';

    const popularList = document.getElementById('popular-competitions');
    const tableBody = document.getElementById('competition-table-body');
    const searchInput = document.getElementById('competition-search');
    const imagePath = /^\/competitions\/[A-Z0-9]{1,12}\/logo$/;
    let competitions = [];
    let selectionPending = false;

    function openCompetition(code) {
        localStorage.setItem('campionato', code);
        window.location.href = 'teams.html';
    }

    async function selectCompetition(code) {
        if (selectionPending) {
            return;
        }
        selectionPending = true;
        try {
            await axios.post('/competitions/selections', { competition: code });
            const selected = competitions.find(function (competition) {
                return competition.Value === code;
            });
            if (selected) {
                selected.SelectionCount = Number(selected.SelectionCount) + 1;
                renderPopular();
            }
        } catch (error) {
            console.error('Impossibile registrare la selezione del campionato.', error);
        }
        openCompetition(code);
    }

    window.addEventListener('pageshow', function () {
        selectionPending = false;
    });

    function showMessage(container, message, className) {
        const paragraph = document.createElement('p');
        paragraph.className = className;
        paragraph.textContent = message;
        container.replaceChildren(paragraph);
    }

    function renderPopular() {
        const ranked = competitions
            .filter(function (competition) { return Number(competition.SelectionCount) > 0; })
            .sort(function (left, right) {
                return Number(right.SelectionCount) - Number(left.SelectionCount)
                    || left.Name.localeCompare(right.Name, 'it');
            })
            .slice(0, 6);

        if (!ranked.length) {
            popularList.classList.add('is-empty');
            showMessage(popularList,
                'Nessun campionato ancora in classifica. Aprine uno per iniziare.',
                'competition-state');
            const searchButton = document.createElement('button');
            searchButton.className = 'competition-empty-action';
            searchButton.type = 'button';
            searchButton.textContent = 'Cerca un campionato';
            searchButton.addEventListener('click', function () { searchInput.focus(); });
            popularList.append(searchButton);
            return;
        }

        popularList.classList.remove('is-empty');
        const fragment = document.createDocumentFragment();
        ranked.forEach(function (competition) {
            const card = document.createElement('button');
            card.className = 'competition-card';
            card.type = 'button';
            card.addEventListener('click', function () {
                selectCompetition(competition.Value);
            });

            if (competition.LogoUrl && imagePath.test(competition.LogoUrl)) {
                const logo = document.createElement('span');
                logo.className = 'competition-card__logo';
                const image = document.createElement('img');
                image.src = competition.LogoUrl;
                image.alt = '';
                image.setAttribute('aria-hidden', 'true');
                image.addEventListener('error', function () {
                    logo.remove();
                    card.classList.add('competition-card--no-logo');
                });
                logo.append(image);
                card.append(logo);
            } else {
                card.classList.add('competition-card--no-logo');
            }

            const details = document.createElement('span');
            details.className = 'competition-card__details';
            const name = document.createElement('span');
            name.className = 'competition-card__name';
            name.textContent = competition.Name;
            details.append(name);
            card.append(details);
            fragment.append(card);
        });
        popularList.replaceChildren(fragment);
    }

    function filteredCompetitions() {
        const query = searchInput.value.trim().toLocaleLowerCase('it');
        return competitions.filter(function (competition) {
            return competition.Name.toLocaleLowerCase('it').includes(query)
                || competition.Value.toLocaleLowerCase('it').includes(query);
        });
    }

    function renderTable() {
        const visible = filteredCompetitions();
        if (!visible.length) {
            const row = document.createElement('tr');
            const cell = document.createElement('td');
            cell.colSpan = 2;
            cell.className = 'competition-table-empty';
            cell.textContent = 'Nessun campionato trovato.';
            row.append(cell);
            tableBody.replaceChildren(row);
            return;
        }

        const fragment = document.createDocumentFragment();
        visible.forEach(function (competition, index) {
            const row = document.createElement('tr');
            const numberCell = document.createElement('td');
            numberCell.textContent = String(index + 1);
            const nameCell = document.createElement('td');
            const button = document.createElement('button');
            button.className = 'button-table';
            button.type = 'button';
            const logoSlot = document.createElement('span');
            logoSlot.className = 'competition-table-logo-slot';
            if (competition.LogoUrl && imagePath.test(competition.LogoUrl)) {
                const image = document.createElement('img');
                image.className = 'competition-table-logo';
                image.src = competition.LogoUrl;
                image.alt = '';
                image.setAttribute('aria-hidden', 'true');
                image.addEventListener('error', function () { image.remove(); });
                logoSlot.append(image);
            }
            const label = document.createElement('span');
            label.textContent = competition.Name;
            button.append(logoSlot, label);
            button.addEventListener('click', function () {
                selectCompetition(competition.Value);
            });
            nameCell.append(button);
            row.append(numberCell, nameCell);
            fragment.append(row);
        });
        tableBody.replaceChildren(fragment);
    }

    searchInput.addEventListener('input', renderTable);
    searchInput.addEventListener('keydown', function (event) {
        if (event.key !== 'Enter' || !searchInput.value.trim()) {
            return;
        }
        const visible = filteredCompetitions();
        const exact = visible.find(function (competition) {
            return competition.Name.toLocaleLowerCase('it') === searchInput.value.trim().toLocaleLowerCase('it')
                || competition.Value.toLocaleLowerCase('it') === searchInput.value.trim().toLocaleLowerCase('it');
        });
        if (exact || visible.length === 1) {
            event.preventDefault();
            selectCompetition((exact || visible[0]).Value);
        }
    });

    axios.get('/list_competitions')
        .then(function (response) {
            competitions = response.data;
            renderPopular();
            renderTable();
        })
        .catch(function (error) {
            console.error('Impossibile caricare i campionati.', error);
            showMessage(popularList, 'Campionati non disponibili. Ricarica la pagina.', 'competition-state');
            const row = document.createElement('tr');
            const cell = document.createElement('td');
            cell.colSpan = 2;
            cell.textContent = 'Campionati non disponibili. Ricarica la pagina.';
            row.append(cell);
            tableBody.replaceChildren(row);
        });
});
