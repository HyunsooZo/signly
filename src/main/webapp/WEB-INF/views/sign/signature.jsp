<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

            <!DOCTYPE html>
            <html lang="ko">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta http-equiv="X-UA-Compatible" content="ie=edge">
                <title>계약서 서명 - Deally</title>

                <!-- CSS -->
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
                <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css"
                    rel="stylesheet">
                <link rel="preconnect" href="https://fonts.googleapis.com">
                <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                <link
                    href="https://fonts.googleapis.com/css2?family=Lobster&family=Noto+Sans+KR:wght@400;500;700&display=swap"
                    rel="stylesheet">
                <link href="<c:url value='/css/common.css' />" rel="stylesheet">
                <link href="<c:url value='/css/contract-common.css' />" rel="stylesheet">
                <link href="<c:url value='/css/signature.css' />" rel="stylesheet">

                <!-- 서명 페이지 전용 스타일 - navbar 메뉴만 숨김 -->
                <style>
                    .navbar-nav {
                        display: none !important;
                    }

                    .navbar-toggler {
                        display: none !important;
                    }
                </style>

                <!-- 메타 태그 -->
                <meta name="description" content="계약서 전자서명 페이지">
                <meta name="robots" content="noindex, nofollow">

            </head>

            <body>
                <jsp:include page="../common/navbar.jsp">
                    <jsp:param name="currentPage" value="" />
                </jsp:include>

                <!-- 메인 컨텐츠 -->
                <div class="container mt-4">
                    <div class="main-content-card">
                        <!-- 서명 단계 표시 -->
                        <div class="signature-steps">
                            <div class="signature-step completed">
                                <div class="signature-step-icon">✓</div>
                                <span>계약서 확인</span>
                            </div>
                            <div class="signature-step-connector"></div>
                            <div class="signature-step active">
                                <div class="signature-step-icon">2</div>
                                <span>전자서명</span>
                            </div>
                            <div class="signature-step-connector"></div>
                            <div class="signature-step pending">
                                <div class="signature-step-icon">3</div>
                                <span>완료</span>
                            </div>
                        </div>

                        <!-- 페이지 헤더 -->
                        <div class="signature-page-header">
                            <h1 class="signature-page-title">계약서에 서명해 주세요</h1>
                            <p class="signature-page-subtitle">
                                아래 계약서 내용을 확인하신 후, 전자서명을 진행해 주세요.
                            </p>
                        </div>

                        <!-- 계약서 정보 -->
                        <div class="contract-info card mb-4">
                            <div class="card-header">
                                <h3>계약서 정보</h3>
                            </div>
                            <div class="card-body">
                                <div class="row">
                                    <div class="col-md-6">
                                        <div class="info-item">
                                            <label class="info-label">계약서 제목</label>
                                            <div class="info-value">
                                                <c:out value="${contract.title}" />
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <div class="info-item">
                                            <label class="info-label">계약 당사자</label>
                                            <div class="info-value">
                                                <c:out value="${contract.firstParty.name}" /> ↔
                                                <c:out value="${contract.secondParty.name}" />
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <div class="info-item">
                                            <label class="info-label">계약서 생성일</label>
                                            <div class="info-value">
                                                <fmt:formatDate value="${contract.createdAt}" pattern="yyyy년 MM월 dd일" />
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <div class="info-item">
                                            <label class="info-label">서명 만료일</label>
                                            <div class="info-value text-warning">
                                                <fmt:formatDate value="${contract.expiresAt}"
                                                    pattern="yyyy년 MM월 dd일 HH:mm" />
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- 서명자 정보 -->
                        <div class="signer-info">
                            <h3 class="signer-info-title">서명자 정보</h3>
                            <div class="signer-details">
                                <div class="signer-detail">
                                    <span class="signer-detail-label">서명자 이름</span>
                                    <span class="signer-detail-value">
                                        <c:out value="${contract.secondParty.name}" />
                                    </span>
                                </div>
                                <div class="signer-detail">
                                    <span class="signer-detail-label">이메일</span>
                                    <span class="signer-detail-value">
                                        <c:out value="${contract.secondParty.email}" />
                                    </span>
                                </div>
                                <div class="signer-detail">
                                    <span class="signer-detail-label">연락처</span>
                                    <span class="signer-detail-value">
                                        -
                                    </span>
                                </div>
                                <div class="signer-detail">
                                    <span class="signer-detail-label">소속</span>
                                    <span class="signer-detail-value">
                                        <c:out
                                            value="${empty contract.secondParty.organizationName ? '-' : contract.secondParty.organizationName}" />
                                    </span>
                                </div>
                            </div>
                        </div>

                        <!-- 계약서 내용 -->
                        <div class="contract-content-wrapper">
                            <div class="contract-content">
                                <h2 class="contract-title">
                                    <c:out value="${contract.title}" />
                                </h2>
                                <div class="contract-body contract-content--html" id="contractContentContainer"
                                    style="background-color: white; color: black; padding: 2rem;">
                                    <c:choose>
                                        <c:when test="${not empty processedContent}">
                                            ${processedContent}
                                        </c:when>
                                        <c:otherwise>
                                            ${contract.content}
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>

                        <!-- 서명 패드 -->
                        <div id="signaturePadContainer">
                            <div class="signature-pad">
                                <div class="signature-header">
                                    <h3 class="signature-title">전자서명을 입력해 주세요</h3>
                                    <span class="signature-required">*</span>
                                </div>
                                <div class="signature-canvas-container">
                                    <canvas class="signature-canvas"></canvas>
                                    <div class="signature-placeholder">여기에 서명해 주세요</div>
                                </div>
                                <div class="signature-controls">
                                    <button type="button" class="btn btn-secondary signature-clear">
                                        다시 서명
                                    </button>
                                </div>
                                <div class="signature-error" style="display: none;"></div>
                            </div>
                        </div>

                        <!-- 서명 완료 액션 -->
                        <div class="signature-actions text-center mt-4">
                            <button type="button" class="btn btn-secondary btn-lg me-3" onclick="history.back()">
                                <i class="icon-back"></i>
                                이전으로
                            </button>
                            <button type="button" class="btn btn-primary btn-lg" id="completeSigningBtn" disabled>
                                <i class="icon-signature"></i>
                                서명 완료
                            </button>
                        </div>

                        <!-- 법적 고지 -->
                        <div class="legal-notice mt-5 p-4 bg-light rounded">
                            <h4 class="text-primary mb-3">📋 전자서명 법적 효력 안내</h4>
                            <ul class="list-unstyled mb-0">
                                <li class="mb-2">
                                    <strong>✓ 법적 효력:</strong>
                                    본 전자서명은 「전자서명법」에 따라 서면서명과 동일한 법적 효력을 갖습니다.
                                </li>
                                <li class="mb-2">
                                    <strong>✓ 본인 확인:</strong>
                                    서명 시 IP주소, 접속시간, 기기정보가 자동으로 기록됩니다.
                                </li>
                                <li class="mb-2">
                                    <strong>✓ 위조 방지:</strong>
                                    블록체인 기술을 통해 서명의 무결성이 보장됩니다.
                                </li>
                                <li>
                                    <strong>✓ 보관 기간:</strong>
                                    서명된 계약서는 법정 보관기간에 따라 안전하게 보관됩니다.
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>

                <!-- 서명 최종 확인 모달 -->
                <div class="modal fade" id="signatureConfirmModal" tabindex="-1">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h5 class="modal-title">서명 최종 확인</h5>
                            </div>
                            <div class="modal-body">
                                <p class="mb-3">작성하신 서명을 확인해 주세요.</p>

                                <div class="signature-preview-container">
                                    <img id="signaturePreviewImage" class="signature-preview-image" alt="서명 미리보기">
                                </div>

                                <div class="alert alert-warning">
                                    <strong>⚠️ 주의사항</strong><br>
                                    서명을 제출하면 계약서를 수정하거나 취소할 수 없습니다.<br>
                                    서명 내용과 계약서를 다시 한 번 확인해 주세요.
                                </div>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-secondary" id="cancelSignBtn">
                                    취소
                                </button>
                                <button type="button" class="btn btn-primary" id="finalSignBtn">
                                    <span class="spinner d-none"></span>
                                    확인 및 제출
                                </button>
                            </div>
                        </div>
                    </div>
                </div>

                <div id="signingData" hidden data-contract-id="<c:out value='${contract.id}'/>"
                    data-token="<c:out value='${token}'/>" data-contract-title="<c:out value='${contract.title}'/>"
                    data-signer-name="<c:out value='${contract.secondParty.name}'/>"
                    data-signer-email="<c:out value='${contract.secondParty.email}'/>">
                </div>

                <!-- JavaScript -->
                <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
                <script src="<c:url value='/js/common.js' />"></script>
                <script src="https://cdn.jsdelivr.net/npm/signature_pad@4.1.5/dist/signature_pad.umd.min.js"></script>
                <script src="/js/signature.js"></script>
                <script>
                    // Initialize CSRF tokens and SignatureManager
                    document.addEventListener('DOMContentLoaded', () => {
                        console.log('[JSP] DOMContentLoaded - Setting CSRF tokens');


                        console.log('[JSP] Creating SignatureManager');
                        window.signatureManager = new SignatureManager();
                    });
                </script>

            </body>

            </html>