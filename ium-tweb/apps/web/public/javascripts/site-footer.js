document.addEventListener('DOMContentLoaded', () => {
    const narrowViewport = window.matchMedia('(max-width: 900px)');
    for (const footer of document.querySelectorAll('[data-site-footer]')) {
        const toggle = footer.querySelector('.site-footer__toggle');
        const panel = footer.querySelector('.site-footer__main');
        if (!toggle || !panel) continue;
        footer.classList.add('site-footer--interactive');

        function setOpen(open) {
            panel.classList.toggle('is-open', open);
            toggle.setAttribute('aria-expanded', String(open));
        }

        toggle.addEventListener('click', () => setOpen(toggle.getAttribute('aria-expanded') !== 'true'));
        document.addEventListener('click', (event) => {
            if (!footer.contains(event.target)) setOpen(false);
        });
        document.addEventListener('keydown', (event) => {
            if (event.key === 'Escape' && toggle.getAttribute('aria-expanded') === 'true') {
                setOpen(false);
                toggle.focus();
            }
        });
        narrowViewport.addEventListener('change', () => setOpen(false));
    }
});
