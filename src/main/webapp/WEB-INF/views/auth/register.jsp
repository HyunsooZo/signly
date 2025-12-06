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
                            <div class="alert alert-danger alert-dismissible fade show" role="alert"
                                data-auto-dismiss="true">
                                <c:out value="${errorMessage}" />
                                <button type="button" class="btn-close" data-bs-dismiss="alert"
                                    aria-label="Close"></button>
                            </div>
                        </c:if>

                        <form action="/register" method="post" class="auth-form">
                            <div class="form-group">
                                <label for="email" class="form-label">이메일 *</label>
                                <input type="email" class="form-control" id="email" name="email" value="<c:out value="
                                    ${param.email}" />" required placeholder="your@email.com">
                                <c:if test="${not empty fieldErrors.email}">
                                    <div class="field-error">
                                        <c:out value="${fieldErrors.email}" />
                                    </div>
                                </c:if>
                            </div>

                            <div class="form-group">
                                <label for="password" class="form-label">
                                    비밀번호 *
                                    <span class="visually-hidden">(영문, 숫자, 특수문자만 입력 가능)</span>
                                </label>
                                <input type="password" class="form-control" id="password" name="password" required
                                    placeholder="8자 이상, 영문/숫자/특수문자 포함" inputmode="latin" style="ime-mode: disabled"
                                    autocomplete="new-password" autocorrect="off" autocapitalize="off"
                                    spellcheck="false" aria-describedby="password-help password-status"
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
                                    <div class="field-error">
                                        <c:out value="${fieldErrors.password}" />
                                    </div>
                                </c:if>
                            </div>

                            <div class="form-group">
                                <label for="confirmPassword" class="form-label">
                                    비밀번호 확인 *
                                    <span class="visually-hidden">(영문, 숫자, 특수문자만 입력 가능)</span>
                                </label>
                                <input type="password" class="form-control" id="confirmPassword" name="confirmPassword"
                                    required placeholder="비밀번호를 다시 입력해주세요" inputmode="latin" style="ime-mode: disabled"
                                    autocomplete="new-password" autocorrect="off" autocapitalize="off"
                                    spellcheck="false" aria-describedby="confirmPassword-help confirmPassword-status"
                                    aria-label="비밀번호 확인 영문 전용 입력 필드">
                                <div class="invalid-feedback" id="confirmPassword-korean-error" style="display: none;">
                                    비밀번호에는 한글을 사용할 수 없습니다. 영문, 숫자, 특수문자만 입력 가능합니다.
                                </div>
                                <div id="confirmPassword-help" class="form-text text-muted">
                                    영문, 숫자, 특수문자만 입력 가능합니다
                                </div>
                                <div id="confirmPassword-status" class="form-text" aria-live="polite"></div>
                                <div id="confirmPassword-keyboard-indicator" class="keyboard-indicator"
                                    style="display: none;">
                                    <div class="indicator-title">입력 모드</div>
                                    <div class="indicator-status">영문 전용</div>
                                    <div class="indicator-keys">
                                        <span class="key">A-Z</span>
                                        <span class="key">0-9</span>
                                        <span class="key">!@#$</span>
                                    </div>
                                </div>
                                <c:if test="${not empty fieldErrors.confirmPassword}">
                                    <div class="field-error">
                                        <c:out value="${fieldErrors.confirmPassword}" />
                                    </div>
                                </c:if>
                            </div>

                            <div class="form-group">
                                <label for="name" class="form-label">이름 *</label>
                                <input type="text" class="form-control" id="name" name="name" value="<c:out value="
                                    ${param.name}" />" required placeholder="홍길동">
                                <c:if test="${not empty fieldErrors.name}">
                                    <div class="field-error">
                                        <c:out value="${fieldErrors.name}" />
                                    </div>
                                </c:if>
                            </div>

                            <div class="form-group">
                                <label for="companyName" class="form-label">회사명</label>
                                <input type="text" class="form-control" id="companyName" name="companyName"
                                    value="<c:out value=" ${param.companyName}" />" placeholder="(주)예시회사">
                                <c:if test="${not empty fieldErrors.companyName}">
                                    <div class="field-error">
                                        <c:out value="${fieldErrors.companyName}" />
                                    </div>
                                </c:if>
                            </div>

                            <div class="form-group">
                                <label for="businessPhone" class="form-label">사업장 전화번호</label>
                                <input type="text" class="form-control" id="businessPhone" name="businessPhone"
                                    value="<c:out value=" ${param.businessPhone}" />" placeholder="02-1234-5678">
                                <c:if test="${not empty fieldErrors.businessPhone}">
                                    <div class="field-error">
                                        <c:out value="${fieldErrors.businessPhone}" />
                                    </div>
                                </c:if>
                            </div>

                            <div class="form-group">
                                <label for="businessAddress" class="form-label">사업장 주소</label>
                                <input type="text" class="form-control" id="businessAddress" name="businessAddress"
                                    value="<c:out value=" ${param.businessAddress}" />" placeholder="서울시 강남구 테헤란로 123">
                                <c:if test="${not empty fieldErrors.businessAddress}">
                                    <div class="field-error">
                                        <c:out value="${fieldErrors.businessAddress}" />
                                    </div>
                                </c:if>
                            </div>

                            <div class="form-group">
                                <label for="userType" class="form-label">사용자 유형 *</label>
                                <select class="form-control" id="userType" name="userType" required>
                                    <option value="">선택해주세요</option>
                                    <option value="OWNER" <c:if test="${param.userType == 'OWNER'}">selected</c:if>>사업자
                                        (계약서 생성/발송)
                                    </option>
                                    <option value="CONTRACTOR" <c:if test="${param.userType == 'CONTRACTOR'}">selected
                                        </c:if>>계약자 (서명
                                        전용)
                                    </option>
                                </select>
                                <div class="form-text">사업자는 계약서를 생성하고 발송할 수 있으며, 계약자는 서명만 가능합니다.</div>
                                <c:if test="${not empty fieldErrors.userType}">
                                    <div class="field-error">
                                        <c:out value="${fieldErrors.userType}" />
                                    </div>
                                </c:if>
                            </div>

                            <div class="form-group form-check">
                                <input type="checkbox" class="form-check-input" id="agreeTerms" name="agreeTerms"
                                    required>
                                <label class="form-check-label" for="agreeTerms">
                                    <a href="javascript:void(0)" id="link-terms">이용약관</a> 및 <a href="javascript:void(0)"
                                        id="link-privacy">개인정보처리방침</a>에
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

                <!-- 이용약관 모달 -->
                <div class="modal fade" id="termsModal" tabindex="-1" aria-labelledby="termsModalLabel"
                    aria-hidden="true">
                    <div class="modal-dialog modal-lg modal-dialog-scrollable">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h5 class="modal-title" id="termsModalLabel">이용약관</h5>
                                <button type="button" class="btn-close" data-bs-dismiss="modal"
                                    aria-label="Close"></button>
                            </div>
                            <div class="modal-body">
                                <h6>제1조 (목적)</h6>
                                <p>본 약관은 Signly(이하 "회사")가 제공하는 전자계약 서비스(이하 "서비스")의 이용과 관련하여 회사와 회원 간의 권리, 의무 및 책임사항, 기타
                                    필요한 사항을 규정함을 목적으로 합니다.</p>

                                <h6>제2조 (정의)</h6>
                                <p>1. "서비스"라 함은 구현되는 단말기(PC, TV, 휴대형단말기 등 각종 유무선 장치를 포함)와 상관없이 회원이 이용할 수 있는 Signly 전자계약
                                    및 관련 제반 서비스를 의미합니다.</p>
                                <p>2. "회원"이라 함은 회사의 "서비스"에 접속하여 이 약관에 따라 "회사"와 이용계약을 체결하고 "회사"가 제공하는 "서비스"를 이용하는 고객을
                                    말합니다.</p>

                                <h6>제3조 (약관의 게시와 개정)</h6>
                                <p>1. "회사"는 이 약관의 내용을 회원이 쉽게 알 수 있도록 서비스 초기 화면에 게시합니다.</p>
                                <p>2. "회사"는 "약관의 규제에 관한 법률", "정보통신망 이용촉진 및 정보보호 등에 관한 법률(이하 "정보통신망법")" 등 관련법을 위배하지 않는
                                    범위에서 이 약관을 개정할 수 있습니다.</p>

                                <h6>제4조 (서비스의 제공 등)</h6>
                                <p>1. 회사는 회원에게 아래와 같은 서비스를 제공합니다.</p>
                                <ul>
                                    <li>전자문서 작성 및 편집 서비스</li>
                                    <li>전자서명 서비스</li>
                                    <li>전자문서 보관 및 관리 서비스</li>
                                    <li>기타 회사가 추가 개발하거나 다른 회사와의 제휴계약 등을 통해 회원에게 제공하는 일체의 서비스</li>
                                </ul>

                                <h6>제5조 (서비스의 중단)</h6>
                                <p>1. 회사는 컴퓨터 등 정보통신설비의 보수점검, 교체 및 고장, 통신두절 또는 운영상 상당한 이유가 있는 경우 서비스의 제공을 일시적으로 중단할 수
                                    있습니다.</p>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 개인정보처리방침 모달 -->
                <div class="modal fade" id="privacyModal" tabindex="-1" aria-labelledby="privacyModalLabel"
                    aria-hidden="true">
                    <div class="modal-dialog modal-lg modal-dialog-scrollable">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h5 class="modal-title" id="privacyModalLabel">개인정보처리방침</h5>
                                <button type="button" class="btn-close" data-bs-dismiss="modal"
                                    aria-label="Close"></button>
                            </div>
                            <div class="modal-body">
                                <h6>1. 개인정보의 처리 목적</h6>
                                <p>Signly(이하 "회사")는 다음의 목적을 위하여 개인정보를 처리합니다. 처리하고 있는 개인정보는 다음의 목적 이외의 용도로는 이용되지 않으며 이용
                                    목적이 변경되는 경우에는 「개인정보 보호법」 제18조에 따라 별도의 동의를 받는 등 필요한 조치를 이행할 예정입니다.</p>
                                <ul>
                                    <li>회원 가입 의사 확인, 회원제 서비스 제공에 따른 본인 식별·인증, 회원자격 유지·관리, 서비스 부정이용 방지, 각종 고지·통지, 고충처리
                                        목적으로 개인정보를 처리합니다.</li>
                                </ul>

                                <h6>2. 개인정보의 처리 및 보유 기간</h6>
                                <p>① 회사는 법령에 따른 개인정보 보유·이용기간 또는 정보주체로부터 개인정보를 수집 시에 동의받은 개인정보 보유·이용기간 내에서 개인정보를
                                    처리·보유합니다.</p>
                                <p>② 각각의 개인정보 처리 및 보유 기간은 다음과 같습니다.</p>
                                <ul>
                                    <li>회원 가입 및 관리 : 회원 탈퇴 시까지</li>
                                    <li>다만, 다음의 사유에 해당하는 경우에는 해당 사유 종료 시까지
                                        <ul>
                                            <li>관계 법령 위반에 따른 수사·조사 등이 진행 중인 경우에는 해당 수사·조사 종료 시까지</li>
                                            <li>서비스 이용에 따른 채권·채무관계 잔존 시에는 해당 채권·채무관계 정산 시까지</li>
                                        </ul>
                                    </li>
                                </ul>

                                <h6>3. 정보주체와 법정대리인의 권리·의무 및 그 행사방법</h6>
                                <p>① 정보주체는 회사에 대해 언제든지 개인정보 열람·정정·삭제·처리정지 요구 등의 권리를 행사할 수 있습니다.</p>

                                <h6>4. 처리하는 개인정보의 항목 작성</h6>
                                <p>① 회사는 다음의 개인정보 항목을 처리하고 있습니다.</p>
                                <ul>
                                    <li>필수항목 : 이메일, 비밀번호, 이름, 회사명(선택), 전화번호(선택)</li>
                                </ul>

                                <h6>5. 개인정보의 파기</h6>
                                <p>① 회사는 개인정보 보유기간의 경과, 처리목적 달성 등 개인정보가 불필요하게 되었을 때에는 지체없이 해당 개인정보를 파기합니다.</p>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
                            </div>
                        </div>
                    </div>
                </div>

                <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
                <script src="/js/common.js"></script>
                <script src="/js/alerts.js"></script>
                <script src="/js/auth-pages.js"></script>
            </body>

            </html>