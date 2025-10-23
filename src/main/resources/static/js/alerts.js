document.addEventListener('DOMContentLoaded', () => {
    const autoDismissAlerts = document.querySelectorAll('[data-auto-dismiss="true"]');

    autoDismissAlerts.forEach(alert => {
        const timeout = parseInt(alert.dataset.autoDismissTimeout, 10) || 4000;

        const handleDismiss = () => {
            const bootstrapAlert = bootstrap.Alert.getOrCreateInstance(alert);
            bootstrapAlert.close();
        };

        const timerId = setTimeout(handleDismiss, timeout);

        alert.addEventListener('mouseenter', () => clearTimeout(timerId));

        alert.addEventListener('mouseleave', () => {
            setTimeout(handleDismiss, 1500);
        });
    });
});
