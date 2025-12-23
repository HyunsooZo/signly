<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
    <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
        <!DOCTYPE html>
        <html lang="ko">
        <jsp:include page="../common/header.jsp">
            <jsp:param name="pageTitle" value="비밀번호 변경" />
            <jsp:param name="additionalCss" value="/css/auth.css" />
        </jsp:include>

        <body class="auth-page">
            <div class="auth-container">
                <div class="auth-card">
                    <div class="auth-header">
                        <h1 class="auth-title">🔒 비밀번호 변경</h1>
                        <p class="auth-subtitle">새로운 비밀번호로 변경합니다</p>
                    </div>

                    <c:if test="${not empty errorMessage}">
                        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                            <c:out value="${errorMessage}" />
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        </div>
                    </c:if>

                    <c:if test="${not empty successMessage}">
                        <div class="alert alert-success alert-dismissible fade show" role="alert">
                            <c:out value="${successMessage}" />
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        </div>
                    </c:if>

                    <div class="alert alert-info">
                        <strong>💡 비밀번호 변경 안내</strong>
                        <ul class="mb-0 mt-2 small">
                            <li>최근 90일 이내 사용한 비밀번호는 재사용할 수 없습니다</li>
                            <li>8자 이상이어야 하며 영문, 숫자, 특수문자를 포함해야 합니다</li>
                            <li>보안을 위해 정기적으로 비밀번호를 변경해주세요</li>
                        </ul>
                    </div>

                    <form:form action="/change-password" method="post" modelAttribute="changePasswordCommand" class="auth-form">
                        <div class="form-group">
                            <label for="oldPassword" class="form-label">현재 비밀번호</label>
                            <form:password path="oldPassword" class="form-control" id="oldPassword" required 
                                         placeholder="현재 비밀번호를 입력해주세요" />
                            <form:errors path="oldPassword" class="invalid-feedback d-block" />
                        </div>

                        <div class="form-group">
                            <label for="newPassword" class="form-label">새 비밀번호</label>
                            <form:password path="newPassword" class="form-control" id="newPassword" required 
                                         placeholder="새 비밀번호를 입력해주세요" />
                            <form:errors path="newPassword" class="invalid-feedback d-block" />
                            <div class="form-text text-muted">
                                8자 이상, 영문, 숫자, 특수문자 포함
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="confirmPassword" class="form-label">새 비밀번호 확인</label>
                            <form:password path="confirmPassword" class="form-control" id="confirmPassword" required 
                                         placeholder="새 비밀번호를 다시 입력해주세요" />
                            <form:errors path="confirmPassword" class="invalid-feedback d-block" />
                            <div id="passwordMatchError" class="invalid-feedback" style="display: none;">
                                비밀번호가 일치하지 않습니다.
                            </div>
                        </div>

                        <div class="password-strength" style="display: none;">
                            <label class="form-label">비밀번호 강도</label>
                            <div class="progress mb-2">
                                <div id="passwordStrengthBar" class="progress-bar" role="progressbar" 
                                     style="width: 0%" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100">
                                </div>
                            </div>
                            <small id="passwordStrengthText" class="form-text text-muted"></small>
                        </div>

                        <button type="submit" class="btn btn-primary btn-auth">
                            🔄 비밀번호 변경
                        </button>
                    </form:form>

                    <div class="auth-footer">
                        <hr class="auth-divider">
                        <p><a href="/profile" class="btn btn-sm btn-outline-secondary">프로필로 돌아가기</a></p>
                    </div>
                </div>
            </div>

            <jsp:include page="../common/footer.jsp" />
            <script>
                // 비밀번호 확인 일치 여부 검증
                function validatePasswordMatch() {
                    const newPassword = document.getElementById('newPassword');
                    const confirmPassword = document.getElementById('confirmPassword');
                    const matchError = document.getElementById('passwordMatchError');
                    
                    if (confirmPassword.value && newPassword.value !== confirmPassword.value) {
                        confirmPassword.classList.add('is-invalid');
                        matchError.style.display = 'block';
                        return false;
                    } else {
                        confirmPassword.classList.remove('is-invalid');
                        matchError.style.display = 'none';
                        return true;
                    }
                }

                // 비밀번호 강도 검증
                function checkPasswordStrength(password) {
                    let strength = 0;
                    const strengthBar = document.getElementById('passwordStrengthBar');
                    const strengthText = document.getElementById('passwordStrengthText');
                    const strengthContainer = document.querySelector('.password-strength');

                    if (password.length >= 8) strength++;
                    if (/[a-z]/.test(password)) strength++;
                    if (/[A-Z]/.test(password)) strength++;
                    if (/[0-9]/.test(password)) strength++;
                    if (/[^A-Za-z0-9]/.test(password)) strength++;

                    if (password.length > 0) {
                        strengthContainer.style.display = 'block';
                    }

                    let strengthLevel, strengthClass, strengthColor;
                    
                    switch(strength) {
                        case 0:
                        case 1:
                            strengthLevel = '매우 약함';
                            strengthClass = 'bg-danger';
                            strengthColor = 'text-danger';
                            break;
                        case 2:
                            strengthLevel = '약함';
                            strengthClass = 'bg-warning';
                            strengthColor = 'text-warning';
                            break;
                        case 3:
                            strengthLevel = '보통';
                            strengthClass = 'bg-info';
                            strengthColor = 'text-info';
                            break;
                        case 4:
                            strengthLevel = '강함';
                            strengthClass = 'bg-primary';
                            strengthColor = 'text-primary';
                            break;
                        case 5:
                            strengthLevel = '매우 강함';
                            strengthClass = 'bg-success';
                            strengthColor = 'text-success';
                            break;
                    }

                    strengthBar.className = `progress-bar ${strengthClass}`;
                    strengthBar.style.width = `${(strength / 5) * 100}%`;
                    strengthBar.setAttribute('aria-valuenow', (strength / 5) * 100);
                    strengthText.textContent = strengthLevel;
                    strengthText.className = `form-text ${strengthColor}`;
                }

                // 이벤트 리스너
                document.addEventListener('DOMContentLoaded', function() {
                    const newPasswordInput = document.getElementById('newPassword');
                    const confirmPasswordInput = document.getElementById('confirmPassword');

                    newPasswordInput.addEventListener('input', function() {
                        checkPasswordStrength(this.value);
                        if (confirmPasswordInput.value) {
                            validatePasswordMatch();
                        }
                    });

                    confirmPasswordInput.addEventListener('input', validatePasswordMatch);
                });
            </script>
        </body>
        </html>