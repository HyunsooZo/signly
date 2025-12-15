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
    }

    determineContentType() {
        // HTML 내용인지 자동 감지 (태그가 하나라도 있으면 HTML로 판단)
        const looksLikeHtml = this.contractRawContent && /<[a-z][\s\S]*>/i.test(this.contractRawContent);

        this.isHtmlPreset = (this.presetTypeValue && this.presetTypeValue !== 'NONE') || looksLikeHtml;

        console.log('=== Contract Content Type Detection ===');
        console.log('presetType:', this.presetTypeValue);
        console.log('looksLikeHtml:', looksLikeHtml);
        console.log('isHtmlPreset:', this.isHtmlPreset);
        console.log('content preview:', this.contractRawContent.substring(0, 200));
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
        const container = document.getElementById('templateHtmlContainer');
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

        const processSelector = (selector) => {
            selector = selector.trim();
            if (selector === 'body' || selector === 'html') {
                return scopeSelector;
            }
            if (selector.startsWith('body ')) {
                return scopeSelector + ' ' + selector.substring(5);
            }
            if (selector.startsWith('html ')) {
                return scopeSelector + ' ' + selector.substring(5);
            }
            return scopeSelector + ' ' + selector;
        };

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
                            .map(selector => processSelector(selector))
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
            const selectors = selector.split(',').map(s => {
                s = s.trim();
                if (s.startsWith('@') || s.includes(':root')) {
                    return s;
                }
                if (s === 'body' || s === 'html') {
                    return scopeSelector;
                }
                if (s.startsWith('body ')) {
                    return scopeSelector + ' ' + s.substring(5);
                }
                if (s.startsWith('html ')) {
                    return scopeSelector + ' ' + s.substring(5);
                }
                return scopeSelector + ' ' + s;
            });
            return selectors.join(', ') + ' {';
        });
    }

    renderHtmlContract(htmlContent) {
        console.log('=== renderHtmlContract called ===');
        console.log('htmlContent length:', htmlContent.length);

        const container = document.getElementById('templateHtmlContainer');
        if (!container) {
            console.error('Container not found!');
            return;
        }

        console.log('Container found, rendering HTML...');

        // preset-document 클래스 추가 (form.js와 동일하게)
        container.className = 'preset-document';
        container.innerHTML = ''; // Clear existing content

        const tempWrapper = document.createElement('div');
        tempWrapper.innerHTML = htmlContent;

        const styles = [];
        const styleElements = tempWrapper.querySelectorAll('style');
        styleElements.forEach(styleEl => {
            if (styleEl.textContent) {
                styles.push(this.scopeCssText(styleEl.textContent, '#templateHtmlContainer'));
            }
            styleEl.parentNode.removeChild(styleEl);
        });

        if (styles.length > 0) {
            const scopedStyle = document.createElement('style');
            scopedStyle.textContent = styles.join('\n');
            container.appendChild(scopedStyle);
        }

        const bodyContent = tempWrapper.querySelector('body') || tempWrapper;

        // Move children to container to preserve event listeners and structure
        while (bodyContent.firstChild) {
            container.appendChild(bodyContent.firstChild);
        }

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

        // Replace placeholders in text nodes only to avoid breaking HTML structure
        // But for simplicity and performance in this specific case, innerHTML replacement is acceptable 
        // if we do it carefully. However, since we just appended children, we are working with DOM.
        // Let's use a tree walker or just simple innerHTML replacement on the container *after* appending.
        // Note: innerHTML replacement destroys event listeners, but for a contract view this is usually fine.

        let finalHtml = container.innerHTML;
        Object.entries(placeholders).forEach(([key, value]) => {
            const regex = new RegExp(`\\{${key}\\}`, 'g');
            finalHtml = finalHtml.replace(regex, value);
        });

        // 서명 플레이스홀더 처리
        finalHtml = finalHtml.replace(/\[EMPLOYER_SIGNATURE_IMAGE\]/g, '<span class="text-muted small">(갑 서명 위치)</span>');
        finalHtml = finalHtml.replace(/\[EMPLOYEE_SIGNATURE_IMAGE\]/g, '<span class="text-muted small">(을 서명 위치)</span>');

        container.innerHTML = finalHtml;
        console.log('HTML rendering complete');
    }

    async submitFormWithJwt(form) {
        if (!form) {
            return;
        }

        try {
            if (window.jwtClient) {
                // JWT 클라이언트로 폼 제출
                const response = await window.jwtClient.submitFormWithAuth(form);

                if (response.ok) {
                    // 성공 시 페이지 리로드 또는 리다이렉트
                    window.location.reload();
                } else {
                    throw new Error('요청 처리 중 오류가 발생했습니다.');
                }
            } else {
                // JWT 클라이언트가 없으면 일반 폼 제출
                form.submit();
            }
        } catch (error) {
            console.error('Form submission error:', error);
            if (window.Signly && window.Signly.showAlert) {
                window.Signly.showAlert('요청 처리 중 오류가 발생했습니다.', 'danger');
            }
        }
    }

    async downloadPdf() {
        const form = document.createElement('form');
        form.method = 'post';
        form.action = `/contracts/${this.getContractId()}/pdf`;
        document.body.appendChild(form);
        await this.submitFormWithJwt(form);
    }

    resendEmail() {
        if (window.showConfirmModal) {
            window.showConfirmModal(
                '서명 요청 이메일을 다시 전송하시겠습니까?',
                async () => {
                    const form = document.createElement('form');
                    form.method = 'post';
                    form.action = `/contracts/${this.getContractId()}/resend`;
                    document.body.appendChild(form);
                    await this.submitFormWithJwt(form);
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
                async () => {
                    const form = document.createElement('form');
                    form.method = 'post';
                    form.action = `/contracts/${this.getContractId()}/cancel`;
                    document.body.appendChild(form);
                    await this.submitFormWithJwt(form);
                },
                '취소',
                '닫기',
                'btn-danger'
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

    previewContract() {
        const previewContent = document.getElementById('previewContent');
        const previewModal = document.getElementById('previewModal');

        if (!previewContent || !previewModal) {
            console.error('Preview modal or content element not found');
            return;
        }

        // 현재 계약서 컨테이너에서 HTML 가져오기
        const container = document.getElementById('templateHtmlContainer');
        if (!container) {
            previewContent.innerHTML = '<p class="text-muted">계약서 내용이 없습니다.</p>';
        } else {
            // 컨테이너 내용을 복제하여 미리보기에 표시
            previewContent.innerHTML = container.innerHTML;
        }

        // Bootstrap 모달 표시
        const modalInstance = new bootstrap.Modal(previewModal);
        modalInstance.show();
    }
}

// Global functions for onclick handlers
window.downloadPdf = function () {
    if (window.contractDetail) {
        window.contractDetail.downloadPdf();
    }
};

window.resendEmail = function () {
    if (window.contractDetail) {
        window.contractDetail.resendEmail();
    }
};

window.cancelContract = function () {
    if (window.contractDetail) {
        window.contractDetail.cancelContract();
    }
};

window.deleteContract = function () {
    if (window.contractDetail) {
        window.contractDetail.deleteContract();
    }
};

window.previewContract = function () {
    if (window.contractDetail) {
        window.contractDetail.previewContract();
    }
};

// Initialize the contract detail when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.contractDetail = new ContractDetail();
});
