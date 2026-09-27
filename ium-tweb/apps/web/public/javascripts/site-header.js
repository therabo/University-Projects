(function () {
    'use strict';

    const OPEN_CLASS = 'is-open';

    function initialiseHeader(header) {
        const labels = {
            profile: { closed: 'Apri il menu profilo', open: 'Chiudi il menu profilo' },
            search: { closed: 'Apri la ricerca', open: 'Chiudi la ricerca' }
        };
        const toggles = new Map(
            Array.from(header.querySelectorAll('[data-header-toggle]')).map(function (toggle) {
                return [toggle.dataset.headerToggle, toggle];
            })
        );
        const panels = new Map(
            Array.from(header.querySelectorAll('[data-header-panel]')).map(function (panel) {
                return [panel.dataset.headerPanel, panel];
            })
        );

        let activeName = null;

        function setPanelState(name, isOpen) {
            const toggle = toggles.get(name);
            const panel = panels.get(name);

            if (!toggle || !panel) {
                return;
            }

            toggle.setAttribute('aria-expanded', String(isOpen));
            if (labels[name]) {
                toggle.setAttribute('aria-label', isOpen ? labels[name].open : labels[name].closed);
            }
            panel.setAttribute('aria-hidden', String(!isOpen));
            panel.classList.toggle(OPEN_CLASS, isOpen);

            if (isOpen) {
                activeName = name;
            } else if (activeName === name) {
                activeName = null;
            }
        }

        function closeActivePanel(restoreFocus) {
            if (!activeName) {
                return;
            }

            const toggle = toggles.get(activeName);
            setPanelState(activeName, false);

            if (restoreFocus && toggle) {
                toggle.focus();
            }
        }

        function focusPanel(name) {
            const panel = panels.get(name);
            const focusTarget = name === 'search'
                ? panel && panel.querySelector('input')
                : panel && panel.querySelector('[role="menuitem"]');

            if (focusTarget) {
                window.requestAnimationFrame(function () {
                    focusTarget.focus();
                });
            }
        }

        toggles.forEach(function (toggle, name) {
            const panel = panels.get(name);
            if (!panel) {
                return;
            }

            setPanelState(name, false);
            toggle.addEventListener('click', function () {
                const shouldOpen = activeName !== name;
                closeActivePanel(false);

                if (shouldOpen) {
                    setPanelState(name, true);
                    focusPanel(name);
                }
            });
        });

        document.addEventListener('pointerdown', function (event) {
            if (activeName && !header.contains(event.target)) {
                closeActivePanel(false);
            }
        });

        header.addEventListener('keydown', function (event) {
            if (event.key === 'Escape') {
                closeActivePanel(true);
            }
        });

        header.querySelector('[data-site-logout]')?.addEventListener('click', function () {
            try { localStorage.removeItem('chat_username'); } catch (error) {}
            window.location.href = 'login.html';
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('[data-site-header]').forEach(initialiseHeader);
    });
})();
