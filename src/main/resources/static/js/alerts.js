document.addEventListener('DOMContentLoaded', () => {
    const autoDismissAlerts = document.querySelectorAll('[data-auto-dismiss="true"]');

    autoDismissAlerts.forEach(alert => {
        const baseTimeout = parseInt(alert.dataset.autoDismissTimeout, 10) || 4000;
        let dismissTimer = null;

        const clearTimer = () => {
            if (dismissTimer) {
                clearTimeout(dismissTimer);
                dismissTimer = null;
            }
        };

        const removeAlert = () => {
            if (alert.dataset.dismissed === 'true') {
                return;
            }
            alert.dataset.dismissed = 'true';
            alert.classList.remove('show');
            alert.classList.add('fade');
            setTimeout(() => {
                alert.remove();
            }, 300);
        };

        const startTimer = (timeout = baseTimeout) => {
            clearTimer();
            dismissTimer = setTimeout(removeAlert, timeout);
        };

        const closeButton = alert.querySelector('[data-bs-dismiss="alert"]');
        if (closeButton) {
            closeButton.addEventListener('click', () => {
                clearTimer();
                removeAlert();
            });
        }

        alert.addEventListener('mouseenter', clearTimer);
        alert.addEventListener('mouseleave', () => startTimer(1500));

        startTimer();
    });
});
