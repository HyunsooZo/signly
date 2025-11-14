// 페이지 전환 로딩 바
(function () {
    // NProgress 설정
    NProgress.configure({
        showSpinner: false,
        trickleSpeed: 200,
        minimum: 0.08
    });

    // 페이지 로드 시작
    window.addEventListener('beforeunload', function () {
        NProgress.start();
    });

    // 페이지 로드 완료
    window.addEventListener('load', function () {
        NProgress.done();
    });

    // 모든 링크에 로딩 바 추가
    document.addEventListener('click', function (e) {
        const link = e.target.closest('a');
        if (link && link.href && !link.target && !link.hasAttribute('download')) {
            // 같은 도메인의 링크만
            if (link.hostname === window.location.hostname) {
                NProgress.start();
            }
        }
    });

    // 폼 제출 시 로딩 바
    document.addEventListener('submit', function (e) {
        NProgress.start();
    });

    // 페이지 진입 시 즉시 완료
    NProgress.done();
})();
