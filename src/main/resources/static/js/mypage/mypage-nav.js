/**
 * mypage-nav.js
 * 모바일 환경에서 sidebar-nav의 현재 활성 탭(active)이
 * 화면 중앙에 보이도록 자동 스크롤합니다.
 * PC(769px 이상)에서는 동작하지 않습니다.
 */
(function () {
    'use strict';

    function scrollNavToActive() {
        // 모바일 환경에서만 실행
        if (window.innerWidth > 768) return;

        var nav = document.getElementById('sidebarNav');
        if (!nav) return;

        var activeTab = nav.querySelector('.menu-button.active');
        if (!activeTab) return;

        // 활성 탭의 중심을 nav의 중심에 맞추는 scrollLeft 계산
        var navWidth     = nav.offsetWidth;
        var tabOffsetLeft = activeTab.offsetLeft;
        var tabWidth     = activeTab.offsetWidth;

        var targetScrollLeft = tabOffsetLeft - (navWidth / 2) + (tabWidth / 2);

        // 음수 방지
        targetScrollLeft = Math.max(0, targetScrollLeft);

        nav.scrollLeft = targetScrollLeft;
    }

    // DOM이 완전히 준비된 후 실행
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', scrollNavToActive);
    } else {
        scrollNavToActive();
    }

    // 화면 회전 시 재계산
    window.addEventListener('orientationchange', function () {
        setTimeout(scrollNavToActive, 100);
    });

    window.addEventListener('resize', function () {
        scrollNavToActive();
    });

}());