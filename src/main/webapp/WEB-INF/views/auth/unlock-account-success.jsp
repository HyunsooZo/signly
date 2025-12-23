<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="ko">
        <jsp:include page="../common/header.jsp">
            <jsp:param name="pageTitle" value="계정 잠금 해제 완료" />
            <jsp:param name="additionalCss" value="/css/auth.css" />
        </jsp:include>

        <body class="auth-page">
            <div class="auth-container">
                <div class="auth-card">
                    <div class="auth-header">
                        <h1 class="auth-title">✅ 잠금 해제 완료</h1>
                        <p class="auth-subtitle">계정이 성공적으로 잠금 해제되었습니다</p>
                    </div>

                    <div class="alert alert-success">
                        <strong>🎉 성공!</strong>
                        <c:if test="${not empty successMessage}">
                            <p><c:out value="${successMessage}" /></p>
                        </c:if>
                        <c:if test="${empty successMessage}">
                            <p>계정이 성공적으로 잠금 해제되었습니다. 이메일로 임시 비밀번호가 발송되었습니다.</p>
                        </c:if>
                    </div>

                    <div class="next-steps">
                        <h5>다음 단계</h5>
                        <ol>
                            <li><strong>이메일 확인</strong> - 임시 비밀번호가 포함된 이메일을 확인하세요</li>
                            <li><strong>로그인</strong> - 임시 비밀번호로 로그인하세요</li>
                            <li><strong>비밀번호 변경</strong> - 보안을 위해 새로운 비밀번호로 변경하세요</li>
                        </ol>
                    </div>

                    <div class="alert alert-warning">
                        <strong>⚠️ 중요</strong>
                        <p>임시 비밀번호는 24시간 후에 만료됩니다. 반드시 24시간 이내에 로그인하고 비밀번호를 변경해주세요.</p>
                    </div>

                    <div class="action-buttons">
                        <a href="/login" class="btn btn-primary btn-auth w-100 mb-2">
                            🔑 로그인하기
                        </a>
                        <a href="/forgot-password" class="btn btn-outline-secondary btn-auth w-100">
                            비밀번호를 잊으셨나요?
                        </a>
                    </div>

                    <div class="help-info">
                        <h6>📧 이메일이 오지 않나요?</h6>
                        <ul class="small">
                            <li>스팸 메일함을 확인해보세요</li>
                            <li>이메일 주소가 정확한지 확인하세요</li>
                            <li>5분 이내에 이메일이 도착하지 않으면 <a href="/account-locked">다시 요청</a>해보세요</li>
                        </ul>
                    </div>

                    <div class="auth-footer">
                        <hr class="auth-divider">
                        <p>기타 문의사항이 있으신가요? <a href="/contact">고객센터</a></p>
                    </div>
                </div>
            </div>

            <jsp:include page="../common/footer.jsp" />
        </body>
        </html>