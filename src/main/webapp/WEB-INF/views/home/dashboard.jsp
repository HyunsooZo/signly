<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="ko">
<jsp:include page="../common/header.jsp">
    <jsp:param name="additionalCss" value="/css/dashboard.css"/>
</jsp:include>
<body>
<jsp:include page="../common/navbar.jsp">
    <jsp:param name="currentPage" value="home"/>
</jsp:include>

<div class="container mt-4">
    <div class="main-content-card">
        <!-- 헤더 -->
        <div class="row mb-4">
            <div class="col-12">
                <h2 class="mb-2">
                    <i class="bi bi-house-door text-primary me-2"></i>
                    대시보드
                </h2>
                <p class="text-muted mb-0">Signly 전자계약 시스템에 오신 것을 환영합니다</p>
            </div>
        </div>

        <!-- 알림 메시지 -->
        <c:if test="${not empty successMessage}">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                <i class="bi bi-check-circle me-2"></i><c:out value="${successMessage}"/>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <i class="bi bi-exclamation-triangle me-2"></i><c:out value="${errorMessage}"/>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <!-- 프로필 완성 알림 -->
        <div id="profileCompletionAlert" class="alert alert-info alert-dismissible fade show" role="alert" style="display: none;">
            <div class="d-flex align-items-center">
                <i class="bi bi-person-check me-3 fs-4"></i>
                <div class="flex-grow-1">
                    <h6 class="alert-heading mb-1">프로필 정보를 완성해주세요</h6>
                    <p class="mb-2">Google 계정으로 로그인하셨습니다. 계약서 생성을 위해 회사 정보를 입력해주세요.</p>
                    <div class="d-flex align-items-center gap-2">
                        <button type="button" class="btn btn-primary btn-sm" onclick="goToProfile()">
                            <i class="bi bi-pencil-square me-1"></i>프로필 완성하기
                        </button>
                        <button type="button" class="btn btn-outline-secondary btn-sm" onclick="dismissProfileAlert()">
                            나중에 하기
                        </button>
                    </div>
                </div>
                <button type="button" class="btn-close" onclick="dismissProfileAlert()"></button>
            </div>
        </div>

        <!-- 통계 카드 -->
        <div class="row mb-4">
            <div class="col-md-6 col-lg-3 mb-3">
                <div class="stats-card bg-primary">
                    <div class="stats-content">
                        <div class="stats-icon">
                            <i class="bi bi-file-earmark-text"></i>
                        </div>
                        <div class="stats-info">
                            <h3><c:out value="${templateStats.total}"/></h3>
                            <p>총 템플릿</p>
                        </div>
                    </div>
                    <div class="stats-footer">
                        <a href="/templates" class="text-white text-decoration-none">
                            <small>템플릿 관리 <i class="bi bi-arrow-right"></i></small>
                        </a>
                    </div>
                </div>
            </div>

            <div class="col-md-6 col-lg-3 mb-3">
                <div class="stats-card bg-success">
                    <div class="stats-content">
                        <div class="stats-icon">
                            <i class="bi bi-check-circle"></i>
                        </div>
                        <div class="stats-info">
                            <h3><c:out value="${templateStats.active}"/></h3>
                            <p>활성 템플릿</p>
                        </div>
                    </div>
                    <div class="stats-footer">
                        <a href="/templates?status=ACTIVE" class="text-white text-decoration-none">
                            <small>활성 템플릿 보기 <i class="bi bi-arrow-right"></i></small>
                        </a>
                    </div>
                </div>
            </div>

            <div class="col-md-6 col-lg-3 mb-3">
                <div class="stats-card bg-info">
                    <div class="stats-content">
                        <div class="stats-icon">
                            <i class="bi bi-file-earmark-check"></i>
                        </div>
                        <div class="stats-info">
                            <h3><c:out value="${contractStats.total}"/></h3>
                            <p>총 계약서</p>
                        </div>
                    </div>
                    <div class="stats-footer">
                        <a href="/contracts" class="text-white text-decoration-none">
                            <small>계약서 관리 <i class="bi bi-arrow-right"></i></small>
                        </a>
                    </div>
                </div>
            </div>

            <div class="col-md-6 col-lg-3 mb-3">
                <div class="stats-card bg-warning">
                    <div class="stats-content">
                        <div class="stats-icon">
                            <i class="bi bi-clock"></i>
                        </div>
                        <div class="stats-info">
                            <h3><c:out value="${contractStats.pending}"/></h3>
                            <p>서명 대기</p>
                        </div>
                    </div>
                    <div class="stats-footer">
                        <a href="/contracts?status=PENDING" class="text-white text-decoration-none">
                            <small>대기 중인 계약서 <i class="bi bi-arrow-right"></i></small>
                        </a>
                    </div>
                </div>
            </div>
        </div>

        <!-- 빠른 작업 -->
        <div class="row mb-4">
            <div class="col-12">
                <div class="card">
                    <div class="card-header">
                        <h5 class="card-title mb-0">
                            <i class="bi bi-lightning me-2"></i>빠른 작업
                        </h5>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-6 col-lg-3 mb-3">
                                <a href="/templates/new" class="quick-action-btn">
                                    <div class="quick-action-icon bg-primary">
                                        <i class="bi bi-file-earmark-plus"></i>
                                    </div>
                                    <div class="quick-action-content">
                                        <h6>새 템플릿 생성</h6>
                                        <small class="text-muted">계약서 템플릿을 새로 만들어보세요</small>
                                    </div>
                                </a>
                            </div>

                            <div class="col-md-6 col-lg-3 mb-3">
                                <a href="/contracts/new" class="quick-action-btn">
                                    <div class="quick-action-icon bg-success">
                                        <i class="bi bi-file-earmark-check"></i>
                                    </div>
                                    <div class="quick-action-content">
                                        <h6>새 계약서 생성</h6>
                                        <small class="text-muted">템플릿을 사용해 계약서를 생성하세요</small>
                                    </div>
                                </a>
                            </div>

                            <div class="col-md-6 col-lg-3 mb-3">
                                <a href="/templates" class="quick-action-btn">
                                    <div class="quick-action-icon bg-info">
                                        <i class="bi bi-collection"></i>
                                    </div>
                                    <div class="quick-action-content">
                                        <h6>템플릿 관리</h6>
                                        <small class="text-muted">기존 템플릿을 수정하거나 관리하세요</small>
                                    </div>
                                </a>
                            </div>

                            <div class="col-md-6 col-lg-3 mb-3">
                                <a href="/contracts" class="quick-action-btn">
                                    <div class="quick-action-icon bg-warning">
                                        <i class="bi bi-clipboard-check"></i>
                                    </div>
                                    <div class="quick-action-content">
                                        <h6>계약서 관리</h6>
                                        <small class="text-muted">진행 중인 계약서를 확인하세요</small>
                                    </div>
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 최근 활동 -->
        <div class="row">
            <!-- 최근 템플릿 -->
            <div class="col-lg-6 mb-4">
                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h5 class="card-title mb-0">
                            <i class="bi bi-file-earmark-text me-2"></i>최근 템플릿
                        </h5>
                        <a href="/templates" class="btn btn-sm btn-outline-primary">전체보기</a>
                    </div>
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${empty recentTemplates}">
                                <div class="text-center py-4">
                                    <i class="bi bi-file-earmark-text display-4 text-muted"></i>
                                    <p class="text-muted mt-2">아직 생성된 템플릿이 없습니다</p>
                                    <a href="/templates/new" class="btn btn-primary btn-sm">첫 번째 템플릿 생성</a>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="template" items="${recentTemplates}">
                                    <div class="recent-item">
                                        <div class="recent-item-icon">
                                            <i class="bi bi-file-earmark-text text-primary"></i>
                                        </div>
                                        <div class="recent-item-content">
                                            <h6 class="mb-1">
                                                <a href="/templates/<c:out value='${template.templateId}'/>"
                                                   class="text-decoration-none">
                                                    <c:out value="${template.title}"/>
                                                </a>
                                            </h6>
                                            <small class="text-muted">
                                                <fmt:formatDate value="${template.updatedAtDate}"
                                                                pattern="yyyy-MM-dd HH:mm"/>
                                            </small>
                                        </div>
                                        <div class="recent-item-status">
                                            <c:choose>
                                                <c:when test="${template.status == 'DRAFT'}">
                                                    <span class="badge bg-secondary">초안</span>
                                                </c:when>
                                                <c:when test="${template.status == 'ACTIVE'}">
                                                    <span class="badge bg-success">활성</span>
                                                </c:when>
                                                <c:when test="${template.status == 'ARCHIVED'}">
                                                    <span class="badge bg-warning">보관</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-light text-dark"><c:out
                                                            value="${template.status}"/></span>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <!-- 최근 계약서 -->
            <div class="col-lg-6 mb-4">
                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h5 class="card-title mb-0">
                            <i class="bi bi-file-earmark-check me-2"></i>최근 계약서
                        </h5>
                        <a href="/contracts" class="btn btn-sm btn-outline-primary">전체보기</a>
                    </div>
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${empty recentContracts}">
                                <div class="text-center py-4">
                                    <i class="bi bi-file-earmark-check display-4 text-muted"></i>
                                    <p class="text-muted mt-2">아직 생성된 계약서가 없습니다</p>
                                    <a href="/contracts/new" class="btn btn-primary btn-sm">첫 번째 계약서 생성</a>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="contract" items="${recentContracts}">
                                    <div class="recent-item">
                                        <div class="recent-item-icon">
                                            <i class="bi bi-file-earmark-check text-success"></i>
                                        </div>
                                        <div class="recent-item-content">
                                            <h6 class="mb-1">
                                                <a href="/contracts/<c:out value='${contract.id}'/>"
                                                   class="text-decoration-none">
                                                    <c:out value="${contract.title}"/>
                                                </a>
                                            </h6>
                                            <small class="text-muted">
                                                <fmt:formatDate value="${contract.updatedAt}"
                                                                pattern="yyyy-MM-dd HH:mm"/>
                                            </small>
                                        </div>
                                        <div class="recent-item-status">
                                            <c:choose>
                                                <c:when test="${contract.status == 'DRAFT'}">
                                                    <span class="badge bg-secondary">초안</span>
                                                </c:when>
                                                <c:when test="${contract.status == 'PENDING'}">
                                                    <span class="badge bg-warning">서명 대기</span>
                                                </c:when>
                                                <c:when test="${contract.status == 'SIGNED'}">
                                                    <span class="badge bg-success">서명 완료</span>
                                                </c:when>
                                                <c:when test="${contract.status == 'CANCELLED'}">
                                                    <span class="badge bg-danger">취소</span>
                                                </c:when>
                                                <c:when test="${contract.status == 'EXPIRED'}">
                                                    <span class="badge bg-dark">만료</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-light text-dark"><c:out
                                                            value="${contract.status}"/></span>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    // OAuth2 토큰 처리 (쿼리 파라미터로 전달된 경우)
    (function () {
        const urlParams = new URLSearchParams(window.location.search);
        const accessToken = urlParams.get('access_token');
        const refreshToken = urlParams.get('refresh_token');

        if (accessToken && refreshToken) {
            console.log('[INFO] OAuth2 로그인 성공 - 토큰 저장');
            localStorage.setItem('signly_access_token', accessToken);
            localStorage.setItem('signly_refresh_token', refreshToken);

            // URL에서 토큰 제거 (보안)
            window.history.replaceState({}, document.title, '/home');
        }
    })();

    // 로그인 시 사용자 정보를 로컬스토리지에 저장
    <c:if test="${not empty currentUserName}">
    (function () {
        const userInfo = {
            name: '${currentUserName}',
            email: '${currentUserEmail}',
            userId: '${currentUserId}',
            companyName: '${currentUserCompany}' || '',
            businessPhone: '${currentUserBusinessPhone}' || '',
            businessAddress: '${currentUserBusinessAddress}' || '',
            updatedAt: new Date().toISOString()
        };
        localStorage.setItem('signly_user_info', JSON.stringify(userInfo));
        console.log('[INFO] 사용자 정보 로컬스토리지 저장:', userInfo);

        if (userInfo.userId) {
            fetch('/api/first-party-signature/me', {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                    'X-User-Id': userInfo.userId
                }
            }).then(response => {
                if (response.status === 204) {
                    localStorage.removeItem('signly_owner_signature');
                    return null;
                }
                if (!response.ok) {
                    throw new Error('status ' + response.status);
                }
                return response.json();
            }).then(payload => {
                if (!payload) {
                    return;
                }
                const signaturePayload = {
                    dataUrl: payload.dataUrl,
                    updatedAt: payload.updatedAt || new Date().toISOString()
                };
                localStorage.setItem('signly_owner_signature', JSON.stringify(signaturePayload));
                console.log('[INFO] 사업주 서명 정보를 로컬스토리지에 동기화했습니다.');
            }).catch(error => {
                console.warn('[WARN] 사업주 서명 정보를 가져오지 못했습니다:', error);
            });
        }
    })();
    </c:if>

    // 프로필 완성 상태 확인 및 알림 표시
    (function () {
        // 페이지 로드 후 프로필 상태 확인
        checkProfileStatus();
    })();

    function checkProfileStatus() {
        fetch('/api/profile-status', {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('프로필 상태를 확인할 수 없습니다.');
            }
            return response.json();
        })
        .then(data => {
            console.log('[INFO] 프로필 상태:', data);
            
            // 프로필이 완성되지 않은 경우 알림 표시
            if (!data.isProfileComplete) {
                showProfileCompletionAlert(data);
            }
        })
        .catch(error => {
            console.warn('[WARN] 프로필 상태 확인 실패:', error);
        });
    }

    function showProfileCompletionAlert(profileData) {
        const alertElement = document.getElementById('profileCompletionAlert');
        if (alertElement) {
            alertElement.style.display = 'block';
            
            // 누락된 필드에 따라 메시지 동적 변경
            if (profileData.missingFields && profileData.missingFields.company) {
                const messageElement = alertElement.querySelector('p');
                messageElement.textContent = `${profileData.userName}님, 계약서 생성을 위해 회사 정보를 입력해주세요.`;
            }
        }
    }

    function dismissProfileAlert() {
        const alertElement = document.getElementById('profileCompletionAlert');
        if (alertElement) {
            alertElement.style.display = 'none';
            // 24시간 동안 알림 숨김 (localStorage 사용)
            localStorage.setItem('profile_alert_dismissed', Date.now().toString());
        }
    }

    function goToProfile() {
        // 프로필 페이지로 이동
        window.location.href = '/profile/info';
    }
</script>
<jsp:include page="../common/footer.jsp"/>
</body>
</html>
