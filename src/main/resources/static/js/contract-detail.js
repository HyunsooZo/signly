/**
 * Contract Detail JavaScript
 * Handles contract viewing and management functionality
 */

class ContractDetail {
    constructor() {
        this.previewData = {};
        this.contractRawContent = '';
        this.presetTypeValue = '';
        this.isHtmlPreset = false;
        this.csrfParam = '';
        this.csrfToken = '';

        this.init();
    }

    init() {
        this.loadData();
        this.determineContentType();
        this.renderContent();
    }

    loadData() {
        const previewDataElement = document.getElementById('contractPreviewData');
        this.previewData = previewDataElement ? previewDataElement.dataset : {};

        const contractContentTextarea = document.getElementById('contractContentHtml');
        this.contractRawContent = contractContentTextarea ? contractContentTextarea.value : '';

        this.presetTypeValue = (this.previewData.presetType || '').toUpperCase();
        this.csrfParam = window.csrfParam || '';
        this.csrfToken = window.csrfToken || '';
    }

    determineContentType() {
        // HTML 내용인지 자동 감지 (meta, div, style 태그 등이 있으면 HTML로 판단)
        const looksLikeHtml = this.contractRawContent && (
            this.contractRawContent.includes('<meta') ||
            this.contractRawContent.includes('<div') ||
            this.contractRawContent.includes('<style') ||
            this.contractRawContent.includes('<body')
        );

        this.isHtmlPreset = (this.presetTypeValue && this.presetTypeValue !== 'NONE') || looksLikeHtml;
    }

    renderContent() {
        if (this.isHtmlPreset) {
            this.renderHtmlContract(this.contractRawContent);
        } else {
            this.renderPlainTextContract(this.contractRawContent);
        }
    }

    getPreviewDefaults() {
        return {
            firstPartyName: this.previewData.firstPartyName || '-',
            firstPartyEmail: this.previewData.firstPartyEmail || '-',
            firstPartyOrg: this.previewData.firstPartyOrg || '-',
            secondPartyName: this.previewData.secondPartyName || '-',
            secondPartyEmail: this.previewData.secondPartyEmail || '-',
            secondPartyOrg: this.previewData.secondPartyOrg || '-',
            contractTitle: this.previewData.contractTitle || '-'
        };
    }

    renderPlainTextContract(content) {
        const container = document.getElementById('contractContentHtmlContainer');
        if (!container) {
            return;
        }
        container.textContent = content || '';
        container.style.whiteSpace = 'pre-wrap';
        container.style.fontFamily = "'Times New Roman', serif";
        container.style.backgroundColor = 'white';
        container.style.color = 'black';
        container.style.padding = '2rem';
    }

    scopeCssText(cssText, scopeSelector) {
        if (!cssText || !cssText.trim()) {
            return '';
        }

        let tempStyle;
        try {
            tempStyle = document.createElement('style');
            tempStyle.textContent = cssText;
            document.head.appendChild(tempStyle);

            const sheet = tempStyle.sheet;
            if (!sheet || !sheet.cssRules) {
                return this.naiveScopeCss(cssText, scopeSelector);
            }

            const processRules = (rules) => {
                const output = [];
                Array.from(rules).forEach(rule => {
                    const ruleType = rule.type;
                    const CSSRuleRef = window.CSSRule || {};

                    if (CSSRuleRef.STYLE_RULE !== undefined && ruleType === CSSRuleRef.STYLE_RULE) {
                        const scopedSelectors = rule.selectorText
                            .split(',')
                            .map(selector => scopeSelector + ' ' + selector.trim())
                            .join(', ');
                        output.push(scopedSelectors + ' { ' + rule.style.cssText + ' }');
                    } else if (CSSRuleRef.MEDIA_RULE !== undefined && ruleType === CSSRuleRef.MEDIA_RULE) {
                        const inner = processRules(rule.cssRules);
                        if (inner.length) {
                            output.push('@media ' + rule.conditionText + ' {\n' + inner.join('\n') + '\n}');
                        }
                    } else if (CSSRuleRef.SUPPORTS_RULE !== undefined && ruleType === CSSRuleRef.SUPPORTS_RULE) {
                        const inner = processRules(rule.cssRules);
                        if (inner.length) {
                            output.push('@supports ' + rule.conditionText + ' {\n' + inner.join('\n') + '\n}');
                        }
                    } else {
                        output.push(rule.cssText);
                    }
                });
                return output;
            };

            const scopedRules = processRules(sheet.cssRules);
            return scopedRules.join('\n');
        } catch (error) {
            console.warn('CSS scoping failed, using fallback:', error);
            return this.naiveScopeCss(cssText, scopeSelector);
        } finally {
            if (tempStyle && tempStyle.parentNode) {
                tempStyle.parentNode.removeChild(tempStyle);
            }
        }
    }

    naiveScopeCss(cssText, scopeSelector) {
        if (!cssText || !cssText.trim()) {
            return '';
        }

        return cssText.replace(/([^{}]+)\s*\{/g, (match, selector) => {
            selector = selector.trim();
            if (selector.startsWith('@') || selector.includes(':root')) {
                return match;
            }
            return scopeSelector + ' ' + selector + ' {';
        });
    }

    renderHtmlContract(htmlContent) {
        const container = document.getElementById('contractContentHtmlContainer');
        if (!container) {
            return;
        }

        const tempWrapper = document.createElement('div');
        tempWrapper.innerHTML = htmlContent;

        const styles = [];
        const styleElements = tempWrapper.querySelectorAll('style');
        styleElements.forEach(styleEl => {
            if (styleEl.textContent) {
                styles.push(this.scopeCssText(styleEl.textContent, '#contractContentHtmlContainer'));
            }
            styleEl.parentNode.removeChild(styleEl);
        });

        const scopedStyle = document.createElement('style');
        scopedStyle.textContent = styles.join('\n');
        container.appendChild(scopedStyle);

        const bodyContent = tempWrapper.querySelector('body') || tempWrapper;
        container.innerHTML += bodyContent.innerHTML;

        const defaults = this.getPreviewDefaults();
        const placeholders = {
            'CONTRACT_TITLE': defaults.contractTitle,
            'FIRST_PARTY_NAME': defaults.firstPartyName,
            'FIRST_PARTY_EMAIL': defaults.firstPartyEmail,
            'FIRST_PARTY_ORG': defaults.firstPartyOrg,
            'SECOND_PARTY_NAME': defaults.secondPartyName,
            'SECOND_PARTY_EMAIL': defaults.secondPartyEmail,
            'SECOND_PARTY_ORG': defaults.secondPartyOrg
        };

        let finalHtml = container.innerHTML;
        Object.entries(placeholders).forEach(([key, value]) => {
            const regex = new RegExp(`\\{${key}\\}`, 'g');
            finalHtml = finalHtml.replace(regex, value);
        });

        container.innerHTML = finalHtml;
    }

    appendCsrfField(form) {
        if (this.csrfParam && this.csrfToken) {
            const csrfField = document.createElement('input');
            csrfField.type = 'hidden';
            csrfField.name = this.csrfParam;
            csrfField.value = this.csrfToken;
            form.appendChild(csrfField);
        }
    }

    downloadPdf() {
        const form = document.createElement('form');
        form.method = 'post';
        form.action = `/contracts/${this.getContractId()}/pdf`;
        this.appendCsrfField(form);
        document.body.appendChild(form);
        form.submit();
    }

    resendEmail() {
        if (window.showConfirmModal) {
            window.showConfirmModal(
                '서명 요청 이메일을 다시 전송하시겠습니까?',
                () => {
                    const form = document.createElement('form');
                    form.method = 'post';
                    form.action = `/contracts/${this.getContractId()}/resend`;
                    this.appendCsrfField(form);
                    document.body.appendChild(form);
                    form.submit();
                },
                '재전송',
                '취소',
                'btn-warning'
            );
        }
    }

    cancelContract() {
        if (window.showConfirmModal) {
            window.showConfirmModal(
                '계약서를 취소하시겠습니까? 취소된 계약서는 더 이상 서명할 수 없습니다.',
                () => {
                    const form = document.createElement('form');
                    form.method = 'post';
                    form.action = `/contracts/${this.getContractId()}/cancel`;
                    this.appendCsrfField(form);
                    document.body.appendChild(form);
                    form.submit();
                },
                '취소',
                '닫기',
                'btn-danger'
            );
        }
    }

    completeContract() {
        if (window.showConfirmModal) {
            window.showConfirmModal(
                '계약을 완료하시겠습니까?',
                () => {
                    const form = document.createElement('form');
                    form.method = 'post';
                    form.action = `/contracts/${this.getContractId()}/complete`;
                    this.appendCsrfField(form);
                    document.body.appendChild(form);
                    form.submit();
                },
                '완료',
                '취소',
                'btn-success'
            );
        }
    }

    deleteContract() {
        const deleteModal = document.getElementById('deleteModal');
        if (deleteModal) {
            new bootstrap.Modal(deleteModal).show();
        }
    }

    getContractId() {
        // Try to get contract ID from the page data or URL
        const metaTag = document.querySelector('meta[name="contract-id"]');
        if (metaTag) {
            return metaTag.content;
        }

        // Fallback: try to extract from URL path
        const pathParts = window.location.pathname.split('/');
        const contractIndex = pathParts.indexOf('contracts');
        if (contractIndex !== -1 && pathParts[contractIndex + 1]) {
            return pathParts[contractIndex + 1];
        }

        return '';
    }
}

// Global functions for onclick handlers
window.downloadPdf = function() {
    if (window.contractDetail) {
        window.contractDetail.downloadPdf();
    }
};

window.resendEmail = function() {
    if (window.contractDetail) {
        window.contractDetail.resendEmail();
    }
};

window.cancelContract = function() {
    if (window.contractDetail) {
        window.contractDetail.cancelContract();
    }
};

window.completeContract = function() {
    if (window.contractDetail) {
        window.contractDetail.completeContract();
    }
};

window.deleteContract = function() {
    if (window.contractDetail) {
        window.contractDetail.deleteContract();
    }
};

// Initialize the contract detail when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    // Set CSRF data from JSP
    window.csrfParam = '<c:out value="${_csrf.parameterName}"/>';
    window.csrfToken = '<c:out value="${_csrf.token}"/>';

    window.contractDetail = new ContractDetail();
});