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
        this.firstPartyNameInput = document.getElementById('firstPartyName') || document.getElementById('templateFirstPartyName');
        this.firstPartyEmailInput = document.getElementById('firstPartyEmail') || document.getElementById('templateFirstPartyEmail');
        this.firstPartyAddressInput = document.getElementById('firstPartyAddress') || document.getElementById('templateFirstPartyAddress');
        this.secondPartyNameInput = document.getElementById('secondPartyName') || document.getElementById('templateSecondPartyName');
        this.secondPartyEmailInput = document.getElementById('secondPartyEmail') || document.getElementById('templateSecondPartyEmail');
        this.secondPartyAddressInput = document.getElementById('secondPartyAddress');

        // ===== STATE =====
        this.customVariableValues = {};
        this.customVariables = [];
        this.legacyVariableCounter = 0;
        this.variableDefinitions = [];

        // ===== CONSTANTS =====
        this.PLACEHOLDER_REGEX = /\{([^{}]+)\}|\[([^\[\]]+)\]/g;
        this.IGNORED_PLACEHOLDERS = new Set(['EMPLOYER_SIGNATURE_IMAGE', 'EMPLOYEE_SIGNATURE_IMAGE']);

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

    loadVariableDefinitions() {
        try {
            const definitionsScript = document.getElementById('variableDefinitions');
            if (definitionsScript) {
                const definitionsText = definitionsScript.textContent.trim();
                if (definitionsText) {
                    this.variableDefinitions = JSON.parse(definitionsText);
                    console.log('Loaded variable definitions:', this.variableDefinitions.length);
                }
            }
        } catch (error) {
            console.error('Failed to load variable definitions:', error);
        }
    }

    init() {
        // 먼저 JSON 데이터 파싱
        this.parseJsonData();
        this.loadVariableDefinitions();
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

        // 이벤트 리스너 설정 (반드시 호출해야 함)
        this.setupEventListeners();
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

        // 초기화 후 자동 입력 적용
        setTimeout(() => {
            this.applyOwnerInfoToNormalForm();
            this.validateInitialization();
        }, 100);

        // 실시간 유효성 검사 설정 (초기화 후 약간 지연)
        setTimeout(() => {
            this.setupRealtimeValidation();
            // 초기 상태에서 한번 유효성 검사 실행
            this.validateAllFields();
        }, 200);
    }

    validateInitialization() {
        // 초기화 상태 검증
        const criticalFields = [
            'templateContentHidden',
            'templateTitleHidden',
            'templateFirstPartyName',
            'templateFirstPartyEmail',
            'templateSecondPartyName',
            'templateSecondPartyEmail'
        ];

        const missingFields = criticalFields.filter(id => !document.getElementById(id));

        if (missingFields.length > 0) {
            console.error('[ContractForm] Critical fields missing:', missingFields);
        } else {
            console.log('[ContractForm] All critical fields found');

            // 초기 값 상태 로그
            criticalFields.forEach(id => {
                const element = document.getElementById(id);
                console.log(`[ContractForm] ${id}:`, element?.value?.length || 0, 'characters');
            });
        }
    }

    setupFormValidation() {
        const forms = document.getElementsByClassName('contract-form');
        Array.from(forms).forEach(form => {
            form.addEventListener('submit', (event) => {
                this.handleFormSubmit(event, form);
            });
        });
    }

    setupRealtimeValidation() {
        // 실시간 검증할 필드들 정의
        const validationFields = [
            { id: 'templateTitleHidden', name: '계약서 제목', type: 'text' },
            { id: 'templateContentHidden', name: '계약서 내용', type: 'content' },
            { id: 'templateFirstPartyName', name: '갑(사업주) 이름', type: 'text' },
            { id: 'templateFirstPartyEmail', name: '갑(사업주) 이메일', type: 'email' },
            { id: 'templateSecondPartyName', name: '을(근로자) 이름', type: 'text' },
            { id: 'templateSecondPartyEmail', name: '을(근로자) 이메일', type: 'email' }
        ];

        validationFields.forEach(field => {
            const element = document.getElementById(field.id);
            if (element) {
                // input 이벤트 (실시간)
                element.addEventListener('input', () => {
                    this.validateFieldRealtime(element, field);
                });

                // blur 이벤트 (포커스 아웃)
                element.addEventListener('blur', () => {
                    this.validateFieldRealtime(element, field);
                });

                // change 이벤트 (값 변경)
                element.addEventListener('change', () => {
                    this.validateFieldRealtime(element, field);
                });
            }
        });

        // 템플릿 모드에서의 추가 검증
        this.setupTemplateModeValidation();
    }

    validateFieldRealtime(element, fieldConfig) {
        const validationResult = this.validateSingleField(element, fieldConfig);

        if (!validationResult.isValid) {
            // 실시간 검증 시에는 자동 포커스 방지, 애니메이션만 적용
            showFieldError(element, validationResult.error, {
                autoFocus: false,
                animate: true
            });
        } else {
            clearFieldError(element, true);
        }

        return validationResult.isValid;
    }

    setupTemplateModeValidation() {
        // 템플릿 콘텐츠 변경 감지
        if (this.contractContentTextarea) {
            this.contractContentTextarea.addEventListener('input', () => {
                this.updateTemplateContentHidden();
                const hiddenField = document.getElementById('templateContentHidden');
                if (hiddenField) {
                    this.validateFieldRealtime(hiddenField, {
                        id: 'templateContentHidden',
                        name: '계약서 내용',
                        type: 'content'
                    });
                }
            });
        }

        // 템플릿 제목 변경 감지
        if (this.templateTitleInput) {
            this.templateTitleInput.addEventListener('input', () => {
                this.updateTemplateTitleHidden();
                const hiddenField = document.getElementById('templateTitleHidden');
                if (hiddenField) {
                    this.validateFieldRealtime(hiddenField, {
                        id: 'templateTitleHidden',
                        name: '계약서 제목',
                        type: 'text'
                    });
                }
            });
        }
    }

    updateTemplateContentHidden() {
        if (this.templateHiddenContent) {
            // 템플릿 모드에서는 현재 템플릿 내용을 hidden 필드에 업데이트
            if (this.templateLayout && this.templateLayout.style.display !== 'none') {
                this.updateTemplateContent();
            }
        }
    }

    updateTemplateTitleHidden() {
        if (this.templateTitleInput && this.templateHiddenTitle) {
            this.templateHiddenTitle.value = this.templateTitleInput.value.trim();
        }
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
            const raw = localStorage.getItem('deally_user_info');
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
            const raw = localStorage.getItem('deally_owner_signature');
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

            // 갑(사업주) 서명 이미지 표시 (저장 시에는 플레이스홀더로 변환)
            if (varName === 'EMPLOYER_SIGNATURE_IMAGE') {
                // 화면에는 실제 서명 이미지를 보여줌
                const signatureImg = this.createSignatureImage();
                element.innerHTML = '';
                element.appendChild(signatureImg);
                // 저장 시 플레이스홀더로 변환하기 위한 마커
                element.setAttribute('data-preserve-placeholder', 'EMPLOYER_SIGNATURE_IMAGE');
                return;
            }

            // 을(근로자) 서명 플레이스홀더 - 나중에 서명하므로 빈 공간으로 표시
            if (varName === 'EMPLOYEE_SIGNATURE_IMAGE') {
                element.innerHTML = '';
                // 빈 공간 (서명 대기)
                const emptySpan = document.createElement('span');
                emptySpan.style.cssText = 'display: inline-block; width: 90px; height: 40px;';
                emptySpan.innerHTML = '&nbsp;'; // 공간 유지
                element.appendChild(emptySpan);
                // 저장 시 플레이스홀더로 변환하기 위한 마커
                element.setAttribute('data-preserve-placeholder', 'EMPLOYEE_SIGNATURE_IMAGE');
                return;
            }

            // 변수를 input으로 교체 (DB 정의 사용)
            const varDef = this.variableDefinitions.find(v => v.name === varName);

            const input = document.createElement('input');
            input.className = 'contract-input-inline';
            input.setAttribute('data-variable-name', varName);

            // DB 정의를 사용하거나 기본값 사용
            if (varDef) {
                input.type = this.getHtmlInputType(varDef.type);
                if (varDef.validationRule && varDef.validationRule.maxLength) {
                    input.maxLength = varDef.validationRule.maxLength;
                    input.size = Math.min(varDef.validationRule.maxLength, 20);
                } else {
                    input.size = 10;
                }
                input.placeholder = varDef.placeholderExample || '입력하세요';
            } else {
                input.type = 'text';
                input.size = 10;
                input.placeholder = '입력하세요';
            }

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
        // Find variable definition in database
        const varDef = this.variableDefinitions.find(v => v.name === varName);

        const wrapper = document.createElement('span');
        wrapper.className = 'contract-variable-underline';
        wrapper.setAttribute('data-variable-name', varName);

        const input = document.createElement('input');
        input.className = 'contract-input-inline';
        input.setAttribute('data-variable-name', varName);

        // Use database definition if available, otherwise fallback to legacy logic
        if (varDef) {
            // Use HTML5 input type based on database definition
            input.type = this.getHtmlInputType(varDef.type);
            
            // Set size and maxLength based on database definition or fallback
            if (varDef.validationRule && varDef.validationRule.maxLength) {
                input.maxLength = varDef.validationRule.maxLength;
                input.size = Math.min(varDef.validationRule.maxLength, 20);
            } else {
                input.size = this.getDefaultInputSize(varName);
            }
            
            // Set placeholder from database
            input.placeholder = varDef.placeholderExample || this.getPlaceholderExample(varName);
            
            // Add HTML5 validation attributes
            if (varDef.validationRule && varDef.validationRule.pattern) {
                input.pattern = varDef.validationRule.pattern;
            }
            if (varDef.required) {
                input.required = true;
            }
        } else {
            // Fallback to legacy logic
            const upper = varName.toUpperCase();
            const normalized = upper.replace(/[-_\s]/g, '');
            
            input.type = 'text';
            input.size = this.getDefaultInputSize(varName);
            input.placeholder = this.getPlaceholderExample(varName);
        }

        // 자동 값 설정
        const value = this.getDefaultValueForVariable(varName);
        if (value) {
            input.value = value;
        }

        input.addEventListener('input', () => this.updateTemplateContent());

        wrapper.appendChild(input);
        return wrapper;
    }

    getHtmlInputType(variableType) {
        const typeMap = {
            'TEXT': 'text',
            'TIME': 'time',
            'DATE': 'date',
            'EMAIL': 'email',
            'NUMBER': 'number',
            'TEL': 'tel',
            'URL': 'url',
            'IMAGE': 'text' // For now, use text for image variables
        };
        return typeMap[variableType] || 'text';
    }

    getDefaultInputSize(varName) {
        // DB 데이터 우선 (대부분 createVariableInput에서 이미 처리됨)
        const varDef = this.variableDefinitions.find(v => v.name === varName);
        if (varDef && varDef.inputSize) {
            return varDef.inputSize;
        }

        // 폴백: 기본값 반환
        return 10;
    }

    getPlaceholderExample(varName) {
        // DB 데이터 우선
        const varDef = this.variableDefinitions.find(v => v.name === varName);
        if (varDef && varDef.placeholderExample) {
            return varDef.placeholderExample;
        }
        
        // 폴백: 기본 메시지
        return '입력하세요';
    }

    createSignatureImage() {
        // 래퍼 생성 (밑줄 포함)
        const wrapper = document.createElement('span');
        wrapper.className = 'contract-signature-wrapper';
        wrapper.style.cssText = 'display: inline-block; border-bottom: 1px solid #000; min-width: 80px; text-align: center; vertical-align: bottom; padding-bottom: 2px; margin: 0 2px; line-height: 1;';

        // localStorage에서 서명 이미지 가져오기
        const signatureRaw = localStorage.getItem('deally_owner_signature');

        if (!signatureRaw) {
            // 서명이 없으면 빈 span 반환
            const span = document.createElement('span');
            span.textContent = '(서명 없음)';
            span.style.cssText = 'color: #999; font-size: 11px; display: inline-block; padding: 5px 0;';
            wrapper.appendChild(span);
            return wrapper;
        }

        try {
            const signatureData = JSON.parse(signatureRaw);
            const imgSrc = signatureData.dataUrl || signatureData.imageData || signatureData.signatureData || signatureData;

            if (!imgSrc) {
                console.warn('[WARN] 서명 이미지 데이터가 없습니다:', signatureData);
                const span = document.createElement('span');
                span.textContent = '(서명 없음)';
                span.style.cssText = 'color: #999; font-size: 11px; display: inline-block; padding: 5px 0;';
                wrapper.appendChild(span);
                return wrapper;
            }

            const img = document.createElement('img');
            img.src = imgSrc;
            img.className = 'signature-stamp-image-element';
            img.style.cssText = 'display: inline-block; max-width: 90px; max-height: 40px; vertical-align: bottom;';
            img.alt = '사업주 서명';

            wrapper.appendChild(img);
            return wrapper;
        } catch (error) {
            console.error('[ERROR] 서명 이미지 파싱 실패:', error);
            const span = document.createElement('span');
            span.textContent = '(서명 오류)';
            span.style.cssText = 'color: #f00; font-size: 11px; display: inline-block; padding: 5px 0;';
            wrapper.appendChild(span);
            return wrapper;
        }
    }

    getDefaultValueForVariable(varName) {
        if (!varName || !this.ownerInfo) return '';

        // DB 기반 변수 정의에서 defaultSource 확인
        const varDef = this.variableDefinitions.find(v => v.name === varName);
        if (varDef && varDef.defaultSource) {
            // DB에 정의된 defaultSource에 따라 값 반환
            const sourceMap = {
                'OWNER_NAME': this.ownerInfo.name,
                'OWNER_EMAIL': this.ownerInfo.email,
                'OWNER_COMPANY': this.ownerInfo.companyName,
                'OWNER_PHONE': this.ownerInfo.businessPhone,
                'OWNER_ADDRESS': this.ownerInfo.businessAddress
            };
            return sourceMap[varDef.defaultSource] || '';
        }

        // 폴백: 기본 매핑 (최소한의 하드코딩)
        const nameUpper = varName.toUpperCase();
        if (nameUpper === 'EMPLOYER') return this.ownerInfo.name || '';
        if (nameUpper === 'COMPANY_NAME') return this.ownerInfo.companyName || '';
        if (nameUpper === 'EMPLOYER_EMAIL') return this.ownerInfo.email || '';
        if (nameUpper === 'EMPLOYER_PHONE') return this.ownerInfo.businessPhone || '';
        if (nameUpper === 'EMPLOYER_ADDRESS') return this.ownerInfo.businessAddress || '';
        if (nameUpper === 'CONTRACT_DATE') return new Date().toISOString().split('T')[0];

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

        // 갑(사업주) 서명 플레이스홀더 복원
        const placeholderElements = clone.querySelectorAll('[data-preserve-placeholder]');
        placeholderElements.forEach(element => {
            const placeholderName = element.getAttribute('data-preserve-placeholder');
            if (placeholderName) {
                // 플레이스홀더 텍스트로 교체 (대괄호 포함)
                const placeholderText = document.createTextNode('[' + placeholderName + ']');
                element.parentNode.replaceChild(placeholderText, element);
            }
        });

        // 을(근로자) 서명 이미지는 그대로 유지
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
                headers: { 'Accept': 'application/json' }
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

    validateAllFields() {
        let isValid = true;
        let firstErrorField = null;
        let errorMessage = '';
        const errorFields = [];

        // 검증할 필드 목록 (우선순위 순)
        const validationFields = [
            { id: 'templateTitleHidden', name: '계약서 제목', type: 'text', required: true },
            { id: 'templateContentHidden', name: '계약서 내용', type: 'content', required: true },
            { id: 'templateFirstPartyName', name: '갑(사업주) 이름', type: 'text', required: true },
            { id: 'templateFirstPartyEmail', name: '갑(사업주) 이메일', type: 'email', required: true },
            { id: 'templateSecondPartyName', name: '을(근로자) 이름', type: 'text', required: true },
            { id: 'templateSecondPartyEmail', name: '을(근로자) 이메일', type: 'email', required: true }
        ];

        // 필드 존재 여부 먼저 확인
        const missingFields = validationFields.filter(config =>
            !document.getElementById(config.id)
        );

        if (missingFields.length > 0) {
            console.error('[ContractForm] Missing validation fields:', missingFields.map(f => f.id));
            return {
                isValid: false,
                firstErrorField: null,
                errorMessage: '필수 폼 필드가 누락되었습니다. 페이지를 새로고침해주세요.',
                errorFields: missingFields
            };
        }

        // 각 필드 검증
        for (const fieldConfig of validationFields) {
            const element = document.getElementById(fieldConfig.id);
            if (!element) {
                console.error(`[ContractForm] Field not found: ${fieldConfig.id}`);
                continue;
            }

            const validationResult = this.validateSingleField(element, fieldConfig);

            if (!validationResult.isValid) {
                isValid = false;
                errorFields.push({
                    element,
                    name: fieldConfig.name,
                    error: validationResult.error
                });

                if (!firstErrorField) {
                    firstErrorField = element;
                    errorMessage = validationResult.error;
                }
            } else {
                // 유효한 필드는 에러 표시 제거
                clearFieldError(element);
            }
        }

        // 이메일 중복 검사
        const firstEmail = document.getElementById('templateFirstPartyEmail')?.value?.trim();
        const secondEmail = document.getElementById('templateSecondPartyEmail')?.value?.trim();

        if (firstEmail && secondEmail && firstEmail === secondEmail) {
            isValid = false;
            const secondEmailField = document.getElementById('templateSecondPartyEmail');
            if (!firstErrorField) {
                firstErrorField = secondEmailField;
                errorMessage = '갑과 을의 이메일 주소는 달라야 합니다.';
            }
            showFieldError(secondEmailField, '갑과 을의 이메일 주소는 달라야 합니다.');
        }

        return {
            isValid,
            firstErrorField,
            errorMessage,
            errorFields
        };
    }

    validateSingleField(element, fieldConfig) {
        const value = element.value?.trim();

        // 필수값 검사
        if (fieldConfig.required && !value) {
            return {
                isValid: false,
                error: `${fieldConfig.name}은(는) 필수 항목입니다.`
            };
        }

        // 타입별 검사
        if (value) {
            switch (fieldConfig.type) {
                case 'email':
                    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                    if (!emailRegex.test(value)) {
                        return {
                            isValid: false,
                            error: `${fieldConfig.name}은(는) 올바른 이메일 형식이어야 합니다.`
                        };
                    }
                    break;

                case 'text':
                    if (value.length > 100) {
                        return {
                            isValid: false,
                            error: `${fieldConfig.name}은(는) 100자를 초과할 수 없습니다.`
                        };
                    }
                    break;

                case 'content':
                    if (value.length > 100000) {
                        return {
                            isValid: false,
                            error: `${fieldConfig.name}은(는) 100,000자를 초과할 수 없습니다.`
                        };
                    }
                    break;
            }
        }

        return {
            isValid: true,
            error: null
        };
    }

    validateHiddenFields() {
        let isValid = true;
        let firstErrorField = null;
        let firstVisibleElement = null;

        // 필수 hidden 필드들 검증
        const requiredHiddenFields = [
            { id: 'templateFirstPartyName', name: '갑(사업주) 이름' },
            { id: 'templateFirstPartyEmail', name: '갑(사업주) 이메일' },
            { id: 'templateSecondPartyName', name: '을(근로자) 이름' },
            { id: 'templateSecondPartyEmail', name: '을(근로자) 이메일' },
            { id: 'templateTitleHidden', name: '계약서 제목' },
            { id: 'templateContentHidden', name: '계약서 내용' }
        ];

        requiredHiddenFields.forEach(field => {
            const element = document.getElementById(field.id);
            if (element) {
                const value = element.value?.trim();
                if (!value) {
                    isValid = false;
                    if (!firstErrorField) {
                        firstErrorField = { element, name: field.name, id: field.id };
                    }

                    // 에러 표시
                    showFieldError(element, `${field.name}은(는) 필수입니다.`);

                    // 숨겨진 필드를 실제 보이는 필드로 매핑하여 포커스
                    if (!firstVisibleElement) {
                        let visibleElement = null;

                        // 필드별 매핑 로직
                        switch (field.id) {
                            case 'templateSecondPartyEmail':
                                // 근로자 이메일은 실제 보이는 input 필드
                                visibleElement = document.getElementById('templateSecondPartyEmail');
                                break;

                            case 'templateFirstPartyName':
                            case 'templateFirstPartyEmail':
                            case 'templateSecondPartyName':
                                // 변수 입력 필드들 - HTML 컨테이너 내에서 해당 변수 찾기
                                const varName = field.id.replace('template', '').replace('Hidden', '');
                                visibleElement = document.querySelector(`[data-var-name="${varName}"]`);
                                if (!visibleElement) {
                                    // 대체: 템플릿 HTML 컨테이너 내에서 name 속성으로 찾기
                                    visibleElement = this.templateHtmlContainer?.querySelector(`[name="${varName}"]`);
                                }
                                break;

                            case 'templateTitleHidden':
                                // 제목은 현재 수정 불가능할 수 있음 - 템플릿 레이아웃 제목 영역
                                visibleElement = document.getElementById('templateLayoutTitle');
                                break;

                            case 'templateContentHidden':
                                // 커스텀 컨텐츠 에디터
                                visibleElement = document.getElementById('customContentPreview');
                                if (!visibleElement || visibleElement.offsetParent === null) {
                                    // 템플릿 HTML 컨테이너가 보이는 경우
                                    visibleElement = this.templateHtmlContainer;
                                }
                                break;
                        }

                        // 포커스 가능한 visible 요소 저장
                        if (visibleElement && visibleElement.offsetParent !== null) {
                            firstVisibleElement = visibleElement;
                        }
                    }
                } else {
                    // 에러 제거
                    clearFieldError(element);
                }
            }
        });

        // 첫 번째 에러 필드로 포커스 및 스크롤
        if (!isValid && firstErrorField) {
            setTimeout(() => {
                // 보이는 요소로 포커스 및 스크롤
                if (firstVisibleElement) {
                    // 스크롤
                    firstVisibleElement.scrollIntoView({ behavior: 'smooth', block: 'center' });

                    // 포커스 (입력 가능한 요소인 경우)
                    if (typeof firstVisibleElement.focus === 'function') {
                        setTimeout(() => {
                            firstVisibleElement.focus();
                        }, 300);
                    }
                }

                // 에러 메시지 표시
                showAlertModal(`${firstErrorField.name}은(는) 필수 항목입니다. 입력해주세요.`);
            }, 100);
        }

        return isValid;
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
            // hidden 필드 값도 동기화
            const hiddenFirstPartyName = document.getElementById('templateFirstPartyName');
            if (hiddenFirstPartyName) {
                hiddenFirstPartyName.value = this.ownerInfo.name;
            }
        }
        if (this.firstPartyEmailInput && !this.firstPartyEmailInput.value && this.ownerInfo.email) {
            this.firstPartyEmailInput.value = this.ownerInfo.email;
            // hidden 필드 값도 동기화
            const hiddenFirstPartyEmail = document.getElementById('templateFirstPartyEmail');
            if (hiddenFirstPartyEmail) {
                hiddenFirstPartyEmail.value = this.ownerInfo.email;
            }
        }
        if (this.firstPartyAddressInput && !this.firstPartyAddressInput.value && this.ownerInfo.companyName) {
            this.firstPartyAddressInput.value = this.ownerInfo.companyName;
            // hidden 필드 값도 동기화
            const hiddenFirstPartyAddress = document.getElementById('templateFirstPartyAddress');
            if (hiddenFirstPartyAddress) {
                hiddenFirstPartyAddress.value = this.ownerInfo.companyName;
            }
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
                localStorage.removeItem('deally_owner_signature');
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
            localStorage.removeItem('deally_owner_signature');
            return;
        }

        try {
            const payload = {
                dataUrl: dataUrl,
                updatedAt: updatedAt || new Date().toISOString()
            };
            localStorage.setItem('deally_owner_signature', JSON.stringify(payload));
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

                // 서명 플레이스홀더 처리 (data-preserve-placeholder 속성을 가진 요소들)
                const signaturePlaceholders = clone.querySelectorAll('[data-preserve-placeholder]');
                signaturePlaceholders.forEach(element => {
                    const placeholderType = element.getAttribute('data-preserve-placeholder');
                    if (placeholderType === 'EMPLOYER_SIGNATURE_IMAGE') {
                        // 갑(사업주) 서명은 이미 표시되어 있음 (createSignatureImage로 생성됨)
                        // 추가 처리 불필요
                    } else if (placeholderType === 'EMPLOYEE_SIGNATURE_IMAGE') {
                        // 을(근로자) 서명은 빈 공간으로 표시 (아직 서명 전)
                        // 추가 처리 불필요
                    }
                });

                // 텍스트 노드에서 플레이스홀더 문자열 제거 ([EMPLOYER_SIGNATURE_IMAGE], [EMPLOYEE_SIGNATURE_IMAGE])
                const walker = document.createTreeWalker(clone, NodeFilter.SHOW_TEXT);
                const textNodes = [];
                while (walker.nextNode()) {
                    if (walker.currentNode.nodeValue &&
                        (walker.currentNode.nodeValue.includes('[EMPLOYER_SIGNATURE_IMAGE]') ||
                            walker.currentNode.nodeValue.includes('[EMPLOYEE_SIGNATURE_IMAGE]'))) {
                        textNodes.push(walker.currentNode);
                    }
                }
                textNodes.forEach(node => {
                    // EMPLOYER_SIGNATURE_IMAGE 플레이스홀더를 실제 서명 이미지로 교체
                    if (node.nodeValue.includes('[EMPLOYER_SIGNATURE_IMAGE]')) {
                        const signatureImg = this.createSignatureImage();
                        const fragment = document.createDocumentFragment();
                        const parts = node.nodeValue.split('[EMPLOYER_SIGNATURE_IMAGE]');

                        fragment.appendChild(document.createTextNode(parts[0]));
                        if (signatureImg) {
                            fragment.appendChild(signatureImg);
                        }
                        if (parts[1]) {
                            fragment.appendChild(document.createTextNode(parts[1]));
                        }
                        node.parentNode.replaceChild(fragment, node);
                    }
                    // EMPLOYEE_SIGNATURE_IMAGE 플레이스홀더는 빈 공간으로 교체
                    else if (node.nodeValue.includes('[EMPLOYEE_SIGNATURE_IMAGE]')) {
                        node.nodeValue = node.nodeValue.replace('[EMPLOYEE_SIGNATURE_IMAGE]', '');
                    }
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
        console.log('[ContractForm] Form submit triggered');

        // ✅ 핵심 수정: 가장 먼저 폼 제출 중단
        event.preventDefault();
        event.stopPropagation();
        event.stopImmediatePropagation();

        // 템플릿 모드인 경우 hidden 필드 업데이트 (검증 전에 수행)
        if (this.templateLayout && this.templateLayout.style.display !== 'none') {
            console.log('[ContractForm] Updating template content before validation');
            this.updateTemplateContent();
        }

        // 전체 필드 유효성 검사 (프론트엔드 선 검증)
        const validationResult = this.validateAllFields();
        console.log('[ContractForm] Validation result:', validationResult);

        if (!validationResult.isValid) {
            console.log('[ContractForm] Validation failed:', validationResult.errorMessage);

            // 첫 번째 에러 필드로 포커스
            if (validationResult.firstErrorField) {
                setTimeout(() => {
                    validationResult.firstErrorField.focus();
                    validationResult.firstErrorField.scrollIntoView({
                        behavior: 'smooth',
                        block: 'center'
                    });
                }, 100);
            }

            showAlertModal(validationResult.errorMessage || '입력값을 확인해주세요.');
            return false;
        }

        // 템플릿 모드인 경우 추가 검증
        if (this.templateLayout && this.templateLayout.style.display !== 'none') {
            // 콘텐츠가 비어있는지 최종 확인
            if (this.templateHiddenContent && !this.templateHiddenContent.value.trim()) {
                console.log('[ContractForm] Template content is empty after update');
                showAlertModal('계약서 내용이 비어있습니다. 템플릿 변수를 입력해주세요.');
                // 첫 번째 빈 변수 입력 필드로 포커스
                const firstEmptyInput = this.templateHtmlContainer?.querySelector('input[data-variable-name]:not([value])');
                if (firstEmptyInput) {
                    firstEmptyInput.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    setTimeout(() => firstEmptyInput.focus(), 300);
                }
                return false;
            }

            // 템플릿 HTML 내의 모든 입력 필드가 채워져 있는지 확인
            const emptyInputs = this.templateHtmlContainer?.querySelectorAll('input[data-variable-name]');
            if (emptyInputs) {
                for (const input of emptyInputs) {
                    if (!input.value || input.value.trim() === '') {
                        console.log('[ContractForm] Empty variable input found:', input.getAttribute('data-variable-name'));
                        showAlertModal('모든 필드를 입력해주세요.');
                        input.scrollIntoView({ behavior: 'smooth', block: 'center' });
                        setTimeout(() => input.focus(), 300);
                        return false;
                    }
                }
            }

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

        // 템플릿 모드에서는 visible input 값을 hidden field로 동기화하고 중복 제출 방지
        if (this.templateLayout && this.templateLayout.style.display !== 'none') {
            // secondPartyEmail: visible input → hidden field
            const visibleSecondEmail = document.querySelector('#templateLayout input[type="email"][name="secondPartyEmail"]');
            const hiddenSecondEmail = document.querySelector('input[type="hidden"][name="secondPartyEmail"]');

            console.log('[ContractForm] Email field sync debug:');
            console.log('  - visibleSecondEmail found:', !!visibleSecondEmail, 'value:', visibleSecondEmail?.value);
            console.log('  - hiddenSecondEmail found:', !!hiddenSecondEmail, 'value:', hiddenSecondEmail?.value);

            if (visibleSecondEmail && hiddenSecondEmail) {
                // 값 복사
                hiddenSecondEmail.value = visibleSecondEmail.value;
                // 중복 제출 방지: visible 필드의 name 제거 (hidden 필드만 제출되도록)
                visibleSecondEmail.removeAttribute('name');
                console.log('[ContractForm] Synced secondPartyEmail and removed name from visible field:', visibleSecondEmail.value);
            } else if (!visibleSecondEmail) {
                console.warn('[ContractForm] Visible secondPartyEmail field not found!');
            } else if (!hiddenSecondEmail) {
                console.warn('[ContractForm] Hidden secondPartyEmail field not found!');
            }

            // firstPartyEmail 값도 확인
            const firstEmail = document.getElementById('templateFirstPartyEmail');
            console.log('  - firstPartyEmail found:', !!firstEmail, 'value:', firstEmail?.value);
        }

        // CSRF 토큰 확인
        if (window.ensureCsrfToken) {
            window.ensureCsrfToken(form);
        }

        // ✅ 모든 검증 통과 시 폼 제출
        console.log('[ContractForm] All validations passed, submitting form');
        console.log('[ContractForm] Form data:', {
            title: document.getElementById('templateTitleHidden')?.value,
            contentLength: document.getElementById('templateContentHidden')?.value?.length,
            firstPartyName: document.getElementById('templateFirstPartyName')?.value,
            firstPartyEmail: document.getElementById('templateFirstPartyEmail')?.value,
            secondPartyName: document.getElementById('templateSecondPartyName')?.value,
            secondPartyEmail: document.getElementById('templateSecondPartyEmail')?.value
        });
        form.submit();
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
