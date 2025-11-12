/**
 * Contract Form JavaScript
 * Handles contract creation and editing functionality
 */

class ContractForm {
    constructor() {
        this.contractContentTextarea = document.getElementById('content');
        this.currentUserId = document.body?.dataset?.currentUserId || '';
        this.existingContractData = null;
        this.selectedTemplateData = null;
        this.hasSelectedPreset = false;
        this.ownerInfo = null;
        this.ownerSignatureInfo = null;
        this.ownerSignatureDataUrl = '';
        this.ownerSignatureUpdatedAt = '';
        
        this.PLACEHOLDER_REGEX = /\{([^{}]+)\}|\[([^\[\]]+)\]/g;
        this.IGNORED_PLACEHOLDERS = new Set(['EMPLOYER_SIGNATURE_IMAGE']);
        
        this.customVariableContainer = document.getElementById('customVariablesContainer');
        this.customVariableFieldsWrapper = document.getElementById('customVariableFields');
        this.customContentPreviewWrapper = document.getElementById('customContentPreviewWrapper');
        this.customContentPreview = document.getElementById('customContentPreview');
        this.customVariableValues = {};
        this.customVariables = [];
        
        this.init();
    }
    
    init() {
        this.parseJsonData();
        this.loadOwnerData();
        this.setupEventListeners();
        this.initializeLayout();
    }
    
    parseJsonData() {
        const existingContractDataElement = document.getElementById('existingContractData');
        if (existingContractDataElement) {
            try {
                this.existingContractData = JSON.parse(existingContractDataElement.textContent);
            } catch (error) {
                console.error('[ERROR] 기존 계약 데이터 파싱 실패:', error);
            }
        }
        
        const selectedTemplateDataElement = document.getElementById('selectedTemplateData');
        if (selectedTemplateDataElement) {
            try {
                this.selectedTemplateData = JSON.parse(selectedTemplateDataElement.textContent);
            } catch (error) {
                console.error('[ERROR] 선택된 템플릿 데이터 파싱 실패:', error);
            }
        }
        
        this.hasSelectedPreset = document.body.dataset.hasSelectedPreset === 'true';
    }
    
    loadOwnerData() {
        this.ownerInfo = StorageUtils.readOwnerInfo();
        this.ownerSignatureInfo = StorageUtils.readOwnerSignature();
        this.ownerSignatureDataUrl = this.ownerSignatureInfo.dataUrl || '';
        this.ownerSignatureUpdatedAt = this.ownerSignatureInfo.updatedAt || '';
    }
    
    setupEventListeners() {
        document.addEventListener('DOMContentLoaded', () => {
            this.initializeExpirationInputs();
            this.setupFormValidation();
            this.setupTemplateSelection();
            this.setupAutoLoad();
        });
    }
    
    initializeLayout() {
        document.addEventListener('DOMContentLoaded', () => {
            if (this.selectedTemplateData) {
                console.log('[INIT] Template edit detected, switching to preset layout');
                this.switchToPresetMode();
            } else if (this.existingContractData && this.existingContractData.content) {
                console.log('[INIT] Existing contract content detected, switching to preset layout');
                this.switchToPresetMode();
            } else if (this.hasSelectedPreset) {
                console.log('[INIT] Preset mode detected, will load preset');
            } else {
                console.log('[INIT] Normal mode detected, switching to normal layout');
                this.switchToNormalMode();
            }
        });
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
    
    updateExpirationMinAttributes() {
        const minValue = this.toLocalDateTimeValue(new Date());
        const normal = document.getElementById('expiresAt');
        const preset = document.getElementById('presetExpiresAt');
        if (normal) {
            normal.min = minValue;
        }
        if (preset) {
            preset.min = minValue;
        }
    }
    
    initializeExpirationInputs() {
        this.updateExpirationMinAttributes();
        
        const expiresAtInput = document.getElementById('expiresAt');
        const presetExpiresAtInput = document.getElementById('presetExpiresAt');
        
        const setMinDateTime = () => {
            const now = new Date();
            const year = now.getFullYear();
            const month = String(now.getMonth() + 1).padStart(2, '0');
            const day = String(now.getDate()).padStart(2, '0');
            const hours = String(now.getHours()).padStart(2, '0');
            const minutes = String(now.getMinutes()).padStart(2, '0');
            const minDateTime = `${year}-${month}-${day}T${hours}:${minutes}`;
            
            if (expiresAtInput) {
                expiresAtInput.min = minDateTime;
            }
            if (presetExpiresAtInput) {
                presetExpiresAtInput.min = minDateTime;
            }
        };
        
        setMinDateTime();
        setInterval(setMinDateTime, 60000);
        
        [expiresAtInput, presetExpiresAtInput].forEach(input => {
            if (input) {
                input.addEventListener('input', function() {
                    if (this.value && this.value < this.min) {
                        this.value = this.min;
                    }
                });
            }
        });
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
    
    extractCustomVariables(content) {
        const variables = new Set();
        this.forEachPlaceholder(content, (name) => {
            if (!this.IGNORED_PLACEHOLDERS.has(name)) {
                variables.add(name);
            }
        });
        return Array.from(variables).sort();
    }
    
    updateCustomVariableFields() {
        if (!this.customVariableFieldsWrapper) {
            return;
        }
        
        const content = this.contractContentTextarea ? this.contractContentTextarea.value : '';
        const variables = this.extractCustomVariables(content);
        
        if (variables.length === 0) {
            this.customVariableContainer.style.display = 'none';
            this.customVariableValues = {};
            this.customVariables = [];
            return;
        }
        
        this.customVariableContainer.style.display = 'block';
        this.customVariables = variables;
        
        let html = '';
        variables.forEach(variable => {
            const value = this.customVariableValues[variable] || '';
            html += `
                <div class="col-md-6 mb-3">
                    <label for="cv_${variable}" class="form-label">${variable}</label>
                    <input type="text" class="form-control" id="cv_${variable}" 
                           value="${value}" placeholder="${variable} 값을 입력하세요">
                </div>
            `;
        });
        
        this.customVariableFieldsWrapper.innerHTML = html;
        
        variables.forEach(variable => {
            const input = document.getElementById(`cv_${variable}`);
            if (input) {
                input.addEventListener('input', () => {
                    this.customVariableValues[variable] = input.value;
                    this.updateCustomContentPreview();
                });
            }
        });
        
        this.updateCustomContentPreview();
    }
    
    applyCustomVariablesToContent(content) {
        if (!content) {
            return content;
        }
        
        let result = content;
        this.forEachPlaceholder(result, (name, placeholder) => {
            if (!this.IGNORED_PLACEHOLDERS.has(name) && this.customVariableValues[name]) {
                result = result.replace(new RegExp(placeholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), 
                                       this.customVariableValues[name]);
            }
        });
        
        return result;
    }
    
    updateCustomContentPreview() {
        if (!this.customContentPreview || !this.contractContentTextarea) {
            return;
        }
        
        const content = this.contractContentTextarea.value;
        const processedContent = this.applyCustomVariablesToContent(content);
        
        if (processedContent.trim()) {
            this.customContentPreviewWrapper.style.display = 'block';
            this.customContentPreview.innerHTML = processedContent.replace(/\n/g, '<br>');
        } else {
            this.customContentPreviewWrapper.style.display = 'none';
        }
    }
    
    readOwnerSignature() {
        try {
            const raw = localStorage.getItem('signly_owner_signature');
            if (!raw) {
                return { dataUrl: '', updatedAt: '' };
            }
            const parsed = JSON.parse(raw);
            if (!parsed || typeof parsed !== 'object') {
                return { dataUrl: '', updatedAt: '' };
            }
            return {
                dataUrl: parsed.dataUrl || '',
                updatedAt: parsed.updatedAt || ''
            };
        } catch (error) {
            console.warn('[WARN] 서명 정보를 불러올 수 없습니다:', error);
            return { dataUrl: '', updatedAt: '' };
        }
    }
    
    applyOwnerSignature(content) {
        if (!content) {
            return content;
        }
        
        if (!this.ownerSignatureDataUrl) {
            return content;
        }
        
        return content.replace(/\[EMPLOYER_SIGNATURE_IMAGE\]/g, this.ownerSignatureDataUrl);
    }
    
    switchToNormalMode() {
        const normalLayout = document.getElementById('normalLayout');
        const presetLayout = document.getElementById('presetLayout');
        const normalTab = document.getElementById('normalTab');
        const presetTab = document.getElementById('presetTab');
        
        if (normalLayout) normalLayout.style.display = 'block';
        if (presetLayout) presetLayout.style.display = 'none';
        if (normalTab) normalTab.classList.add('active');
        if (presetTab) presetTab.classList.remove('active');
        
        this.updateCustomVariableFields();
    }
    
    switchToPresetMode() {
        const normalLayout = document.getElementById('normalLayout');
        const presetLayout = document.getElementById('presetLayout');
        const normalTab = document.getElementById('normalTab');
        const presetTab = document.getElementById('presetTab');
        
        if (normalLayout) normalLayout.style.display = 'none';
        if (presetLayout) presetLayout.style.display = 'block';
        if (normalTab) normalTab.classList.remove('active');
        if (presetTab) presetTab.classList.add('active');
    }
    
    handleTemplateSelection(button) {
        const templateId = button.dataset.templateButton;
        const templateTitle = button.dataset.templateTitle;
        const templateContent = button.dataset.templateContent;
        const templateVariables = JSON.parse(button.dataset.templateVariables || '{}');
        
        document.querySelectorAll('[data-template-card]').forEach(card => {
            card.classList.remove('border-primary', 'bg-light');
        });
        
        const card = button.closest('[data-template-card]');
        if (card) {
            card.classList.add('border-primary', 'bg-light');
        }
        
        document.querySelectorAll('[data-template-button]').forEach(btn => {
            btn.classList.remove('btn-primary');
            btn.classList.add('btn-outline-primary');
        });
        button.classList.remove('btn-outline-primary');
        button.classList.add('btn-primary');
        
        const templateIdInput = document.getElementById('templateId');
        if (templateIdInput) {
            templateIdInput.value = templateId;
        }
        
        this.loadTemplateAsPreset(templateTitle, templateContent, templateVariables);
    }
    
    loadTemplateAsPreset(title, content, variables = {}) {
        this.switchToPresetMode();
        
        const presetTitle = document.getElementById('presetTitle');
        if (presetTitle) {
            presetTitle.value = title;
        }
        
        const presetContent = document.getElementById('presetContent');
        if (presetContent) {
            presetContent.innerHTML = content;
        }
        
        this.customVariableValues = { ...variables };
        this.updatePresetVariableFields();
        this.updatePresetPreview();
    }
    
    updatePresetVariableFields() {
        const presetVariableFields = document.getElementById('presetVariableFields');
        const presetVariableContainer = document.getElementById('presetVariableContainer');
        
        if (!presetVariableFields || !presetVariableContainer) {
            return;
        }
        
        const presetContent = document.getElementById('presetContent');
        if (!presetContent) {
            return;
        }
        
        const content = presetContent.innerHTML || '';
        const variables = this.extractCustomVariables(content);
        
        if (variables.length === 0) {
            presetVariableContainer.style.display = 'none';
            return;
        }
        
        presetVariableContainer.style.display = 'block';
        
        let html = '';
        variables.forEach(variable => {
            const value = this.customVariableValues[variable] || '';
            html += `
                <div class="col-md-6 mb-3">
                    <label for="preset_cv_${variable}" class="form-label">${variable}</label>
                    <input type="text" class="form-control" id="preset_cv_${variable}" 
                           value="${value}" placeholder="${variable} 값을 입력하세요">
                </div>
            `;
        });
        
        presetVariableFields.innerHTML = html;
        
        variables.forEach(variable => {
            const input = document.getElementById(`preset_cv_${variable}`);
            if (input) {
                input.addEventListener('input', () => {
                    this.customVariableValues[variable] = input.value;
                    this.updatePresetPreview();
                });
            }
        });
        
        this.updatePresetPreview();
    }
    
    updatePresetPreview() {
        const presetContent = document.getElementById('presetContent');
        const presetPreview = document.getElementById('presetPreview');
        const presetPreviewWrapper = document.getElementById('presetPreviewWrapper');
        
        if (!presetContent || !presetPreview) {
            return;
        }
        
        const content = presetContent.innerHTML || '';
        const processedContent = this.applyCustomVariablesToContent(content);
        
        if (processedContent.trim()) {
            presetPreviewWrapper.style.display = 'block';
            presetPreview.innerHTML = processedContent;
        } else {
            presetPreviewWrapper.style.display = 'none';
        }
    }
    
    updatePresetContent() {
        const presetContent = document.getElementById('presetContent');
        const contractContent = document.getElementById('content');
        
        if (presetContent && contractContent) {
            const content = presetContent.innerHTML || '';
            const processedContent = this.applyCustomVariablesToContent(content);
            contractContent.value = processedContent;
        }
    }
    
    validateAllTemplateVariables() {
        const presetVariableFields = document.getElementById('presetVariableFields');
        if (!presetVariableFields) {
            return true;
        }
        
        const inputs = presetVariableFields.querySelectorAll('input[type="text"]');
        for (const input of inputs) {
            if (!input.value.trim()) {
                input.focus();
                return false;
            }
        }
        return true;
    }
    
    applyExistingContractData() {
        if (!this.existingContractData) {
            return;
        }
        
        const fields = [
            'title', 'content', 'firstPartyName', 'firstPartyEmail', 'firstPartyPhone',
            'secondPartyName', 'secondPartyEmail', 'secondPartyPhone', 'expiresAt'
        ];
        
        fields.forEach(field => {
            const element = document.getElementById(field);
            if (element && this.existingContractData[field]) {
                element.value = this.existingContractData[field];
            }
        });
        
        if (this.existingContractData.content) {
            this.updateCustomVariableFields();
        }
    }
    
    loadExistingContractAsPreset() {
        if (!this.existingContractData || !this.existingContractData.content) {
            return;
        }
        
        this.switchToPresetMode();
        
        const presetTitle = document.getElementById('presetTitle');
        if (presetTitle && this.existingContractData.title) {
            presetTitle.value = this.existingContractData.title;
        }
        
        const presetContent = document.getElementById('presetContent');
        if (presetContent) {
            presetContent.innerHTML = this.existingContractData.content;
        }
        
        this.updatePresetVariableFields();
        this.updatePresetPreview();
    }
    
    setupFormValidation() {
        document.addEventListener('DOMContentLoaded', () => {
            const form = document.querySelector('.contract-form');
            if (!form) return;
            
            form.addEventListener('submit', (event) => {
                const presetLayout = document.getElementById('presetLayout');
                if (presetLayout && presetLayout.style.display !== 'none') {
                    if (!this.validateAllTemplateVariables()) {
                        event.preventDefault();
                        event.stopPropagation();
                        if (window.showAlertModal) {
                            showAlertModal('입력한 변수 값을 확인해주세요.');
                        }
                        return false;
                    }
                    
                    this.updatePresetContent();
                    
                    const normalLayout = document.getElementById('normalLayout');
                    if (normalLayout) {
                        const normalInputs = normalLayout.querySelectorAll('input, textarea, select');
                        normalInputs.forEach(field => {
                            field.disabled = true;
                            field.required = false;
                        });
                    }
                } else {
                    if (this.contractContentTextarea) {
                        const resolvedContent = this.applyOwnerSignature(
                            this.applyCustomVariablesToContent(this.contractContentTextarea.value || '')
                        );
                        this.contractContentTextarea.value = resolvedContent;
                    }
                    
                    const presetInputs = presetLayout.querySelectorAll('input, textarea, select');
                    presetInputs.forEach(field => {
                        field.disabled = true;
                        field.required = false;
                    });
                }
                
                const firstEmail = document.getElementById('firstPartyEmail')?.value ||
                                  document.getElementById('presetFirstPartyEmail')?.value;
                const secondEmail = document.getElementById('secondPartyEmail')?.value ||
                                   document.getElementById('presetSecondPartyEmail')?.value;
                
                if (firstEmail && secondEmail && firstEmail === secondEmail) {
                    event.preventDefault();
                    event.stopPropagation();
                    if (window.showAlertModal) {
                        showAlertModal('갑과 을의 이메일 주소는 달라야 합니다.');
                    }
                    return false;
                }
                
                if (form.checkValidity() === false) {
                    event.preventDefault();
                    event.stopPropagation();
                }
                form.classList.add('was-validated');
            }, false);
        });
    }
    
    setupTemplateSelection() {
        document.addEventListener('DOMContentLoaded', () => {
            const templateIdInput = document.getElementById('templateId');
            const templateIdValue = templateIdInput ? templateIdInput.value : '';
            
            if (templateIdValue) {
                const selector = `[data-template-button="${templateIdValue}"]`;
                const button = document.querySelector(selector);
                if (button) {
                    this.handleTemplateSelection(button);
                    return;
                }
            }
            
            const preselectedCard = document.querySelector('[data-template-card][data-template-selected="true"]');
            if (preselectedCard) {
                const button = preselectedCard.querySelector('[data-template-button]');
                if (button) {
                    this.handleTemplateSelection(button);
                }
            }
        });
    }
    
    setupAutoLoad() {
        document.addEventListener('DOMContentLoaded', () => {
            if (this.selectedTemplateData && this.selectedTemplateData.renderedHtml) {
                if (this.selectedTemplateData.templateId) {
                    const templateIdInput = document.getElementById('templateId');
                    if (templateIdInput) {
                        templateIdInput.value = this.selectedTemplateData.templateId;
                    }
                }
                this.loadTemplateAsPreset(
                    this.selectedTemplateData.title || '',
                    this.selectedTemplateData.renderedHtml || '',
                    this.selectedTemplateData.variables || {}
                );
            } else if (this.existingContractData && this.existingContractData.content) {
                this.loadExistingContractAsPreset();
            } else if (!this.hasSelectedPreset) {
                this.applyExistingContractData();
            }
        });
    }
    
    loadPresetById(presetId) {
        // This would typically make an API call to load preset data
        // For now, we'll just log it - the actual implementation would depend on the backend API
        console.log('[INFO] Loading preset by ID:', presetId);
        // In a real implementation, this would fetch the preset data and load it
        // For now, this is a placeholder that matches the original functionality
    }
}

// Initialize the contract form when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.contractForm = new ContractForm();
});