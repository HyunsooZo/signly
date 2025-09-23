<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title><c:out value="${pageTitle}" /></title>

    <!-- CSS -->
    <link href="<c:url value='/css/common.css' />" rel="stylesheet">

    <!-- 메타 태그 -->
    <meta name="description" content="Signly - 안전하고 간편한 전자계약 서비스">
    <meta name="keywords" content="전자계약, 전자서명, 디지털 계약, e-signature">
    <meta name="author" content="Signly">
</head>
<body>
    <!-- 헤더 -->
    <jsp:include page="../common/header.jsp" />

    <!-- 메인 컨텐츠 -->
    <main class="main-content">
        <!-- 히어로 섹션 -->
        <section class="hero-section">
            <div class="hero-container">
                <div class="hero-content">
                    <h1 class="hero-title">
                        안전하고 간편한<br>
                        <span class="text-primary">전자계약 서비스</span>
                    </h1>
                    <p class="hero-description">
                        Signly와 함께 종이 계약서의 번거로움을 없애고,<br>
                        언제 어디서나 안전하게 계약을 체결하세요.
                    </p>
                    <div class="hero-actions">
                        <c:choose>
                            <c:when test="${not empty sessionScope.user}">
                                <a href="<c:url value='/contracts' />" class="btn btn-primary btn-lg">
                                    계약서 관리하기
                                </a>
                                <a href="<c:url value='/templates' />" class="btn btn-outline btn-lg">
                                    템플릿 만들기
                                </a>
                            </c:when>
                            <c:otherwise>
                                <a href="<c:url value='/register' />" class="btn btn-primary btn-lg">
                                    무료로 시작하기
                                </a>
                                <a href="<c:url value='/about' />" class="btn btn-outline btn-lg">
                                    서비스 알아보기
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
                <div class="hero-image">
                    <img src="<c:url value='/images/hero-illustration.svg' />" alt="전자계약 서비스" class="hero-img">
                </div>
            </div>
        </section>

        <!-- 주요 기능 섹션 -->
        <section class="features-section">
            <div class="container">
                <div class="section-header">
                    <h2 class="section-title">왜 Signly를 선택해야 할까요?</h2>
                    <p class="section-description">
                        비즈니스의 효율성을 높이는 전자계약의 핵심 기능들을 제공합니다.
                    </p>
                </div>

                <div class="features-grid">
                    <div class="feature-card">
                        <div class="feature-icon">📝</div>
                        <h3 class="feature-title">간편한 계약서 작성</h3>
                        <p class="feature-description">
                            미리 준비된 템플릿으로 빠르게 계약서를 작성하고,
                            필요에 따라 자유롭게 커스터마이징할 수 있습니다.
                        </p>
                    </div>

                    <div class="feature-card">
                        <div class="feature-icon">✍️</div>
                        <h3 class="feature-title">안전한 전자서명</h3>
                        <p class="feature-description">
                            법적 효력이 인정되는 전자서명으로 언제 어디서나
                            안전하게 계약을 체결할 수 있습니다.
                        </p>
                    </div>

                    <div class="feature-card">
                        <div class="feature-icon">📱</div>
                        <h3 class="feature-title">모바일 지원</h3>
                        <p class="feature-description">
                            PC는 물론 스마트폰, 태블릿에서도 자유롭게
                            계약서를 확인하고 서명할 수 있습니다.
                        </p>
                    </div>

                    <div class="feature-card">
                        <div class="feature-icon">🔒</div>
                        <h3 class="feature-title">보안 및 암호화</h3>
                        <p class="feature-description">
                            금융권 수준의 보안 기술로 계약서와 개인정보를
                            안전하게 보호합니다.
                        </p>
                    </div>

                    <div class="feature-card">
                        <div class="feature-icon">📊</div>
                        <h3 class="feature-title">계약 현황 관리</h3>
                        <p class="feature-description">
                            실시간으로 계약 진행 상황을 확인하고,
                            체계적으로 계약서를 관리할 수 있습니다.
                        </p>
                    </div>

                    <div class="feature-card">
                        <div class="feature-icon">⚡</div>
                        <h3 class="feature-title">빠른 처리</h3>
                        <p class="feature-description">
                            기존 종이 계약 대비 90% 단축된 시간으로
                            계약 프로세스를 완료할 수 있습니다.
                        </p>
                    </div>
                </div>
            </div>
        </section>

        <!-- CTA 섹션 -->
        <c:if test="${empty sessionScope.user}">
            <section class="cta-section">
                <div class="container">
                    <div class="cta-content">
                        <h2 class="cta-title">지금 바로 시작하세요!</h2>
                        <p class="cta-description">
                            Signly와 함께 더 효율적인 비즈니스를 만들어보세요.
                        </p>
                        <div class="cta-actions">
                            <a href="<c:url value='/register' />" class="btn btn-primary btn-lg">
                                무료 회원가입
                            </a>
                            <a href="<c:url value='/contracts/search' />" class="btn btn-secondary btn-lg">
                                계약서 조회하기
                            </a>
                        </div>
                    </div>
                </div>
            </section>
        </c:if>
    </main>

    <!-- 푸터 -->
    <jsp:include page="../common/footer.jsp" />

    <!-- JavaScript -->
    <script src="<c:url value='/js/common.js' />"></script>
</body>
</html>

<style>
/* 히어로 섹션 */
.hero-section {
    background: linear-gradient(135deg, var(--primary-color) 0%, #3b82f6 100%);
    color: white;
    padding: 4rem 0;
    min-height: 600px;
    display: flex;
    align-items: center;
}

.hero-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 1rem;
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 4rem;
    align-items: center;
}

.hero-title {
    font-size: 3rem;
    font-weight: 800;
    line-height: 1.2;
    margin-bottom: 1.5rem;
}

.hero-description {
    font-size: 1.25rem;
    line-height: 1.6;
    opacity: 0.9;
    margin-bottom: 2rem;
}

.hero-actions {
    display: flex;
    gap: 1rem;
    flex-wrap: wrap;
}

.hero-img {
    width: 100%;
    height: auto;
    max-width: 500px;
}

/* 기능 섹션 */
.features-section {
    padding: 5rem 0;
    background-color: white;
}

.section-header {
    text-align: center;
    margin-bottom: 4rem;
}

.section-title {
    font-size: 2.5rem;
    font-weight: 700;
    color: var(--gray-900);
    margin-bottom: 1rem;
}

.section-description {
    font-size: 1.125rem;
    color: var(--gray-600);
    max-width: 600px;
    margin: 0 auto;
}

.features-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 2rem;
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 1rem;
}

.feature-card {
    background-color: white;
    padding: 2rem;
    border-radius: var(--border-radius-lg);
    box-shadow: var(--box-shadow);
    text-align: center;
    transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.feature-card:hover {
    transform: translateY(-4px);
    box-shadow: var(--box-shadow-lg);
}

.feature-icon {
    font-size: 3rem;
    margin-bottom: 1rem;
}

.feature-title {
    font-size: 1.5rem;
    font-weight: 600;
    color: var(--gray-900);
    margin-bottom: 1rem;
}

.feature-description {
    color: var(--gray-600);
    line-height: 1.6;
}

/* CTA 섹션 */
.cta-section {
    background-color: var(--gray-50);
    padding: 4rem 0;
}

.cta-content {
    text-align: center;
    max-width: 800px;
    margin: 0 auto;
    padding: 0 1rem;
}

.cta-title {
    font-size: 2.5rem;
    font-weight: 700;
    color: var(--gray-900);
    margin-bottom: 1rem;
}

.cta-description {
    font-size: 1.25rem;
    color: var(--gray-600);
    margin-bottom: 2rem;
}

.cta-actions {
    display: flex;
    gap: 1rem;
    justify-content: center;
    flex-wrap: wrap;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
    .hero-container {
        grid-template-columns: 1fr;
        gap: 2rem;
        text-align: center;
    }

    .hero-title {
        font-size: 2rem;
    }

    .hero-description {
        font-size: 1rem;
    }

    .section-title {
        font-size: 2rem;
    }

    .cta-title {
        font-size: 2rem;
    }

    .cta-actions {
        flex-direction: column;
        align-items: center;
    }

    .features-grid {
        grid-template-columns: 1fr;
    }
}
</style>