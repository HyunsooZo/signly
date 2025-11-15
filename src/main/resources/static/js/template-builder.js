/**
 * Template Builder - 템플릿 편집기 JavaScript 모듈
 * 템플릿 생성, 편집, 미리보기 기능 제공
 */
const TemplateBuilder = {
    // 상태 관리
    state: {
        clauseCounter: 0,
        activeElement: null,
        sections: []
    },

    // 상수 정의
    constants: {
        FRONTEND_TYPES: new Set(['text', 'clause', 'dotted', 'footer', 'signature', 'html', 'title']),
        VARIABLE_DISPLAY_NAMES: {
            'EMPLOYER': '사업주',
            'EMPLOYEE': '근로자',
            'WORKPLACE': '근무장소',
            'CONTRACT_START_DATE': '시작일',
            'CONTRACT_END_DATE': '종료일',
            'JOB_DESCRIPTION': '업무내용',
            'WORK_START_TIME': '근무시작',
            'WORK_END_TIME': '근무종료',
            'BREAK_START_TIME': '휴게시작',
            'BREAK_END_TIME': '휴게종료',
            'WORK_DAYS': '근무일수',
            'HOLIDAYS': '휴일',
            'MONTHLY_SALARY': '월급',
            'BONUS': '상여금',
            'OTHER_ALLOWANCES': '기타수당',
            'PAYMENT_DAY': '지급일',
            'PAYMENT_METHOD': '지급방법',
            'CONTRACT_DATE': '계약일',
            'EMPLOYEE_ADDRESS': '근로자주소',
            'EMPLOYEE_PHONE': '근로자연락처',
            'COMPANY_NAME': '회사명',
            'EMPLOYER_ADDRESS': '사업주주소',
            'EMPLOYER_PHONE': '사업주전화',
            'EMPLOYEE_SIGNATURE_IMAGE': '근로자서명',
            'EMPLOYER_SIGNATURE_IMAGE': '사업주서명',
            'EMPLOYEE_ID': '주민등록번호',
            'BUSINESS_NUMBER': '사업자번호',
            'HOURLY_WAGE': '시급',
            'EMPLOYER_SIGNATURE': '사업주서명',
            'EMPLOYEE_SIGNATURE': '근로자서명',
            'SIGNATURE_DATE': '서명일'
        }
    },

    // 유틸리티 함수
    utils: {
        htmlEntityDecoder: document.createElement('textarea'),

        decodeHtmlEntities(value) {
            if (!value || typeof value !== 'string') {
                return value;
            }
            const temp = document.createElement('div');
            temp.innerHTML = value;
            return temp.textContent || temp.innerText || value;
        },

        ensureString(value) {
            if (value === null || value === undefined) {
                return '';
            }
            return typeof value === 'string' ? value : String(value);
        },

        encodeMetadata(metadata) {
            metadata = metadata || {};
            try {
                return encodeURIComponent(JSON.stringify(metadata));
            } catch (error) {
                console.warn('Failed to encode metadata', error);
                return encodeURIComponent('{}');
            }
        },

        decodeMetadata(encoded) {
            if (!encoded) {
                return {};
            }
            try {
                return JSON.parse(decodeURIComponent(encoded));
            } catch (error) {
                console.warn('Failed to decode metadata', error);
                return {};
            }
        },

        getDisplayName(varName) {
            return TemplateBuilder.constants.VARIABLE_DISPLAY_NAMES[varName] || varName;
        }
    },

    // 섹션 데이터 처리
    dataProcessor: {
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
        },

        normalizeFrontendType(type, metadata) {
            metadata = metadata || {};
            if (metadata && typeof metadata === 'object' && metadata.kind && TemplateBuilder.constants.FRONTEND_TYPES.has(String(metadata.kind).toLowerCase())) {
                return String(metadata.kind).toLowerCase();
            }
            if (!type) {
                return 'text';
            }
            const raw = String(type);
            const lower = raw.toLowerCase();
            if (TemplateBuilder.constants.FRONTEND_TYPES.has(lower)) {
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
        },

        ensureMetadataForType(type, metadata) {
            metadata = metadata || {};
            const base = metadata && typeof metadata === 'object' ? Object.assign({}, metadata) : {};
            if (type === 'html' || type === 'signature') {
                base.rawHtml = true;
            }
            if (type === 'signature' && base.signature === undefined) {
                base.signature = true;
            }
            if (type === 'title' && base.header === undefined) {
                base.header = true;
            }
            if (!base.kind) {
                base.kind = type;
            }
            return base;
        },

        coerceMetadata(metadata) {
            if (!metadata) {
                return {};
            }
            if (typeof metadata === 'string') {
                const decoded = TemplateBuilder.utils.decodeHtmlEntities(metadata);
                try {
                    return JSON.parse(decoded);
                } catch (error) {
                    console.warn('Failed to parse metadata string', error);
                    return {};
                }
            }
            if (typeof metadata === 'object') {
                return Object.assign({}, metadata);
            }
            return {};
        },

        mapFrontendTypeToServer(type, metadata) {
            metadata = metadata || {};
            switch (type) {
                case 'title':
                    return 'HEADER';
                case 'clause':
                case 'text':
                    return 'PARAGRAPH';
                case 'dotted':
                    return 'DOTTED_BOX';
                case 'footer':
                    return 'FOOTER';
                case 'html':
                    return metadata && metadata.rawHtml ? 'CUSTOM' : 'PARAGRAPH';
                case 'signature':
                    return 'CUSTOM';
                default:
                    return 'PARAGRAPH';
            }
        },

        normalizePreviewContent(type, rawContent) {
            console.debug('[normalizePreviewContent] input:', type, JSON.stringify(rawContent));
            if (rawContent === null || rawContent === undefined) {
                console.debug('[normalizePreviewContent] null/undefined -> empty string');
                return '';
            }

            let value = TemplateBuilder.utils.ensureString(rawContent);
            console.debug('[normalizePreviewContent] after ensureString:', JSON.stringify(value));

            if (type === 'signature' || type === 'html') {
                console.debug('[normalizePreviewContent] signature/html - keeping HTML as is');
                value = value.replace(/<span class="template-variable"[^>]*>[\s\S]*?<\/span>/g, function(match) {
                    return '<span class="blank-line"></span>';
                });
                value = value.replace(/\[[\w_]+\]/g, '<span class="blank-line"></span>');
                return value;
            }

            const BLANK_MARKER = '___BLANK_LINE___';
            value = value.replace(/<span class="template-variable"[^>]*>[\s\S]*?<\/span>\s*<span class="template-variable-remove"[^>]*>[\s\S]*?<\/span>/g, function(match) {
                return BLANK_MARKER;
            });
            value = value.replace(/<span class="template-variable"[^>]*>[\s\S]*?<\/span>/g, function(match) {
                return BLANK_MARKER;
            });
            value = value.replace(/<span class="template-variable-remove"[^>]*>[\s\S]*?<\/span>/g, '');

            value = TemplateBuilder.utils.decodeHtmlEntities(value);
            console.debug('[normalizePreviewContent] after decodeHtmlEntities:', JSON.stringify(value));

            value = value.replace(new RegExp(BLANK_MARKER, 'g'), '<span class="blank-line"></span>');

            return value;
        }
    },

    // 변수 처리
    variables: {
        convertVariablesToBrackets(html) {
            if (!html) return '';
            return html.replace(/<span class="template-variable"[^>]*data-var-name="([^"]+)"[^>]*>[\s\S]*?<\/span>/g, '[$1]');
        },

        convertBracketsToVariables(html) {
            if (!html) return '';
            return html.replace(/\[([A-Z_]+)\]/g, function(match, varName) {
                const displayName = TemplateBuilder.utils.getDisplayName(varName);
                return '<span class="template-variable" contenteditable="false" data-var-name="' + varName + '">' +
                       '<span>' + displayName + '</span>' +
                       '<span class="template-variable-remove"></span>' +
                       '</span>';
            });
        },

        insert(variable) {
            if (!TemplateBuilder.state.activeElement) {
                showAlertModal('먼저 텍스트를 입력할 위치를 클릭해주세요.');
                return;
            }

            if (TemplateBuilder.state.activeElement.contentEditable === 'true') {
                const selection = window.getSelection();
                let range;

                if (selection.rangeCount === 0 || !TemplateBuilder.state.activeElement.contains(selection.anchorNode)) {
                    range = document.createRange();
                    range.selectNodeContents(TemplateBuilder.state.activeElement);
                    range.collapse(false);
                    selection.removeAllRanges();
                    selection.addRange(range);
                } else {
                    range = selection.getRangeAt(0);
                }

                const varSpan = document.createElement('span');
                varSpan.className = 'template-variable';
                varSpan.contentEditable = 'false';
                varSpan.setAttribute('data-var-name', variable.replace(/[\[\]]/g, ''));

                const varText = document.createElement('span');
                varText.textContent = TemplateBuilder.utils.getDisplayName(variable.replace(/[\[\]]/g, ''));

                const removeBtn = document.createElement('span');
                removeBtn.className = 'template-variable-remove';
                removeBtn.onclick = function(e) {
                    e.stopPropagation();
                    varSpan.remove();
                    TemplateBuilder.sections.updateSectionsData();
                };

                varSpan.appendChild(varText);
                varSpan.appendChild(removeBtn);

                range.deleteContents();
                range.insertNode(varSpan);

                const space = document.createTextNode(' ');
                range.setStartAfter(varSpan);
                range.insertNode(space);
                range.setStartAfter(space);
                range.collapse(true);

                selection.removeAllRanges();
                selection.addRange(range);

                TemplateBuilder.state.activeElement.focus();
            } else if (TemplateBuilder.state.activeElement.tagName === 'TEXTAREA') {
                const start = TemplateBuilder.state.activeElement.selectionStart;
                const end = TemplateBuilder.state.activeElement.selectionEnd;
                const text = TemplateBuilder.state.activeElement.value;
                TemplateBuilder.state.activeElement.value = text.substring(0, start) + variable + text.substring(end);
                TemplateBuilder.state.activeElement.selectionStart = TemplateBuilder.state.activeElement.selectionEnd = start + variable.length;
                TemplateBuilder.state.activeElement.focus();
            }

            TemplateBuilder.ui.closeVariableModal();
            TemplateBuilder.sections.updateSectionsData();
        }
    },

    // 섹션 관리
    sections: {
        add(type, content, afterElement, metadata) {
            content = content || '';
            metadata = metadata || {};
            const section = this.createSectionElement(type, content, metadata);
            const documentBody = document.getElementById('documentBody');

            if (afterElement) {
                afterElement.insertAdjacentElement('afterend', section);
            } else {
                const placeholder = documentBody.querySelector('.add-section-placeholder');
                if (placeholder) {
                    documentBody.insertBefore(section, placeholder);
                } else {
                    documentBody.appendChild(section);
                }
            }

            this.setActiveSection(section);
            this.updateSectionsData();
        },

        createSectionElement(type, content, metadata) {
            content = content || '';
            metadata = metadata || {};
            const coercedMetadata = TemplateBuilder.dataProcessor.coerceMetadata(metadata);
            const normalizedType = TemplateBuilder.dataProcessor.normalizeFrontendType(type, coercedMetadata);
            const normalizedMetadata = TemplateBuilder.dataProcessor.ensureMetadataForType(normalizedType, coercedMetadata);
            const section = document.createElement('div');
            section.className = 'editable-section';
            section.dataset.type = normalizedType;
            section.dataset.id = 'section-' + Date.now();
            this.setSectionMetadata(section, normalizedMetadata);

            let processedContent = content;
            if (normalizedType !== 'html' && normalizedType !== 'signature') {
                // processedContent = TemplateBuilder.variables.convertBracketsToVariables(content || '');
            } else if (normalizedType === 'signature') {
                if (content) {
                    processedContent = TemplateBuilder.variables.convertBracketsToVariables(content);
                }
            }

            let innerHTML = '';

            switch(normalizedType) {
                case 'text':
                    innerHTML = '<div contenteditable="true" class="section-text" data-placeholder="텍스트를 입력하세요...">' + (processedContent || '') + '</div>';
                    break;

                case 'title':
                    innerHTML = '<div contenteditable="true" class="section-title" data-placeholder="제목을 입력하세요...">' + (processedContent || '') + '</div>';
                    break;

                case 'clause':
                    TemplateBuilder.state.clauseCounter++;
                    innerHTML = '<div class="section-clause">' +
                        '<span class="clause-number">' + TemplateBuilder.state.clauseCounter + '.</span>' +
                        '<span contenteditable="true" data-placeholder="조항 내용을 입력하세요...">' + (processedContent || '') + '</span>' +
                        '</div>';
                    break;

                case 'dotted':
                    innerHTML = '<div contenteditable="true" class="section-dotted-box" data-placeholder="점선 박스 내용을 입력하세요...">' + (processedContent || '') + '</div>';
                    break;

                case 'footer':
                    innerHTML = '<div contenteditable="true" class="section-footer" data-placeholder="꼬릿말을 입력하세요...">' + (processedContent || '') + '</div>';
                    break;

                case 'signature':
                    if (content && processedContent) {
                        innerHTML = '<div class="section-signature" contenteditable="true">' + processedContent + '</div>';
                    } else {
                        innerHTML = '<div class="section-signature" contenteditable="true">' +
                            '<div class="signature-section">' +
                                '<div class="signature-block signature-block--employee">' +
                                    '<div class="signature-line">(근로자) 주소: <span class="template-variable" contenteditable="false"><span>EMPLOYEE_ADDRESS</span><span class="template-variable-remove"></span></span></div>' +
                                    '<div class="signature-line">연락처: <span class="template-variable" contenteditable="false"><span>EMPLOYEE_PHONE</span><span class="template-variable-remove"></span></span></div>' +
                                    '<div class="signature-line">성명: <span class="template-variable" contenteditable="false"><span>EMPLOYEE</span><span class="template-variable-remove"></span></span> (인)</div>' +
                                '</div>' +
                                '<div class="signature-block signature-block--employer">' +
                                    '<div class="signature-line">(사업주) 사업체명: <span class="template-variable" contenteditable="false"><span>COMPANY_NAME</span><span class="template-variable-remove"></span></span></div>' +
                                    '<div class="signature-line signature-line-indent">주소: <span class="template-variable" contenteditable="false"><span>EMPLOYER_ADDRESS</span><span class="template-variable-remove"></span></span></div>' +
                                    '<div class="signature-line signature-line--seal signature-line-indent">' +
                                        '대표자: <span class="template-variable" contenteditable="false"><span>EMPLOYER</span><span class="template-variable-remove"></span></span> ' +
                                        '<span class="signature-stamp-label">(인)' +
                                            '<span class="signature-stamp-wrapper"><span class="template-variable" contenteditable="false"><span>EMPLOYER_SIGNATURE_IMAGE</span><span class="template-variable-remove"></span></span></span>' +
                                        '</span>' +
                                    '</div>' +
                                    '<div class="signature-line">(전화: <span class="template-variable" contenteditable="false"><span>EMPLOYER_PHONE</span><span class="template-variable-remove"></span></span>)</div>' +
                                '</div>' +
                            '</div>' +
                        '</div>';
                    }
                    break;

                case 'html':
                    innerHTML = '<div class="html-section">' +
                        '<textarea class="html-editor" placeholder="HTML 코드를 입력하세요...">' + (content || '') + '</textarea>' +
                        '<div class="html-preview mt-2"></div>' +
                        '</div>';
                    break;
                default:
                    innerHTML = '<div contenteditable="true" class="section-text" data-placeholder="텍스트를 입력하세요...">' + (processedContent || '') + '</div>';
                    break;
            }

            section.innerHTML = innerHTML + this.createSectionControls();

            section.querySelectorAll('.template-variable-remove').forEach(function(btn) {
                btn.onclick = function(e) {
                    e.stopPropagation();
                    btn.parentElement.remove();
                    TemplateBuilder.sections.updateSectionsData();
                };
            });

            return section;
        },

        createSectionControls() {
            return '<div class="section-controls">' +
                '<button class="section-control-btn" onclick="TemplateBuilder.sections.moveSection(this, \'up\')" title="위로">' +
                    '<i class="bi bi-arrow-up"></i>' +
                '</button>' +
                '<button class="section-control-btn" onclick="TemplateBuilder.sections.moveSection(this, \'down\')" title="아래로">' +
                    '<i class="bi bi-arrow-down"></i>' +
                '</button>' +
                '<button class="section-control-btn" onclick="TemplateBuilder.sections.duplicateSection(this)" title="복사">' +
                    '<i class="bi bi-files"></i>' +
                '</button>' +
                '<button class="section-control-btn" onclick="TemplateBuilder.sections.deleteSection(this)" title="삭제">' +
                    '<i class="bi bi-trash"></i>' +
                '</button>' +
            '</div>';
        },

        createPlaceholder() {
            const placeholder = document.createElement('div');
            placeholder.className = 'add-section-placeholder';
            placeholder.onclick = function() { TemplateBuilder.ui.showAddSectionMenu(this); };
            placeholder.innerHTML = '<i class="bi bi-plus-circle"></i><div>여기를 클릭하여 섹션을 추가하세요</div>';
            return placeholder;
        },

        setActiveSection(section) {
            document.querySelectorAll('.editable-section').forEach(function(s) { s.classList.remove('active'); });
            section.classList.add('active');
            TemplateBuilder.state.activeElement = section.querySelector('[contenteditable="true"], .html-editor');
        },

        moveSection(button, direction) {
            const section = button.closest('.editable-section');
            if (direction === 'up' && section.previousElementSibling && !section.previousElementSibling.classList.contains('add-section-placeholder')) {
                section.parentNode.insertBefore(section, section.previousElementSibling);
            } else if (direction === 'down' && section.nextElementSibling && !section.nextElementSibling.classList.contains('add-section-placeholder')) {
                section.parentNode.insertBefore(section.nextElementSibling, section);
            }
            this.updateSectionsData();
        },

        duplicateSection(button) {
            const section = button.closest('.editable-section');
            const clone = section.cloneNode(true);
            clone.dataset.id = 'section-' + Date.now();
            section.parentNode.insertBefore(clone, section.nextSibling);
            this.updateSectionsData();
        },

        deleteSection(button) {
            showConfirmModal(
                '이 섹션을 삭제하시겠습니까?',
                function() {
                    const section = button.closest('.editable-section');
                    section.remove();
                    TemplateBuilder.sections.updateSectionsData();

                    const documentBody = document.getElementById('documentBody');
                    if (documentBody.children.length === 0) {
                        documentBody.appendChild(TemplateBuilder.sections.createPlaceholder());
                    }
                },
                '삭제',
                '취소',
                'btn-danger'
            );
        },

        setSectionMetadata(section, metadata) {
            metadata = metadata || {};
            section.dataset.metadata = TemplateBuilder.utils.encodeMetadata(metadata);
        },

        getSectionMetadata(section) {
            if (!section || !section.dataset) {
                return {};
            }
            return TemplateBuilder.utils.decodeMetadata(section.dataset.metadata);
        },

        updateSectionContent(element) {
            const section = element.closest('.editable-section');
            if (section) {
                this.updateSectionsData();
            }
        },

        updateSectionsData() {
            const documentBody = document.getElementById('documentBody');
            TemplateBuilder.state.sections = [];

            let clauseIndex = 0;

            documentBody.querySelectorAll('.editable-section').forEach(function(section, index) {
                const existingMetadata = TemplateBuilder.sections.getSectionMetadata(section);
                const type = TemplateBuilder.dataProcessor.normalizeFrontendType(section.dataset.type, existingMetadata);
                const metadata = TemplateBuilder.dataProcessor.ensureMetadataForType(type, existingMetadata);
                TemplateBuilder.sections.setSectionMetadata(section, metadata);
                section.dataset.type = type;

                let content = '';

                if (type === 'html') {
                    const textarea = section.querySelector('.html-editor');
                    content = textarea ? textarea.value : '';

                    const preview = section.querySelector('.html-preview');
                    if (preview) {
                        preview.innerHTML = TemplateBuilder.dataProcessor.normalizePreviewContent(type, content);
                    }
                } else if (type === 'signature') {
                    const signatureElement = section.querySelector('.section-signature');
                    content = signatureElement ? TemplateBuilder.variables.convertVariablesToBrackets(signatureElement.innerHTML) : '';
                } else if (type === 'clause') {
                    clauseIndex++;
                    const numberElement = section.querySelector('.clause-number');
                    if (numberElement) {
                        numberElement.textContent = clauseIndex + '.';
                    }
                    const clauseContentElement = section.querySelector('.section-clause > [contenteditable="true"]');
                    content = clauseContentElement ? clauseContentElement.innerHTML : '';
                } else {
                    const editableElement = section.querySelector('[contenteditable="true"]');
                    content = editableElement ? editableElement.innerHTML : '';
                }

                TemplateBuilder.state.sections.push({
                    sectionId: section.dataset.id,
                    type: type,
                    order: index,
                    content: content,
                    metadata: metadata
                });
            });

            TemplateBuilder.state.clauseCounter = clauseIndex;
        },

        loadInitialSections() {
            const scriptEl = document.getElementById('initialSections');
            if (scriptEl) {
                try {
                    const rawPayload = scriptEl.textContent || '[]';
                    console.debug('[TemplateEditor] raw initial sections payload:', rawPayload.substring(0, 500));

                    const sectionsData = TemplateBuilder.dataProcessor.parseSectionsPayload(rawPayload);
                    console.debug('[TemplateEditor] parsed sections array:', sectionsData);
                    const documentBody = document.getElementById('documentBody');

                    if (sectionsData.length > 0) {
                        documentBody.innerHTML = '';

                        TemplateBuilder.state.clauseCounter = 0;
                        sectionsData.slice().sort(function(a, b) {
                                const orderA = typeof a.order === 'number' ? a.order : parseInt(a.order, 10) || 0;
                                const orderB = typeof b.order === 'number' ? b.order : parseInt(b.order, 10) || 0;
                                return orderA - orderB;
                            })
                            .forEach(function(sectionData) {
                                if (!sectionData) {
                                    return;
                                }
                                const metadata = TemplateBuilder.dataProcessor.coerceMetadata(sectionData.metadata);
                                const section = TemplateBuilder.sections.createSectionElement(sectionData.type, sectionData.content, metadata);
                                section.dataset.id = sectionData.sectionId || ('section-' + Date.now());
                                documentBody.appendChild(section);
                            });

                        documentBody.appendChild(TemplateBuilder.sections.createPlaceholder());
                        TemplateBuilder.sections.updateSectionsData();
                    } else {
                        if (!documentBody.querySelector('.add-section-placeholder')) {
                            documentBody.appendChild(TemplateBuilder.sections.createPlaceholder());
                        }
                    }
                } catch (e) {
                    console.error('Failed to load initial sections:', e);
                }
            }
        }
    },

    // UI 관리
    ui: {
        showVariableModal() {
            document.getElementById('variableModal').classList.add('show');
            document.getElementById('modalBackdrop').classList.add('show');
        },

        closeVariableModal() {
            const modal = document.getElementById('variableModal');
            const backdrop = document.getElementById('modalBackdrop');
            
            if (modal) {
                modal.classList.remove('show');
            }
            if (backdrop) {
                backdrop.classList.remove('show');
            }
        },

        showAddSectionMenu(placeholder) {
            const menu = '<div class="add-section-menu">' +
                '<button class="toolbar-btn toolbar-btn-sm" onclick="TemplateBuilder.sections.addFromPlaceholder(\'text\', this)">' +
                    '<i class="bi bi-text-left"></i> 텍스트' +
                '</button>' +
                '<button class="toolbar-btn toolbar-btn-sm" onclick="TemplateBuilder.sections.addFromPlaceholder(\'title\', this)">' +
                    '<i class="bi bi-type"></i> 타이틀' +
                '</button>' +
                '<button class="toolbar-btn toolbar-btn-sm" onclick="TemplateBuilder.sections.addFromPlaceholder(\'clause\', this)">' +
                    '<i class="bi bi-list-ol"></i> 조항' +
                '</button>' +
                '<button class="toolbar-btn toolbar-btn-sm" onclick="TemplateBuilder.sections.addFromPlaceholder(\'dotted\', this)">' +
                    '<i class="bi bi-border-style"></i> 점선' +
                '</button>' +
                '<button class="toolbar-btn toolbar-btn-sm" onclick="TemplateBuilder.sections.addFromPlaceholder(\'footer\', this)">' +
                    '<i class="bi bi-text-center"></i> 꼬릿말' +
                '</button>' +
                '<button class="toolbar-btn toolbar-btn-sm" onclick="TemplateBuilder.sections.addFromPlaceholder(\'signature\', this)">' +
                    '<i class="bi bi-pen"></i> 서명란' +
                '</button>' +
                '<button class="toolbar-btn toolbar-btn-sm" onclick="TemplateBuilder.sections.addFromPlaceholder(\'html\', this)">' +
                    '<i class="bi bi-code-slash"></i> HTML' +
                '</button>' +
            '</div>';
            placeholder.innerHTML = menu;
        }
    },

    // 프리셋 관리
    presets: {
        load() {
            fetch('/templates/presets')
                .then(response => response.json())
                .then(presets => {
                    this.display(presets);
                })
                .catch(error => {
                    console.error('Failed to load preset templates:', error);
                    this.displayError();
                });
        },

        display(presets) {
            const loading = document.getElementById('presetLoading');
            const grid = document.getElementById('presetGrid');
            
            loading.style.display = 'none';
            grid.style.display = 'grid';
            
            if (presets.length === 0) {
                grid.innerHTML = '<div class="preset-empty">' +
                    '<i class="bi bi-inbox"></i>' +
                    '<div>사용 가능한 프리셋 템플릿이 없습니다</div>' +
                    '</div>';
                return;
            }
            
            grid.innerHTML = presets.map(preset => this.createCard(preset)).join('');
        },

        displayError() {
            const loading = document.getElementById('presetLoading');
            const grid = document.getElementById('presetGrid');
            
            loading.style.display = 'none';
            grid.style.display = 'grid';
            grid.innerHTML = '<div class="preset-empty">' +
                '<i class="bi bi-exclamation-triangle"></i>' +
                '<div>프리셋 템플릿을 불러오는 중 오류가 발생했습니다</div>' +
                '</div>';
        },

        createCard(preset) {
            return '<div class="preset-card" data-preset-id="' + preset.id + '">' +
                '<div class="preset-card-header">' +
                    '<div class="preset-card-icon">' +
                        '<i class="bi bi-file-text"></i>' +
                    '</div>' +
                    '<h6 class="preset-card-title">' + preset.name + '</h6>' +
                '</div>' +
                '<div class="preset-card-description">' +
                    '미리 만들어진 템플릿을 기반으로 빠르게 시작할 수 있습니다' +
                '</div>' +
                '<div class="preset-card-action">' +
                    '<span class="preset-card-preview">클릭하여 미리보기</span>' +
                    '<button class="preset-card-button" onclick="TemplateBuilder.presets.loadTemplate(\'' + preset.id + '\', \'' + preset.name + '\')">' +
                        '선택하여 시작하기' +
                    '</button>' +
                '</div>' +
            '</div>';
        },

        loadTemplate(presetId, presetName) {
            // 현재 작업 내용이 있는지 확인
            if (TemplateBuilder.state.sections.length > 0 || document.getElementById('templateTitle').value.trim()) {
                showConfirmModal(
                    '현재 작업 중인 내용이 있습니다. 프리셋 템플릿을 로드하면 현재 내용이 초기화됩니다. 계속하시겠습니까?',
                    function() {
                        TemplateBuilder.presets.performLoad(presetId, presetName);
                    },
                    '로드',
                    '취소',
                    'btn-primary'
                );
            } else {
                TemplateBuilder.presets.performLoad(presetId, presetName);
            }
        },

        performLoad(presetId, presetName) {
            fetch('/templates/presets/' + presetId + '/sections')
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Failed to load preset template');
                    }
                    return response.json();
                })
                .then(data => {
                    TemplateBuilder.presets.loadSections(data.sections, presetName);
                })
                .catch(error => {
                    console.error('Failed to load preset sections:', error);
                    showAlertModal('프리셋 템플릿을 불러오는 중 오류가 발생했습니다.');
                });
        },

        loadSections(presetSections, presetName) {
            // clauseCounter 초기화
            TemplateBuilder.state.clauseCounter = 0;
            
            // 현재 섹션들 모두 제거
            const documentBody = document.getElementById('documentBody');
            documentBody.innerHTML = '';
            
            // 템플릿 제목 설정
            document.getElementById('templateTitle').value = presetName;
            
            // 프리셋 섹션들을 빌더에 추가
            presetSections.forEach(function(sectionData) {
                const metadata = TemplateBuilder.dataProcessor.coerceMetadata(sectionData.metadata);
                
                // 변수들을 템플릿 변수 형식으로 변환
                let content = sectionData.content;
                if (sectionData.type !== 'html' && sectionData.type !== 'signature') {
                    content = TemplateBuilder.variables.convertBracketsToVariables(content);
                }
                
                const section = TemplateBuilder.sections.createSectionElement(sectionData.type, content, metadata);
                section.dataset.id = sectionData.sectionId || ('section-' + Date.now() + '-' + Math.random());
                documentBody.appendChild(section);
            });
            
            // 플레이스홀더 추가
            documentBody.appendChild(TemplateBuilder.sections.createPlaceholder());
            
            // 섹션 데이터 업데이트
            TemplateBuilder.sections.updateSectionsData();
            
            // 성공 메시지 표시
            showAlertModal('프리셋 템플릿이 성공적으로 로드되었습니다. 필요에 맞게 수정하여 사용하세요.');
            
            // 프리셋 섹션 접기
            TemplateBuilder.presets.collapseSection();
        },

        toggleSection() {
            const content = document.getElementById('presetContent');
            const icon = document.getElementById('presetToggleIcon');
            
            if (content.classList.contains('collapsed')) {
                content.classList.remove('collapsed');
                icon.className = 'bi bi-chevron-up';
            } else {
                content.classList.add('collapsed');
                icon.className = 'bi bi-chevron-down';
            }
        },

        collapseSection() {
            const content = document.getElementById('presetContent');
            const icon = document.getElementById('presetToggleIcon');
            content.classList.add('collapsed');
            icon.className = 'bi bi-chevron-down';
        }
    },

    // 미리보기 기능
    preview: {
        generate(previewSections) {
            previewSections = previewSections || TemplateBuilder.state.sections;
            const title = document.getElementById('templateTitle').value || '제목 없음';

            let bodyContent = '';
            let clauseIndex = 0;

            previewSections.forEach(function(section) {
                let rawContent = TemplateBuilder.dataProcessor.normalizePreviewContent(section.type, section.content);
                const codePoints = rawContent ? Array.from(rawContent).map(function(ch) { return ch.charCodeAt(0).toString(16); }) : [];
                console.debug('[TemplateEditor] rendering section type:', section.type, 'content length:', rawContent ? rawContent.length : 0, 'value:', JSON.stringify(rawContent), 'codes:', codePoints);
                switch (section.type) {
                    case 'html':
                        bodyContent += rawContent;
                        console.debug('[TemplateEditor] appended markup:', rawContent && rawContent.substring ? rawContent.substring(0, 200) : rawContent);
                        break;
                    case 'signature':
                        const signatureMarkup = '<div class="section-signature">' + rawContent + '</div>';
                        console.debug('[TemplateEditor] appended markup:', signatureMarkup.substring(0, 200));
                        bodyContent += signatureMarkup;
                        break;
                    case 'clause':
                        clauseIndex++;
                        const clauseMarkup = '<div class="section-clause"><span class="clause-number">' + clauseIndex + '.</span> <span>' + rawContent + '</span></div>';
                        console.debug('[TemplateEditor] appended markup:', clauseMarkup.substring(0, 200));
                        bodyContent += clauseMarkup;
                        break;
                    case 'title':
                        const titleMarkup = '<h2 class="section-title">' + rawContent + '</h2>';
                        console.debug('[TemplateEditor] appended markup:', titleMarkup.substring(0, 200));
                        bodyContent += titleMarkup;
                        break;
                    case 'dotted':
                        const dottedMarkup = '<div class="section-dotted">' + rawContent + '</div>';
                        console.debug('[TemplateEditor] appended markup:', dottedMarkup.substring(0, 200));
                        bodyContent += dottedMarkup;
                        break;
                    case 'footer':
                        const footerMarkup = '<div class="section-footer">' + rawContent + '</div>';
                        console.debug('[TemplateEditor] appended markup:', footerMarkup.substring(0, 200));
                        bodyContent += footerMarkup;
                        break;
                    default:
                        console.debug('[TemplateEditor] default case - rawContent before markup:', JSON.stringify(rawContent), 'type:', typeof rawContent, 'length:', rawContent ? rawContent.length : 'null');
                        const textMarkup = '<div class="section-text">' + rawContent + '</div>';
                        console.debug('[TemplateEditor] default case - textMarkup:', textMarkup.substring(0, 200));
                        console.debug('[TemplateEditor] appended markup:', textMarkup.substring(0, 200));
                        bodyContent += textMarkup;
                        break;
                }
            });

            console.debug('[TemplateEditor] body content snippet:', bodyContent.substring(0, 500));

            return '<!DOCTYPE html>' +
            '<html lang="ko">' +
            '<head>' +
                '<meta charset="UTF-8">' +
                '<meta name="viewport" content="width=device-width, initial-scale=1.0">' +
                '<title>' + title + '</title>' +
                '<link rel="stylesheet" href="/css/contract-common.css">' +
            '</head>' +
            '<body>' +
                bodyContent +
            '</body>' +
            '</html>';
        },

        show() {
            TemplateBuilder.sections.updateSectionsData();

            if (TemplateBuilder.state.sections.length === 0) {
                showAlertModal('미리볼 섹션이 없습니다. 먼저 섹션을 추가해주세요.');
                return;
            }

            const previewHtml = this.generate();
            const previewTitle = document.getElementById('templateTitle').value || '제목 없음';
            const previewFrame = document.getElementById('previewFrame');

            if (previewFrame) {
                previewFrame.srcdoc = previewHtml;

                const modalElement = document.getElementById('previewModal');
                const modalTitle = document.getElementById('previewModalLabel');
                if (modalTitle) {
                    modalTitle.textContent = previewTitle + ' 미리보기';
                }

                if (modalElement) {
                    const modal = new bootstrap.Modal(modalElement);
                    modal.show();
                }
            }
        }
    },

    // 저장 기능
    save: {
        perform() {
            const title = document.getElementById('templateTitle').value;

            if (!title) {
                showAlertModal('템플릿 제목을 입력해주세요.');
                document.getElementById('templateTitle').focus();
                return;
            }

            if (TemplateBuilder.state.sections.length === 0) {
                showAlertModal('최소 하나 이상의 섹션을 추가해주세요.');
                return;
            }

            TemplateBuilder.sections.updateSectionsData();

            document.getElementById('formTitle').value = title;

            const serializedSections = TemplateBuilder.state.sections.map(function(section, index) {
                return {
                    sectionId: section.sectionId,
                    type: TemplateBuilder.dataProcessor.mapFrontendTypeToServer(section.type, section.metadata),
                    order: index,
                    content: section.content,
                    metadata: section.metadata || {}
                };
            });

            const templateData = {
                version: '1.0',
                metadata: {
                    title: title,
                    description: '',
                    createdBy: '',
                    variables: {}
                },
                sections: serializedSections
            };

            document.getElementById('sectionsJson').value = JSON.stringify(templateData);

            document.getElementById('templateForm').submit();
        }
    },

    // 이벤트 리스너 설정
    setupEventListeners() {
        document.getElementById('documentBody').addEventListener('click', function(e) {
            const section = e.target.closest('.editable-section');
            if (section) {
                TemplateBuilder.sections.setActiveSection(section);
            }
        });

        document.getElementById('documentBody').addEventListener('input', function(e) {
            if (e.target.contentEditable === 'true') {
                TemplateBuilder.sections.updateSectionContent(e.target);
            }
        });

        document.getElementById('modalBackdrop').addEventListener('click', TemplateBuilder.ui.closeVariableModal);

        // HTML 에디터 실시간 미리보기
        document.addEventListener('input', function(e) {
            if (e.target.classList.contains('html-editor')) {
                const preview = e.target.parentElement.querySelector('.html-preview');
                if (preview) {
                    preview.innerHTML = e.target.value;
                }
            }
        });

        // 키보드 단축키
        document.addEventListener('keydown', function(e) {
            if ((e.ctrlKey || e.metaKey) && e.key === 's') {
                e.preventDefault();
                TemplateBuilder.save.perform();
            }

            if ((e.ctrlKey || e.metaKey) && e.key === 'p') {
                e.preventDefault();
                TemplateBuilder.preview.show();
            }

            if ((e.ctrlKey || e.metaKey) && e.key === 'b') {
                e.preventDefault();
                TemplateBuilder.ui.showVariableModal();
            }
        });
    },

    // 초기화
    init() {
        TemplateBuilder.sections.loadInitialSections();
        TemplateBuilder.setupEventListeners();
        TemplateBuilder.presets.load();

        // 전역 API 노출
        window.SignlyTemplateEditor = {
            getSectionsSnapshot: function() {
                TemplateBuilder.sections.updateSectionsData();
                return JSON.parse(JSON.stringify(TemplateBuilder.state.sections));
            },
            getPreviewHtml: function() {
                TemplateBuilder.sections.updateSectionsData();
                return TemplateBuilder.preview.generate();
            }
        };

        // 전역 함수 노출 (기존 호환성)
        window.insertVariable = TemplateBuilder.variables.insert;
        window.showVariableModal = TemplateBuilder.ui.showVariableModal;
        window.closeVariableModal = TemplateBuilder.ui.closeVariableModal;
        window.addSectionFromPlaceholder = TemplateBuilder.sections.addFromPlaceholder;
        window.previewTemplate = TemplateBuilder.preview.show;
        window.saveTemplate = TemplateBuilder.save.perform;
        window.togglePresetSection = TemplateBuilder.presets.toggleSection;
        window.loadPresetTemplate = TemplateBuilder.presets.loadTemplate;
    }
};

// DOM 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    TemplateBuilder.init();
});

// 섹션 추가 함수 추가
TemplateBuilder.sections.addFromPlaceholder = function(type, button) {
    const placeholder = button.closest('.add-section-placeholder');
    const section = this.createSectionElement(type);
    placeholder.insertAdjacentElement('beforebegin', section);

    this.setActiveSection(section);
    this.updateSectionsData();
};