/**
 * 공통 알림 모달 표시 함수
 * @param {string} message - 표시할 메시지
 */
function showAlertModal(message) {
    // 기존 모달이 있으면 재사용, 없으면 생성
    let modal = document.getElementById('commonAlertModal');

    if (!modal) {
        const modalHtml = `
            <div class="modal fade" id="commonAlertModal" tabindex="-1">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">알림</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body" id="commonAlertModalBody">
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-primary" data-bs-dismiss="modal">확인</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        document.body.insertAdjacentHTML('beforeend', modalHtml);
        modal = document.getElementById('commonAlertModal');

        // 모달이 닫힐 때 정리
        modal.addEventListener('hidden.bs.modal', function () {
            // backdrop 제거 (혹시 남아있을 경우)
            const backdrops = document.querySelectorAll('.modal-backdrop');
            backdrops.forEach(backdrop => backdrop.remove());
        });
    }

    // 메시지 업데이트
    const modalBody = document.getElementById('commonAlertModalBody');
    modalBody.textContent = message;

    // 모달 표시
    const bsModal = new bootstrap.Modal(modal);
    bsModal.show();
}

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
