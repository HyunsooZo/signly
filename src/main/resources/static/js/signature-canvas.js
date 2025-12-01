/**
 * Canvas 기반 서명 기능 - Deally
 */

class SignatureCanvas {
    constructor(canvasElement, options = {}) {
        this.canvas = canvasElement;
        this.ctx = this.canvas.getContext('2d');
        this.isDrawing = false;
        this.lastX = 0;
        this.lastY = 0;

        // 기본 옵션
        this.options = {
            strokeColor: '#000000',
            strokeWidth: 2,
            backgroundColor: '#ffffff',
            maxWidth: 600,
            maxHeight: 300,
            penColor: '#000000',
            penWidth: 2,
            ...options
        };

        this.init();
        this.bindEvents();
    }

    /**
     * 캔버스 초기화
     */
    init() {
        // 캔버스 크기 설정
        this.setCanvasSize();

        // 캔버스 스타일 설정
        this.ctx.lineCap = 'round';
        this.ctx.lineJoin = 'round';
        this.ctx.strokeStyle = this.options.strokeColor;
        this.ctx.lineWidth = this.options.strokeWidth;

        // 배경색 설정
        this.clear();

        // 레티나 디스플레이 대응
        this.setupHighDPI();
    }

    /**
     * 캔버스 크기 설정
     */
    setCanvasSize() {
        const container = this.canvas.parentElement;
        const containerWidth = container.clientWidth;
        const containerHeight = container.clientHeight;

        // 최대 크기 제한
        const width = Math.min(containerWidth, this.options.maxWidth);
        const height = Math.min(containerHeight, this.options.maxHeight);

        this.canvas.width = width;
        this.canvas.height = height;

        // CSS 크기도 설정
        this.canvas.style.width = width + 'px';
        this.canvas.style.height = height + 'px';
    }

    /**
     * 고해상도 디스플레이 대응
     */
    setupHighDPI() {
        const devicePixelRatio = window.devicePixelRatio || 1;
        const backingStoreRatio = this.ctx.webkitBackingStorePixelRatio ||
                                 this.ctx.mozBackingStorePixelRatio ||
                                 this.ctx.msBackingStorePixelRatio ||
                                 this.ctx.oBackingStorePixelRatio ||
                                 this.ctx.backingStorePixelRatio || 1;

        const ratio = devicePixelRatio / backingStoreRatio;

        if (devicePixelRatio !== backingStoreRatio) {
            const oldWidth = this.canvas.width;
            const oldHeight = this.canvas.height;

            this.canvas.width = oldWidth * ratio;
            this.canvas.height = oldHeight * ratio;

            this.canvas.style.width = oldWidth + 'px';
            this.canvas.style.height = oldHeight + 'px';

            this.ctx.scale(ratio, ratio);
        }
    }

    /**
     * 이벤트 바인딩
     */
    bindEvents() {
        // 마우스 이벤트
        this.canvas.addEventListener('mousedown', this.startDrawing.bind(this));
        this.canvas.addEventListener('mousemove', this.draw.bind(this));
        this.canvas.addEventListener('mouseup', this.stopDrawing.bind(this));
        this.canvas.addEventListener('mouseout', this.stopDrawing.bind(this));

        // 터치 이벤트 (모바일 지원)
        this.canvas.addEventListener('touchstart', this.handleTouch.bind(this));
        this.canvas.addEventListener('touchmove', this.handleTouch.bind(this));
        this.canvas.addEventListener('touchend', this.stopDrawing.bind(this));

        // 드래그 방지
        this.canvas.addEventListener('dragstart', (e) => e.preventDefault());

        // 윈도우 리사이즈 시 캔버스 크기 조정
        window.addEventListener('resize', this.debounce(() => {
            this.setCanvasSize();
            this.clear();
        }, 250));
    }

    /**
     * 터치 이벤트 처리
     */
    handleTouch(e) {
        e.preventDefault();

        const touch = e.touches[0] || e.changedTouches[0];
        const mouseEvent = new MouseEvent(this.getTouchEventType(e.type), {
            clientX: touch.clientX,
            clientY: touch.clientY
        });

        this.canvas.dispatchEvent(mouseEvent);
    }

    /**
     * 터치 이벤트 타입 변환
     */
    getTouchEventType(touchEventType) {
        switch (touchEventType) {
            case 'touchstart': return 'mousedown';
            case 'touchmove': return 'mousemove';
            case 'touchend': return 'mouseup';
            default: return touchEventType;
        }
    }

    /**
     * 그리기 시작
     */
    startDrawing(e) {
        this.isDrawing = true;
        const coords = this.getCoordinates(e);
        this.lastX = coords.x;
        this.lastY = coords.y;

        // 점 찍기 (클릭만 했을 때)
        this.ctx.beginPath();
        this.ctx.arc(this.lastX, this.lastY, this.options.strokeWidth / 2, 0, Math.PI * 2);
        this.ctx.fill();

        this.onDrawingStart();
    }

    /**
     * 그리기
     */
    draw(e) {
        if (!this.isDrawing) return;

        const coords = this.getCoordinates(e);

        this.ctx.beginPath();
        this.ctx.moveTo(this.lastX, this.lastY);
        this.ctx.lineTo(coords.x, coords.y);
        this.ctx.stroke();

        this.lastX = coords.x;
        this.lastY = coords.y;

        this.onDrawing();
    }

    /**
     * 그리기 종료
     */
    stopDrawing() {
        if (this.isDrawing) {
            this.isDrawing = false;
            this.onDrawingEnd();
        }
    }

    /**
     * 좌표 계산
     */
    getCoordinates(e) {
        const rect = this.canvas.getBoundingClientRect();
        const scaleX = this.canvas.width / rect.width;
        const scaleY = this.canvas.height / rect.height;

        return {
            x: (e.clientX - rect.left) * scaleX,
            y: (e.clientY - rect.top) * scaleY
        };
    }

    /**
     * 캔버스 지우기
     */
    clear() {
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

        // 배경색 설정
        this.ctx.fillStyle = this.options.backgroundColor;
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);

        // 펜 스타일 복원
        this.ctx.strokeStyle = this.options.strokeColor;
        this.ctx.fillStyle = this.options.strokeColor;

        this.onClear();
    }

    /**
     * 서명 데이터 가져오기 (Base64)
     */
    getSignatureData(format = 'image/png', quality = 0.9) {
        if (this.isEmpty()) {
            return null;
        }

        return this.canvas.toDataURL(format, quality);
    }

    /**
     * 서명 데이터 설정
     */
    setSignatureData(dataUrl) {
        const img = new Image();
        img.onload = () => {
            this.clear();
            this.ctx.drawImage(img, 0, 0, this.canvas.width, this.canvas.height);
            this.onSignatureLoaded();
        };
        img.src = dataUrl;
    }

    /**
     * 캔버스가 비어있는지 확인
     */
    isEmpty() {
        const imageData = this.ctx.getImageData(0, 0, this.canvas.width, this.canvas.height);
        const pixels = imageData.data;

        // 모든 픽셀이 배경색과 같은지 확인
        for (let i = 0; i < pixels.length; i += 4) {
            // RGB 값이 배경색과 다르면 비어있지 않음
            if (pixels[i] !== 255 || pixels[i + 1] !== 255 || pixels[i + 2] !== 255) {
                return false;
            }
        }

        return true;
    }

    /**
     * 펜 설정 변경
     */
    setPenStyle(color, width) {
        this.options.strokeColor = color;
        this.options.strokeWidth = width;
        this.ctx.strokeStyle = color;
        this.ctx.lineWidth = width;
        this.ctx.fillStyle = color;
    }

    /**
     * 캔버스 크기 조정
     */
    resize(width, height) {
        const imageData = this.getSignatureData();

        this.canvas.width = width;
        this.canvas.height = height;
        this.canvas.style.width = width + 'px';
        this.canvas.style.height = height + 'px';

        this.init();

        if (imageData) {
            this.setSignatureData(imageData);
        }
    }

    /**
     * 디바운스 함수
     */
    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    // 이벤트 콜백 함수들 (오버라이드 가능)
    onDrawingStart() {
        // 그리기 시작 시 호출
        this.canvas.dispatchEvent(new CustomEvent('signatureStart'));
    }

    onDrawing() {
        // 그리는 중 호출
        this.canvas.dispatchEvent(new CustomEvent('signatureDrawing'));
    }

    onDrawingEnd() {
        // 그리기 종료 시 호출
        this.canvas.dispatchEvent(new CustomEvent('signatureEnd'));
    }

    onClear() {
        // 지우기 시 호출
        this.canvas.dispatchEvent(new CustomEvent('signatureClear'));
    }

    onSignatureLoaded() {
        // 서명 로드 시 호출
        this.canvas.dispatchEvent(new CustomEvent('signatureLoaded'));
    }

    /**
     * 소멸자
     */
    destroy() {
        // 이벤트 리스너 제거
        this.canvas.removeEventListener('mousedown', this.startDrawing);
        this.canvas.removeEventListener('mousemove', this.draw);
        this.canvas.removeEventListener('mouseup', this.stopDrawing);
        this.canvas.removeEventListener('mouseout', this.stopDrawing);
        this.canvas.removeEventListener('touchstart', this.handleTouch);
        this.canvas.removeEventListener('touchmove', this.handleTouch);
        this.canvas.removeEventListener('touchend', this.stopDrawing);
    }
}

/**
 * 서명 패드 컴포넌트
 */
class SignaturePad {
    constructor(container, options = {}) {
        this.container = typeof container === 'string' ?
            document.querySelector(container) : container;

        this.options = {
            title: '서명해 주세요',
            clearButtonText: '지우기',
            submitButtonText: '서명 완료',
            showClearButton: true,
            showSubmitButton: true,
            required: false,
            ...options
        };

        this.signatureCanvas = null;
        this.init();
    }

    /**
     * 서명 패드 초기화
     */
    init() {
        this.createElements();
        this.initCanvas();
        this.bindEvents();
    }

    /**
     * DOM 요소 생성
     */
    createElements() {
        this.container.innerHTML = `
            <div class="signature-pad">
                <div class="signature-header">
                    <h3 class="signature-title">${this.options.title}</h3>
                    ${this.options.required ? '<span class="signature-required">*</span>' : ''}
                </div>
                <div class="signature-canvas-container">
                    <canvas class="signature-canvas"></canvas>
                    <div class="signature-placeholder">여기에 서명해 주세요</div>
                </div>
                <div class="signature-controls">
                    ${this.options.showClearButton ?
                        `<button type="button" class="btn btn-secondary signature-clear">
                            ${this.options.clearButtonText}
                        </button>` : ''
                    }
                    ${this.options.showSubmitButton ?
                        `<button type="button" class="btn btn-primary signature-submit">
                            ${this.options.submitButtonText}
                        </button>` : ''
                    }
                </div>
                <div class="signature-error" style="display: none;"></div>
            </div>
        `;

        // 요소 참조 저장
        this.canvasElement = this.container.querySelector('.signature-canvas');
        this.placeholder = this.container.querySelector('.signature-placeholder');
        this.clearButton = this.container.querySelector('.signature-clear');
        this.submitButton = this.container.querySelector('.signature-submit');
        this.errorElement = this.container.querySelector('.signature-error');
    }

    /**
     * 캔버스 초기화
     */
    initCanvas() {
        this.signatureCanvas = new SignatureCanvas(this.canvasElement, this.options);

        // 캔버스 이벤트 리스너
        this.canvasElement.addEventListener('signatureStart', () => {
            this.hidePlaceholder();
            this.hideError();
        });

        this.canvasElement.addEventListener('signatureClear', () => {
            this.showPlaceholder();
            this.updateSubmitButton();
        });

        this.canvasElement.addEventListener('signatureEnd', () => {
            this.updateSubmitButton();
        });
    }

    /**
     * 이벤트 바인딩
     */
    bindEvents() {
        if (this.clearButton) {
            this.clearButton.addEventListener('click', () => {
                this.clear();
            });
        }

        if (this.submitButton) {
            this.submitButton.addEventListener('click', () => {
                this.submit();
            });
        }
    }

    /**
     * 플레이스홀더 숨기기
     */
    hidePlaceholder() {
        if (this.placeholder) {
            this.placeholder.style.display = 'none';
        }
    }

    /**
     * 플레이스홀더 보이기
     */
    showPlaceholder() {
        if (this.placeholder) {
            this.placeholder.style.display = 'flex';
        }
    }

    /**
     * 제출 버튼 상태 업데이트
     */
    updateSubmitButton() {
        if (this.submitButton) {
            const isEmpty = this.signatureCanvas.isEmpty();
            this.submitButton.disabled = isEmpty;
        }
    }

    /**
     * 서명 지우기
     */
    clear() {
        this.signatureCanvas.clear();
        this.showPlaceholder();
        this.hideError();
        this.updateSubmitButton();

        // 이벤트 발생
        this.container.dispatchEvent(new CustomEvent('signatureClear'));
    }

    /**
     * 서명 제출
     */
    submit() {
        if (this.signatureCanvas.isEmpty()) {
            this.showError('서명을 입력해 주세요.');
            return false;
        }

        const signatureData = this.signatureCanvas.getSignatureData();

        // 이벤트 발생
        const event = new CustomEvent('signatureSubmit', {
            detail: { signatureData }
        });

        this.container.dispatchEvent(event);
        return true;
    }

    /**
     * 서명 데이터 가져오기
     */
    getSignatureData() {
        return this.signatureCanvas.getSignatureData();
    }

    /**
     * 서명 데이터 설정
     */
    setSignatureData(dataUrl) {
        this.signatureCanvas.setSignatureData(dataUrl);
        this.hidePlaceholder();
        this.updateSubmitButton();
    }

    /**
     * 비어있는지 확인
     */
    isEmpty() {
        return this.signatureCanvas.isEmpty();
    }

    /**
     * 에러 메시지 표시
     */
    showError(message) {
        if (this.errorElement) {
            this.errorElement.textContent = message;
            this.errorElement.style.display = 'block';
        }
    }

    /**
     * 에러 메시지 숨기기
     */
    hideError() {
        if (this.errorElement) {
            this.errorElement.style.display = 'none';
        }
    }

    /**
     * 유효성 검사
     */
    validate() {
        if (this.options.required && this.isEmpty()) {
            this.showError('서명은 필수입니다.');
            return false;
        }

        this.hideError();
        return true;
    }

    /**
     * 소멸자
     */
    destroy() {
        if (this.signatureCanvas) {
            this.signatureCanvas.destroy();
        }
        this.container.innerHTML = '';
    }
}

// 전역 객체로 노출
window.SignatureCanvas = SignatureCanvas;
window.SignaturePad = SignaturePad;