/**
 * theme-toggle.js
 * Theme management for devNectar.
 * The core 'initial' theme application is handled via a blocking script in the <head>
 * to prevent the "light theme flash" (FOUC) during navigation.
 * This script handles user interactions and syncing the state.
 */
document.addEventListener('DOMContentLoaded', () => {
    let themeToggle = document.getElementById('theme-toggle');
    const body = document.body;

    // The theme initialization is handled by the Theme Bridge script in <head>
    // Just sync the icon icon
    const currentTheme = document.documentElement.classList.contains('dark-theme') ? 'dark' : 'light';
    
    // Inject floating toggle if missing
    if (!themeToggle) {
        themeToggle = injectFloatingToggle();
    }
    
    updateToggleIcon(currentTheme);

    // Theme Toggle Click Handler
    if (themeToggle) {
        themeToggle.addEventListener('click', (e) => {
            e.preventDefault();
            const isDark = document.documentElement.classList.contains('dark-theme');
            const newTheme = isDark ? 'light' : 'dark';
            
            // Apply immediately to avoid transition on manual click? Maybe transition is fine on click.
            // But we can toggle transition mode here if needed.
            applyTheme(newTheme);
            localStorage.setItem('devnectar_theme', newTheme);
        });
    }

    /**
     * Applies the specified theme to the document root.
     */
    function applyTheme(theme) {
        const doc = document.documentElement;
        if (theme === 'dark') {
            doc.classList.add('dark-theme');
            doc.classList.remove('light-theme');
        } else {
            doc.classList.add('light-theme');
            doc.classList.remove('dark-theme');
        }
        updateToggleIcon(theme);
    }

    /**
     * Updates the theme toggle icon.
     */
    function updateToggleIcon(theme) {
        if (!themeToggle) return;
        const icon = themeToggle.querySelector('i');
        if (!icon) return;

        if (theme === 'light') {
            icon.className = 'fas fa-moon';
            themeToggle.title = 'Current: Light Mode';
        } else {
            icon.className = 'fas fa-sun';
            themeToggle.title = 'Current: Dark Mode';
        }
    }

    /**
     * Injects a floating toggle button into the page.
     */
    function injectFloatingToggle() {
        const btn = document.createElement('button');
        btn.id = 'theme-toggle';
        btn.className = 'floating-theme-toggle';
        btn.innerHTML = '<i></i>';
        
        Object.assign(btn.style, {
            position: 'fixed', bottom: '24px', right: '24px',
            width: '46px', height: '46px', borderRadius: '14px',
            backgroundColor: 'var(--bg-card)', 
            border: '1px solid var(--border-color)',
            color: 'var(--text-primary)', zIndex: '9999',
            cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center',
            boxShadow: '0 15px 35px rgba(0,0,0,0.2)', transition: 'all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)'
        });

        btn.addEventListener('mouseenter', () => {
            btn.style.transform = 'scale(1.1) translateY(-3px)';
            btn.style.backgroundColor = 'var(--brand-green)';
            btn.style.color = 'white';
        });
        btn.addEventListener('mouseleave', () => {
            btn.style.transform = 'scale(1)';
            btn.style.backgroundColor = 'var(--bg-card)';
            btn.style.color = 'var(--text-primary)';
        });

        document.body.appendChild(btn);
        return btn;
    }
});
