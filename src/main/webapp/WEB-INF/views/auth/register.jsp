<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>회원가입 - Signly</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Lobster&display=swap" rel="stylesheet">
    <link href="/css/common.css" rel="stylesheet">
    <link href="/css/auth.css" rel="stylesheet">
</head>
<body>
<div class="auth-container">
    <div class="auth-card">
        <div class="auth-header">
            <h1 class="auth-title">Signly</h1>
            <p class="auth-subtitle">전자계약 서비스에 오신 것을 환영합니다</p>
        </div>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert" data-auto-dismiss="true">
                <c:out value="${errorMessage}"/>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <form action="/register" method="post" class="auth-form">
            <div class="form-group">
                <label for="email" class="form-label">이메일 *</label>
                <input type="email" class="form-control" id="email" name="email"
                       value="<c:out value="${param.email}"/>" required placeholder="your@email.com">
                <c:if test="${not empty fieldErrors.email}">
                    <div class="field-error"><c:out value="${fieldErrors.email}"/></div>
                </c:if>
            </div>

            <div class="form-group">
                <label for="password" class="form-label">
                    비밀번호 *
                    <span class="visually-hidden">(영문, 숫자, 특수문자만 입력 가능)</span>
                </label>
                <input type="password"
                       class="form-control"
                       id="password"
                       name="password"
                       required
                       placeholder="8자 이상, 영문/숫자/특수문자 포함"
                       inputmode="latin"
                       style="ime-mode: disabled"
                       autocomplete="new-password"
                       autocorrect="off"
                       autocapitalize="off"
                       spellcheck="false"
                       aria-describedby="password-help password-status"
                       aria-label="비밀번호 영문 전용 입력 필드">
                <div class="invalid-feedback" id="password-korean-error" style="display: none;">
                    비밀번호에는 한글을 사용할 수 없습니다. 영문, 숫자, 특수문자만 입력 가능합니다.
                </div>
                <div id="password-help" class="form-text text-muted">
                    비밀번호는 8자 이상이며, 영문, 숫자, 특수문자를 포함해야 합니다.
                </div>
                <div id="password-status" class="form-text" aria-live="polite"></div>
                <div id="password-keyboard-indicator" class="keyboard-indicator" style="display: none;">
                    <div class="indicator-title">입력 모드</div>
                    <div class="indicator-status">영문 전용</div>
                    <div class="indicator-keys">
                        <span class="key">A-Z</span>
                        <span class="key">0-9</span>
                        <span class="key">!@#$</span>
                    </div>
                </div>
                <c:if test="${not empty fieldErrors.password}">
                    <div class="field-error"><c:out value="${fieldErrors.password}"/></div>
                </c:if>
            </div>

            <div class="form-group">
                <label for="confirmPassword" class="form-label">
                    비밀번호 확인 *
                    <span class="visually-hidden">(영문, 숫자, 특수문자만 입력 가능)</span>
                </label>
                <input type="password"
                       class="form-control"
                       id="confirmPassword"
                       name="confirmPassword"
                       required
                       placeholder="비밀번호를 다시 입력해주세요"
                       inputmode="latin"
                       style="ime-mode: disabled"
                       autocomplete="new-password"
                       autocorrect="off"
                       autocapitalize="off"
                       spellcheck="false"
                       aria-describedby="confirmPassword-help confirmPassword-status"
                       aria-label="비밀번호 확인 영문 전용 입력 필드">
                <div class="invalid-feedback" id="confirmPassword-korean-error" style="display: none;">
                    비밀번호에는 한글을 사용할 수 없습니다. 영문, 숫자, 특수문자만 입력 가능합니다.
                </div>
                <div id="confirmPassword-help" class="form-text text-muted">
                    영문, 숫자, 특수문자만 입력 가능합니다
                </div>
                <div id="confirmPassword-status" class="form-text" aria-live="polite"></div>
                <div id="confirmPassword-keyboard-indicator" class="keyboard-indicator" style="display: none;">
                    <div class="indicator-title">입력 모드</div>
                    <div class="indicator-status">영문 전용</div>
                    <div class="indicator-keys">
                        <span class="key">A-Z</span>
                        <span class="key">0-9</span>
                        <span class="key">!@#$</span>
                    </div>
                </div>
                <c:if test="${not empty fieldErrors.confirmPassword}">
                    <div class="field-error"><c:out value="${fieldErrors.confirmPassword}"/></div>
                </c:if>
            </div>

            <div class="form-group">
                <label for="name" class="form-label">이름 *</label>
                <input type="text" class="form-control" id="name" name="name"
                       value="<c:out value="${param.name}"/>" required placeholder="홍길동">
                <c:if test="${not empty fieldErrors.name}">
                    <div class="field-error"><c:out value="${fieldErrors.name}"/></div>
                </c:if>
            </div>

            <div class="form-group">
                <label for="companyName" class="form-label">회사명</label>
                <input type="text" class="form-control" id="companyName" name="companyName"
                       value="<c:out value="${param.companyName}"/>" placeholder="(주)예시회사">
                <c:if test="${not empty fieldErrors.companyName}">
                    <div class="field-error"><c:out value="${fieldErrors.companyName}"/></div>
                </c:if>
            </div>

            <div class="form-group">
                <label for="businessPhone" class="form-label">사업장 전화번호</label>
                <input type="text" class="form-control" id="businessPhone" name="businessPhone"
                       value="<c:out value="${param.businessPhone}"/>" placeholder="02-1234-5678">
                <c:if test="${not empty fieldErrors.businessPhone}">
                    <div class="field-error"><c:out value="${fieldErrors.businessPhone}"/></div>
                </c:if>
            </div>

            <div class="form-group">
                <label for="businessAddress" class="form-label">사업장 주소</label>
                <input type="text" class="form-control" id="businessAddress" name="businessAddress"
                       value="<c:out value="${param.businessAddress}"/>" placeholder="서울시 강남구 테헤란로 123">
                <c:if test="${not empty fieldErrors.businessAddress}">
                    <div class="field-error"><c:out value="${fieldErrors.businessAddress}"/></div>
                </c:if>
            </div>

            <div class="form-group">
                <label for="userType" class="form-label">사용자 유형 *</label>
                <select class="form-control" id="userType" name="userType" required>
                    <option value="">선택해주세요</option>
                    <option value="OWNER" <c:if test="${param.userType == 'OWNER'}">selected</c:if>>사업자 (계약서 생성/발송)
                    </option>
                    <option value="CONTRACTOR" <c:if test="${param.userType == 'CONTRACTOR'}">selected</c:if>>계약자 (서명
                        전용)
                    </option>
                </select>
                <div class="form-text">사업자는 계약서를 생성하고 발송할 수 있으며, 계약자는 서명만 가능합니다.</div>
                <c:if test="${not empty fieldErrors.userType}">
                    <div class="field-error"><c:out value="${fieldErrors.userType}"/></div>
                </c:if>
            </div>

            <div class="form-group form-check">
                <input type="checkbox" class="form-check-input" id="agreeTerms" name="agreeTerms" required>
                <label class="form-check-label" for="agreeTerms">
                    <a href="#" onclick="openTermsModal()">이용약관</a> 및 <a href="#"
                                                                         onclick="openPrivacyModal()">개인정보처리방침</a>에
                    동의합니다 *
                </label>
            </div>


            <button type="submit" class="btn btn-primary btn-auth">회원가입</button>
        </form>

        <div class="auth-footer">
            <p>이미 계정이 있으신가요? <a href="/login">로그인</a></p>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="/js/common.js"></script>
<script src="/js/alerts.js"></script>
<script src="/js/auth-pages.js"></script>
</body>
</html>
