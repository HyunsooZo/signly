/**
 * Signature JavaScript
 * Handles digital signature functionality
 */

class SignatureManager {
    constructor() {
        this.contractData = {};
        this.csrfParam = '';
        this.csrfToken = '';
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
        
        this.csrfParam = window.csrfParam || '';
        this.csrfToken = window.csrfToken || '';
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
        
        const container = document.getElementById('signaturePadContainer');
        if (!container) {
            return;
        }
        
        this.signatureCanvas = container.querySelector('.signature-canvas');
        this.signaturePlaceholder = container.querySelector('.signature-placeholder');
        this.signatureSubmitButton = container.querySelector('.signature-submit');
        this.signatureClearButton = container.querySelector('.signature-clear');
        
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
            this.hidePlaceholder();
            this.updateSignatureState();
        };
        
        this.signaturePad.onEnd = () => {
            this.updateSignatureState();
        };
        
        this.resizeSignatureCanvas();
        
        // 이전 리스너 제거 후 새로 추가
        window.removeEventListener('resize', this.resizeSignatureCanvas);
        window.addEventListener('resize', () => this.resizeSignatureCanvas());
        
        // 브라우저 환경에서도 서명 시작/완료 감지를 위한 추가 이벤트 리스너
        // resizeSignatureCanvas 이후에 추가해야 canvas가 준비된 상태
        this.signatureCanvas.addEventListener('mousedown', () => this.hidePlaceholder());
        this.signatureCanvas.addEventListener('touchstart', () => this.hidePlaceholder());
        this.signatureCanvas.addEventListener('mouseup', () => this.updateSignatureState());
        this.signatureCanvas.addEventListener('touchend', () => this.updateSignatureState());
        
        if (this.signatureClearButton) {
            this.signatureClearButton.addEventListener('click', () => {
                this.signaturePad.clear();
                this.showPlaceholder();
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
    }
    
    resizeSignatureCanvas() {
        if (!this.signatureCanvas) {
            return;
        }
        
        const ratio = Math.max(window.devicePixelRatio || 1, 1);
        const rect = this.signatureCanvas.getBoundingClientRect();
        
        // 현재 서명 데이터 저장
        const currentData = this.signaturePad ? this.signaturePad.toData() : [];
        
        // 캔버스 크기 조정
        this.signatureCanvas.width = rect.width * ratio;
        this.signatureCanvas.height = rect.height * ratio;
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
        if (!this.signaturePad || !this.signatureSubmitButton) {
            return;
        }
        
        const hasSignature = !this.signaturePad.isEmpty();
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
        const confirmBtn = modal.querySelector('.confirm-signature-btn');
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
            
            if (this.csrfParam && this.csrfToken) {
                payload.append(this.csrfParam, this.csrfToken);
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

// Initialize the signature manager when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    // Set CSRF data from JSP
    window.csrfParam = '${_csrf.parameterName}';
    window.csrfToken = '${_csrf.token}';
    
    window.signatureManager = new SignatureManager();
});