/**
 * Template Form JavaScript
 * Handles template creation and editing functionality
 */

class TemplateForm {
    constructor() {
        this.clauseCounter = 0;
        this.activeElement = null;
        this.sections = [];

        this.FRONTEND_TYPES = new Set(['text', 'clause', 'dotted', 'footer', 'signature', 'html', 'title']);
        this.htmlEntityDecoder = document.createElement('textarea');

        this.init();
    }

    init() {
        this.loadInitialSections();
        this.setupEventListeners();
        this.loadPresets();
    }

    loadInitialSections() {
        const initialSectionsElement = document.getElementById('initialSections');
        if (initialSectionsElement) {
            try {
                const rawSections = initialSectionsElement.textContent;
                this.sections = this.parseSectionsPayload(rawSections);
                this.renderSections();
            } catch (error) {
                console.error('Failed to load initial sections:', error);
                this.sections = [];
            }
        }
    }

    setupEventListeners() {
        document.addEventListener('input', (e) => {
            if (e.target.classList.contains('html-editor')) {
                const preview = e.target.parentElement.querySelector('.html-preview');
                if (preview) {
                    preview.innerHTML = e.target.value;
                }
            }
        });

        document.addEventListener('keydown', (e) => {
            if ((e.ctrlKey || e.metaKey) && e.key === 's') {
                e.preventDefault();
                this.saveTemplate();
            }

            if ((e.ctrlKey || e.metaKey) && e.key === 'p') {
                e.preventDefault();
                this.previewTemplate();
            }

            if ((e.ctrlKey || e.metaKey) && e.key === 'b') {
                e.preventDefault();
                this.showVariableModal();
            }
        });
    }

    decodeHtmlEntities(value) {
        if (!value || typeof value !== 'string') {
            return value;
        }
        const temp = document.createElement('div');
        temp.innerHTML = value;
        return temp.textContent || temp.innerText || value;
    }

    parseSectionsPayload(raw) {
        if (!raw) {
            return [];
        }

        let parsed = raw;

        if (typeof raw === 'string') {
            const trimmed = raw.trim();
            if (!trimmed) {
                return [];
            }
            try {
                parsed = JSON.parse(trimmed);
            } catch (error) {
                console.warn('Failed to parse sections JSON', error);
                return [];
            }
        }

        if (Array.isArray(parsed)) {
            return parsed;
        }

        if (parsed && typeof parsed === 'object') {
            if (Array.isArray(parsed.sections)) {
                return parsed.sections;
            }
            if (Array.isArray(parsed.data)) {
                return parsed.data;
            }
            if (Array.isArray(parsed.content)) {
                return parsed.content;
            }
            if (typeof parsed.sectionsJson === 'string') {
                return this.parseSectionsPayload(parsed.sectionsJson);
            }
            return [parsed];
        }

        return [];
    }

    normalizeFrontendType(type, metadata) {
        metadata = metadata || {};
        if (metadata && typeof metadata === 'object' && metadata.kind && this.FRONTEND_TYPES.has(String(metadata.kind).toLowerCase())) {
            return String(metadata.kind).toLowerCase();
        }
        if (!type) {
            return 'text';
        }
        const raw = String(type);
        const lower = raw.toLowerCase();
        if (this.FRONTEND_TYPES.has(lower)) {
            return lower;
        }
        switch (raw.toUpperCase()) {
            case 'HEADER':
                return 'title';
            case 'DOTTED_BOX':
                return 'dotted';
            case 'FOOTER':
                return 'footer';
            case 'CUSTOM':
                if (metadata && metadata.signature) {
                    return 'signature';
                }
                if (metadata && metadata.rawHtml) {
                    return 'html';
                }
                return 'text';
            case 'PARAGRAPH':
            default:
                return 'text';
        }
    }

    ensureMetadataForType(type, metadata) {
        metadata = metadata || {};
        const base = metadata && typeof metadata === 'object' ? Object.assign({}, metadata) : {};

        switch (type) {
            case 'title':
                return Object.assign(base, {
                    kind: 'title',
                    level: base.level || 1,
                    centered: base.centered !== false
                });
            case 'clause':
                return Object.assign(base, {
                    kind: 'clause',
                    number: base.number !== false,
                    indent: base.indent !== false
                });
            case 'dotted':
                return Object.assign(base, {
                    kind: 'dotted',
                    height: base.height || 60
                });
            case 'footer':
                return Object.assign(base, {
                    kind: 'footer',
                    position: base.position || 'center'
                });
            case 'signature':
                return Object.assign(base, {
                    kind: 'signature',
                    required: base.required !== false
                });
            case 'html':
                return Object.assign(base, {
                    kind: 'html',
                    rawHtml: true
                });
            case 'text':
            default:
                return Object.assign(base, {
                    kind: 'text'
                });
        }
    }

    coerceMetadata(metadata) {
        if (!metadata) {
            return {};
        }

        if (typeof metadata === 'string') {
            try {
                metadata = JSON.parse(metadata);
            } catch (e) {
                return {};
            }
        }

        if (typeof metadata !== 'object' || Array.isArray(metadata)) {
            return {};
        }

        return metadata;
    }

    createSectionElement(type, content, metadata) {
        const section = document.createElement('div');
        section.className = 'section';
        section.dataset.type = type;

        const normalizedType = this.normalizeFrontendType(type, metadata);
        const finalMetadata = this.ensureMetadataForType(normalizedType, metadata);

        section.dataset.metadata = JSON.stringify(finalMetadata);

        let innerHTML = '';

        switch (normalizedType) {
            case 'title':
                const level = finalMetadata.level || 1;
                const centered = finalMetadata.centered !== false;
                innerHTML = `<h${level} class="section-title ${centered ? 'text-center' : ''}" contenteditable="true" data-placeholder="제목을 입력하세요">${content || ''}</h${level}>`;
                break;

            case 'clause':
                this.clauseCounter++;
                const number = finalMetadata.number !== false;
                const indent = finalMetadata.indent !== false;
                innerHTML = `<div class="clause-section ${indent ? 'indented' : ''}" contenteditable="true" data-placeholder="조항 내용을 입력하세요">${number ? `${this.clauseCounter}. ` : ''}${content || ''}</div>`;
                break;

            case 'dotted':
                const height = finalMetadata.height || 60;
                innerHTML = `<div class="dotted-box" style="height: ${height}px;" contenteditable="true" data-placeholder="서명란 내용을 입력하세요">${content || ''}</div>`;
                break;

            case 'footer':
                const position = finalMetadata.position || 'center';
                innerHTML = `<div class="footer-section text-${position}" contenteditable="true" data-placeholder="푸터 내용을 입력하세요">${content || ''}</div>`;
                break;

            case 'signature':
                innerHTML = `<div class="signature-section"><div class="signature-placeholder">[서명]</div></div>`;
                break;

            case 'html':
                innerHTML = `<div class="html-section"><textarea class="html-editor" placeholder="HTML 코드를 입력하세요">${content || ''}</textarea><div class="html-preview">${content || ''}</div></div>`;
                break;

            case 'text':
            default:
                innerHTML = `<div class="text-section" contenteditable="true" data-placeholder="내용을 입력하세요">${content || ''}</div>`;
                break;
        }

        section.innerHTML = innerHTML;

        section.addEventListener('click', (e) => {
            this.activeElement = section;
            this.updateToolbar();
        });

        section.addEventListener('input', () => {
            this.updateSectionsData();
        });

        return section;
    }

    createPlaceholder() {
        const placeholder = document.createElement('div');
        placeholder.className = 'section-placeholder';
        placeholder.innerHTML = '<div class="placeholder-text">+ 여기에 섹션을 추가하세요</div>';

        placeholder.addEventListener('click', () => {
            this.showSectionModal();
        });

        return placeholder;
    }

    renderSections() {
        const documentBody = document.getElementById('documentBody');
        if (!documentBody) {
            return;
        }

        documentBody.innerHTML = '';

        this.sections.forEach((sectionData, index) => {
            const metadata = this.coerceMetadata(sectionData.metadata);
            const section = this.createSectionElement(sectionData.type, sectionData.content, metadata);
            section.dataset.id = sectionData.sectionId || ('section-' + Date.now() + '-' + Math.random());
            documentBody.appendChild(section);
        });

        documentBody.appendChild(this.createPlaceholder());
        this.updateSectionsData();
    }

    updateSectionsData() {
        const documentBody = document.getElementById('documentBody');
        if (!documentBody) {
            return;
        }

        const sectionElements = documentBody.querySelectorAll('.section:not(.section-placeholder)');
        this.sections = [];

        sectionElements.forEach(element => {
            const type = element.dataset.type;
            const metadata = this.coerceMetadata(element.dataset.metadata);
            let content = '';

            const editableElement = element.querySelector('[contenteditable="true"], .html-editor');
            if (editableElement) {
                if (editableElement.classList.contains('html-editor')) {
                    content = editableElement.value;
                } else {
                    content = editableElement.innerHTML;
                }
            }

            this.sections.push({
                type: type,
                content: content,
                metadata: metadata,
                sectionId: element.dataset.id || ('section-' + Date.now() + '-' + Math.random())
            });
        });

        const sectionsJsonInput = document.getElementById('sectionsJson');
        if (sectionsJsonInput) {
            sectionsJsonInput.value = JSON.stringify(this.sections);
        }
    }

    insertVariable(variable) {
        if (this.activeElement) {
            const editableElement = this.activeElement.querySelector('[contenteditable="true"]');
            if (editableElement) {
                const selection = window.getSelection();
                const range = selection.getRangeAt(0);
                const textNode = document.createTextNode(variable);
                range.insertNode(textNode);
                range.setStartAfter(textNode);
                range.collapse(true);
                selection.removeAllRanges();
                selection.addRange(range);
                this.updateSectionsData();
            }
        }
    }

    showSectionModal() {
        const modal = document.getElementById('sectionModal');
        if (modal) {
            const bsModal = new bootstrap.Modal(modal);
            bsModal.show();
        }
    }

    showVariableModal() {
        const modal = document.getElementById('variableModal');
        if (modal) {
            const bsModal = new bootstrap.Modal(modal);
            bsModal.show();
        }
    }

    addSection(type) {
        const section = this.createSectionElement(type, '', {});
        const placeholder = document.querySelector('.section-placeholder');
        if (placeholder) {
            placeholder.parentNode.insertBefore(section, placeholder);
        }

        this.activeElement = section;
        this.updateSectionsData();
        this.updateToolbar();

        const modal = document.getElementById('sectionModal');
        if (modal) {
            bootstrap.Modal.getInstance(modal).hide();
        }

        const editableElement = section.querySelector('[contenteditable="true"]');
        if (editableElement) {
            editableElement.focus();
        }
    }

    deleteSection() {
        if (this.activeElement && !this.activeElement.classList.contains('section-placeholder')) {
            this.activeElement.remove();
            this.activeElement = null;
            this.updateSectionsData();
            this.updateToolbar();
        }
    }

    duplicateSection() {
        if (this.activeElement && !this.activeElement.classList.contains('section-placeholder')) {
            const clone = this.activeElement.cloneNode(true);
            clone.dataset.id = 'section-' + Date.now() + '-' + Math.random();

            clone.addEventListener('click', (e) => {
                this.activeElement = clone;
                this.updateToolbar();
            });

            clone.addEventListener('input', () => {
                this.updateSectionsData();
            });

            this.activeElement.parentNode.insertBefore(clone, this.activeElement.nextSibling);
            this.activeElement = clone;
            this.updateSectionsData();
            this.updateToolbar();
        }
    }

    updateToolbar() {
        const deleteBtn = document.getElementById('deleteSectionBtn');
        const duplicateBtn = document.getElementById('duplicateSectionBtn');

        if (this.activeElement && !this.activeElement.classList.contains('section-placeholder')) {
            if (deleteBtn) deleteBtn.disabled = false;
            if (duplicateBtn) duplicateBtn.disabled = false;
        } else {
            if (deleteBtn) deleteBtn.disabled = true;
            if (duplicateBtn) duplicateBtn.disabled = true;
        }
    }

    saveTemplate() {
        this.updateSectionsData();

        const form = document.getElementById('templateForm');
        if (form && form.checkValidity()) {
            form.submit();
        } else if (form) {
            form.reportValidity();
        }
    }

    previewTemplate() {
        this.updateSectionsData();

        const form = document.getElementById('templateForm');
        const formData = new FormData(form);

        fetch('/templates/preview', {
            method: 'POST',
            body: formData
        })
            .then(response => response.text())
            .then(html => {
                const previewWindow = window.open('', '_blank');
                previewWindow.document.write(html);
                previewWindow.document.close();
            })
            .catch(error => {
                console.error('Preview failed:', error);
                if (window.showAlertModal) {
                    showAlertModal('미리보기 생성 중 오류가 발생했습니다.');
                }
            });
    }

    loadPresets() {
        const presetLoading = document.getElementById('presetLoading');
        const presetGrid = document.getElementById('presetGrid');

        fetch('/templates/presets')
            .then(response => response.json())
            .then(presets => {
                if (presetLoading) presetLoading.style.display = 'none';
                if (presetGrid) {
                    presetGrid.style.display = 'grid';
                    presetGrid.innerHTML = '';

                    presets.forEach(preset => {
                        const card = this.createPresetCard(preset);
                        presetGrid.appendChild(card);
                    });
                }
            })
            .catch(error => {
                console.error('Failed to load presets:', error);
                if (presetLoading) {
                    presetLoading.innerHTML = '<div class="text-danger">프리셋을 불러올 수 없습니다.</div>';
                }
            });
    }

    createPresetCard(preset) {
        const card = document.createElement('div');
        card.className = 'preset-card';
        card.innerHTML = `
            <div class="preset-card-body">
                <h6 class="preset-card-title">${preset.name}</h6>
                <p class="preset-card-description">${preset.description || ''}</p>
                <button class="btn btn-sm btn-primary" onclick="window.templateForm.loadPreset('${preset.id}', '${preset.name}')">
                    불러오기
                </button>
            </div>
        `;
        return card;
    }

    loadPreset(presetId, presetName) {
        fetch(`/templates/presets/${presetId}`)
            .then(response => response.json())
            .then(data => {
                this.loadPresetSections(data.sections, presetName);
            })
            .catch(error => {
                console.error('Failed to load preset sections:', error);
                if (window.showAlertModal) {
                    showAlertModal('프리셋 템플릿을 불러오는 중 오류가 발생했습니다.');
                }
            });
    }

    loadPresetSections(presetSections, presetName) {
        this.clauseCounter = 0;

        const documentBody = document.getElementById('documentBody');
        if (documentBody) {
            documentBody.innerHTML = '';
        }

        const templateTitle = document.getElementById('templateTitle');
        if (templateTitle) {
            templateTitle.value = presetName;
        }

        presetSections.forEach((sectionData) => {
            const metadata = this.coerceMetadata(sectionData.metadata);

            let content = sectionData.content;
            if (sectionData.type !== 'html' && sectionData.type !== 'signature') {
                content = this.convertBracketsToVariables(content);
            }

            const section = this.createSectionElement(sectionData.type, content, metadata);
            section.dataset.id = sectionData.sectionId || ('section-' + Date.now() + '-' + Math.random());

            if (documentBody) {
                documentBody.appendChild(section);
            }
        });

        if (documentBody) {
            documentBody.appendChild(this.createPlaceholder());
        }

        this.updateSectionsData();

        if (window.showAlertModal) {
            showAlertModal('프리셋 템플릿이 성공적으로 로드되었습니다. 필요에 맞게 수정하여 사용하세요.');
        }

        this.collapsePresetSection();
    }

    convertBracketsToVariables(content) {
        if (!content) {
            return content;
        }

        return content
            .replace(/\[EMPLOYER\]/g, '{EMPLOYER}')
            .replace(/\[COMPANY_NAME\]/g, '{COMPANY_NAME}')
            .replace(/\[EMPLOYEE\]/g, '{EMPLOYEE}')
            .replace(/\[EMPLOYEE_ID\]/g, '{EMPLOYEE_ID}')
            .replace(/\[START_DATE\]/g, '{START_DATE}')
            .replace(/\[END_DATE\]/g, '{END_DATE}')
            .replace(/\[SALARY\]/g, '{SALARY}')
            .replace(/\[POSITION\]/g, '{POSITION}')
            .replace(/\[WORKPLACE\]/g, '{WORKPLACE}')
            .replace(/\[WORK_HOURS\]/g, '{WORK_HOURS}');
    }

    togglePresetSection() {
        const content = document.getElementById('presetContent');
        const icon = document.getElementById('presetToggleIcon');

        if (content && icon) {
            if (content.classList.contains('collapsed')) {
                content.classList.remove('collapsed');
                icon.className = 'bi bi-chevron-up';
            } else {
                content.classList.add('collapsed');
                icon.className = 'bi bi-chevron-down';
            }
        }
    }

    collapsePresetSection() {
        const content = document.getElementById('presetContent');
        const icon = document.getElementById('presetToggleIcon');

        if (content && icon) {
            content.classList.add('collapsed');
            icon.className = 'bi bi-chevron-down';
        }
    }
}

// Global functions for onclick handlers
window.insertVariable = function (variable) {
    if (window.templateForm) {
        window.templateForm.insertVariable(variable);
    }
};

window.togglePresetSection = function () {
    if (window.templateForm) {
        window.templateForm.togglePresetSection();
    }
};

window.addSection = function (type) {
    if (window.templateForm) {
        window.templateForm.addSection(type);
    }
};

window.deleteSection = function () {
    if (window.templateForm) {
        window.templateForm.deleteSection();
    }
};

window.duplicateSection = function () {
    if (window.templateForm) {
        window.templateForm.duplicateSection();
    }
};

window.saveTemplate = function () {
    if (window.templateForm) {
        window.templateForm.saveTemplate();
    }
};

window.previewTemplate = function () {
    if (window.templateForm) {
        window.templateForm.previewTemplate();
    }
};

window.showVariableModal = function () {
    if (window.templateForm) {
        window.templateForm.showVariableModal();
    }
};

window.showSectionModal = function () {
    if (window.templateForm) {
        window.templateForm.showSectionModal();
    }
};

// Initialize the template form when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.templateForm = new TemplateForm();
});