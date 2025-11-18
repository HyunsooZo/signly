/**
 * Contract Form Handler
 * Manages all contract form operations including preset, template, and custom contracts
 * Refactored from inline JavaScript in form.jsp
 */
class ContractForm {
    constructor() {
        // ===== DOM REFERENCES =====
        this.contractContentTextarea = document.getElementById('content');
        this.currentUserId = document.body?.dataset?.currentUserId || '';

        // Layout elements
        this.normalLayout = document.getElementById('normalLayout');
        this.templateLayout = document.getElementById('templateLayout');

        // Template elements
        this.templateHtmlContainer = document.getElementById('templateHtmlContainer');
        this.templateTitleDisplay = document.getElementById('templateLayoutTitle');
        this.templateHiddenContent = document.getElementById('templateContentHidden');
        this.templateHiddenTitle = document.getElementById('templateTitleHidden');
        this.templateFirstPartyName = document.getElementById('templateFirstPartyName');
        this.templateFirstPartyEmail = document.getElementById('templateFirstPartyEmail');
        this.templateFirstPartyAddress = document.getElementById('templateFirstPartyAddress');
        this.templateSecondPartyName = document.getElementById('templateSecondPartyName');
        this.templateSecondPartyEmailInput = document.getElementById('templateSecondPartyEmail');
        this.templateExpiresAtInput = document.getElementById('templateExpiresAt');
        this.templateExpirationCard = document.getElementById('templateExpirationCard');

        // Normal layout elements
        this.customVariableContainer = document.getElementById('customVariablesContainer');
        this.customVariableFieldsWrapper = document.getElementById('customVariableFields');
        this.customContentPreviewWrapper = document.getElementById('customContentPreviewWrapper');
        this.customContentPreview = document.getElementById('customContentPreview');

        // Other elements
        this.presetSelect = document.getElementById('presetSelect');
        this.expiresAtInput = document.getElementById('expiresAt');
        this.firstPartyNameInput = document.getElementById('firstPartyName');
        this.firstPartyEmailInput = document.getElementById('firstPartyEmail');
        this.firstPartyAddressInput = document.getElementById('firstPartyAddress');
        this.secondPartyNameInput = document.getElementById('secondPartyName');
        this.secondPartyEmailInput = document.getElementById('secondPartyEmail');
        this.secondPartyAddressInput = document.getElementById('secondPartyAddress');

        // ===== STATE =====
        this.customVariableValues = {};
        this.customVariables = [];
        this.legacyVariableCounter = 0;

        // ===== CONSTANTS =====
        this.PLACEHOLDER_REGEX = /\{([^{}]+)\}|\[([^\[\]]+)\]/g;
        this.IGNORED_PLACEHOLDERS = new Set(['EMPLOYER_SIGNATURE_IMAGE']);

        // ===== DATA =====
        this.ownerInfo = null;
        this.ownerSignatureInfo = null;
        this.ownerSignatureDataUrl = '';
        this.ownerSignatureUpdatedAt = '';
        this.existingContractData = null;
        this.selectedTemplateData = null;
        this.hasSelectedPreset = document.querySelector('input[name="selectedPreset"]')?.value || '';

        // ===== INITIALIZATION =====
        this.init();
    }

    // ========================================
    // SECTION 1: INITIALIZATION
    // ========================================

    init() {
        // 먼저 JSON 데이터 파싱
        this.parseJsonData();
        this.loadOwnerData();

        console.log('[DEBUG] init - selectedTemplateData:', this.selectedTemplateData);
        console.log('[DEBUG] init - existingContractData:', this.existingContractData);

        // Check if script tag exists
        const templateEl = document.getElementById('selectedTemplateData');
        console.log('[DEBUG] selectedTemplateData element exists:', !!templateEl);
        if (templateEl) {
            console.log('[DEBUG] selectedTemplateData content:', templateEl.textContent);
        }

        // 초기 레이아웃 설정
        if (this.selectedTemplateData) {
            console.log('[DEBUG] Loading template with data:', {
                title: this.selectedTemplateData.title,
                hasHtml: !!this.selectedTemplateData.renderedHtml,
                htmlLength: this.selectedTemplateData.renderedHtml?.length || 0
            });
            this.switchToTemplateMode();
            this.loadTemplateAsPreset(
                this.selectedTemplateData.title || '',
                this.selectedTemplateData.renderedHtml || '',
                this.selectedTemplateData.variables || {}
            );
        } else {
            console.log('[DEBUG] No template data, redirecting to select-type');
            // 파라미터 없으면 select-type.jsp로 리다이렉트
            window.location.href = '/contracts/select-type';
        }
    }

    parseJsonData() {
        // Parse existingContractData
        const existingEl = document.getElementById('existingContractData');
        if (existingEl) {
            try {
                this.existingContractData = JSON.parse(existingEl.textContent);
            } catch (error) {
                console.error('[ERROR] 기존 계약 데이터 파싱 실패:', error);
            }
        }

        // Parse selectedTemplateData
        const templateEl = document.getElementById('selectedTemplateData');
        if (templateEl) {
            try {
                this.selectedTemplateData = JSON.parse(templateEl.textContent);
            } catch (error) {
                console.error('[ERROR] 선택된 템플릿 데이터 파싱 실패:', error);
            }
        }
    }

    loadOwnerData() {
        this.ownerInfo = this.readOwnerInfo();
        this.ownerSignatureInfo = this.readOwnerSignature();
        this.ownerSignatureDataUrl = this.ownerSignatureInfo.dataUrl || '';
        this.ownerSignatureUpdatedAt = this.ownerSignatureInfo.updatedAt || '';
    }

    setupEventListeners() {
        // Content textarea input
        if (this.contractContentTextarea) {
            this.contractContentTextarea.addEventListener('input', () => {
                this.detectCustomVariables();
                this.updateDirectPreview();
            });
        }

        // Preset select change
        if (this.presetSelect) {
            this.presetSelect.addEventListener('change', async (event) => {
                const presetId = event.target.value;
                if (!presetId) return;
                await this.loadPresetById(presetId);
                this.presetSelect.value = '';
            });
        }

        // Form submit validation
        this.setupFormValidation();
    }

    setupFormValidation() {
        const forms = document.getElementsByClassName('contract-form');
        Array.from(forms).forEach(form => {
            form.addEventListener('submit', (event) => {
                this.handleFormSubmit(event, form);
            });
        });
    }

    checkAutoLoad() {
        // Check for selectedPreset
        if (this.hasSelectedPreset) {
            this.loadPresetById(this.hasSelectedPreset);
        }

        // Check for existing contract data
        if (this.existingContractData && this.existingContractData.content) {
            this.loadExistingContractAsPreset();
        }
    }

    // ========================================
    // SECTION 2: UTILITY METHODS
    // ========================================

    readOwnerInfo() {
        const datasetInfo = (() => {
            const dataset = document.body?.dataset || {};
            if (
                dataset.ownerName ||
                dataset.ownerEmail ||
                dataset.ownerCompany ||
                dataset.ownerPhone ||
                dataset.ownerAddress
            ) {
                return {
                    name: dataset.ownerName || '',
                    email: dataset.ownerEmail || '',
                    userId: dataset.currentUserId || '',
                    companyName: dataset.ownerCompany || '',
                    businessPhone: dataset.ownerPhone || '',
                    businessAddress: dataset.ownerAddress || ''
                };
            }
            return null;
        })();

        try {
            const raw = localStorage.getItem('signly_user_info');
            if (!raw) {
                return datasetInfo;
            }
            const parsed = JSON.parse(raw);
            if (!parsed || typeof parsed !== 'object') {
                return datasetInfo;
            }
            return {
                name: parsed.name || datasetInfo?.name || '',
                email: parsed.email || datasetInfo?.email || '',
                userId: parsed.userId || datasetInfo?.userId || '',
                companyName: parsed.companyName || datasetInfo?.companyName || '',
                businessPhone: parsed.businessPhone || datasetInfo?.businessPhone || '',
                businessAddress: parsed.businessAddress || datasetInfo?.businessAddress || ''
            };
        } catch (error) {
            console.warn('[WARN] 사용자 정보를 불러올 수 없습니다:', error);
            return datasetInfo;
        }
    }

    readOwnerSignature() {
        try {
            const raw = localStorage.getItem('signly_owner_signature');
            if (!raw) {
                return {};
            }
            const parsed = JSON.parse(raw);
            if (!parsed || typeof parsed !== 'object') {
                return {};
            }
            return {
                dataUrl: typeof parsed.dataUrl === 'string' ? parsed.dataUrl : '',
                updatedAt: typeof parsed.updatedAt === 'string' ? parsed.updatedAt : ''
            };
        } catch (error) {
            console.warn('[WARN] 사업주 서명 데이터를 불러올 수 없습니다:', error);
            return {};
        }
    }

    forEachPlaceholder(text, callback) {
        if (!text) {
            return;
        }
        this.PLACEHOLDER_REGEX.lastIndex = 0;
        let match;
        while ((match = this.PLACEHOLDER_REGEX.exec(text)) !== null) {
            const name = (match[1] ? match[1].trim() : (match[2] ? match[2].trim() : ''));
            if (!name) {
                continue;
            }
            callback(name, match[0], match.index);
        }
    }

    toLocalDateTimeValue(date) {
        const pad = (value) => String(value).padStart(2, '0');
        return [
            date.getFullYear(),
            '-', pad(date.getMonth() + 1),
            '-', pad(date.getDate()),
            'T', pad(date.getHours()),
            ':', pad(date.getMinutes())
        ].join('');
    }

    decodeHtmlEntities(value) {
        if (!value) {
            return '';
        }
        const textarea = document.createElement('textarea');
        textarea.innerHTML = value;
        return textarea.value;
    }

    sanitizeHtml(html) {
        if (!html) {
            return '';
        }
        const template = document.createElement('template');
        template.innerHTML = html;
        template.content.querySelectorAll('script').forEach(node => node.remove());
        return template.innerHTML;
    }

    escapeRegExp(text) {
        return text.replace(/[.*+?^$()|[\]{}\\]/g, '\\$&');
    }

    // ========================================
    // SECTION 3: EXPIRATION DATE MANAGEMENT
    // ========================================

    updateExpirationMinAttributes() {
        const minValue = this.toLocalDateTimeValue(new Date());
        const normal = this.expiresAtInput;
        const preset = this.templateExpiresAtInput;
        if (normal) {
            normal.min = minValue;
        }
        if (preset) {
            preset.min = minValue;
        }
    }

    syncExpirationField(sourceField) {
        const normal = this.expiresAtInput;
        const preset = this.templateExpiresAtInput;
        if (!sourceField) {
            return;
        }
        if (sourceField === normal && preset) {
            preset.value = normal.value;
        } else if (sourceField === preset && normal) {
            normal.value = preset.value;
        }
    }

    initializeExpirationInputs() {
        if (!this.expiresAtInput && !this.templateExpiresAtInput) {
            return;
        }

        this.updateExpirationMinAttributes();

        const defaultValue = this.toLocalDateTimeValue(new Date(Date.now() + 24 * 60 * 60 * 1000));
        const minValue = this.expiresAtInput?.min || this.templateExpiresAtInput?.min || this.toLocalDateTimeValue(new Date());
        let initialValue = '';
        if (this.expiresAtInput && this.expiresAtInput.value) {
            initialValue = this.expiresAtInput.value;
        } else if (this.templateExpiresAtInput && this.templateExpiresAtInput.value) {
            initialValue = this.templateExpiresAtInput.value;
        } else {
            initialValue = defaultValue;
        }

        if (minValue && initialValue < minValue) {
            initialValue = minValue;
        }

        if (this.expiresAtInput && !this.expiresAtInput.value) {
            this.expiresAtInput.value = initialValue;
        } else if (this.expiresAtInput && this.expiresAtInput.value && this.expiresAtInput.value < minValue) {
            this.expiresAtInput.value = minValue;
        }
        if (this.templateExpiresAtInput && !this.templateExpiresAtInput.value) {
            this.templateExpiresAtInput.value = initialValue;
        } else if (this.templateExpiresAtInput && this.templateExpiresAtInput.value && this.templateExpiresAtInput.value < minValue) {
            this.templateExpiresAtInput.value = minValue;
        }

        [this.expiresAtInput, this.templateExpiresAtInput].forEach((field) => {
            if (!field) {
                return;
            }
            field.addEventListener('focus', () => {
                this.updateExpirationMinAttributes();
                if (field.min && field.value && field.value < field.min) {
                    field.value = field.min;
                    this.syncExpirationField(field);
                }
            });
            field.addEventListener('input', () => this.syncExpirationField(field));
        });

        this.syncExpirationField(this.expiresAtInput && this.expiresAtInput.value ? this.expiresAtInput : this.templateExpiresAtInput);
    }

    // ========================================
    // SECTION 4: PRESET MODE METHODS
    // ========================================

    switchToTemplateMode() {
        if (this.normalLayout) {
            this.normalLayout.style.display = 'none';
        }
        if (this.templateLayout) {
            this.templateLayout.style.display = '';
        }
        if (this.mainFormCol) {
            this.mainFormCol.classList.remove('col-lg-8');
            this.mainFormCol.classList.add('col-lg-12');
        }
        if (this.partyInfoCol) {
            this.partyInfoCol.style.display = 'none';
        }
        if (this.actionButtonRow) {
            this.actionButtonRow.style.display = 'none';
        }
        if (this.partyInfoCard) {
            this.partyInfoCard.style.display = 'none';
        }
        if (this.expirationCard) {
            this.expirationCard.style.display = 'none';
        }

        if (this.contentSection) {
            this.contentSection.style.display = 'none';
        }

        this.updateExpirationMinAttributes();
        const normalExpires = this.expiresAtInput;


        // 일반 레이아웃의 required 필드 비활성화
        if (this.normalLayout) {
            document.querySelectorAll('#normalLayout [required]').forEach(field => {
                field.removeAttribute('required');
            });
        }
    }

    switchToNormalMode() {
        // 에러 처리용으로 유지 (실제로는 사용되지 않음)
        console.warn('[WARN] switchToNormalMode() called but normalLayout has been removed');
    }

    renderTemplateHtml(html, templateName) {
        if (!this.templateHtmlContainer) return;

        console.log('[DEBUG] renderTemplateHtml - Input HTML length:', html.length);
        console.log('[DEBUG] renderTemplateHtml - First 500 chars:', html.substring(0, 500));

        // 백엔드에서 이미 렌더링된 HTML을 그대로 사용
        // body 태그 내용만 추출
        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = html;

        const bodyTag = tempDiv.querySelector('body');
        let contentHtml = bodyTag ? bodyTag.innerHTML : tempDiv.innerHTML;

        // body 안에 있는 style 태그 제거
        contentHtml = contentHtml.replace(/<style[^>]*>[\s\S]*?<\/style>/gi, '');

        console.log('[DEBUG] Content HTML length:', contentHtml.length);
        console.log('[DEBUG] Content HTML first 500 chars:', contentHtml.substring(0, 500));

        // 컨테이너에 preset-document 클래스 추가하고 HTML 삽입
        this.templateHtmlContainer.className = 'preset-document';
        this.templateHtmlContainer.innerHTML = contentHtml;

        console.log('[DEBUG] After innerHTML - container has', this.templateHtmlContainer.children.length, 'children');
        console.log('[DEBUG] Container HTML:', this.templateHtmlContainer.innerHTML.substring(0, 500));

        // 변수를 입력 필드로 교체
        this.replaceVariablesWithInputs(this.templateHtmlContainer);
        this.restoreSavedVariableInputs(this.templateHtmlContainer);

        // 제목 설정
        if (this.templateTitleDisplay) {
            this.templateTitleDisplay.textContent = templateName || '계약서';
        }
        if (this.templateHiddenTitle) {
            this.templateHiddenTitle.value = templateName || '계약서';
        }
    }

    replaceVariablesWithInputs(container) {
        // 먼저 data-variable-name 속성을 가진 요소들을 찾아서 input으로 교체
        const variableElements = container.querySelectorAll('[data-variable-name]');
        variableElements.forEach(element => {
            const varName = element.getAttribute('data-variable-name');
            if (!varName) return;

            // 이미 input이 있으면 스킵
            if (element.querySelector('input')) return;

            // 서명 이미지 처리
            if (varName === 'EMPLOYER_SIGNATURE_IMAGE') {
                const signatureImg = this.createSignatureImage();
                element.innerHTML = '';
                element.appendChild(signatureImg);
                return;
            }

            // 변수를 input으로 교체
            const input = document.createElement('input');
            input.type = 'text';
            input.className = 'contract-input-inline';
            input.setAttribute('data-variable-name', varName);

            // 변수 타입에 따라 size 설정
            const upper = varName.toUpperCase();
            const normalized = upper.replace(/[-_\s]/g, '');
            let inputSize = 10;
            let maxLength = null;

            if (normalized.includes('NAME') || upper === 'EMPLOYER' || upper === 'EMPLOYEE' ||
                upper.includes('이름') || upper === '사업주' || upper === '근로자') {
                inputSize = 6;
                maxLength = 10;
            } else if (normalized.includes('DATE') || upper.includes('날짜')) {
                inputSize = 11;
                maxLength = 10;
            } else if (normalized.includes('ADDRESS') || upper.includes('주소')) {
                inputSize = 20;
                maxLength = 50;
            } else if (normalized.includes('PHONE') || upper.includes('전화')) {
                inputSize = 13;
                maxLength = 15;
            }

            input.size = inputSize;
            if (maxLength) input.maxLength = maxLength;
            input.placeholder = this.getPlaceholderExample(varName, upper, normalized);

            const value = this.getDefaultValueForVariable(varName);
            if (value) input.value = value;

            input.addEventListener('input', () => this.updateTemplateContent());

            // ✅ 핵심 수정: wrapper 구조 보존
            element.innerHTML = '';
            element.appendChild(input);
        });

        // 텍스트 노드에서 [변수명] 또는 {변수명} 패턴도 처리
        const walker = document.createTreeWalker(container, NodeFilter.SHOW_TEXT);
        const textNodes = [];

        while (walker.nextNode()) {
            const node = walker.currentNode;
            if (node.nodeValue && (node.nodeValue.includes('[') || node.nodeValue.includes('{'))) {
                textNodes.push(node);
            }
        }

        textNodes.forEach(node => {
            const text = node.nodeValue;
            const parent = node.parentNode;
            if (!parent) return;

            const fragment = document.createDocumentFragment();
            let lastIndex = 0;
            const regex = /\[([^\]]+)\]|\{([^}]+)\}/g;
            let match;

            while ((match = regex.exec(text)) !== null) {
                if (match.index > lastIndex) {
                    fragment.appendChild(document.createTextNode(text.substring(lastIndex, match.index)));
                }

                const varName = match[1] || match[2];

                if (varName === 'EMPLOYER_SIGNATURE_IMAGE') {
                    const signatureImg = this.createSignatureImage();
                    fragment.appendChild(signatureImg);
                } else {
                    const input = this.createVariableInput(varName);
                    fragment.appendChild(input);
                }

                lastIndex = regex.lastIndex;
            }

            if (lastIndex < text.length) {
                fragment.appendChild(document.createTextNode(text.substring(lastIndex)));
            }

            parent.replaceChild(fragment, node);
        });

        // TODO: [REFACTORING] This method violates SRP and needs refactoring
        // - Extract VariableTypeResolver domain service for type inference
        // - Extract VariableInputRenderer UI component for element creation
        // - Extract VariableValueResolver for default value resolution
        // See GitHub Issue #XXX for detailed refactoring plan

        // 사업주 정보 자동 입력
        this.applyOwnerInfoToTemplateForm();
    }

    restoreSavedVariableInputs(container) {
        const savedSpans = container.querySelectorAll('.contract-variable-underline');
        savedSpans.forEach(span => {
            if (span.querySelector('input')) {
                return;
            }
            let varName = span.getAttribute('data-variable-name');
            if (!varName) {
                varName = 'LEGACY_FIELD_' + (this.legacyVariableCounter++);
                span.setAttribute('data-variable-name', varName);
            }
            const value = span.textContent || '';
            const wrapper = this.createVariableInput(varName);
            const input = wrapper.querySelector('input[data-variable-name]');
            if (input) {
                input.value = value;
            }
            if (varName) {
                this.customVariableValues[varName] = value;
            }
            span.replaceWith(wrapper);
        });
    }

    createVariableInput(varName) {
        const upper = varName.toUpperCase();
        const normalized = upper.replace(/[-_\s]/g, '');

        // 변수 타입에 따라 적절한 문자 수 결정 (size 속성)
        let inputSize = 10;
        let maxLength = null;

        // 이름 관련 (최대 6자) - 한글과 영문 모두 지원
        if (normalized.includes('NAME') || upper === 'EMPLOYER' || upper === 'EMPLOYEE' ||
            upper.includes('이름') || upper === '사업주' || upper === '근로자' || upper === '직원' ||
            upper === '갑' || upper === '을') {
            inputSize = 6;
            maxLength = 10;
        }
        // 날짜 관련 (yyyy-mm-dd = 10자)
        else if (normalized.includes('DATE') || upper.includes('날짜') || upper.includes('일자') ||
            upper.includes('계약일') || upper.includes('시작일') || upper.includes('종료일')) {
            inputSize = 11;
            maxLength = 10;
        }
        // 시간 관련 (hh:mm = 5자)
        else if (normalized.includes('TIME') || upper.includes('시간') || upper.includes('시각')) {
            inputSize = 6;
            maxLength = 5;
        }
        // 요일, 숫자 등 짧은 값
        else if (normalized.includes('DAY') || normalized.includes('DAYS') || normalized.includes('HOLIDAYS') ||
            upper.includes('요일') || upper.includes('휴일')) {
            inputSize = 4;
            maxLength = 10;
        }
        // 주소, 장소, 업무 등 긴 값 (최대 20자)
        else if (normalized.includes('ADDRESS') || normalized.includes('WORKPLACE') || normalized.includes('DESCRIPTION') ||
            upper.includes('주소') || upper.includes('장소') || upper.includes('업무') || upper.includes('내용')) {
            inputSize = 20;
            maxLength = 50;
        }
        // 급여, 금액 관련
        else if (normalized.includes('SALARY') || normalized.includes('BONUS') || normalized.includes('ALLOWANCE') ||
            normalized.includes('PAYMENT') || normalized.includes('METHOD') ||
            upper.includes('급여') || upper.includes('임금') || upper.includes('금액') || upper.includes('지급') || upper.includes('방법')) {
            inputSize = 12;
            maxLength = 30;
        }
        // 전화번호
        else if (normalized.includes('PHONE') || normalized.includes('TEL') || upper.includes('전화') || upper.includes('연락처')) {
            inputSize = 13;
            maxLength = 15;
        }
        // 이메일
        else if (normalized.includes('EMAIL') || normalized.includes('MAIL') || upper.includes('이메일') || upper.includes('메일')) {
            inputSize = 20;
            maxLength = 50;
        }
        // 회사명/조직명
        else if (normalized.includes('COMPANY') || normalized.includes('ORGANIZATION') ||
            upper.includes('회사') || upper.includes('조직')) {
            inputSize = 15;
            maxLength = 30;
        }

        const wrapper = document.createElement('span');
        wrapper.className = 'contract-variable-underline';
        wrapper.setAttribute('data-variable-name', varName);

        const input = document.createElement('input');
        input.type = 'text';
        input.className = 'contract-input-inline';
        input.size = inputSize;
        if (maxLength) {
            input.maxLength = maxLength;
        }
        input.setAttribute('data-variable-name', varName);

        // 적절한 플레이스홀더 설정
        input.placeholder = this.getPlaceholderExample(varName, upper, normalized);

        // 자동 값 설정
        const value = this.getDefaultValueForVariable(varName);
        if (value) {
            input.value = value;
        }

        input.addEventListener('input', () => this.updatePresetContent());

        wrapper.appendChild(input);
        return wrapper;
    }

    getPlaceholderExample(varName, upper, normalized) {
        // 이름 관련
        if (normalized.includes('NAME') || upper === 'EMPLOYER' || upper === 'EMPLOYEE' ||
            upper.includes('이름') || upper === '사업주' || upper === '근로자' || upper === '직원' ||
            upper === '갑' || upper === '을') {
            if (upper.includes('EMPLOYEE') || upper.includes('근로자') || upper.includes('직원') || upper === '을') {
                return '예) 홍길동';
            }
            return '예) 김철수';
        }
        // 날짜 관련
        if (normalized.includes('DATE') || upper.includes('날짜') || upper.includes('일자') ||
            upper.includes('계약일') || upper.includes('시작일') || upper.includes('종료일')) {
            return '예) 2025-01-01';
        }
        // 시간 관련
        if (normalized.includes('TIME') || upper.includes('시간') || upper.includes('시각')) {
            return '예) 09:00';
        }
        // 요일
        if (normalized.includes('DAY') || normalized.includes('DAYS') || upper.includes('요일')) {
            return '예) 월~금';
        }
        // 휴일
        if (normalized.includes('HOLIDAYS') || upper.includes('휴일')) {
            return '예) 토, 일요일';
        }
        // 주소
        if (normalized.includes('ADDRESS') || upper.includes('주소')) {
            return '예) 서울시 강남구';
        }
        // 장소
        if (normalized.includes('WORKPLACE') || upper.includes('장소')) {
            return '예) 본사 사무실';
        }
        // 업무 내용
        if (normalized.includes('DESCRIPTION') || normalized.includes('JOB') || upper.includes('업무') || upper.includes('내용')) {
            return '예) 소프트웨어 개발';
        }
        // 급여
        if (normalized.includes('SALARY') || upper.includes('급여') || upper.includes('임금')) {
            return '예) 3,000,000';
        }
        // 상여금
        if (normalized.includes('BONUS') || upper.includes('상여')) {
            return '예) 연 500만원';
        }
        // 수당
        if (normalized.includes('ALLOWANCE') || upper.includes('수당')) {
            return '예) 식대 10만원';
        }
        // 지급일
        if (normalized.includes('PAYMENT') && normalized.includes('DAY') || upper.includes('지급일')) {
            return '예) 25';
        }
        // 지급방법
        if (normalized.includes('METHOD') || upper.includes('방법')) {
            return '예) 계좌이체';
        }
        // 전화번호
        if (normalized.includes('PHONE') || normalized.includes('TEL') || upper.includes('전화') || upper.includes('연락처')) {
            return '예) 010-1234-5678';
        }
        // 이메일
        if (normalized.includes('EMAIL') || normalized.includes('MAIL') || upper.includes('이메일') || upper.includes('메일')) {
            return '예) hong@example.com';
        }
        // 회사명
        if (normalized.includes('COMPANY') || normalized.includes('ORGANIZATION') || upper.includes('회사') || upper.includes('조직')) {
            return '예) (주)테크컴퍼니';
        }

        // 기본값
        return '';
    }

    createSignatureImage() {
        // localStorage에서 서명 이미지 가져오기
        const signatureRaw = localStorage.getItem('signly_owner_signature');

        if (!signatureRaw) {
            // 서명이 없으면 빈 span 반환
            const span = document.createElement('span');
            span.textContent = '(서명 없음)';
            span.style.cssText = 'color: #999; font-size: 11px;';
            return span;
        }

        try {
            const signatureData = JSON.parse(signatureRaw);
            const imgSrc = signatureData.dataUrl || signatureData.imageData || signatureData.signatureData || signatureData;

            if (!imgSrc) {
                console.warn('[WARN] 서명 이미지 데이터가 없습니다:', signatureData);
                const span = document.createElement('span');
                span.textContent = '(서명 없음)';
                span.style.cssText = 'color: #999; font-size: 11px;';
                return span;
            }

            const img = document.createElement('img');
            img.src = imgSrc;
            img.className = 'signature-stamp-image-element';
            img.style.cssText = 'display: inline-block; max-width: 90px; max-height: 40px; vertical-align: middle;';
            img.alt = '사업주 서명';

            return img;
        } catch (error) {
            console.error('[ERROR] 서명 이미지 파싱 실패:', error);
            const span = document.createElement('span');
            span.textContent = '(서명 오류)';
            span.style.cssText = 'color: #f00; font-size: 11px;';
            return span;
        }
    }

    getDefaultValueForVariable(varName) {
        if (!varName) return '';

        const upper = varName.toUpperCase();
        const normalized = upper.replace(/[-_\s]/g, '');

        // 사업주/고용주 이름
        if (normalized.includes('EMPLOYER') && normalized.includes('NAME') ||
            upper === 'EMPLOYER' ||
            normalized.includes('OWNER') && normalized.includes('NAME') ||
            upper === '사업주' || upper === '사업주명' || upper === '갑') {
            return this.ownerInfo?.name || '';
        }

        // 사업주/고용주 이메일
        if (normalized.includes('EMPLOYER') && normalized.includes('EMAIL') ||
            normalized.includes('OWNER') && normalized.includes('EMAIL') ||
            upper === '사업주이메일') {
            return this.ownerInfo?.email || '';
        }

        // 회사명/조직명
        if (normalized.includes('COMPANY') || normalized.includes('ORGANIZATION') ||
            upper === '회사' || upper === '회사명' || upper === '조직' || upper === '조직명') {
            return this.ownerInfo?.companyName || '';
        }

        // 사업주 전화번호
        if (normalized.includes('EMPLOYER') && (normalized.includes('PHONE') || normalized.includes('TEL')) ||
            normalized.includes('OWNER') && (normalized.includes('PHONE') || normalized.includes('TEL')) ||
            upper === '사업주전화번호' || upper === '사업장전화번호' || upper === '업체전화번호') {
            return this.ownerInfo?.businessPhone || '';
        }

        // 사업주 주소
        if (normalized.includes('EMPLOYER') && normalized.includes('ADDRESS') ||
            normalized.includes('OWNER') && normalized.includes('ADDRESS') ||
            upper === '사업주주소' || upper === '사업장주소' || upper === '업체주소') {
            return this.ownerInfo?.businessAddress || '';
        }

        // 근로자/직원 이름
        if (normalized.includes('EMPLOYEE') && normalized.includes('NAME') ||
            upper === 'EMPLOYEE' ||
            upper === '근로자' || upper === '근로자명' || upper === '직원' || upper === '직원명' || upper === '을') {
            return '';
        }

        // 날짜 관련
        if (normalized.includes('DATE') || normalized.includes('START') ||
            upper === '날짜' || upper === '계약일' || upper === '시작일') {
            return new Date().toISOString().split('T')[0];
        }

        return '';
    }

    applyOwnerInfoToTemplateForm() {
        if (!this.ownerInfo) return;

        if (this.templateFirstPartyName) {
            this.templateFirstPartyName.value = this.ownerInfo.name || '';
        }
        if (this.templateFirstPartyEmail) {
            this.templateFirstPartyEmail.value = this.ownerInfo.email || '';
        }
        if (this.templateFirstPartyAddress) {
            this.templateFirstPartyAddress.value = this.ownerInfo.companyName || '';
        }
    }

    updateTemplateContent() {
        if (!this.templateHtmlContainer) return;

        // 모든 입력 필드의 값을 변수로 치환한 HTML 생성
        const clone = this.templateHtmlContainer.cloneNode(true);
        const inputs = clone.querySelectorAll('input[data-variable-name]');

        inputs.forEach(input => {
            const varName = input.getAttribute('data-variable-name');
            const value = (input.value || '').trim(); // 앞뒤 공백 제거
            const wrapper = input.parentNode;
            if (wrapper) {
                wrapper.setAttribute('data-variable-name', varName || '');
                wrapper.textContent = value;
            } else {
                input.replaceWith(document.createTextNode(value));
            }

            // secondPartyName hidden 필드 업데이트
            if (varName.toUpperCase() === 'EMPLOYEE' || varName.toUpperCase() === 'EMPLOYEE_NAME') {
                if (this.templateSecondPartyName) {
                    this.templateSecondPartyName.value = value;
                }
            }
        });

        // 서명 이미지를 img src에서 실제 이미지 데이터로 교체
        const signatureImgs = clone.querySelectorAll('img.signature-stamp-image-element');
        signatureImgs.forEach(img => {
            // 이미 src가 있으면 그대로 유지 (Base64 데이터)
            if (img.src) {
                // img 태그를 그대로 유지
            } else {
                // 없으면 제거
                img.remove();
            }
        });

        // 빈 변수 뒤의 불필요한 텍스트 정리
        this.cleanupEmptyVariableContext(clone);

        // Hidden textarea에 HTML 저장
        if (this.templateHiddenContent) {
            this.templateHiddenContent.value = clone.innerHTML;
        }
    }

    cleanupEmptyVariableContext(container) {
        const walker = document.createTreeWalker(container, NodeFilter.SHOW_TEXT);
        const textNodes = [];

        while (walker.nextNode()) {
            const node = walker.currentNode;
            if (node.nodeValue && node.nodeValue.trim() === '') {
                textNodes.push(node);
            }
        }

        textNodes.forEach(node => {
            // 부모가 p 태그이고 다른 자식이 없으면 p 태그 제거
            const parent = node.parentNode;
            if (parent && parent.tagName === 'P' && parent.children.length === 0) {
                parent.remove();
            } else {
                node.remove();
            }
        });
    }

    // ========================================
    // SECTION 5: PRESET LOADING
    // ========================================

    async loadPresetById(presetId) {
        try {
            const response = await fetch('/templates/presets/' + presetId, {
                headers: {'Accept': 'application/json'}
            });

            if (!response.ok) {
                showAlertModal('표준 양식을 불러오지 못했습니다.');
                return;
            }

            const preset = await response.json();
            console.log('[DEBUG] Preset data:', preset);

            const sectionHtml = Array.isArray(preset.sections)
                ? preset.sections.map(section => section.content || '').join('\n')
                : '';

            // renderedHtml이 이미 HTML이면 그대로 사용, 아니면 디코딩
            let rendered = preset.renderedHtml || sectionHtml || '';

            console.log('[DEBUG] Initial HTML length:', rendered.length);
            console.log('[DEBUG] Has <br> tags:', rendered.includes('<br'));
            console.log('[DEBUG] Has <span> tags:', rendered.includes('<span'));
            console.log('[DEBUG] Has &lt; entities:', rendered.includes('&lt;'));
            console.log('[DEBUG] Has &gt; entities:', rendered.includes('&gt;'));

            // HTML 엔티티가 인코딩되어 있는지 확인
            if (rendered.includes('&lt;') || rendered.includes('&gt;')) {
                console.log('[DEBUG] Decoding HTML entities...');
                rendered = this.decodeHtmlEntities(rendered);
                console.log('[DEBUG] After decode - Has <br> tags:', rendered.includes('<br'));
            }

            console.log('[DEBUG] Final HTML length:', rendered.length);
            console.log('[DEBUG] HTML preview (first 500):', rendered.substring(0, 500));
            console.log('[DEBUG] HTML contains "section-number":', rendered.includes('section-number'));

            // 템플릿 모드로 전환
            this.switchToTemplateMode();

            // HTML 렌더링
            this.renderTemplateHtml(rendered, preset.name);

            // 수정 모드인 경우 기존 데이터로 필드 채우기
            this.applyExistingContractData();

        } catch (error) {
            console.error('프리셋 로딩 실패:', error);
            showAlertModal('표준 양식을 불러오지 못했습니다.');
        }
    }

    loadTemplateAsPreset(templateTitle, templateHtml, templateVariables) {
        console.log('[DEBUG] Loading template as preset:', templateTitle);
        console.log('[DEBUG] Template HTML length:', templateHtml ? templateHtml.length : 0);
        console.log('[DEBUG] Template variables:', templateVariables);
        console.log('[DEBUG] templateHtmlContainer exists:', !!this.templateHtmlContainer);

        let rendered = templateHtml || '';

        // HTML 엔티티가 인코딩되어 있는지 확인
        if (rendered.includes('&lt;') || rendered.includes('&gt;')) {
            console.log('[DEBUG] Decoding HTML entities...');
            rendered = this.decodeHtmlEntities(rendered);
        }

        console.log('[DEBUG] Rendered HTML length after decode:', rendered.length);
        console.log('[DEBUG] First 200 chars:', rendered.substring(0, 200));

        // 템플릿 모드로 전환
        this.switchToTemplateMode();

        // HTML 렌더링
        this.renderTemplateHtml(rendered, templateTitle);

        // 변수 기반 입력 폼 생성
        if (templateVariables && Object.keys(templateVariables).length > 0) {
            this.renderTemplateVariableForms(templateVariables);
        }

        this.applyExistingContractData();
    }

    extractSavedVariableValues(savedHtml) {
        if (!savedHtml) {
            return [];
        }
        const decoded = this.decodeHtmlEntities(savedHtml);
        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = decoded;
        const nodes = tempDiv.querySelectorAll('.contract-variable-underline');
        return Array.from(nodes).map(node => ({
            name: (node.getAttribute('data-variable-name') || '').trim(),
            value: (node.textContent || '').trim()
        }));
    }

    fillPresetInputsFromSavedValues(savedValues, attempt = 0) {
        if (!this.templateHtmlContainer || attempt > 10) {
            return;
        }
        const inputs = this.templateHtmlContainer.querySelectorAll('input[data-variable-name]');
        if (!inputs.length) {
            setTimeout(() => this.fillPresetInputsFromSavedValues(savedValues, attempt + 1), 60);
            return;
        }

        const valueMap = new Map();
        savedValues.forEach(entry => {
            if (entry.name) {
                valueMap.set(entry.name.toUpperCase(), entry.value);
            }
        });

        inputs.forEach((input, index) => {
            const key = (input.getAttribute('data-variable-name') || '').trim();
            let value = key ? valueMap.get(key.toUpperCase()) : undefined;
            if (value === undefined && savedValues[index]) {
                value = savedValues[index].value;
            }
            if (value !== undefined) {
                input.value = value;
                if (key) {
                    this.customVariableValues[key] = value;
                }
            }
        });

        this.updateTemplateContent();
        this.updateDirectPreview();
    }

    applyExistingContractData() {
        if (!this.existingContractData) {
            return;
        }

        if (this.existingContractData.secondPartyEmail) {
            const emailField = this.templateSecondPartyEmailInput || this.secondPartyEmailInput;
            if (emailField) {
                emailField.value = this.existingContractData.secondPartyEmail;
            }
        }

        if (this.existingContractData.firstPartyName) {
            const target = this.templateFirstPartyName || this.firstPartyNameInput;
            if (target) {
                target.value = this.existingContractData.firstPartyName;
            }
        }
        if (this.existingContractData.firstPartyEmail) {
            const target = this.templateFirstPartyEmail || this.firstPartyEmailInput;
            if (target) {
                target.value = this.existingContractData.firstPartyEmail;
            }
        }
        if (this.existingContractData.firstPartyAddress) {
            const target = this.templateFirstPartyAddress || this.firstPartyAddressInput;
            if (target) {
                target.value = this.existingContractData.firstPartyAddress;
            }
        }
        if (this.existingContractData.secondPartyName) {
            const target = this.templateSecondPartyName || this.secondPartyNameInput;
            if (target) {
                target.value = this.existingContractData.secondPartyName;
            }
        }

        if (this.existingContractData.content) {
            const savedValues = this.extractSavedVariableValues(this.existingContractData.content);
            if (savedValues.length) {
                this.fillPresetInputsFromSavedValues(savedValues);
            } else if (this.contractContentTextarea && !this.contractContentTextarea.value) {
                this.contractContentTextarea.value = this.decodeHtmlEntities(this.existingContractData.content);
                this.detectCustomVariables();
                this.updateDirectPreview();
            }
        }
    }

    loadExistingContractAsPreset() {
        if (!this.existingContractData || !this.existingContractData.content) {
            return;
        }
        const decoded = this.decodeHtmlEntities(this.existingContractData.content);
        this.switchToTemplateMode();
        const title = this.existingContractData.title || this.templateHiddenTitle?.value || '계약서';
        this.renderTemplateHtml(decoded, title);
        this.applyExistingContractData();
    }

    // ========================================
    // SECTION 6: TEMPLATE VARIABLE FORMS
    // ========================================

    renderTemplateVariableForms(variables) {
        if (!this.templateHtmlContainer) return;

        console.log('[DEBUG] Rendering variable forms for:', variables);

        // 컨테이너 아래에 변수 입력 섹션 추가
        let variableSection = document.getElementById('templateVariableSection');
        if (!variableSection) {
            variableSection = document.createElement('div');
            variableSection.id = 'templateVariableSection';
            variableSection.className = 'card mb-4';
            variableSection.innerHTML = `
                    <div class="card-header">
                        <h5 class="card-title mb-0">
                            <i class="bi bi-sliders2-vertical me-2"></i>변수 값 입력
                        </h5>
                    </div>
                    <div class="card-body">
                        <div class="row g-3" id="templateVariableFields"></div>
                    </div>
                `;

            // templateExpirationCard 이전에 삽입
            const expirationCard = document.getElementById('templateExpirationCard');
            if (expirationCard && expirationCard.parentNode) {
                expirationCard.parentNode.insertBefore(variableSection, expirationCard);
            }
        }

        const fieldsContainer = document.getElementById('templateVariableFields');
        if (!fieldsContainer) return;

        fieldsContainer.innerHTML = '';

        // 각 변수에 대한 입력 필드 생성
        Object.entries(variables).forEach(([varName, varDef]) => {
            const fieldWrapper = document.createElement('div');
            fieldWrapper.className = 'col-md-6';

            const label = document.createElement('label');
            label.className = 'form-label';
            label.textContent = varDef.label || varName;
            if (varDef.required) {
                const requiredSpan = document.createElement('span');
                requiredSpan.className = 'text-danger ms-1';
                requiredSpan.textContent = '*';
                label.appendChild(requiredSpan);
            }

            let input;

            // 타입에 따라 다른 입력 요소 생성
            switch (varDef.type) {
                case 'DATE':
                    input = document.createElement('input');
                    input.type = 'date';
                    input.className = 'form-control';
                    break;
                case 'EMAIL':
                    input = document.createElement('input');
                    input.type = 'email';
                    input.className = 'form-control';
                    input.placeholder = varDef.defaultValue || 'example@domain.com';
                    break;
                case 'NUMBER':
                    input = document.createElement('input');
                    input.type = 'number';
                    input.className = 'form-control';
                    input.placeholder = varDef.defaultValue || '0';
                    break;
                case 'TEXTAREA':
                    input = document.createElement('textarea');
                    input.className = 'form-control';
                    input.rows = 3;
                    input.placeholder = varDef.defaultValue || '';
                    break;
                default: // TEXT
                    input = document.createElement('input');
                    input.type = 'text';
                    input.className = 'form-control';
                    input.placeholder = varDef.defaultValue || '';
            }

            input.id = 'var_' + varName;
            input.setAttribute('data-variable-name', varName);
            input.setAttribute('data-variable-type', varDef.type);
            input.required = varDef.required;
            if (varDef.defaultValue) {
                input.value = varDef.defaultValue;
            }

            // 입력 시 HTML 컨테이너의 변수 치환
            input.addEventListener('input', () => {
                this.updateTemplateVariableInHtml(varName, input.value);
            });

            // 클라이언트 측 검증
            input.addEventListener('blur', () => {
                this.validateVariableInput(input, varDef);
            });

            fieldWrapper.appendChild(label);
            fieldWrapper.appendChild(input);
            fieldsContainer.appendChild(fieldWrapper);

            // 기본값이 있으면 즉시 HTML에 반영
            if (varDef.defaultValue) {
                this.updateTemplateVariableInHtml(varName, varDef.defaultValue);
            }
        });
    }

    updateTemplateVariableInHtml(varName, value) {
        if (!this.templateHtmlContainer) return;

        // {{varName}} 패턴을 찾아서 value로 치환
        const pattern = new RegExp('\\{\\{' + varName + '\\}\\}', 'g');
        const walker = document.createTreeWalker(this.templateHtmlContainer, NodeFilter.SHOW_TEXT);
        const textNodes = [];

        while (walker.nextNode()) {
            if (walker.currentNode.nodeValue && walker.currentNode.nodeValue.includes('{{' + varName + '}}')) {
                textNodes.push(walker.currentNode);
            }
        }

        textNodes.forEach(node => {
            node.nodeValue = node.nodeValue.replace(pattern, value || '');
        });

        this.updatePresetContent();
    }

    validateVariableInput(input, varDef) {
        const value = input.value.trim();

        // 유효성 표시 제거
        input.classList.remove('is-invalid', 'is-valid');
        const existingFeedback = input.parentElement.querySelector('.invalid-feedback');
        if (existingFeedback) {
            existingFeedback.remove();
        }

        // 필수 필드 체크
        if (varDef.required && !value) {
            this.showValidationError(input, varDef.label + '은(는) 필수 입력 항목입니다.');
            return false;
        }

        // 값이 있으면 타입별 검증
        if (value) {
            let errorMessage = null;

            switch (varDef.type) {
                case 'NUMBER':
                    if (isNaN(value) || !/^-?\d+(\.\d+)?$/.test(value)) {
                        errorMessage = '숫자만 입력 가능합니다.';
                    }
                    break;
                case 'EMAIL':
                    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                    if (!emailRegex.test(value)) {
                        errorMessage = '올바른 이메일 형식이 아닙니다.';
                    }
                    break;
                case 'DATE':
                    const dateRegex = /^\d{4}-\d{2}-\d{2}$/;
                    if (!dateRegex.test(value)) {
                        errorMessage = '날짜 형식은 YYYY-MM-DD 이어야 합니다.';
                    }
                    break;
            }

            if (errorMessage) {
                this.showValidationError(input, errorMessage);
                return false;
            }
        }

        // 검증 통과
        input.classList.add('is-valid');
        return true;
    }

    showValidationError(input, message) {
        input.classList.add('is-invalid');

        const feedback = document.createElement('div');
        feedback.className = 'invalid-feedback';
        feedback.textContent = message;

        input.parentElement.appendChild(feedback);
    }

    validateAllTemplateVariables() {
        const variableInputs = document.querySelectorAll('[data-variable-name][data-variable-type]');
        let allValid = true;

        variableInputs.forEach(input => {
            const varName = input.getAttribute('data-variable-name');
            const varType = input.getAttribute('data-variable-type');
            const isRequired = input.hasAttribute('required');

            const varDef = {
                label: input.parentElement.querySelector('label')?.textContent.replace('*', '').trim() || varName,
                type: varType,
                required: isRequired
            };

            if (!this.validateVariableInput(input, varDef)) {
                allValid = false;
            }
        });

        return allValid;
    }

    // ========================================
    // SECTION 7: TEMPLATE SELECTION UI
    // ========================================

    highlightTemplateCard(selectedCard) {
        const cards = document.querySelectorAll('[data-template-card]');
        cards.forEach(card => {
            card.classList.remove('border-primary', 'shadow');
            const button = card.querySelector('[data-template-button]');
            if (button) {
                button.classList.remove('btn-primary', 'text-white');
                button.classList.add('btn-outline-primary');
            }
        });

        if (!selectedCard) {
            return;
        }

        selectedCard.classList.add('border-primary', 'shadow');
        const activeButton = selectedCard.querySelector('[data-template-button]');
        if (activeButton) {
            activeButton.classList.remove('btn-outline-primary');
            activeButton.classList.add('btn-primary', 'text-white');
        }
    }

    handleTemplateSelection(button) {
        if (!button) {
            return;
        }

        const templateId = button.getAttribute('data-template-id');
        const templateTitle = button.getAttribute('data-template-title') || '';
        const templateContent = button.getAttribute('data-template-content') || '';

        const templateIdInput = document.getElementById('templateId');
        if (templateIdInput) {
            templateIdInput.value = templateId || '';
        }

        const selectedCard = button.closest('[data-template-card]');
        this.highlightTemplateCard(selectedCard);

        if (templateTitle || templateContent) {
            // select-type.jsp에서 온 것이므로 URL로 이동
            window.location.href = `/contracts/new?templateId=${templateId}`;
        }
    }

    // ========================================
    // SECTION 8: CUSTOM CONTRACT METHODS
    // ========================================

    applyOwnerInfoToNormalForm() {
        if (!this.ownerInfo) {
            return;
        }
        // 일반 폼에 사업주 정보 자동 입력
        if (this.firstPartyNameInput && !this.firstPartyNameInput.value && this.ownerInfo.name) {
            this.firstPartyNameInput.value = this.ownerInfo.name;
        }
        if (this.firstPartyEmailInput && !this.firstPartyEmailInput.value && this.ownerInfo.email) {
            this.firstPartyEmailInput.value = this.ownerInfo.email;
        }
        if (this.firstPartyAddressInput && !this.firstPartyAddressInput.value && this.ownerInfo.companyName) {
            this.firstPartyAddressInput.value = this.ownerInfo.companyName;
        }
    }

    detectCustomVariables() {
        if (!this.contractContentTextarea) {
            return;
        }
        const content = this.contractContentTextarea.value || '';
        const detected = new Set();
        this.forEachPlaceholder(content, (name) => {
            if (!this.IGNORED_PLACEHOLDERS.has(name)) {
                detected.add(name);
            }
        });
        this.customVariables = Array.from(detected);
        this.renderCustomVariableInputs();
    }

    renderCustomVariableInputs() {
        if (!this.customVariableContainer || !this.customVariableFieldsWrapper) {
            return;
        }

        this.customVariableFieldsWrapper.innerHTML = '';

        if (!this.customVariables.length) {
            this.customVariableContainer.style.display = 'none';
            this.updateDirectPreview();
            return;
        }

        this.customVariableContainer.style.display = '';

        this.customVariables.forEach(variableName => {
            if (!(variableName in this.customVariableValues)) {
                this.customVariableValues[variableName] = this.suggestDefaultValue(variableName) || '';
            }

            const sanitizedId = 'variable-' + variableName.replace(/[^a-zA-Z0-9_-]+/g, '-');
            const wrapper = document.createElement('div');
            wrapper.className = 'col-md-6';

            const label = document.createElement('label');
            label.className = 'form-label';
            label.setAttribute('for', sanitizedId);
            label.textContent = variableName;

            const input = document.createElement('input');
            input.type = 'text';
            input.className = 'form-control';
            input.id = sanitizedId;
            input.setAttribute('data-variable-field', variableName);
            input.value = this.customVariableValues[variableName] || '';
            input.placeholder = variableName;

            input.addEventListener('input', (event) => {
                this.customVariableValues[variableName] = event.target.value;
                this.updateInlineVariableDisplays(variableName, event.target.value);
            });

            wrapper.appendChild(label);
            wrapper.appendChild(input);
            this.customVariableFieldsWrapper.appendChild(wrapper);
        });

        this.updateDirectPreview();
    }

    suggestDefaultValue(variableName) {
        // getDefaultValueForVariable과 동일한 로직 사용
        return this.getDefaultValueForVariable(variableName);
    }

    updateDirectPreview() {
        if (!this.customContentPreviewWrapper || !this.customContentPreview) {
            return;
        }

        if (!this.contractContentTextarea) {
            this.customContentPreviewWrapper.style.display = 'none';
            return;
        }

        const raw = this.contractContentTextarea.value || '';
        if (!raw.trim()) {
            this.customContentPreviewWrapper.style.display = 'none';
            this.customContentPreview.innerHTML = '';
            return;
        }

        this.customContentPreviewWrapper.style.display = '';
        const withVariables = this.applyCustomVariablesToContent(raw);
        const withSignature = this.applyOwnerSignature(withVariables);
        const sanitized = this.sanitizeHtml(withSignature);
        const template = document.createElement('template');
        template.innerHTML = sanitized;
        this.transformPlaceholdersForInlineEditing(template.content);
        this.customContentPreview.innerHTML = '';
        this.customContentPreview.appendChild(template.content);
    }

    transformPlaceholdersForInlineEditing(root) {
        if (!root) {
            return;
        }

        const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, null);
        const textNodes = [];
        while (walker.nextNode()) {
            const node = walker.currentNode;
            if (node.nodeValue && (node.nodeValue.includes('{') || node.nodeValue.includes('['))) {
                textNodes.push(node);
            }
        }

        textNodes.forEach(node => this.replaceTextNodeWithInputs(node));
    }

    replaceTextNodeWithInputs(textNode) {
        const text = textNode.nodeValue;
        this.PLACEHOLDER_REGEX.lastIndex = 0;
        if (!this.PLACEHOLDER_REGEX.test(text)) {
            return;
        }
        this.PLACEHOLDER_REGEX.lastIndex = 0;
        const parent = textNode.parentNode;
        if (!parent) {
            return;
        }

        const fragment = document.createDocumentFragment();
        let lastIndex = 0;
        let match;
        while ((match = this.PLACEHOLDER_REGEX.exec(text)) !== null) {
            const fullMatch = match[0];
            const varName = match[1] ? match[1].trim() : (match[2] ? match[2].trim() : '');
            if (match.index > lastIndex) {
                fragment.appendChild(document.createTextNode(text.slice(lastIndex, match.index)));
            }
            if (varName && !this.IGNORED_PLACEHOLDERS.has(varName)) {
                fragment.appendChild(this.createInlineVariableElement(varName));
            } else {
                fragment.appendChild(document.createTextNode(fullMatch));
            }
            lastIndex = this.PLACEHOLDER_REGEX.lastIndex;
        }

        if (lastIndex < text.length) {
            fragment.appendChild(document.createTextNode(text.slice(lastIndex)));
        }

        parent.replaceChild(fragment, textNode);
    }

    createInlineVariableElement(variableName) {
        const wrapper = document.createElement('span');
        wrapper.className = 'custom-variable-inline';

        const label = document.createElement('span');
        label.className = 'custom-variable-inline-label';
        label.textContent = `{${variableName}}`;

        const input = document.createElement('input');
        input.type = 'text';
        input.className = 'custom-variable-inline-input';
        input.setAttribute('data-variable-name', variableName);
        if (!(variableName in this.customVariableValues)) {
            this.customVariableValues[variableName] = this.suggestDefaultValue(variableName) || '';
        }
        input.value = this.customVariableValues[variableName] || '';

        input.addEventListener('input', (event) => {
            const newValue = event.target.value;
            this.customVariableValues[variableName] = newValue;
            this.updateVariablePanelField(variableName, newValue, event.target);
            this.updateInlineVariableDisplays(variableName, newValue, event.target);
        });

        wrapper.appendChild(label);
        wrapper.appendChild(input);
        return wrapper;
    }

    updateInlineVariableDisplays(variableName, value, sourceElement) {
        if (!variableName) {
            return;
        }
        const selector = `.custom-variable-inline-input[data-variable-name="${variableName}"]`;
        const inputs = this.customContentPreview?.querySelectorAll(selector) || [];
        inputs.forEach(input => {
            if (input === sourceElement) {
                return;
            }
            if (input.value !== value) {
                input.value = value || '';
            }
        });
    }

    updateVariablePanelField(variableName, value, sourceElement) {
        if (!this.customVariableFieldsWrapper) {
            return;
        }
        const selector = `[data-variable-field="${variableName}"]`;
        const field = this.customVariableFieldsWrapper.querySelector(selector);
        if (field && field !== sourceElement && field.value !== value) {
            field.value = value || '';
        }
    }

    insertVariable(variable) {
        if (!this.contractContentTextarea) return;

        const start = this.contractContentTextarea.selectionStart;
        const end = this.contractContentTextarea.selectionEnd;
        const text = this.contractContentTextarea.value;

        this.contractContentTextarea.value = text.substring(0, start) + variable + text.substring(end);
        this.contractContentTextarea.selectionStart = this.contractContentTextarea.selectionEnd = start + variable.length;
        this.contractContentTextarea.focus();
    }

    // ========================================
    // SECTION 9: SIGNATURE MANAGEMENT
    // ========================================

    async initializeOwnerSignature() {
        if (this.ownerSignatureDataUrl) {
            return;
        }

        if (!this.currentUserId) {
            return;
        }

        try {
            const response = await fetch('/api/first-party-signature/me', {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                    'X-User-Id': this.currentUserId
                }
            });

            if (response.status === 204) {
                localStorage.removeItem('signly_owner_signature');
                return;
            }

            if (!response.ok) {
                console.warn('[WARN] 사업주 서명 정보를 불러오지 못했습니다. 상태:', response.status);
                return;
            }

            const payload = await response.json();
            if (payload && typeof payload === 'object' && payload.dataUrl) {
                this.ownerSignatureDataUrl = payload.dataUrl;
                this.ownerSignatureUpdatedAt = payload.updatedAt || '';
                this.persistOwnerSignature(this.ownerSignatureDataUrl, this.ownerSignatureUpdatedAt);
                this.reapplyOwnerSignature();
            }
        } catch (error) {
            console.warn('[WARN] 사업주 서명 정보를 가져오는 중 오류 발생:', error);
        }
    }

    persistOwnerSignature(dataUrl, updatedAt) {
        if (!dataUrl) {
            localStorage.removeItem('signly_owner_signature');
            return;
        }

        try {
            const payload = {
                dataUrl: dataUrl,
                updatedAt: updatedAt || new Date().toISOString()
            };
            localStorage.setItem('signly_owner_signature', JSON.stringify(payload));
        } catch (error) {
            console.warn('[WARN] 사업주 서명 정보를 localStorage에 저장할 수 없습니다:', error);
        }
    }

    reapplyOwnerSignature() {
        if (this.contractContentTextarea && this.contractContentTextarea.value) {
            this.contractContentTextarea.value = this.applyOwnerSignature(this.contractContentTextarea.value);
            this.updateInlineVariableDisplays(null, null);
            this.updateDirectPreview();
        }
    }

    applyOwnerSignature(html) {
        if (!html || typeof html !== 'string') {
            return html;
        }
        if (!html.includes('[EMPLOYER_SIGNATURE_IMAGE]')) {
            return html;
        }

        const signatureMarkup = this.ownerSignatureDataUrl
            ? '<img src="' + this.ownerSignatureDataUrl + '" alt="사업주 서명" class="signature-stamp-image-element">'
            : '';

        return html.replace(/\[EMPLOYER_SIGNATURE_IMAGE\]/g, signatureMarkup);
    }

    applyCustomVariablesToContent(rawContent) {
        if (!rawContent || typeof rawContent !== 'string') {
            return rawContent;
        }

        return rawContent.replace(this.PLACEHOLDER_REGEX, (match, curly, bracket) => {
            const variableName = (curly ? curly.trim() : (bracket ? bracket.trim() : ''));
            if (!variableName || this.IGNORED_PLACEHOLDERS.has(variableName)) {
                return match;
            }
            return this.customVariableValues[variableName] || '';
        });
    }

    // ========================================
    // SECTION 10: FORM SUBMISSION & PREVIEW
    // ========================================

    previewContract() {
        const previewContent = document.getElementById('previewContent');
        if (!previewContent) return;

        let htmlToPreview = '';

        // 템플릿 모드인 경우
        if (this.templateLayout && this.templateLayout.style.display !== 'none') {
            // 템플릿 컨테이너에서 HTML 가져오기
            if (this.templateHtmlContainer) {
                const clone = this.templateHtmlContainer.cloneNode(true);

                // input 필드를 실제 값으로 교체
                const inputs = clone.querySelectorAll('input[data-variable-name]');
                inputs.forEach(input => {
                    const value = input.value || '';
                    const span = document.createElement('span');
                    span.textContent = value;
                    span.style.borderBottom = '1px solid #333';
                    span.style.display = 'inline-block';
                    span.style.minWidth = '80px';
                    input.parentNode.replaceChild(span, input);
                });

                htmlToPreview = clone.innerHTML;
            } else {
                htmlToPreview = '<p>템플릿 내용이 없습니다.</p>';
            }
        } else {
            // 일반 모드인 경우
            if (!this.contractContentTextarea) {
                htmlToPreview = '<p>계약서 내용이 없습니다.</p>';
            } else {
                const raw = this.contractContentTextarea.value || '';
                const withVariables = this.applyCustomVariablesToContent(raw);
                const withSignature = this.applyOwnerSignature(withVariables);
                htmlToPreview = this.sanitizeHtml(withSignature);
            }
        }

        previewContent.innerHTML = htmlToPreview;
        new bootstrap.Modal(document.getElementById('previewModal')).show();
    }

    handleFormSubmit(event, form) {
        // 템플릿 모드인 경우
        if (this.templateLayout && this.templateLayout.style.display !== 'none') {
            // 템플릿 변수 검증
            if (!this.validateAllTemplateVariables()) {
                event.preventDefault();
                event.stopPropagation();
                showAlertModal('입력한 변수 값을 확인해주세요.');
                return false;
            }

            // 템플릿 콘텐츠 업데이트
            this.updateTemplateContent();

            // normalLayout의 필드 비활성화
            if (this.normalLayout) {
                const normalInputs = this.normalLayout.querySelectorAll('input, textarea, select');
                normalInputs.forEach(field => {
                    field.disabled = true;
                    field.required = false;
                });
            }
        } else {
            // 일반 모드인 경우
            if (this.contractContentTextarea) {
                const resolvedContent = this.applyOwnerSignature(this.applyCustomVariablesToContent(this.contractContentTextarea.value || ''));
                this.contractContentTextarea.value = resolvedContent;
            }

            // 템플릿 레이아웃의 필드 비활성화
            if (this.templateLayout) {
                const templateInputs = this.templateLayout.querySelectorAll('input, textarea, select');
                templateInputs.forEach(field => {
                    field.disabled = true;
                    field.required = false;
                });
            }
        }

        const firstEmail = this.firstPartyEmailInput?.value ||
            this.templateFirstPartyEmail?.value;
        const secondEmail = this.secondPartyEmailInput?.value ||
            this.templateSecondPartyEmailInput?.value;

        if (firstEmail && secondEmail && firstEmail === secondEmail) {
            event.preventDefault();
            event.stopPropagation();
            showAlertModal('갑과 을의 이메일 주소는 달라야 합니다.');
            return false;
        }

        if (window.ensureCsrfToken) {
            window.ensureCsrfToken(form);
        }

        return true;
    }
}

// ========================================
// GLOBAL INITIALIZATION
// ========================================

// DOM이 로드되면 ContractForm 인스턴스 생성
document.addEventListener('DOMContentLoaded', () => {
    window.contractForm = new ContractForm();
});

// ========================================
// EXPOSE GLOBAL FUNCTIONS FOR JSP
// ========================================

// JSP의 인라인 이벤트 핸들러에서 호출하는 함수들
window.handleTemplateSelection = function (button) {
    if (window.contractForm) {
        window.contractForm.handleTemplateSelection(button);
    }
};

window.previewContract = function () {
    if (window.contractForm) {
        window.contractForm.previewContract();
    }
};
