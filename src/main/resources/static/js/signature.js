/**
 * Signature JavaScript
 * Handles digital signature functionality
 */

console.log('[SignatureManager] Script loaded');

class SignatureManager {
    constructor() {
        this.contractData = {};
        this.signaturePad = null;
        this.signatureCanvas = null;
        this.signaturePlaceholder = null;
        this.signatureClearButton = null;
        this.completeSigningBtn = null;
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
        // DOM이 이미 로드되었는지 확인
        if (document.readyState === 'loading') {
            // 아직 로딩 중이면 DOMContentLoaded 이벤트 대기
            console.log('[SignatureManager] DOM is still loading, waiting for DOMContentLoaded');
            document.addEventListener('DOMContentLoaded', () => {
                console.log('[SignatureManager] DOMContentLoaded event fired');
                this.initSignaturePad();
                this.bindEvents();
            });
        } else {
            // 이미 로드되었으면 바로 실행
            console.log('[SignatureManager] DOM already loaded, initializing immediately');
            this.initSignaturePad();
            this.bindEvents();
        }
    }

    initSignaturePad() {
        console.log('[SignatureManager] initSignaturePad called');

        // 이미 초기화되었으면 무시
        if (this.isInitialized) {
            console.log('[SignatureManager] Already initialized, skipping');
            return;
        }

        const container = document.getElementById('signaturePadContainer');
        if (!container) {
            console.error('[SignatureManager] signaturePadContainer not found');
            return;
        }
        console.log('[SignatureManager] Container found');

        this.signatureCanvas = container.querySelector('.signature-canvas');
        this.signaturePlaceholder = container.querySelector('.signature-placeholder');
        this.signatureClearButton = container.querySelector('.signature-clear');
        this.completeSigningBtn = document.getElementById('completeSigningBtn');

        console.log('[SignatureManager] Canvas:', this.signatureCanvas);
        console.log('[SignatureManager] Complete button:', this.completeSigningBtn);

        if (!this.signatureCanvas) {
            console.error('[SignatureManager] Signature canvas not found');
            return;
        }

        if (!window.SignaturePad) {
            console.error('[SignatureManager] SignaturePad library not loaded');
            return;
        }

        // canvas 크기를 먼저 설정 (SignaturePad 생성 전에 필요)
        console.log('[SignatureManager] Resizing canvas BEFORE SignaturePad creation');
        this.resizeSignatureCanvas();
        console.log('[SignatureManager] Canvas size after resize:', this.signatureCanvas.width, 'x', this.signatureCanvas.height);

        this.signaturePad = new window.SignaturePad(this.signatureCanvas, {
            backgroundColor: 'rgba(0, 0, 0, 0)',
            penColor: '#1d4ed8',
            minWidth: 0.8,
            maxWidth: 2.5
        });
        console.log('[SignatureManager] SignaturePad created');

        this.signaturePad.onBegin = () => {
            console.log('[SignatureManager] SignaturePad onBegin triggered');
            this.hidePlaceholder();
            this.updateSignatureState();
        };

        this.signaturePad.onEnd = () => {
            console.log('[SignatureManager] SignaturePad onEnd triggered');
            this.updateSignatureState();
        };

        // resize 이벤트 리스너 추가
        window.addEventListener('resize', () => this.resizeSignatureCanvas());

        // 브라우저(마우스) 환경에서도 서명 시작/완료 감지를 위한 추가 이벤트 리스너
        // SignaturePad의 onBegin/onEnd는 터치에서만 작동하므로 마우스 이벤트 추가
        this.signatureCanvas.addEventListener('mousedown', (e) => {
            console.log('[SignatureManager] mousedown event', e);
            this.hidePlaceholder();
        });

        this.signatureCanvas.addEventListener('touchstart', (e) => {
            console.log('[SignatureManager] touchstart event', e);
            this.hidePlaceholder();
        });

        // mouseup 이벤트를 document 레벨에서 리스닝 (캔버스 밖에서 놓아도 감지)
        document.addEventListener('mouseup', () => {
            // 서명 중이었는지 확인 (signaturePad가 drawing 상태였는지)
            if (this.signaturePad && !this.signaturePad.isEmpty()) {
                console.log('[SignatureManager] mouseup event (document level)');
                this.updateSignatureState();
            }
        });

        document.addEventListener('touchend', () => {
            // 터치 종료 시에도 상태 업데이트
            if (this.signaturePad && !this.signaturePad.isEmpty()) {
                console.log('[SignatureManager] touchend event (document level)');
                this.updateSignatureState();
            }
        });

        if (this.signatureClearButton) {
            this.signatureClearButton.addEventListener('click', () => {
                this.signaturePad.clear();
                this.showPlaceholder();
                this.updateSignatureState();
                this.handleSignatureClear();
            });
        }

        // 서명 완료 버튼 이벤트 리스너
        if (this.completeSigningBtn) {
            this.completeSigningBtn.addEventListener('click', () => {
                if (this.signaturePad.isEmpty()) {
                    if (window.Deally && window.Deally.showAlert) {
                        window.Deally.showAlert('서명을 입력해 주세요.', 'warning');
                    }
                    return;
                }

                const signatureData = this.signaturePad.toDataURL('image/png');
                this.showSignatureConfirmModal(signatureData);
            });
        }

        this.isInitialized = true;
        this.updateSignatureState();
        console.log('[SignatureManager] Initialization complete');
    }

    resizeSignatureCanvas() {
        if (!this.signatureCanvas) {
            console.error('[SignatureManager] Canvas element not found in resizeSignatureCanvas');
            return;
        }

        const ratio = Math.max(window.devicePixelRatio || 1, 1);
        const rect = this.signatureCanvas.getBoundingClientRect();

        console.log('[SignatureManager] Canvas rect:', rect);
        console.log('[SignatureManager] Device pixel ratio:', ratio);

        // canvas의 실제 크기가 0이면 기본값 사용
        const width = rect.width || 600;
        const height = rect.height || 260;

        console.log('[SignatureManager] Calculated width:', width, 'height:', height);

        // 현재 서명 데이터 저장
        const currentData = this.signaturePad ? this.signaturePad.toData() : [];

        // 캔버스 크기 조정
        this.signatureCanvas.width = width * ratio;
        this.signatureCanvas.height = height * ratio;

        console.log('[SignatureManager] Canvas width set to:', this.signatureCanvas.width);
        console.log('[SignatureManager] Canvas height set to:', this.signatureCanvas.height);

        const ctx = this.signatureCanvas.getContext('2d');
        if (!ctx) {
            console.error('[SignatureManager] Failed to get canvas context');
            return;
        }

        ctx.scale(ratio, ratio);

        // CSS 크기 설정 확인
        this.signatureCanvas.style.width = width + 'px';
        this.signatureCanvas.style.height = height + 'px';
        console.log('[SignatureManager] Canvas style width:', this.signatureCanvas.style.width, 'height:', this.signatureCanvas.style.height);

        // 서명 데이터 복원
        if (this.signaturePad && currentData.length > 0) {
            this.signaturePad.fromData(currentData);
        }

        this.updateSignatureState();

        // 최종 확인
        console.log('[SignatureManager] Final canvas dimensions - width:', this.signatureCanvas.width, 'height:', this.signatureCanvas.height);
        console.log('[SignatureManager] Canvas is ready for drawing');
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

    hidePlaceholder() {
        if (this.signaturePlaceholder) {
            this.signaturePlaceholder.style.display = 'none';
        }
    }

    showPlaceholder() {
        if (this.signaturePlaceholder) {
            this.signaturePlaceholder.style.display = 'block';
        }
    }

    updateSignatureState() {
        if (!this.signaturePad) {
            console.warn('[SignatureManager] updateSignatureState: signaturePad not initialized');
            return;
        }

        const hasSignature = !this.signaturePad.isEmpty();
        console.log('[SignatureManager] updateSignatureState - hasSignature:', hasSignature);

        // 서명 완료 버튼 제어
        if (this.completeSigningBtn) {
            console.log('[SignatureManager] Updating button state - disabled:', !hasSignature);
            this.completeSigningBtn.disabled = !hasSignature;
            if (hasSignature) {
                this.completeSigningBtn.classList.remove('disabled');
                console.log('[SignatureManager] Button enabled');
            } else {
                this.completeSigningBtn.classList.add('disabled');
                console.log('[SignatureManager] Button disabled');
            }
        } else {
            console.warn('[SignatureManager] completeSigningBtn not found');
        }
    }

    handleSignatureClear() {
        // 서명 지우기 후 처리 로직
        console.log('Signature cleared');
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

        // 취소 버튼 이벤트 설정
        const cancelBtn = modal.querySelector('#cancelSignBtn');
        if (cancelBtn) {
            const newCancelBtn = cancelBtn.cloneNode(true);
            cancelBtn.parentNode.replaceChild(newCancelBtn, cancelBtn);

            newCancelBtn.addEventListener('click', () => {
                this.closeModal();
            });
        }

        // 확인 및 제출 버튼 이벤트 설정
        const finalSignBtn = modal.querySelector('#finalSignBtn');
        if (finalSignBtn) {
            const newFinalSignBtn = finalSignBtn.cloneNode(true);
            finalSignBtn.parentNode.replaceChild(newFinalSignBtn, finalSignBtn);

            newFinalSignBtn.addEventListener('click', () => {
                this.submitSignature(signatureData);
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

            // JWT 클라이언트를 사용한 인증 요청
            let response;
            if (window.jwtClient) {
                response = await window.jwtClient.fetchWithAuth('/sign/' + this.contractData.token + '/sign', {
                    method: 'POST',
                    headers,
                    body: payload.toString()
                });
            } else {
                // JWT 클라이언트가 없으면 일반 fetch 사용 (서명 페이지는 토큰 기반 인증)
                response = await fetch('/sign/' + this.contractData.token + '/sign', {
                    method: 'POST',
                    headers,
                    body: payload.toString()
                });
            }

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
            if (window.Deally && window.Deally.showAlert) {
                window.Deally.showAlert(error.message || '서명 처리 중 오류가 발생했습니다.', 'danger');
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