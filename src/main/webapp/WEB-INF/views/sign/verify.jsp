<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle} - Signly</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <link href="/css/common.css" rel="stylesheet">
    <link href="/css/signature.css" rel="stylesheet">
</head>
<body class="verify-page">
    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-6">
                <div class="card shadow-lg">
                    <div class="card-header text-center bg-primary text-white">
                        <h4 class="mb-0">
                            <i class="bi bi-shield-check me-2"></i>
                            서명자 인증
                        </h4>
                    </div>
                    <div class="card-body p-4">
                        <!-- 계약서 정보 -->
                        <div class="mb-4">
                            <h5 class="card-title">${contract.title}</h5>
                            <p class="text-muted">서명 전 본인 확인을 위해 정보를 입력해주세요.</p>
                        </div>

                        <!-- 알림 메시지 -->
                        <c:if test="${not empty errorMessage}">
                            <div class="alert alert-danger" role="alert">
                                <i class="bi bi-exclamation-triangle me-2"></i>${errorMessage}
                            </div>
                        </c:if>

                        <!-- 인증 폼 -->
                        <form method="post" action="/sign/${token}/verify">
                            <div class="mb-3">
                                <label for="signerEmail" class="form-label">
                                    <i class="bi bi-envelope me-2"></i>이메일 주소
                                </label>
                                <input type="email" class="form-control" id="signerEmail" name="signerEmail"
                                       placeholder="${contract.secondParty.email}" required>
                                <div class="form-text">계약서에 등록된 이메일 주소를 입력하세요.</div>
                            </div>

                            <div class="mb-4">
                                <label for="signerName" class="form-label">
                                    <i class="bi bi-person me-2"></i>성명
                                </label>
                                <input type="text" class="form-control" id="signerName" name="signerName"
                                       placeholder="${contract.secondParty.name}" required>
                                <div class="form-text">계약서에 등록된 성명을 입력하세요.</div>
                            </div>

                            <div class="d-grid gap-2">
                                <button type="submit" class="btn btn-primary">
                                    <i class="bi bi-check-circle me-2"></i>인증 완료
                                </button>
                                <button type="button" class="btn btn-outline-secondary" onclick="window.close()">
                                    <i class="bi bi-x-circle me-2"></i>취소
                                </button>
                            </div>
                        </form>
                    </div>
                    <div class="card-footer text-center text-muted">
                        <small>
                            <i class="bi bi-shield-lock me-1"></i>
                            본 인증은 전자서명법에 따른 본인 확인 절차입니다.
                        </small>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>