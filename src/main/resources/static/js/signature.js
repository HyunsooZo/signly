/**
 * Signature JavaScript
 * Handles digital signature functionality
 */

class SignatureManager {
    constructor() {
        this.contractData = {};
        this.signaturePad = null;
        this.signatureCanvas = null;
        this.signaturePlaceholder = null;
        this.signatureSubmitButton = null;
        this.signatureClearButton = null;
        this.isInitialized = false;

        this.init();
    }

    init() {
        this.loadData();
        this.setupEventListeners();
    }

    loadData() {
        const signingDataElement = document.getElementById('signingData');
        const signingDataset = signingDataElement ? signingDataElement.dataset : {};

        this.contractData = {
            id: signingDataset.contractId || '',
            token: signingDataset.token || '',
            title: signingDataset.contractTitle || '',
            signerName: signingDataset.signerName || '',
            signerEmail: signingDataset.signerEmail || ''
        };
    }

    setupEventListeners() {
        document.addEventListener('DOMContentLoaded', () => {
            this.initSignaturePad();
            this.bindEvents();
        });
    }

    initSignaturePad() {
        // 이미 초기화되었으면 무시
        if (this.isInitialized) {
            return;
        }

        console.log('Initializing signature pad...');

        const container = document.getElementById('signaturePadContainer');
        console.log('Container found:', !!container);
        if (!container) {
            return;
        }

        this.signatureCanvas = container.querySelector('.signature-canvas');
        this.signatureSubmitButton = container.querySelector('.signature-submit');
        this.signatureClearButton = container.querySelector('.signature-clear');

        console.log('Canvas found:', !!this.signatureCanvas);
        console.log('Canvas dimensions:', this.signatureCanvas.width, 'x', this.signatureCanvas.height);

        if (!this.signatureCanvas) {
            console.error('Signature canvas not found');
            return;
        }

        this.signaturePad = new window.SignaturePad(this.signatureCanvas, {
            backgroundColor: 'rgba(0, 0, 0, 0)',
            penColor: '#1d4ed8',
            minWidth: 0.8,
            maxWidth: 2.5
        });

        this.signaturePad.onBegin = () => {
            console.log('Signature began');
            this.updateSignatureState();
        };

        this.signaturePad.onEnd = () => {
            console.log('Signature ended');
            this.updateSignatureState();
        };

        this.resizeSignatureCanvas();

        // 이전 리스너 제거 후 새로 추가
        window.removeEventListener('resize', this.resizeSignatureCanvas);
        window.addEventListener('resize', () => this.resizeSignatureCanvas());

        // 브라우저 환경에서도 서명 시작/완료 감지를 위한 추가 이벤트 리스너
        // resizeSignatureCanvas 이후에 추가해야 canvas가 준비된 상태
        this.signatureCanvas.addEventListener('mousedown', (e) => {
            console.log('Canvas mousedown detected');
            e.preventDefault();
        });
        this.signatureCanvas.addEventListener('touchstart', (e) => {
            console.log('Canvas touchstart detected');
            e.preventDefault();
        });
        this.signatureCanvas.addEventListener('mouseup', () => this.updateSignatureState());
        this.signatureCanvas.addEventListener('touchend', () => this.updateSignatureState());

        if (this.signatureClearButton) {
            this.signatureClearButton.addEventListener('click', () => {
                this.signaturePad.clear();
                this.updateSignatureState();
                this.handleSignatureClear();
            });
        }

        if (this.signatureSubmitButton) {
            this.signatureSubmitButton.addEventListener('click', () => {
                if (this.signaturePad.isEmpty()) {
                    if (window.Signly && window.Signly.showAlert) {
                        window.Signly.showAlert('서명을 입력해 주세요.', 'warning');
                    }
                    return;
                }

                const signatureData = this.signaturePad.toDataURL('image/png');
                this.handleSignatureSubmit(signatureData);
            });
        }

        this.isInitialized = true;
        this.updateSignatureState();

        // 초기화 확인
        setTimeout(() => {
            if (this.signaturePad) {
                console.log('SignaturePad initialized successfully');
                console.log('Canvas element:', this.signatureCanvas);
                console.log('SignaturePad object:', this.signaturePad);
                console.log('Canvas dimensions:', this.signatureCanvas.width, 'x', this.signatureCanvas.height);
            } else {
                console.error('SignaturePad initialization failed');
            }
        }, 100);
    }

    resizeSignatureCanvas() {
        if (!this.signatureCanvas) {
            return;
        }

        const ratio = Math.max(window.devicePixelRatio || 1, 1);
        const container = this.signatureCanvas.parentElement;
        const width = container.clientWidth - 32; // padding 제외
        const height = 200; // 명시적 높이

        // 현재 서명 데이터 저장
        const currentData = this.signaturePad ? this.signaturePad.toData() : [];

        // 캔버스 크기 조정
        this.signatureCanvas.width = width * ratio;
        this.signatureCanvas.height = height * ratio;
        this.signatureCanvas.getContext('2d').scale(ratio, ratio);

        // 서명 데이터 복원
        if (this.signaturePad && currentData.length > 0) {
            this.signaturePad.fromData(currentData);
        }

        this.updateSignatureState();
    }

    bindEvents() {
        // 모달 관련 이벤트
        const modal = document.getElementById('signatureConfirmModal');
        if (modal) {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    this.closeModal();
                }
            });
        }

        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.closeModal();
            }
        });

        // 페이지 이탈 방지
        window.addEventListener('beforeunload', (e) => {
            if (this.signaturePad && !this.signaturePad.isEmpty()) {
                e.preventDefault();
                e.returnValue = '서명이 완료되지 않았습니다. 페이지를 떠나시겠습니까?';
            }
        });
    }



    updateSignatureState() {
        if (!this.signaturePad || !this.signatureSubmitButton) {
            return;
        }

        const hasSignature = !this.signaturePad.isEmpty();
        console.log('Signature state updated - hasSignature:', hasSignature);
        this.signatureSubmitButton.disabled = !hasSignature;

        if (hasSignature) {
            this.signatureSubmitButton.classList.remove('disabled');
        } else {
            this.signatureSubmitButton.classList.add('disabled');
        }
    }

    handleSignatureClear() {
        // 서명 지우기 후 처리 로직
        console.log('Signature cleared');
    }

    handleSignatureSubmit(signatureData) {
        // 확인 모달 표시
        this.showSignatureConfirmModal(signatureData);
    }

    showSignatureConfirmModal(signatureData) {
        const modal = document.getElementById('signatureConfirmModal');
        if (!modal) {
            // 모달이 없으면 바로 서명 제출
            this.submitSignature(signatureData);
            return;
        }

        // 모달에 서명 이미지 표시
        const signatureImage = modal.querySelector('.signature-preview-image');
        if (signatureImage) {
            signatureImage.src = signatureData;
        }

        // 확인 버튼 이벤트 설정
        const confirmBtn = modal.querySelector('#finalSignBtn');
        if (confirmBtn) {
            // 기존 이벤트 리스너 제거
            const newConfirmBtn = confirmBtn.cloneNode(true);
            confirmBtn.parentNode.replaceChild(newConfirmBtn, confirmBtn);

            // 새 이벤트 리스너 추가
            newConfirmBtn.addEventListener('click', () => {
                this.submitSignature(signatureData);
                this.closeModal();
            });
        }

        // 모달 표시
        modal.classList.add('show');
        modal.style.display = 'block';
        document.body.classList.add('modal-open');
    }

    async submitSignature(signatureData) {
        const finalSignBtn = document.getElementById('finalSignBtn');
        if (!finalSignBtn) {
            return;
        }

        const originalHtml = finalSignBtn.innerHTML;

        try {
            finalSignBtn.disabled = true;
            finalSignBtn.innerHTML = '<span class="spinner"></span> 서명 처리중...';

            const signatureDataUrl = this.signaturePad ? this.signaturePad.toDataURL('image/png') : null;
            if (!signatureDataUrl) {
                throw new Error('서명 데이터가 존재하지 않습니다.');
            }

            const payload = new URLSearchParams();
            payload.append('signatureData', signatureDataUrl);
            payload.append('signerName', this.contractData.signerName);
            payload.append('signerEmail', this.contractData.signerEmail);

            const headers = {
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
            };

            if (window.csrfManager) {
                window.csrfManager.appendTokenToParams(payload);
            }

            const response = await fetch('/sign/' + this.contractData.token + '/sign', {
                method: 'POST',
                headers,
                body: payload.toString()
            });

            if (!response.ok) {
                throw new Error('서명 처리 중 오류가 발생했습니다.');
            }

            const result = await response.json();

            if (result.success) {
                window.location.href = '/sign/' + this.contractData.token + '/complete';
                return;
            }

            throw new Error(result.message || '서명 처리 중 오류가 발생했습니다.');

        } catch (error) {
            console.error('Signature submission error:', error);
            if (window.Signly && window.Signly.showAlert) {
                window.Signly.showAlert(error.message || '서명 처리 중 오류가 발생했습니다.', 'danger');
            }
            finalSignBtn.disabled = false;
            finalSignBtn.innerHTML = originalHtml;
        }
    }

    closeModal() {
        const modal = document.getElementById('signatureConfirmModal');
        if (modal) {
            modal.classList.remove('show');
            modal.style.display = 'none';
            document.body.classList.remove('modal-open');
        }
    }
}

// Initialize immediately (class defers DOM work internally)
window.signatureManager = new SignatureManager();

// 테스트용 강제 서명 추가
window.testSignature = () => {
    if (window.signatureManager && window.signatureManager.signaturePad) {
        // 간단한 테스트 서명 그리기
        window.signatureManager.signaturePad.fromData([{
            points: [{x: 10, y: 10}, {x: 50, y: 50}, {x: 100, y: 10}],
            penColor: '#1d4ed8'
        }]);
        window.signatureManager.updateSignatureState();
        console.log('Test signature added');
    } else {
        console.error('SignatureManager not initialized');
    }
};
