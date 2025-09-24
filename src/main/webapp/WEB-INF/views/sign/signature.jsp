<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>계약서 서명 - Signly</title>

    <!-- CSS -->
    <link href="<c:url value='/css/common.css' />" rel="stylesheet">
    <link href="<c:url value='/css/signature.css' />" rel="stylesheet">

    <!-- 메타 태그 -->
    <meta name="description" content="계약서 전자서명 페이지">
    <meta name="robots" content="noindex, nofollow">
</head>
<body>
    <!-- 간단한 헤더 (서명 페이지용) -->
    <header class="header">
        <div class="header-container">
            <div class="header-logo">
                <a href="<c:url value='/' />" class="logo-link">
                    <span class="logo-text">Signly</span>
                </a>
            </div>
            <div class="header-info">
                <span class="text-muted">안전한 전자서명</span>
            </div>
        </div>
    </header>

    <!-- 메인 컨텐츠 -->
    <main class="signature-page">
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
                                <fmt:formatDate value="${contract.expiresAt}" pattern="yyyy년 MM월 dd일 HH:mm" />
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
                        <c:out value="${signerInfo.name}" />
                    </span>
                </div>
                <div class="signer-detail">
                    <span class="signer-detail-label">이메일</span>
                    <span class="signer-detail-value">
                        <c:out value="${signerInfo.email}" />
                    </span>
                </div>
                <div class="signer-detail">
                    <span class="signer-detail-label">연락처</span>
                    <span class="signer-detail-value">
                        <c:out value="${signerInfo.phone != null ? signerInfo.phone : '-'}" />
                    </span>
                </div>
                <div class="signer-detail">
                    <span class="signer-detail-label">소속</span>
                    <span class="signer-detail-value">
                        <c:out value="${signerInfo.company != null ? signerInfo.company : '-'}" />
                    </span>
                </div>
            </div>
        </div>

        <!-- 계약서 내용 -->
        <div class="contract-content">
            <h2 class="contract-title">
                <c:out value="${contract.title}" />
            </h2>
            <div class="contract-body">
                ${contract.content}
            </div>
        </div>

        <!-- 서명 패드 -->
        <div id="signaturePadContainer"></div>

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
    </main>

    <!-- 서명 확인 모달 -->
    <div class="modal fade" id="signatureConfirmModal" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">서명 확인</h5>
                </div>
                <div class="modal-body">
                    <p class="mb-3">다음 내용으로 계약서에 서명하시겠습니까?</p>

                    <div class="signature-preview mb-3">
                        <div class="signature-preview-title">서명 미리보기</div>
                        <img id="signaturePreviewImage" class="signature-preview-image" alt="서명 미리보기">
                    </div>

                    <div class="alert alert-info">
                        <strong>📝 주의사항</strong><br>
                        서명 완료 후에는 계약서를 수정하거나 취소할 수 없습니다.<br>
                        계약 내용을 다시 한 번 확인해 주세요.
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                        취소
                    </button>
                    <button type="button" class="btn btn-primary" id="finalSignBtn">
                        <span class="spinner d-none"></span>
                        서명 완료
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- JavaScript -->
    <script src="<c:url value='/js/common.js' />"></script>
    <script src="<c:url value='/js/signature-canvas.js' />"></script>

    <script>
        // 계약서 정보
        const contractData = {
            id: '<c:out value="${contract.id}" />',
            token: '<c:out value="${signToken}" />',
            title: '<c:out value="${contract.title}" />',
            signerName: '<c:out value="${signerInfo.name}" />',
            signerEmail: '<c:out value="${signerInfo.email}" />'
        };

        // 서명 패드 초기화
        let signaturePad;

        document.addEventListener('DOMContentLoaded', function() {
            initSignaturePad();
            bindEvents();
        });

        /**
         * 서명 패드 초기화
         */
        function initSignaturePad() {
            const container = document.getElementById('signaturePadContainer');

            signaturePad = new SignaturePad(container, {
                title: '전자서명을 입력해 주세요',
                clearButtonText: '다시 서명',
                submitButtonText: '서명 확인',
                required: true
            });

            // 서명 패드 이벤트
            container.addEventListener('signatureSubmit', handleSignatureSubmit);
            container.addEventListener('signatureClear', handleSignatureClear);
        }

        /**
         * 이벤트 바인딩
         */
        function bindEvents() {
            // 서명 완료 버튼
            document.getElementById('completeSigningBtn').addEventListener('click', function() {
                if (signaturePad && !signaturePad.isEmpty()) {
                    showSignatureConfirmModal();
                } else {
                    Signly.showAlert('서명을 입력해 주세요.', 'warning');
                }
            });

            // 최종 서명 버튼
            document.getElementById('finalSignBtn').addEventListener('click', submitSignature);
        }

        /**
         * 서명 제출 핸들러
         */
        function handleSignatureSubmit(event) {
            const signatureData = event.detail.signatureData;

            if (signatureData) {
                document.getElementById('completeSigningBtn').disabled = false;
                Signly.showAlert('서명이 입력되었습니다. 서명 완료 버튼을 클릭해 주세요.', 'success');
            }
        }

        /**
         * 서명 지우기 핸들러
         */
        function handleSignatureClear() {
            document.getElementById('completeSigningBtn').disabled = true;
        }

        /**
         * 서명 확인 모달 표시
         */
        function showSignatureConfirmModal() {
            const signatureData = signaturePad.getSignatureData();

            if (signatureData) {
                document.getElementById('signaturePreviewImage').src = signatureData;

                // 모달 표시 (Bootstrap 또는 커스텀 모달)
                const modal = document.getElementById('signatureConfirmModal');
                modal.classList.add('show');
                modal.style.display = 'block';
                document.body.classList.add('modal-open');
            }
        }

        /**
         * 서명 제출
         */
        async function submitSignature() {
            const finalSignBtn = document.getElementById('finalSignBtn');
            const spinner = finalSignBtn.querySelector('.spinner');

            try {
                // 로딩 시작
                finalSignBtn.disabled = true;
                spinner.classList.remove('d-none');
                finalSignBtn.innerHTML = '<span class="spinner"></span> 서명 처리중...';

                const signatureData = signaturePad.getSignatureData();

                // 서명 데이터 전송
                const response = await Signly.sendRequest('/api/sign/' + contractData.token + '/sign', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        signatureData: signatureData,
                        signerName: contractData.signerName,
                        signerEmail: contractData.signerEmail,
                        clientInfo: {
                            userAgent: navigator.userAgent,
                            timestamp: new Date().toISOString(),
                            timezone: Intl.DateTimeFormat().resolvedOptions().timeZone
                        }
                    })
                });

                if (response.success) {
                    // 성공 시 완료 페이지로 이동
                    window.location.href = '/sign/' + contractData.token + '/complete';
                } else {
                    throw new Error(response.message || '서명 처리 중 오류가 발생했습니다.');
                }

            } catch (error) {
                console.error('Signature submission error:', error);
                Signly.showAlert(error.message || '서명 처리 중 오류가 발생했습니다.', 'danger');

                // 로딩 종료
                finalSignBtn.disabled = false;
                spinner.classList.add('d-none');
                finalSignBtn.textContent = '서명 완료';
            }
        }

        /**
         * 모달 닫기
         */
        function closeModal() {
            const modal = document.getElementById('signatureConfirmModal');
            modal.classList.remove('show');
            modal.style.display = 'none';
            document.body.classList.remove('modal-open');
        }

        // 모달 백드롭 클릭 시 닫기
        document.getElementById('signatureConfirmModal').addEventListener('click', function(e) {
            if (e.target === this) {
                closeModal();
            }
        });

        // ESC 키로 모달 닫기
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                closeModal();
            }
        });

        // 페이지 이탈 경고
        window.addEventListener('beforeunload', function(e) {
            if (signaturePad && !signaturePad.isEmpty()) {
                e.preventDefault();
                e.returnValue = '서명이 완료되지 않았습니다. 페이지를 떠나시겠습니까?';
            }
        });
    </script>

    <style>
        /* 간단한 모달 스타일 (Bootstrap 대신) */
        .modal {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.5);
            z-index: 1050;
            display: none;
        }

        .modal.show {
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .modal-dialog {
            max-width: 500px;
            width: 90%;
        }

        .modal-content {
            background-color: white;
            border-radius: var(--border-radius-lg);
            box-shadow: var(--box-shadow-lg);
            overflow: hidden;
        }

        .modal-header {
            padding: 1.5rem;
            border-bottom: 1px solid var(--gray-200);
        }

        .modal-title {
            font-size: var(--font-size-lg);
            font-weight: 600;
            margin: 0;
        }

        .modal-body {
            padding: 1.5rem;
        }

        .modal-footer {
            padding: 1rem 1.5rem;
            border-top: 1px solid var(--gray-200);
            display: flex;
            gap: 1rem;
            justify-content: flex-end;
        }

        .info-item {
            margin-bottom: 1rem;
        }

        .info-label {
            display: block;
            font-size: var(--font-size-sm);
            color: var(--gray-600);
            margin-bottom: 0.25rem;
        }

        .info-value {
            font-weight: 500;
            color: var(--gray-800);
        }

        .row {
            display: flex;
            flex-wrap: wrap;
            margin: -0.5rem;
        }

        .col-md-6 {
            flex: 0 0 50%;
            padding: 0.5rem;
        }

        @media (max-width: 768px) {
            .col-md-6 {
                flex: 0 0 100%;
            }
        }
    </style>
</body>
</html>