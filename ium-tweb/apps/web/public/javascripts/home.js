document.addEventListener('DOMContentLoaded', function() {
    setupStoryPager('wrap-w-items1', 'wrap-w-items2', 'btn-switch-w', 'Mostra altri');
    setupStoryPager('wrap-n-items1', 'wrap-n-items2', 'btn-switch-n', 'Mostra altre');
    setupNewsExcerpts();
    setupHeroCarousel();

    loadMatchSections();
});

function setupStoryPager(firstPageId, secondPageId, buttonId, nextLabel) {
    const firstPage = document.getElementById(firstPageId);
    const secondPage = document.getElementById(secondPageId);
    const button = document.getElementById(buttonId);
    if (!firstPage || !secondPage || !button) return;

    button.addEventListener('click', () => {
        const showSecondPage = secondPage.hidden;
        firstPage.hidden = showSecondPage;
        secondPage.hidden = !showSecondPage;
        button.textContent = showSecondPage ? 'Mostra precedenti' : nextLabel;
        window.requestAnimationFrame(updateNewsExpandButtons);
    });
}

function updateNewsExpandButtons() {
    document.querySelectorAll('[data-news-expand]').forEach((button) => {
        const paragraph = button.previousElementSibling;
        if (!paragraph || !paragraph.getClientRects().length) return;
        button.hidden = !paragraph.classList.contains('is-expanded')
            && paragraph.scrollHeight <= paragraph.clientHeight + 1;
    });
}

function setupNewsExcerpts() {
    document.querySelectorAll('[data-news-expand]').forEach((button, index) => {
        const paragraph = button.previousElementSibling;
        if (!paragraph) return;
        paragraph.id = `news-excerpt-${index + 1}`;
        button.setAttribute('aria-controls', paragraph.id);
        button.addEventListener('click', () => {
            const isExpanded = paragraph.classList.toggle('is-expanded');
            button.setAttribute('aria-expanded', String(isExpanded));
            button.textContent = isExpanded ? 'Mostra meno' : 'Leggi tutto';
            updateNewsExpandButtons();
        });
    });
    updateNewsExpandButtons();
    document.fonts?.ready.then(updateNewsExpandButtons);
    window.addEventListener('resize', updateNewsExpandButtons);
}

function setupHeroCarousel() {
    const carousel = document.querySelector('[data-hero-carousel]');
    if (!carousel) {
        return;
    }

    const slides = Array.from(carousel.querySelectorAll('[data-carousel-slide]'));
    const dots = Array.from(carousel.querySelectorAll('[data-carousel-dot]'));
    const previousButton = carousel.querySelector('[data-carousel-prev]');
    const nextButton = carousel.querySelector('[data-carousel-next]');
    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)');
    let activeIndex = 0;
    let autoplayTimer;

    const normalizeIndex = (index) => (index + slides.length) % slides.length;

    const render = () => {
        const previousFarIndex = normalizeIndex(activeIndex - 2);
        const previousIndex = normalizeIndex(activeIndex - 1);
        const nextIndex = normalizeIndex(activeIndex + 1);
        const nextFarIndex = normalizeIndex(activeIndex + 2);

        slides.forEach((slide, index) => {
            slide.classList.remove(
                'is-active',
                'is-prev',
                'is-next',
                'is-prev-far',
                'is-next-far'
            );

            if (index === activeIndex) {
                slide.classList.add('is-active');
            } else if (index === previousIndex) {
                slide.classList.add('is-prev');
            } else if (index === nextIndex) {
                slide.classList.add('is-next');
            } else if (index === previousFarIndex) {
                slide.classList.add('is-prev-far');
            } else if (index === nextFarIndex) {
                slide.classList.add('is-next-far');
            }

            slide.setAttribute('aria-hidden', index === activeIndex ? 'false' : 'true');
        });

        dots.forEach((dot, index) => {
            const isCurrent = index === activeIndex;
            dot.classList.toggle('is-active', isCurrent);
            if (isCurrent) {
                dot.setAttribute('aria-current', 'true');
            } else {
                dot.removeAttribute('aria-current');
            }
        });
    };

    const goToSlide = (index) => {
        activeIndex = normalizeIndex(index);
        render();
    };

    const stopAutoplay = () => {
        window.clearInterval(autoplayTimer);
    };

    const startAutoplay = () => {
        stopAutoplay();
        if (!reduceMotion.matches && !document.hidden) {
            autoplayTimer = window.setInterval(() => goToSlide(activeIndex + 1), 5500);
        }
    };

    previousButton.addEventListener('click', () => {
        goToSlide(activeIndex - 1);
        startAutoplay();
    });
    nextButton.addEventListener('click', () => {
        goToSlide(activeIndex + 1);
        startAutoplay();
    });
    dots.forEach((dot, index) => {
        dot.addEventListener('click', () => {
            goToSlide(index);
            startAutoplay();
        });
    });

    carousel.addEventListener('keydown', (event) => {
        if (event.key === 'ArrowLeft') {
            event.preventDefault();
            goToSlide(activeIndex - 1);
            startAutoplay();
        } else if (event.key === 'ArrowRight') {
            event.preventDefault();
            goToSlide(activeIndex + 1);
            startAutoplay();
        }
    });
    carousel.addEventListener('mouseenter', stopAutoplay);
    carousel.addEventListener('mouseleave', startAutoplay);
    carousel.addEventListener('focusin', stopAutoplay);
    carousel.addEventListener('focusout', startAutoplay);
    document.addEventListener('visibilitychange', startAutoplay);

    render();
    startAutoplay();
}

const CLUB_CREST_FALLBACK = 'images/club-crest-placeholder.svg';
const CLUB_CREST_PATH = /^\/clubs\/[0-9]+\/crest$/;

function createTextElement(tagName, className, text) {
    const element = document.createElement(tagName);
    element.className = className;
    element.textContent = text;
    return element;
}

function createClubCrest(team) {
    const crest = document.createElement('img');
    crest.className = 'club-crest';
    crest.src = CLUB_CREST_PATH.test(team.crestUrl || '')
        ? team.crestUrl : CLUB_CREST_FALLBACK;
    crest.alt = '';
    crest.width = 48;
    crest.height = 48;
    crest.loading = 'lazy';
    crest.decoding = 'async';
    crest.referrerPolicy = 'no-referrer';
    crest.setAttribute('aria-hidden', 'true');

    crest.addEventListener('error', () => {
        crest.src = CLUB_CREST_FALLBACK;
        crest.classList.add('club-crest--fallback');
    }, { once: true });

    return crest;
}

function createUpcomingTeamRow(meta, team) {
    const row = document.createElement('div');
    row.className = 'single-pp';

    row.append(createTextElement('p', 'match-time', meta));

    const teamIdentity = document.createElement('div');
    teamIdentity.className = 'match-team match-team--upcoming';
    teamIdentity.append(
        createClubCrest(team),
        createTextElement('p', 'match-team__name', team.name)
    );
    row.append(teamIdentity);

    return row;
}

function createUpcomingMatch(fixture) {
    const card = document.createElement('article');
    card.className = 'container-single-pp';
    card.setAttribute('aria-label', `${fixture.homeTeam.name} contro ${fixture.awayTeam.name}`);
    card.append(
        createUpcomingTeamRow(fixture.date, fixture.homeTeam),
        createUpcomingTeamRow(fixture.time, fixture.awayTeam)
    );
    return card;
}

function createResultTeam(team, side) {
    const teamIdentity = document.createElement('div');
    teamIdentity.className = `match-team match-team--result match-team--${side}`;
    const teamName = createTextElement('h3', 'match-team__name', team.name);
    const crest = createClubCrest(team);

    if (side === 'home') {
        teamIdentity.append(teamName, crest);
    } else {
        teamIdentity.append(crest, teamName);
    }

    return teamIdentity;
}

function createResultMatch(result) {
    const card = document.createElement('article');
    card.className = 'container-single-rp';
    card.setAttribute(
        'aria-label',
        `${result.homeTeam.name} ${result.score} ${result.awayTeam.name}, ${result.stadium}`
    );

    const stadium = createTextElement('p', 'stadium-rp', result.stadium);
    const scoreline = document.createElement('div');
    scoreline.className = 'match-rp-risultato';

    const score = createTextElement('p', 'match-score', result.score);
    score.setAttribute('aria-hidden', 'true');
    scoreline.append(
        createResultTeam(result.homeTeam, 'home'),
        score,
        createResultTeam(result.awayTeam, 'away')
    );

    card.append(stadium, scoreline);
    return card;
}

function renderMatchList(container, matches, createCard) {
    const fragment = document.createDocumentFragment();
    matches.forEach((match) => fragment.append(createCard(match)));
    container.replaceChildren(fragment);
    container.setAttribute('aria-busy', 'false');
}

function renderMatchError(container) {
    const message = createTextElement(
        'p',
        'match-list-status',
        'Partite temporaneamente non disponibili.'
    );
    container.replaceChildren(message);
    container.setAttribute('aria-busy', 'false');
}

async function loadMatchSections() {
    const upcomingContainer = document.querySelector('.wrap-pp');
    const resultsContainer = document.querySelector('.wrap-rp');

    if (!upcomingContainer || !resultsContainer) {
        return;
    }

    upcomingContainer.setAttribute('aria-busy', 'true');
    resultsContainer.setAttribute('aria-busy', 'true');

    try {
        const response = await axios.get('/loadHP');
        renderMatchList(upcomingContainer, response.data.upcomingMatches, createUpcomingMatch);
        renderMatchList(resultsContainer, response.data.recentResults, createResultMatch);
    } catch (error) {
        console.error('Impossibile caricare le partite:', error);
        renderMatchError(upcomingContainer);
        renderMatchError(resultsContainer);
    }
}
