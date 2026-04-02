/**
 * security.js
 * Client-side protection layer for devNectar.
 * Restricts access to browser developer tools and source code viewing.
 */
(function() {
    'use strict';

    // 1. Disable Right Click (Context Menu)
    document.addEventListener('contextmenu', function(e) {
        e.preventDefault();
        showSecurityToast("Right-click is disabled for security reasons.");
    }, false);

    // 2. Keyboard Shortcuts Protection
    document.addEventListener('keydown', function(e) {
        // F12 key
        if (e.keyCode === 123) {
            blockAction(e, "Developer Tools are restricted.");
        }

        // Ctrl+Shift+I (Inspect)
        // Ctrl+Shift+J (Console)
        // Ctrl+Shift+C (Element selector)
        if (e.ctrlKey && e.shiftKey && (e.keyCode === 73 || e.keyCode === 74 || e.keyCode === 67)) {
            blockAction(e, "Inspection tools are restricted.");
        }

        // Ctrl+U (View Source)
        if (e.ctrlKey && e.keyCode === 85) {
            blockAction(e, "View Source is restricted.");
        }

        // Ctrl+S (Save Page)
        if (e.ctrlKey && e.keyCode === 83) {
            blockAction(e, "Saving pages is restricted.");
        }
    }, false);

    /**
     * Blocks the event and shows a security message.
     */
    function blockAction(e, message) {
        e.preventDefault();
        e.stopPropagation();
        showSecurityToast(message);
        return false;
    }

    /**
     * Displays a temporary security toast message if possible,
     * otherwise logs to console (though devtools is likely closed).
     */
    function showSecurityToast(message) {
        // Create toast if it doesn't exist
        let toast = document.getElementById('security-toast');
        if (!toast) {
            toast = document.createElement('div');
            toast.id = 'security-toast';
            Object.assign(toast.style, {
                position: 'fixed',
                bottom: '30px',
                left: '50%',
                transform: 'translateX(-50%) translateY(100px)',
                backgroundColor: '#ef4444',
                color: 'white',
                padding: '12px 24px',
                borderRadius: '12px',
                boxShadow: '0 10px 25px rgba(239, 68, 68, 0.3)',
                zIndex: '10000',
                fontFamily: "'Inter', sans-serif",
                fontSize: '0.9rem',
                fontWeight: '600',
                transition: 'transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275)',
                pointerEvents: 'none',
                display: 'flex',
                alignItems: 'center',
                gap: '10px'
            });
            toast.innerHTML = '<i class="fas fa-shield-alt"></i> ' + message;
            document.body.appendChild(toast);
        } else {
            toast.innerHTML = '<i class="fas fa-shield-alt"></i> ' + message;
        }

        // Show toast
        toast.style.transform = 'translateX(-50%) translateY(0)';
        
        // Hide after 3 seconds
        clearTimeout(toast.hideTimeout);
        toast.hideTimeout = setTimeout(() => {
            toast.style.transform = 'translateX(-50%) translateY(100px)';
        }, 3000);
    }

    // 3. Clear Console aggressively (deterrent)
    /*
    setInterval(function() {
        // console.clear();
    }, 5000);
    */

    // 4. Debugger trap (optional, but very effective)
    /*
    (function anonymous() {
        (function() {
            (function() {
                if (window.outerHeight - window.innerHeight > 160 || window.outerWidth - window.innerWidth > 160) {
                    // DevTools likely open
                    // debugger;
                }
            })()
        })()
    })();
    */

    console.log("%c[Security] Protective layer active.", "color: #10b981; font-weight: bold;");
})();
