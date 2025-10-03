<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle} - Signly</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <link href="/css/common.css" rel="stylesheet">
    <link href="/css/contracts.css" rel="stylesheet">
</head>
<body>
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
        <div class="container">
            <a class="navbar-brand" href="/home">
                <i class="bi bi-file-earmark-text me-2"></i>Signly
            </a>
            <div class="navbar-nav ms-auto">
                <a class="nav-link" href="/home">대시보드</a>
                <a class="nav-link" href="/templates">템플릿</a>
                <a class="nav-link active" href="/contracts">계약서</a>
                <a class="nav-link" href="/profile/signature">서명 관리</a>
                <a class="nav-link" href="/logout">로그아웃</a>
            </div>
        </div>
    </nav>

    <div class="container mt-4">
        <div class="row">
            <div class="col-12">
                <div class="d-flex justify-content-between align-items-start mb-4">
                    <div>
                        <h2 class="mb-2">
                            <i class="bi bi-file-earmark-check text-primary me-2"></i>
                            ${contract.title}
                        </h2>
                        <div class="d-flex align-items-center gap-3">
                            <c:choose>
                                <c:when test="${contract.status == 'DRAFT'}">
                                    <span class="badge bg-secondary fs-6">초안</span>
                                </c:when>
                                <c:when test="${contract.status == 'PENDING'}">
                                    <span class="badge bg-warning fs-6">서명 대기</span>
                                </c:when>
                                <c:when test="${contract.status == 'SIGNED'}">
                                    <span class="badge bg-success fs-6">서명 완료</span>
                                </c:when>
                                <c:when test="${contract.status == 'COMPLETED'}">
                                    <span class="badge bg-primary fs-6">완료</span>
                                </c:when>
                                <c:when test="${contract.status == 'CANCELLED'}">
                                    <span class="badge bg-danger fs-6">취소</span>
                                </c:when>
                                <c:when test="${contract.status == 'EXPIRED'}">
                                    <span class="badge bg-dark fs-6">만료</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-light text-dark fs-6">${contract.status}</span>
                                </c:otherwise>
                            </c:choose>
                            <small class="text-muted">
                                생성일: <fmt:formatDate value="${contract.createdAt}" pattern="yyyy-MM-dd HH:mm"/>
                            </small>
                            <small class="text-muted">
                                수정일: <fmt:formatDate value="${contract.updatedAt}" pattern="yyyy-MM-dd HH:mm"/>
                            </small>
                        </div>
                    </div>
                    <a href="/contracts" class="btn btn-outline-secondary">
                        <i class="bi bi-arrow-left me-2"></i>목록으로
                    </a>
                </div>

                <!-- 알림 메시지 -->
                <c:if test="${not empty successMessage}">
                    <div class="alert alert-success alert-dismissible fade show" role="alert">
                        <i class="bi bi-check-circle me-2"></i>${successMessage}
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                </c:if>

                <c:if test="${not empty errorMessage}">
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        <i class="bi bi-exclamation-triangle me-2"></i>${errorMessage}
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                </c:if>
            </div>
        </div>

        <div class="row">
            <!-- 계약서 내용 -->
            <div class="col-lg-8">
                <div class="card">
                    <div class="card-header">
                        <h5 class="card-title mb-0">
                            <i class="bi bi-file-earmark-text me-2"></i>계약서 내용
                        </h5>
                    </div>
                    <div class="card-body p-0">
                        <c:choose>
                            <c:when test="${contract.presetType ne null and contract.presetType ne 'NONE'}">
                                <div class="contract-content contract-content--html" id="contractContentHtmlContainer"></div>
                            </c:when>
                            <c:otherwise>
                                <div class="contract-content">${contract.content}</div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <!-- 서명 정보 -->
                <c:if test="${not empty contract.signatures}">
                    <div class="card mt-4">
                        <div class="card-header">
                            <h5 class="card-title mb-0">
                                <i class="bi bi-pen me-2"></i>서명 정보
                            </h5>
                        </div>
                        <div class="card-body">
                            <c:forEach var="signature" items="${contract.signatures}">
                                <div class="signature-info">
                                    <div class="d-flex justify-content-between align-items-start">
                                        <div>
                                            <h6 class="mb-1">${signature.signerName}</h6>
                                            <p class="text-muted mb-1">${signature.signerEmail}</p>
                                            <small class="text-muted">
                                                서명일시: <fmt:formatDate value="${signature.signedAt}" pattern="yyyy-MM-dd HH:mm:ss"/>
                                            </small>
                                        </div>
                                        <div class="text-end">
                                            <span class="badge bg-success">서명 완료</span>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </c:if>
            </div>

            <!-- 사이드바: 정보 및 작업 -->
            <div class="col-lg-4">
                <div class="action-buttons">
                    <!-- 작업 버튼들 -->
                    <div class="card mb-3">
                        <div class="card-header">
                            <h6 class="card-title mb-0">
                                <i class="bi bi-gear me-2"></i>작업
                            </h6>
                        </div>
                        <div class="card-body">
                            <div class="d-grid gap-2">
                                <c:if test="${contract.status == 'DRAFT'}">
                                    <a href="/contracts/${contract.id}/edit" class="btn btn-primary">
                                        <i class="bi bi-pencil me-2"></i>수정
                                    </a>
                                    <button type="button" class="btn btn-success" onclick="sendForSigning()">
                                        <i class="bi bi-send me-2"></i>서명 요청 전송
                                    </button>
                                    <hr>
                                    <button type="button" class="btn btn-outline-danger" onclick="deleteContract()">
                                        <i class="bi bi-trash me-2"></i>삭제
                                    </button>
                                </c:if>

                                <c:if test="${contract.status == 'PENDING'}">
                                    <button type="button" class="btn btn-outline-info" onclick="resendSigningEmail()">
                                        <i class="bi bi-arrow-repeat me-2"></i>서명 요청 재전송
                                    </button>
                                </c:if>

                                <c:if test="${contract.status == 'PENDING' or contract.status == 'SIGNED'}">
                                    <button type="button" class="btn btn-outline-warning" onclick="cancelContract()">
                                        <i class="bi bi-x-circle me-2"></i>계약 취소
                                    </button>
                                </c:if>

                                <button type="button" class="btn btn-outline-primary" onclick="previewContract()">
                                    <i class="bi bi-eye me-2"></i>미리보기
                                </button>

                                <c:if test="${contract.status == 'SIGNED'}">
                                    <button type="button" class="btn btn-primary" onclick="completeContract()">
                                        <i class="bi bi-check-circle me-2"></i>계약 완료
                                    </button>
                                </c:if>

                                <c:if test="${contract.status == 'PENDING' or contract.status == 'SIGNED'}">
                                    <a href="/sign/${contract.id}" class="btn btn-outline-success" target="_blank">
                                        <i class="bi bi-pen me-2"></i>서명 페이지 보기
                                    </a>
                                </c:if>
                            </div>
                        </div>
                    </div>

                    <!-- 계약서 정보 -->
                    <div class="card mb-3">
                        <div class="card-header">
                            <h6 class="card-title mb-0">
                                <i class="bi bi-info-circle me-2"></i>계약서 정보
                            </h6>
                        </div>
                        <div class="card-body p-0">
                            <div class="contract-info">
                                <div class="info-item">
                                    <span class="fw-medium">계약서 ID:</span>
                                    <span class="text-muted">${contract.id}</span>
                                </div>
                                <div class="info-item">
                                    <span class="fw-medium">템플릿 ID:</span>
                                    <span class="text-muted">${contract.templateId}</span>
                                </div>
                                <div class="info-item">
                                    <span class="fw-medium">상태:</span>
                                    <c:choose>
                                        <c:when test="${contract.status == 'DRAFT'}">
                                            <span class="badge bg-secondary">초안</span>
                                        </c:when>
                                        <c:when test="${contract.status == 'PENDING'}">
                                            <span class="badge bg-warning">서명 대기</span>
                                        </c:when>
                                        <c:when test="${contract.status == 'SIGNED'}">
                                            <span class="badge bg-success">서명 완료</span>
                                        </c:when>
                                        <c:when test="${contract.status == 'COMPLETED'}">
                                            <span class="badge bg-primary">완료</span>
                                        </c:when>
                                        <c:when test="${contract.status == 'CANCELLED'}">
                                            <span class="badge bg-danger">취소</span>
                                        </c:when>
                                        <c:when test="${contract.status == 'EXPIRED'}">
                                            <span class="badge bg-dark">만료</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-light text-dark">${contract.status}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="info-item">
                                    <span class="fw-medium">생성일:</span>
                                    <span class="text-muted">
                                        <fmt:formatDate value="${contract.createdAt}" pattern="yyyy-MM-dd HH:mm"/>
                                    </span>
                                </div>
                                <div class="info-item">
                                    <span class="fw-medium">수정일:</span>
                                    <span class="text-muted">
                                        <fmt:formatDate value="${contract.updatedAt}" pattern="yyyy-MM-dd HH:mm"/>
                                    </span>
                                </div>
                                <c:if test="${not empty contract.expiresAt}">
                                    <div class="info-item">
                                        <span class="fw-medium">만료일:</span>
                                        <span class="text-muted">
                                            <fmt:formatDate value="${contract.expiresAt}" pattern="yyyy-MM-dd HH:mm"/>
                                        </span>
                                    </div>
                                </c:if>
                            </div>
                        </div>
                    </div>

                    <!-- 당사자 정보 -->
                    <div class="card">
                        <div class="card-header">
                            <h6 class="card-title mb-0">
                                <i class="bi bi-people me-2"></i>당사자 정보
                            </h6>
                        </div>
                        <div class="card-body p-0">
                            <div class="contract-info">
                                <div class="mb-3">
                                    <h6 class="text-primary mb-2">갑 (첫 번째 당사자)</h6>
                                    <div class="info-item">
                                        <span class="fw-medium">이름:</span>
                                        <span class="text-muted">${contract.firstParty.name}</span>
                                    </div>
                                    <div class="info-item">
                                        <span class="fw-medium">이메일:</span>
                                        <span class="text-muted">${contract.firstParty.email}</span>
                                    </div>
                                    <c:if test="${not empty contract.firstParty.organizationName}">
                                        <div class="info-item">
                                            <span class="fw-medium">회사/조직:</span>
                                            <span class="text-muted">${contract.firstParty.organizationName}</span>
                                        </div>
                                    </c:if>
                                </div>
                                <div>
                                    <h6 class="text-success mb-2">을 (두 번째 당사자)</h6>
                                    <div class="info-item">
                                        <span class="fw-medium">이름:</span>
                                        <span class="text-muted">${contract.secondParty.name}</span>
                                    </div>
                                    <div class="info-item">
                                        <span class="fw-medium">이메일:</span>
                                        <span class="text-muted">${contract.secondParty.email}</span>
                                    </div>
                                    <c:if test="${not empty contract.secondParty.organizationName}">
                                        <div class="info-item">
                                            <span class="fw-medium">회사/조직:</span>
                                            <span class="text-muted">${contract.secondParty.organizationName}</span>
                                        </div>
                                    </c:if>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- 미리보기 모달 -->
    <div class="modal fade contract-preview-modal" id="previewModal" tabindex="-1">
        <div class="modal-dialog modal-fullscreen-lg-down">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">
                        <i class="bi bi-eye me-2"></i>계약서 미리보기
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body p-4">
                    <div class="alert alert-info">
                        <i class="bi bi-info-circle me-2"></i>
                        아래는 현재 계약서 내용의 미리보기입니다.
                    </div>
                    <div class="border rounded p-4 contract-preview-modal-content" id="previewContent">
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
                </div>
            </div>
        </div>
    </div>

    <!-- 삭제 확인 모달 -->
    <div class="modal fade" id="deleteModal" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">계약서 삭제 확인</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <p>정말로 '<strong>${contract.title}</strong>' 계약서를 삭제하시겠습니까?</p>
                    <p class="text-danger">
                        <i class="bi bi-exclamation-triangle me-2"></i>
                        이 작업은 되돌릴 수 없습니다.
                    </p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">취소</button>
                    <form id="deleteForm" method="post" action="/contracts/${contract.id}/delete" class="d-inline">
                        <c:if test="${not empty _csrf}">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                        </c:if>
                        <button type="submit" class="btn btn-danger">삭제</button>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <div id="contractPreviewData" hidden
         data-first-party-name="<c:out value='${contract.firstParty.name}'/>"
         data-first-party-email="<c:out value='${contract.firstParty.email}'/>"
         data-first-party-org="<c:out value='${empty contract.firstParty.organizationName ? "-" : contract.firstParty.organizationName}'/>"
         data-second-party-name="<c:out value='${contract.secondParty.name}'/>"
         data-second-party-email="<c:out value='${contract.secondParty.email}'/>"
         data-second-party-org="<c:out value='${empty contract.secondParty.organizationName ? "-" : contract.secondParty.organizationName}'/>"
         data-contract-title="<c:out value='${contract.title}'/>"
         data-preset-type="<c:out value='${contract.presetType}'/>">
    </div>

    <textarea id="contractContentHtml" hidden>${fn:escapeXml(contract.content)}</textarea>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        const previewDataElement = document.getElementById('contractPreviewData');
        const previewData = previewDataElement ? previewDataElement.dataset : {};
        const contractContentTextarea = document.getElementById('contractContentHtml');
        const contractRawContent = contractContentTextarea ? contractContentTextarea.value : '';
        const presetTypeValue = (previewData.presetType || '').toUpperCase();
        const isHtmlPreset = presetTypeValue && presetTypeValue !== 'NONE';

        if (isHtmlPreset) {
            renderHtmlContract(contractRawContent);
        } else {
            ensurePlainTextContract();
        }

        const previewDefaults = {
            firstPartyName: previewData.firstPartyName || '-',
            firstPartyEmail: previewData.firstPartyEmail || '-',
            firstPartyOrg: previewData.firstPartyOrg || '-',
            secondPartyName: previewData.secondPartyName || '-',
            secondPartyEmail: previewData.secondPartyEmail || '-',
            secondPartyOrg: previewData.secondPartyOrg || '-',
            contractTitle: previewData.contractTitle || '-'
        };

        const csrfParam = '${_csrf.parameterName}';
        const csrfToken = '${_csrf.token}';

        function ensurePlainTextContract() {
            const contentEl = document.querySelector('.contract-content');
            if (!contentEl) {
                return;
            }
            contentEl.style.whiteSpace = 'pre-wrap';
            contentEl.style.fontFamily = "'Times New Roman', serif";
        }

        function scopeCssText(cssText, scopeSelector) {
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
                    return naiveScopeCss(cssText, scopeSelector);
                }

                const processRules = rules => {
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
                return naiveScopeCss(cssText, scopeSelector);
            } finally {
                if (tempStyle && tempStyle.parentNode) {
                    tempStyle.parentNode.removeChild(tempStyle);
                }
            }
        }

        function naiveScopeCss(cssText, scopeSelector) {
            return cssText
                .split('}')
                .map(ruleText => {
                    const index = ruleText.indexOf('{');
                    if (index === -1) {
                        return '';
                    }
                    const selectorPart = ruleText.slice(0, index).trim();
                    const declarationPart = ruleText.slice(index + 1).trim();
                    if (!selectorPart || !declarationPart) {
                        return '';
                    }
                    if (selectorPart.startsWith('@')) {
                        return selectorPart + ' { ' + declarationPart + ' }';
                    }
                    const scopedSelectors = selectorPart
                        .split(',')
                        .map(selector => scopeSelector + ' ' + selector.trim())
                        .join(', ');
                    return scopedSelectors + ' { ' + declarationPart + ' }';
                })
                .filter(Boolean)
                .join('\n');
        }

        function renderHtmlContract(rawHtml) {
            const container = document.getElementById('contractContentHtmlContainer');
            if (!container) {
                return;
            }

            const working = document.createElement('div');
            working.innerHTML = rawHtml || '';

            const scopedStyles = [];
            const collectedLinks = [];

            working.querySelectorAll('script').forEach(node => node.remove());

            working.querySelectorAll('style').forEach(node => {
                const scoped = scopeCssText(node.textContent || '', '#contractContentHtmlContainer');
                if (scoped) {
                    scopedStyles.push(scoped);
                }
                node.remove();
            });

            working.querySelectorAll('link[rel="stylesheet"]').forEach(node => {
                const href = node.getAttribute('href');
                if (href && href.trim()) {
                    collectedLinks.push(href.trim());
                }
                node.remove();
            });

            const styleElementId = 'contractContentHtmlScopedStyles';
            const existingScopedStyle = document.getElementById(styleElementId);
            if (existingScopedStyle) {
                existingScopedStyle.remove();
            }

            document.querySelectorAll('link[data-contract-content-style="true"]').forEach(linkEl => linkEl.remove());

            const bodyTag = working.querySelector('body');
            const htmlTag = working.querySelector('html');

            let htmlToRender = '';
            if (bodyTag && bodyTag.innerHTML.trim()) {
                htmlToRender = bodyTag.innerHTML;
            } else if (htmlTag && htmlTag.innerHTML.trim()) {
                htmlToRender = htmlTag.innerHTML;
            } else {
                htmlToRender = working.innerHTML;
            }

            container.innerHTML = htmlToRender;
            container.style.whiteSpace = 'normal';
            container.style.fontFamily = "'Times New Roman', serif";

            container.querySelectorAll('table').forEach(table => {
                table.classList.add('table', 'table-bordered');
                table.style.width = '100%';
            });

            if (scopedStyles.length) {
                const scopedStyleEl = document.createElement('style');
                scopedStyleEl.id = styleElementId;
                scopedStyleEl.textContent = scopedStyles.join('\n');
                document.head.appendChild(scopedStyleEl);
            }

            collectedLinks.forEach((href, index) => {
                const linkEl = document.createElement('link');
                linkEl.rel = 'stylesheet';
                linkEl.href = href;
                linkEl.setAttribute('data-contract-content-style', 'true');
                linkEl.setAttribute('data-contract-style-index', String(index));
                document.head.appendChild(linkEl);
            });
        }

        function appendCsrfField(form) {
            if (!csrfParam || !csrfToken) {
                return;
            }
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = csrfParam;
            input.value = csrfToken;
            form.appendChild(input);
        }

        function previewContract() {
            const content = contractRawContent;
            const presetType = presetTypeValue || 'NONE';
            const previewContentEl = document.getElementById('previewContent');

            // 프리셋인 경우 HTML로 렌더링, 아니면 텍스트로 표시
            if (presetType !== 'NONE' && presetType !== '') {
                // 임시 DOM에서 style 태그 제거
                const tempDiv = document.createElement('div');
                tempDiv.innerHTML = content;

                // 모든 style 태그 제거
                const styleTags = tempDiv.querySelectorAll('style');
                styleTags.forEach(tag => tag.remove());

                // body 태그를 div로 변경
                const bodyTags = tempDiv.querySelectorAll('body');
                bodyTags.forEach(tag => {
                    const div = document.createElement('div');
                    div.innerHTML = tag.innerHTML;
                    tag.parentNode.replaceChild(div, tag);
                });

                previewContentEl.innerHTML = tempDiv.innerHTML;
                previewContentEl.style.whiteSpace = 'normal';
                previewContentEl.style.fontFamily = 'inherit';
            } else {
                // 일반 계약서는 변수 치환 후 텍스트로 표시
                let previewContent = content
                    .replace(/\{FIRST_PARTY_NAME\}/g, previewDefaults.firstPartyName)
                    .replace(/\{FIRST_PARTY_EMAIL\}/g, previewDefaults.firstPartyEmail)
                    .replace(/\{FIRST_PARTY_ADDRESS\}/g, previewDefaults.firstPartyOrg)
                    .replace(/\{SECOND_PARTY_NAME\}/g, previewDefaults.secondPartyName)
                    .replace(/\{SECOND_PARTY_EMAIL\}/g, previewDefaults.secondPartyEmail)
                    .replace(/\{SECOND_PARTY_ADDRESS\}/g, previewDefaults.secondPartyOrg)
                    .replace(/\{CONTRACT_TITLE\}/g, previewDefaults.contractTitle)
                    .replace(/\{CONTRACT_DATE\}/g, new Date().toLocaleDateString('ko-KR'))
                    .replace(/\{SIGNATURE_FIRST\}/g, '[갑 서명]')
                    .replace(/\{SIGNATURE_SECOND\}/g, '[을 서명]');

                previewContentEl.textContent = previewContent;
                previewContentEl.style.whiteSpace = 'pre-wrap';
                previewContentEl.style.fontFamily = "'Times New Roman', serif";
            }

            new bootstrap.Modal(document.getElementById('previewModal')).show();
        }

        // 모달 닫을 때 혹시 삽입된 style 정리
        document.getElementById('previewModal').addEventListener('hidden.bs.modal', function () {
            // 모달 내부의 모든 style 태그 제거
            const modalStyles = this.querySelectorAll('style');
            modalStyles.forEach(style => style.remove());
        });

        function sendForSigning() {
            if (confirm('계약서 서명 요청을 전송하시겠습니까?')) {
                const form = document.createElement('form');
                form.method = 'post';
                form.action = '/contracts/${contract.id}/send';
                appendCsrfField(form);
                document.body.appendChild(form);
                form.submit();
            }
        }

        function resendSigningEmail() {
            if (confirm('서명 요청 이메일을 다시 전송하시겠습니까?')) {
                const form = document.createElement('form');
                form.method = 'post';
                form.action = '/contracts/${contract.id}/resend';
                appendCsrfField(form);
                document.body.appendChild(form);
                form.submit();
            }
        }

        function cancelContract() {
            if (confirm('계약서를 취소하시겠습니까? 취소된 계약서는 더 이상 서명할 수 없습니다.')) {
                const form = document.createElement('form');
                form.method = 'post';
                form.action = '/contracts/${contract.id}/cancel';
                appendCsrfField(form);
                document.body.appendChild(form);
                form.submit();
            }
        }

        function completeContract() {
            if (confirm('계약을 완료하시겠습니까?')) {
                const form = document.createElement('form');
                form.method = 'post';
                form.action = '/contracts/${contract.id}/complete';
                appendCsrfField(form);
                document.body.appendChild(form);
                form.submit();
            }
        }

        function deleteContract() {
            new bootstrap.Modal(document.getElementById('deleteModal')).show();
        }
    </script>
</body>
</html>
