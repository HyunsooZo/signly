<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
    <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
        <!DOCTYPE html>
        <html lang="ko">
        <jsp:include page="../common/header.jsp">
            <jsp:param name="pageTitle" value="계정 잠금 해제" />
            <jsp:param name="additionalCss" value="/css/auth.css" />
        </jsp:include>

        <body class="auth-page">
            <div class="auth-container">
                <div class="auth-card">
                    <div class="auth-header">
                        <h1 class="auth-title">🔓 계정 잠금 해제</h1>
                        <p class="auth-subtitle">계정 잠금 해제를 진행합니다</p>
                    </div>

                    <div class="alert alert-info">
                        <strong>보안 확인</strong>
                        <p>이메일로 받은 잠금 해제 토큰을 확인하여 계정 잠금을 해제합니다.</p>
                    </div>

                    <c:if test="${not empty errorMessage}">
                        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                            <c:out value="${errorMessage}" />
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        </div>
                    </c:if>

                    <form:form action="/unlock-account" method="post" modelAttribute="unlockRequest" class="auth-form">
                        <div class="form-group">
                            <label for="token" class="form-label">잠금 해제 토큰</label>
                            <form:input type="text" class="form-control" id="token" path="token" readonly="true" />
                            <small class="form-text text-muted">
                                이메일로 받은 잠금 해제 링크의 토큰이 자동으로 입력되었습니다.
                            </small>
                        </div>

                        <div class="security-info">
                            <h6>🔒 보안 확인 사항</h6>
                            <ul class="small">
                                <li>유효한 잠금 해제 토큰인지 확인</li>
                                <li>24시간 이내에 발급된 토큰인지 확인</li>
                                <li>계정 소유자임을 인증</li>
                            </ul>
                        </div>

                        <button type="submit" class="btn btn-success btn-auth w-100">
                            🔓 계정 잠금 해제
                        </button>
                    </form:form>

                    <div class="auth-footer">
                        <hr class="auth-divider">
                        <p>잠금 해제 링크가 만료되었나요? <a href="/account-locked">다시 요청하기</a></p>
                        <p><a href="/login" class="btn btn-sm btn-outline-primary">로그인 페이지로 돌아가기</a></p>
                    </div>
                </div>
            </div>

            <jsp:include page="../common/footer.jsp" />
            <script>
                // URL에서 토큰 파라미터를 읽어서 form에 설정
                document.addEventListener('DOMContentLoaded', function() {
                    const urlParams = new URLSearchParams(window.location.search);
                    const token = urlParams.get('token');
                    if (token) {
                        document.getElementById('token').value = token;
                    }
                });
            </script>
        </body>
        </html>