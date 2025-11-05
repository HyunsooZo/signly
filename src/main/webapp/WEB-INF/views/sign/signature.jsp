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
    <link href="<c:url value='/css/contract-common.css' />" rel="stylesheet">
    <link href="<c:url value='/css/signature.css' />" rel="stylesheet">

    <!-- 메타 태그 -->
    <meta name="description" content="계약서 전자서명 페이지">
    <meta name="robots" content="noindex, nofollow">
</head>
<body class="signature-view">
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
                        <c:out value="${empty contract.secondParty.organizationName ? '-' : contract.secondParty.organizationName}" />
                    </span>
                </div>
            </div>
        </div>

        <!-- 계약서 내용 -->
        <div class="contract-content">
            <h2 class="contract-title">
                <c:out value="${contract.title}" />
            </h2>
            <div class="contract-body contract-content--html" id="contractContentContainer" style="background-color: white; color: black; padding: 2rem;">
                ${contract.content}
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
                    <button type="button" class="btn btn-primary signature-submit" disabled>
                        서명 확인
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

                    <div class="signature-preview-container">
                        <img id="signaturePreviewImage" class="signature-preview-image" alt="서명 미리보기">
                    </div>

                    <div class="alert alert-info">
                        <strong>📝 주의사항</strong><br>
                        서명 완료 후에는 계약서를 수정하거나 취소할 수 없습니다.<br>
                        계약 내용을 다시 한 번 확인해 주세요.
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" id="cancelSignBtn">
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

    <div id="signingData" hidden
         data-contract-id="<c:out value='${contract.id}'/>"
         data-token="<c:out value='${token}'/>"
         data-contract-title="<c:out value='${contract.title}'/>"
         data-signer-name="<c:out value='${contract.secondParty.name}'/>"
         data-signer-email="<c:out value='${contract.secondParty.email}'/>">
    </div>

    <!-- JavaScript -->
    <script src="<c:url value='/js/common.js' />"></script>
    <script src="https://cdn.jsdelivr.net/npm/signature_pad@4.1.5/dist/signature_pad.umd.min.js"></script>

    <script>
        const signingDataElement = document.getElementById('signingData');
        const signingDataset = signingDataElement ? signingDataElement.dataset : {};

        const contractData = {
            id: signingDataset.contractId || '',
            token: signingDataset.token || '',
            title: signingDataset.contractTitle || '',
            signerName: signingDataset.signerName || '',
            signerEmail: signingDataset.signerEmail || ''
        };

        const csrfParam = '${_csrf.parameterName}';
        const csrfToken = '${_csrf.token}';

        let signaturePad;
        let signatureCanvas;
        let signaturePlaceholder;
        let signatureSubmitButton;
        let signatureClearButton;
        let isInitialized = false;

        document.addEventListener('DOMContentLoaded', () => {
            initSignaturePad();
            bindEvents();
        });

        function initSignaturePad() {
            // 이미 초기화되었으면 무시
            if (isInitialized) {
                return;
            }

            const container = document.getElementById('signaturePadContainer');
            if (!container) {
                return;
            }

            signatureCanvas = container.querySelector('.signature-canvas');
            signaturePlaceholder = container.querySelector('.signature-placeholder');
            signatureSubmitButton = container.querySelector('.signature-submit');
            signatureClearButton = container.querySelector('.signature-clear');

            signaturePad = new window.SignaturePad(signatureCanvas, {
                backgroundColor: 'rgba(0, 0, 0, 0)',
                penColor: '#1d4ed8',
                minWidth: 0.8,
                maxWidth: 2.5
            });

            signaturePad.onBegin = () => {
                hidePlaceholder();
                updateSignatureState();
            };

            signaturePad.onEnd = () => {
                updateSignatureState();
            };

            resizeSignatureCanvas();

            // 이전 리스너 제거 후 새로 추가
            window.removeEventListener('resize', resizeSignatureCanvas);
            window.addEventListener('resize', resizeSignatureCanvas);

            // 브라우저 환경에서도 서명 시작/완료 감지를 위한 추가 이벤트 리스너
            // resizeSignatureCanvas 이후에 추가해야 canvas가 준비된 상태
            signatureCanvas.addEventListener('mousedown', () => hidePlaceholder());
            signatureCanvas.addEventListener('touchstart', () => hidePlaceholder());
            signatureCanvas.addEventListener('mouseup', () => updateSignatureState());
            signatureCanvas.addEventListener('touchend', () => updateSignatureState());

            if (signatureClearButton) {
                signatureClearButton.addEventListener('click', () => {
                    signaturePad.clear();
                    showPlaceholder();
                    updateSignatureState();
                    handleSignatureClear();
                });
            }

            if (signatureSubmitButton) {
                signatureSubmitButton.addEventListener('click', () => {
                    if (signaturePad.isEmpty()) {
                        Signly.showAlert('서명을 입력해 주세요.', 'warning');
                        return;
                    }

                    const signatureData = signaturePad.toDataURL('image/png');
                    handleSignatureSubmit(signatureData);
                });
            }

            isInitialized = true;
            updateSignatureState();

            updateSignatureState();
            isInitialized = true;
        }

        function resizeSignatureCanvas() {
            if (!signaturePad || !signatureCanvas) {
                return;
            }

            const existingData = signaturePad.isEmpty() ? null : signaturePad.toDataURL('image/png');
            const ratio = Math.max(window.devicePixelRatio || 1, 1);
            const parent = signatureCanvas.parentElement;
            const width = Math.min(parent.clientWidth || 600, 600);
            const height = Math.max(220, Math.min(parent.clientHeight || 280, 320));

            signatureCanvas.width = width * ratio;
            signatureCanvas.height = height * ratio;
            signatureCanvas.getContext('2d').scale(ratio, ratio);
            signatureCanvas.style.width = width + 'px';
            signatureCanvas.style.height = height + 'px';

            signaturePad.clear();

            if (existingData) {
                signaturePad.fromDataURL(existingData);
                hidePlaceholder();
            } else {
                showPlaceholder();
            }

            updateSignatureState();
        }

        function updateSignatureState() {
            if (!signatureSubmitButton) {
                return;
            }

            const isEmpty = !signaturePad || signaturePad.isEmpty();
            signatureSubmitButton.disabled = isEmpty;

            // 서명 완료 버튼도 같이 업데이트
            const completeBtn = document.getElementById('completeSigningBtn');
            if (completeBtn) {
                completeBtn.disabled = isEmpty;
            }
        }

        function hidePlaceholder() {
            if (signaturePlaceholder) {
                signaturePlaceholder.style.display = 'none';
            }
        }

        function showPlaceholder() {
            if (signaturePlaceholder) {
                signaturePlaceholder.style.display = '';
            }
        }

        function bindEvents() {
            const completeBtn = document.getElementById('completeSigningBtn');
            completeBtn.addEventListener('click', () => {
                if (signaturePad && !signaturePad.isEmpty()) {
                    showSignatureConfirmModal();
                } else {
                    Signly.showAlert('서명을 입력해 주세요.', 'warning');
                }
            });

            document.getElementById('cancelSignBtn').addEventListener('click', closeModal);
            document.getElementById('finalSignBtn').addEventListener('click', submitSignature);
        }

        function handleSignatureSubmit(signatureData) {
            if (!signatureData) {
                return;
            }

            document.getElementById('completeSigningBtn').disabled = false;
            Signly.showAlert('서명이 입력되었습니다. 서명 완료 버튼을 클릭해 주세요.', 'success');
        }

        function handleSignatureClear() {
            document.getElementById('completeSigningBtn').disabled = true;
        }

        function showSignatureConfirmModal() {
            const signatureData = signaturePad ? signaturePad.toDataURL('image/png') : null;

            if (signatureData) {
                document.getElementById('signaturePreviewImage').src = signatureData;

                const modal = document.getElementById('signatureConfirmModal');
                modal.classList.add('show');
                modal.style.display = 'block';
                document.body.classList.add('modal-open');
            }
        }

        async function submitSignature() {
            const finalSignBtn = document.getElementById('finalSignBtn');
            const originalHtml = finalSignBtn.innerHTML;

            try {
                finalSignBtn.disabled = true;
                finalSignBtn.innerHTML = '<span class="spinner"></span> 서명 처리중...';

                const signatureData = signaturePad ? signaturePad.toDataURL('image/png') : null;
                if (!signatureData) {
                    throw new Error('서명 데이터가 존재하지 않습니다.');
                }

                const payload = new URLSearchParams();
                payload.append('signatureData', signatureData);
                payload.append('signerName', contractData.signerName);
                payload.append('signerEmail', contractData.signerEmail);

                const headers = {
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
                };

                if (csrfParam && csrfToken) {
                    payload.append(csrfParam, csrfToken);
                }

                const response = await fetch('/sign/' + contractData.token + '/sign', {
                    method: 'POST',
                    headers,
                    body: payload.toString()
                });

                if (!response.ok) {
                    throw new Error('서명 처리 중 오류가 발생했습니다.');
                }

                const result = await response.json();

                if (result.success) {
                    window.location.href = '/sign/' + contractData.token + '/complete';
                    return;
                }

                throw new Error(result.message || '서명 처리 중 오류가 발생했습니다.');

            } catch (error) {
                console.error('Signature submission error:', error);
                Signly.showAlert(error.message || '서명 처리 중 오류가 발생했습니다.', 'danger');
                finalSignBtn.disabled = false;
                finalSignBtn.innerHTML = originalHtml;
            }
        }

        function closeModal() {
            const modal = document.getElementById('signatureConfirmModal');
            modal.classList.remove('show');
            modal.style.display = 'none';
            document.body.classList.remove('modal-open');
        }

        document.getElementById('signatureConfirmModal').addEventListener('click', function(e) {
            if (e.target === this) {
                closeModal();
            }
        });

        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                closeModal();
            }
        });

        window.addEventListener('beforeunload', function(e) {
            if (signaturePad && !signaturePad.isEmpty()) {
                e.preventDefault();
                e.returnValue = '서명이 완료되지 않았습니다. 페이지를 떠나시겠습니까?';
            }
        });
    </script>

</body>
</html>
