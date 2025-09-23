<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<footer class="footer">
    <div class="footer-container">
        <div class="footer-content">
            <!-- 회사 정보 -->
            <div class="footer-section">
                <div class="footer-logo">
                    <img src="<c:url value='/images/logo.png' />" alt="Signly" class="footer-logo-img">
                    <span class="footer-logo-text">Signly</span>
                </div>
                <p class="footer-description">
                    안전하고 간편한 전자계약 서비스로<br>
                    비즈니스의 효율성을 높이세요.
                </p>
                <div class="footer-social">
                    <a href="#" class="social-link" title="페이스북">📘</a>
                    <a href="#" class="social-link" title="트위터">🐦</a>
                    <a href="#" class="social-link" title="링크드인">💼</a>
                    <a href="#" class="social-link" title="유튜브">📺</a>
                </div>
            </div>

            <!-- 서비스 링크 -->
            <div class="footer-section">
                <h3 class="footer-title">서비스</h3>
                <ul class="footer-links">
                    <li><a href="<c:url value='/about' />" class="footer-link">서비스 소개</a></li>
                    <li><a href="<c:url value='/pricing' />" class="footer-link">요금제</a></li>
                    <li><a href="<c:url value='/features' />" class="footer-link">주요 기능</a></li>
                    <li><a href="<c:url value='/templates' />" class="footer-link">템플릿</a></li>
                </ul>
            </div>

            <!-- 지원 -->
            <div class="footer-section">
                <h3 class="footer-title">지원</h3>
                <ul class="footer-links">
                    <li><a href="<c:url value='/help' />" class="footer-link">도움말</a></li>
                    <li><a href="<c:url value='/faq' />" class="footer-link">자주 묻는 질문</a></li>
                    <li><a href="<c:url value='/contact' />" class="footer-link">문의하기</a></li>
                    <li><a href="<c:url value='/api-docs' />" class="footer-link">API 문서</a></li>
                </ul>
            </div>

            <!-- 법적 정보 -->
            <div class="footer-section">
                <h3 class="footer-title">법적 정보</h3>
                <ul class="footer-links">
                    <li><a href="<c:url value='/terms' />" class="footer-link">이용약관</a></li>
                    <li><a href="<c:url value='/privacy' />" class="footer-link">개인정보처리방침</a></li>
                    <li><a href="<c:url value='/security' />" class="footer-link">보안정책</a></li>
                    <li><a href="<c:url value='/legal' />" class="footer-link">법적 고지</a></li>
                </ul>
            </div>
        </div>

        <!-- 하단 정보 -->
        <div class="footer-bottom">
            <div class="footer-bottom-content">
                <p class="copyright">
                    © <fmt:formatDate value="<%=new java.util.Date()%>" pattern="yyyy" /> Signly. All rights reserved.
                </p>
                <div class="footer-bottom-links">
                    <a href="<c:url value='/sitemap' />" class="footer-bottom-link">사이트맵</a>
                    <span class="separator">|</span>
                    <a href="<c:url value='/accessibility' />" class="footer-bottom-link">접근성</a>
                    <span class="separator">|</span>
                    <a href="<c:url value='/status' />" class="footer-bottom-link">서비스 상태</a>
                </div>
            </div>
        </div>
    </div>
</footer>

<style>
.footer {
    background-color: var(--gray-900);
    color: var(--gray-300);
    margin-top: auto;
}

.footer-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 3rem 1rem 0;
}

.footer-content {
    display: grid;
    grid-template-columns: 2fr 1fr 1fr 1fr;
    gap: 2rem;
    padding-bottom: 2rem;
}

.footer-section {
    display: flex;
    flex-direction: column;
}

.footer-logo {
    display: flex;
    align-items: center;
    margin-bottom: 1rem;
}

.footer-logo-img {
    height: 28px;
    width: auto;
    margin-right: 0.5rem;
    filter: brightness(0) invert(1);
}

.footer-logo-text {
    font-size: var(--font-size-lg);
    font-weight: 700;
    color: white;
}

.footer-description {
    color: var(--gray-400);
    line-height: 1.6;
    margin-bottom: 1.5rem;
}

.footer-social {
    display: flex;
    gap: 0.5rem;
}

.social-link {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    background-color: var(--gray-800);
    border-radius: 50%;
    text-decoration: none;
    transition: background-color 0.2s ease;
}

.social-link:hover {
    background-color: var(--primary-color);
}

.footer-title {
    font-size: var(--font-size-lg);
    font-weight: 600;
    color: white;
    margin-bottom: 1rem;
}

.footer-links {
    list-style: none;
    padding: 0;
    margin: 0;
}

.footer-links li {
    margin-bottom: 0.5rem;
}

.footer-link {
    color: var(--gray-400);
    text-decoration: none;
    transition: color 0.2s ease;
}

.footer-link:hover {
    color: white;
}

.footer-bottom {
    border-top: 1px solid var(--gray-800);
    padding: 1.5rem 0;
}

.footer-bottom-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 1rem;
}

.copyright {
    color: var(--gray-500);
    margin: 0;
}

.footer-bottom-links {
    display: flex;
    align-items: center;
    gap: 1rem;
}

.footer-bottom-link {
    color: var(--gray-500);
    text-decoration: none;
    font-size: var(--font-size-sm);
    transition: color 0.2s ease;
}

.footer-bottom-link:hover {
    color: var(--gray-300);
}

.separator {
    color: var(--gray-600);
}

/* 반응형 디자인 */
@media (max-width: 768px) {
    .footer-content {
        grid-template-columns: 1fr;
        gap: 2rem;
    }

    .footer-bottom-content {
        flex-direction: column;
        text-align: center;
    }

    .footer-bottom-links {
        justify-content: center;
    }
}

@media (max-width: 1024px) and (min-width: 769px) {
    .footer-content {
        grid-template-columns: 2fr 1fr 1fr;
    }

    .footer-section:last-child {
        grid-column: 2 / 4;
        display: grid;
        grid-template-columns: 1fr 1fr;
    }
}
</style>