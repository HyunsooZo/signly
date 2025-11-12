<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
                        <option value="OWNER" <c:if test="${param.userType == 'OWNER'}">selected</c:if>>사업자 (계약서 생성/발송)</option>
                        <option value="CONTRACTOR" <c:if test="${param.userType == 'CONTRACTOR'}">selected</c:if>>계약자 (서명 전용)</option>
                    </select>
                    <div class="form-text">사업자는 계약서를 생성하고 발송할 수 있으며, 계약자는 서명만 가능합니다.</div>
                    <c:if test="${not empty fieldErrors.userType}">
                        <div class="field-error"><c:out value="${fieldErrors.userType}"/></div>
                    </c:if>
                </div>

                <div class="form-group form-check">
                    <input type="checkbox" class="form-check-input" id="agreeTerms" name="agreeTerms" required>
                    <label class="form-check-label" for="agreeTerms">
                        <a href="#" onclick="openTermsModal()">이용약관</a> 및 <a href="#" onclick="openPrivacyModal()">개인정보처리방침</a>에 동의합니다 *
                    </label>
                </div>

                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

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
    <script>
        // 허용된 키 코드 목록
        const ALLOWED_KEYS = [
            // 백스페이스, 탭, 엔터, ESC
            8, 9, 13, 27,
            // 방향키, Delete, Home, End
            37, 38, 39, 40, 46, 35, 36,
            // 숫자 (0-9)
            48, 49, 50, 51, 52, 53, 54, 55, 56, 57,
            // 숫자 (Numpad)
            96, 97, 98, 99, 100, 101, 102, 103, 104, 105,
            // 영문 대문자 (A-Z)
            65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90,
            // 특수문자
            186, 187, 188, 189, 190, 191, 192, 219, 220, 221, 222
        ];

        // 허용된 Ctrl 조합 키
        const ALLOWED_CTRL_KEYS = [65, 67, 86, 88, 90]; // A, C, V, X, Z

        // 한글 입력 감지 함수
        function containsKorean(text) {
            return /[ㄱ-ㅎ|ㅏ-ㅣ|가-힣]/.test(text);
        }

        // 허용된 문자만 남기기
        function cleanInput(text) {
            return text.replace(/[^a-zA-Z0-9!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/g, '');
        }

        // 경고 메시지 표시
        function showWarning(message) {
            if (!window.inputWarningShown) {
                showAlertModal(message);
                window.inputWarningShown = true;
                setTimeout(() => {
                    window.inputWarningShown = false;
                }, 3000);
            }
        }

        // 입력 상태 업데이트
        function updateInputStatus(input, statusId) {
            const status = document.getElementById(statusId);
            const value = input.value;
            
            if (value.length === 0) {
                status.textContent = '';
                status.className = 'form-text';
            } else if (/^[a-zA-Z0-9!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]*$/.test(value)) {
                status.textContent = '✓ 영문/숫자/특수문자만 입력';
                status.className = 'form-text text-success';
            } else {
                status.textContent = '⚠ 허용되지 않는 문자가 제거되었습니다';
                status.className = 'form-text text-warning';
            }
        }

        // 키보드 표시기 토글
        function toggleKeyboardIndicator(indicatorId, show) {
            const indicator = document.getElementById(indicatorId);
            indicator.style.display = show ? 'block' : 'none';
        }

        // IME 제어 설정
        function setupIMEControl(input) {
            const userAgent = navigator.userAgent.toLowerCase();
            
            if (userAgent.includes('chrome') || userAgent.includes('edge')) {
                input.style.imeMode = 'disabled';
            } else if (userAgent.includes('firefox')) {
                input.addEventListener('compositionstart', blockIME);
            } else if (userAgent.includes('safari')) {
                input.setAttribute('inputmode', 'latin');
            }
        }

        // IME 입력 차단
        function blockIME(e) {
            e.preventDefault();
            const input = e.target;
            input.blur();
            input.focus();
            showWarning('IME 입력이 차단되었습니다. 영문만 입력 가능합니다.');
        }

        // 모바일 지원 설정
        function setupMobileSupport(input) {
            const isMobile = /Android|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
            
            if (isMobile) {
                input.setAttribute('inputmode', 'url');
                input.setAttribute('autocomplete', 'new-password');
                input.setAttribute('autocorrect', 'off');
                input.setAttribute('autocapitalize', 'off');
                input.setAttribute('spellcheck', 'false');
            }
        }

        // 비밀번호 입력 필드 초기화
        function initializePasswordField(inputId, statusId, indicatorId) {
            const input = document.getElementById(inputId);
            
            // IME 제어 설정
            setupIMEControl(input);
            
            // 모바일 지원 설정
            setupMobileSupport(input);
            
            // keydown 이벤트 - 키 레벨 필터링
            input.addEventListener('keydown', function(e) {
                // Ctrl 조합 키 허용
                if (e.ctrlKey && ALLOWED_CTRL_KEYS.includes(e.keyCode)) {
                    return true;
                }
                
                // 허용되지 않는 키 차단
                if (!ALLOWED_KEYS.includes(e.keyCode)) {
                    e.preventDefault();
                    e.stopPropagation();
                    return false;
                }
            });
            
            // keypress 이벤트 - 문자 레벨 필터링
            input.addEventListener('keypress', function(e) {
                const charCode = e.charCode;
                
                // 한글 문자 코드 범위 차단
                if (charCode >= 0xAC00 && charCode <= 0xD7A3) { // 가-힣
                    e.preventDefault();
                    return false;
                }
                
                // 한글 자음/모음 차단
                if ((charCode >= 0x3131 && charCode <= 0x3163) || // ㄱ-ㅣ
                    (charCode >= 0x1100 && charCode <= 0x1112)) { // 초성
                    e.preventDefault();
                    return false;
                }
            });
            
            // input 이벤트 - 입력 후처리
            input.addEventListener('input', function(e) {
                const originalValue = e.target.value;
                const cleanValue = cleanInput(originalValue);
                
                if (originalValue !== cleanValue) {
                    e.target.value = cleanValue;
                    
                    // 커서 위치 유지
                    const cursorPos = e.target.selectionStart;
                    e.target.setSelectionRange(cursorPos, cursorPos);
                    
                    showWarning('허용되지 않는 문자가 제거되었습니다.');
                }
                
                updateInputStatus(e.target, statusId);
            });
            
            // paste 이벤트 - 붙여넣기 제어
            input.addEventListener('paste', function(e) {
                e.preventDefault();
                
                const clipboardData = e.clipboardData || window.clipboardData;
                const pastedText = clipboardData.getData('text');
                const cleanText = cleanInput(pastedText);
                
                const start = this.selectionStart;
                const end = this.selectionEnd;
                const currentValue = this.value;
                
                this.value = currentValue.substring(0, start) + cleanText + currentValue.substring(end);
                this.setSelectionRange(start + cleanText.length, start + cleanText.length);
                
                if (pastedText !== cleanText) {
                    showWarning('붙여넣기에서 허용되지 않는 문자가 제거되었습니다.');
                }
                
                updateInputStatus(this, statusId);
            });
            
            // 포커스 이벤트
            input.addEventListener('focus', function() {
                this.style.imeMode = 'disabled';
                toggleKeyboardIndicator(indicatorId, true);
            });
            
            input.addEventListener('blur', function() {
                toggleKeyboardIndicator(indicatorId, false);
                
                // 기존 비밀번호 일치 검증 로직 유지
                const password = document.getElementById('password').value;
                const confirmPassword = this.value;

                if (password && confirmPassword && password !== confirmPassword) {
                    this.setCustomValidity('비밀번호가 일치하지 않습니다.');
                    this.classList.add('is-invalid');
                } else {
                    this.setCustomValidity('');
                    this.classList.remove('is-invalid');
                }
            });
            
            // compositionstart 이벤트 - IME 시작 차단
            input.addEventListener('compositionstart', blockIME);
        }

        function openTermsModal() {
            showAlertModal('이용약관 모달 준비 중입니다.');
        }

        function openPrivacyModal() {
            showAlertModal('개인정보처리방침 모달 준비 중입니다.');
        }

        // 페이지 로드 시 초기화
        document.addEventListener('DOMContentLoaded', function() {
            // 비밀번호 입력 필드 초기화
            initializePasswordField('password', 'password-status', 'password-keyboard-indicator');
            initializePasswordField('confirmPassword', 'confirmPassword-status', 'confirmPassword-keyboard-indicator');
        });
    </script>
</body>
</html>
