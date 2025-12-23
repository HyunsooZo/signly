<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="ko">
        <jsp:include page="../common/header.jsp">
            <jsp:param name="pageTitle" value="계정 잠금" />
            <jsp:param name="additionalCss" value="/css/auth.css" />
        </jsp:include>

        <body class="auth-page">
            <div class="auth-container">
                <div class="auth-card">
                    <div class="auth-header">
                        <h1 class="auth-title">🔒 계정 잠금</h1>
                        <p class="auth-subtitle">보안을 위해 계정이 잠겼습니다</p>
                    </div>

                    <div class="alert alert-danger">
                        <strong>보안 알림</strong>
                        <p>5회의 로그인 실패로 인해 계정이 잠겼습니다. 계정 보호를 위해 이메일 인증이 필요합니다.</p>
                    </div>

                    <div class="lock-info">
                        <h5>계정 잠금 사유</h5>
                        <ul>
                            <li>5회 이상의 로그인 시도 실패</li>
                            <li>비정상적인 로그인 패턴 감지</li>
                            <li>계정 보호를 위한 자동 잠금</li>
                        </ul>
                    </div>

                    <div class="unlock-info">
                        <h5>계정 잠금 해제 방법</h5>
                        <p>1. 이메일로 발송된 잠금 해제 링크를 확인하세요</p>
                        <p>2. 링크를 클릭하여 잠금 해제를 진행하세요</p>
                        <p>3. 임시 비밀번호로 로그인 후 비밀번호를 변경하세요</p>
                    </div>

                    <div class="alert alert-warning">
                        <strong>⚠️ 잠금 해제 이메일이 오지 않나요?</strong>
                        <p>스팸 메일함을 확인하시거나 아래 버튼을 클릭하여 잠금 해제 이메일을 다시 요청하세요.</p>
                        <form action="/resend-unlock-email" method="post" class="mt-3">
                            <div class="form-group">
                                <input type="email" class="form-control" name="email" required 
                                       placeholder="가입한 이메일 주소를 입력해주세요">
                            </div>
                            <button type="submit" class="btn btn-warning btn-auth w-100 mt-2">
                                🔓 잠금 해제 이메일 재전송
                            </button>
                        </form>
                    </div>

                    <div class="auth-footer">
                        <hr class="auth-divider">
                        <p>다른 문의가 있으신가요? <a href="/contact">고객센터</a></p>
                        <p><a href="/login" class="btn btn-sm btn-outline-primary">로그인 페이지로 돌아가기</a></p>
                    </div>
                </div>
            </div>

            <jsp:include page="../common/footer.jsp" />
        </body>
        </html>