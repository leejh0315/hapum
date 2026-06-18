document.addEventListener('DOMContentLoaded', function () {

    /* ─────────────────────────────────────────
       유틸: 모바일 판별
    ───────────────────────────────────────── */
    function isMobile() {
        return window.innerWidth <= 768;
    }

    /* ─────────────────────────────────────────
       퀵 링크 4글자 two-line 처리 (기존 유지)
    ───────────────────────────────────────── */
    document.querySelectorAll('.link-card span').forEach(span => {
        if (span.textContent.trim().length === 4) {
            span.classList.add('two-line');
        }
    });

    /* ═══════════════════════════════════════════
       0. 스크롤 진행 바
    ═══════════════════════════════════════════ */
    const progressBar = document.createElement('div');
    progressBar.id = 'scroll-progress-bar';
    document.body.prepend(progressBar);

    function updateProgressBar() {
        const d = document.documentElement;
        const pct = (window.scrollY / (d.scrollHeight - window.innerHeight)) * 100;
        progressBar.style.width = Math.min(pct, 100) + '%';
    }

    /* ═══════════════════════════════════════════
       1. 히어로 슬라이드 동적 빌드 (기존 로직 100% 유지)
    ═══════════════════════════════════════════ */
    const heroSlider = document.getElementById('heroSlider');

    const slideData = [
        {
            img: '/img/main/main1.jpg',
            h2: "함께 성장하며 서로 행복을 나누는",
            h1: "하느님 품, <span>하품센터</span>"
        },
        {
            img: '/img/main/main2.jpg',
            h2: "청소년과 청년들을 위한",
            h1: "신앙과 문화의 <span>복합 사목 공간</span>"
        },
        {
            img: '/img/main/main3.jpg',
            h2: "주님 안에서 꿈을 꾸고 기쁨을 가꾸는",
            h1: "우리들의 <span>따뜻한 보금자리</span>"
        },
        {
            img: '/img/main/main4.jpg',
            h2: "함께 기도하고 기쁘게 찬미하는",
            h1: "젊은 신앙인들의 <span>아름다운 쉼터</span>"
        }
    ];

    if (heroSlider) {
        heroSlider.innerHTML = '';
        slideData.forEach((slide, index) => {
            const slideDiv = document.createElement('div');
            slideDiv.className = `slide${index === 0 ? ' active' : ''}`;
            slideDiv.style.backgroundImage = `url('${slide.img}')`;
            slideDiv.innerHTML = `
                <div class="slide-content">
                    <h2>${slide.h2}</h2>
                    <h1>${slide.h1}</h1>
                </div>
            `;
            heroSlider.appendChild(slideDiv);
        });
    }

    /* ─────────────────────────────────────────
       1-1. 슬라이드 인디케이터 (기존 로직 유지)
    ───────────────────────────────────────── */
    const indicatorsContainer = document.getElementById('heroIndicators');

    function buildIndicators(count) {
        if (!indicatorsContainer) return;
        indicatorsContainer.innerHTML = '';
        for (let i = 0; i < count; i++) {
            const dot = document.createElement('span');
            dot.className = 'dot' + (i === 0 ? ' active' : '');
            dot.addEventListener('click', () => { goToSlide(i); resetInterval(); });
            indicatorsContainer.appendChild(dot);
        }
    }

    function updateIndicators(index) {
        if (!indicatorsContainer) return;
        indicatorsContainer.querySelectorAll('.dot').forEach((dot, i) => {
            dot.classList.toggle('active', i === index);
        });
    }

    /* ═══════════════════════════════════════════
       2. 히어로 패럴랙스 + 텍스트 페이드아웃 (기존 유지)
    ═══════════════════════════════════════════ */
    const heroSection   = document.querySelector('.hero-section');
    const heroSlides    = document.querySelectorAll('.hero-slider .slide');
    const slideContents = document.querySelectorAll('.slide-content');

    function updateHeroParallax() {
        if (!heroSection || isMobile()) return;
        const scrollY = window.scrollY;
        const heroH   = heroSection.offsetHeight;

        heroSlides.forEach(slide => {
            slide.style.backgroundPositionY = `calc(50% + ${scrollY * 0.4}px)`;
        });

        const fadeRatio = Math.min(scrollY / (heroH * 0.5), 1);
        slideContents.forEach(content => {
            content.style.opacity   = String(1 - fadeRatio);
            content.style.transform = `translateY(${fadeRatio * -30}px)`;
        });
    }

    /* ═══════════════════════════════════════════
       3. 히어로 슬라이드 자동 롤링 (기존 로직 100% 유지)
    ═══════════════════════════════════════════ */
    let dynamicSlides = document.querySelectorAll('.hero-slider .slide');
    const heroPrev    = document.getElementById('hero-prev');
    const heroNext    = document.getElementById('hero-next');
    let currentSlide  = 0;
    let slideInterval;

    requestAnimationFrame(() => {
        dynamicSlides = document.querySelectorAll('.hero-slider .slide');
        buildIndicators(dynamicSlides.length);

        function goToSlide(index) {
            dynamicSlides.forEach(s => s.classList.remove('active'));
            currentSlide = (index + dynamicSlides.length) % dynamicSlides.length;
            dynamicSlides[currentSlide].classList.add('active');
            updateIndicators(currentSlide);
        }

        function nextSlide() { goToSlide(currentSlide + 1); }
        function prevSlide() { goToSlide(currentSlide - 1); }

        function resetInterval() {
            clearInterval(slideInterval);
            slideInterval = setInterval(nextSlide, 5000);
        }

        if (dynamicSlides.length > 0) {
            slideInterval = setInterval(nextSlide, 5000);
            if (heroNext) heroNext.addEventListener('click', () => { nextSlide(); resetInterval(); });
            if (heroPrev) heroPrev.addEventListener('click', () => { prevSlide(); resetInterval(); });
        }

        let heroTouchStartX = 0;
        let heroTouchEndX   = 0;
        const SWIPE_THRESHOLD = 50;

        if (heroSlider) {
            heroSlider.addEventListener('touchstart', e => {
                heroTouchStartX = e.changedTouches[0].clientX;
            }, { passive: true });

            heroSlider.addEventListener('touchend', e => {
                heroTouchEndX = e.changedTouches[0].clientX;
                const delta = heroTouchStartX - heroTouchEndX;
                if (Math.abs(delta) > SWIPE_THRESHOLD) {
                    if (delta > 0) nextSlide();
                    else prevSlide();
                    resetInterval();
                }
            }, { passive: true });
        }
    });

    function goToSlide(index) {
        dynamicSlides = document.querySelectorAll('.hero-slider .slide');
        dynamicSlides.forEach(s => s.classList.remove('active'));
        currentSlide = (index + dynamicSlides.length) % dynamicSlides.length;
        dynamicSlides[currentSlide].classList.add('active');
        updateIndicators(currentSlide);
    }

    /* ═══════════════════════════════════════════
       4. 프로그램 가로 슬라이더 (기존 로직 100% 유지)
    ═══════════════════════════════════════════ */
    const programTrack = document.getElementById('programTrack');
    const progPrev     = document.getElementById('prog-prev');
    const progNext     = document.getElementById('prog-next');

    if (programTrack && progPrev && progNext) {
        function getProgramScrollStep() {
            const card = programTrack.querySelector('.program-card');
            return card ? card.offsetWidth + 20 : 300;
        }
        progNext.addEventListener('click', () => {
            programTrack.scrollBy({ left: getProgramScrollStep(), behavior: 'smooth' });
        });
        progPrev.addEventListener('click', () => {
            programTrack.scrollBy({ left: -getProgramScrollStep(), behavior: 'smooth' });
        });
    }

    /* ═══════════════════════════════════════════
       6. 관리자 공지 순서 수정 (기존 로직 100% 유지)
    ═══════════════════════════════════════════ */
    const editOrderBtn = document.getElementById('edit-order-btn');
    if (editOrderBtn) {
        editOrderBtn.addEventListener('click', () => {
            alert('순서 수정 모드');
        });
    }

    /* ═══════════════════════════════════════════
       7. SVG 드로우 아이콘 시스템
       ─ CSS 에서 dasharray/offset 절대 건드리지 않음
       ─ JS 가 getTotalLength() 측정 후 inline style 로만 제어
       ─ startDrawAnimations() 호출 시점:
           팝업 있음 → 마지막 팝업 닫힌 직후
           팝업 없음 → DOMContentLoaded 즉시
    ═══════════════════════════════════════════ */
    const pathMeta = []; // { path, len }

    // 1단계: 모든 path 길이 측정 + 초기 숨김 상태 세팅 (팝업 유무와 무관하게 즉시 실행)
    document.querySelectorAll('.draw-icon').forEach(svg => {
        svg.querySelectorAll('path').forEach(path => {
            const len = path.getTotalLength() + 2;
            path.style.strokeDasharray  = len;
            path.style.strokeDashoffset = len;
            path.style.transition       = 'none';
            pathMeta.push({ path, len });
        });
    });

    // 2단계: visibility 공개 (측정 완료 후 바로, 드로우는 아직 안 함)
    requestAnimationFrame(() => {
        requestAnimationFrame(() => {
            document.querySelectorAll('.draw-icon').forEach(svg => svg.classList.add('ready'));
        });
    });

    function drawPath(path, len, duration, delay) {
        path.style.transition       = 'none';
        path.style.strokeDashoffset = len;
        requestAnimationFrame(() => {
            requestAnimationFrame(() => {
                path.style.transition       = `stroke-dashoffset ${duration}ms cubic-bezier(.4,0,.2,1) ${delay}ms`;
                path.style.strokeDashoffset = '0';
            });
        });
    }

    function erasePath(path, len) {
        path.style.transition       = `stroke-dashoffset 400ms cubic-bezier(.4,0,.6,1)`;
        path.style.strokeDashoffset = len;
    }

    function drawCard(cardEl, baseDelay) {
        cardEl.querySelectorAll('.draw-icon path').forEach(path => {
            const meta = pathMeta.find(m => m.path === path);
            if (meta) drawPath(path, meta.len, 1200, baseDelay);
        });
    }

    function eraseCard(cardEl) {
        cardEl.querySelectorAll('.draw-icon path').forEach(path => {
            const meta = pathMeta.find(m => m.path === path);
            if (meta) erasePath(path, meta.len);
        });
    }

    // 퀵링크 카드 Observer (드로우 시스템 준비만, 등록은 startDrawAnimations 에서)
    const linkCardObs = new IntersectionObserver(entries => {
        entries.forEach(entry => {
            const card = entry.target;
            const idx  = [...document.querySelectorAll('.link-card')].indexOf(card);
            if (entry.isIntersecting) drawCard(card, idx * 100);
            else eraseCard(card);
        });
    }, { threshold: 0.2 });

    // 피처 카드 Observer
    const featureCardObs = new IntersectionObserver(entries => {
        entries.forEach(entry => {
            const card = entry.target;
            const idx  = [...document.querySelectorAll('.feature-card')].indexOf(card);
            if (entry.isIntersecting) drawCard(card, idx * 150 + 200);
            else eraseCard(card);
        });
    }, { threshold: 0.3 });

    /**
     * startDrawAnimations()
     * - Observer 를 카드들에 실제 등록해 드로우 애니메이션을 활성화한다.
     * - 팝업이 없으면 즉시 호출, 팝업이 있으면 마지막 팝업 닫힌 후 호출.
     * - 중복 호출 방지용 플래그 포함.
     */
    let drawStarted = false;
    function startDrawAnimations() {
        if (drawStarted) return;
        drawStarted = true;

        // Observer 등록은 ready 클래스 세팅 직후와 동기화
        requestAnimationFrame(() => {
            requestAnimationFrame(() => {
                document.querySelectorAll('.link-card').forEach(card => linkCardObs.observe(card));
                document.querySelectorAll('.feature-card').forEach(card => featureCardObs.observe(card));
            });
        });
    }

    /* ═══════════════════════════════════════════
       5. 팝업 모달
       ─ 실제로 화면에 뜨는 팝업이 있으면 → 마지막 팝업 닫힐 때 드로우 시작
       ─ 팝업이 없거나 모두 "오늘 하루 보지 않기" 상태면 → 즉시 드로우 시작
    ═══════════════════════════════════════════ */
    const modals = document.querySelectorAll('.popup-modal');
    let activePopupCount = 0; // 실제로 화면에 뜬 팝업 수

    modals.forEach(modal => {
        const id        = modal.id.replace('popup-modal-', '');
        const hideKey   = 'popupHideUntil_' + id;
        const hideUntil = localStorage.getItem(hideKey);
        const now       = new Date();

        // 숨김 처리 안 된 팝업만 실제 표시 + 카운트
        if (!hideUntil || new Date(hideUntil) <= now) {
            modal.style.display = 'flex';
            setTimeout(() => modal.classList.add('active'), 10);
            activePopupCount++;
        }

        const closeBtn = document.getElementById('close-popup-btn-' + id);
        const checkbox = document.getElementById('dont-show-' + id);

        // 닫기 공통 함수: 닫힐 때마다 카운트 감소 → 0이 되면 드로우 시작
        function closeModal() {
            modal.classList.remove('active');
            setTimeout(() => { modal.style.display = 'none'; }, 300);

            activePopupCount = Math.max(0, activePopupCount - 1);
            if (activePopupCount === 0) {
                startDrawAnimations();
            }
        }

        if (closeBtn) {
            closeBtn.addEventListener('click', () => {
                if (checkbox && checkbox.checked) {
                    const tomorrow = new Date();
                    tomorrow.setDate(tomorrow.getDate() + 1);
                    localStorage.setItem(hideKey, tomorrow.toISOString());
                }
                closeModal();
            });
        }

        // 배경 클릭으로도 닫기
        modal.addEventListener('click', e => {
            if (e.target === modal) closeModal();
        });
    });

    // 팝업이 하나도 표시되지 않으면 즉시 드로우 시작
    if (activePopupCount === 0) {
        startDrawAnimations();
    }

    /* ═══════════════════════════════════════════
       8. 범용 Reveal Observer
    ═══════════════════════════════════════════ */
    const revObs = new IntersectionObserver(entries => {
        entries.forEach(e => {
            if (e.isIntersecting) e.target.classList.add('in');
            else e.target.classList.remove('in');
        });
    }, { threshold: 0.1, rootMargin: '0px 0px -30px 0px' });

    document.querySelectorAll(
        '.reveal-fade, .reveal-up, .reveal-card, .intro-tagline, .slide-in-left, .slide-in-right'
    ).forEach(el => revObs.observe(el));

    // stagger-child (공지·갤러리) — 딜레이 부여
    document.querySelectorAll('.notice-item.stagger-child').forEach((el, i) => {
        el.style.transitionDelay = `${i * 80}ms`;
    });
    document.querySelectorAll('.gallery-item.stagger-child').forEach((el, i) => {
        el.style.transitionDelay = `${i * 60}ms`;
    });

    const staggerObs = new IntersectionObserver(entries => {
        entries.forEach(e => {
            if (e.isIntersecting) e.target.classList.add('in');
            else e.target.classList.remove('in');
        });
    }, { threshold: 0.05, rootMargin: '0px 0px -30px 0px' });

    document.querySelectorAll('.stagger-child').forEach(el => staggerObs.observe(el));

    /* ═══════════════════════════════════════════
       9. 숫자 카운터
    ═══════════════════════════════════════════ */
    function animateCounter(el, target, dur) {
        if (el._raf) cancelAnimationFrame(el._raf);
        const start   = performance.now();
        const isLarge = target >= 100;
        const ease    = t => 1 - Math.pow(1 - t, 4);

        (function step(now) {
            const p = Math.min((now - start) / dur, 1);
            el.textContent = isLarge
                ? Math.round(ease(p) * target).toLocaleString('ko-KR')
                : Math.round(ease(p) * target);
            if (p < 1) {
                el._raf = requestAnimationFrame(step);
            } else {
                el.textContent = isLarge ? target.toLocaleString('ko-KR') : target;
                el.classList.add('counted');
            }
        })(start);
    }

    const cntObs = new IntersectionObserver(entries => {
        entries.forEach(e => {
            const el = e.target;
            if (e.isIntersecting) {
                el.classList.remove('counted');
                animateCounter(
                    el,
                    parseInt(el.dataset.target),
                    parseInt(el.dataset.target) >= 100 ? 2000 : 1400
                );
            } else {
                if (el._raf) cancelAnimationFrame(el._raf);
                el.classList.remove('counted');
                el.textContent = '0';
            }
        });
    }, { threshold: 0.3 });

    document.querySelectorAll('.stat-num[data-target]').forEach(el => cntObs.observe(el));

    /* ═══════════════════════════════════════════
       10. 피처 카드 3D 틸트 (PC 전용)
    ═══════════════════════════════════════════ */
    if (!isMobile()) {
        document.querySelectorAll('.feature-card').forEach(card => {
            card.addEventListener('mousemove', e => {
                const r  = card.getBoundingClientRect();
                const rx = ((e.clientY - r.top  - r.height / 2) / (r.height / 2)) * -8;
                const ry = ((e.clientX - r.left - r.width  / 2) / (r.width  / 2)) *  8;
                card.style.transform = `translateY(-10px) perspective(1000px) rotateX(${rx}deg) rotateY(${ry}deg) scale3d(1.02,1.02,1.02)`;
            });
            card.addEventListener('mouseleave', () => {
                card.style.transform = '';
            });
        });
    }

    /* ═══════════════════════════════════════════
       11. 위치 배너 패럴랙스
    ═══════════════════════════════════════════ */
    const locationBanner = document.getElementById('locationBanner');

    function updateBannerParallax() {
        if (!locationBanner || isMobile()) return;
        const r = locationBanner.getBoundingClientRect();
        if (r.bottom < 0 || r.top > window.innerHeight) return;
        const p = (window.innerHeight - r.top) / (window.innerHeight + r.height);
        locationBanner.style.backgroundPositionY = `calc(50% + ${(p - 0.5) * 180}px)`;
    }

    /* ═══════════════════════════════════════════
       12. 스크롤 이벤트 통합 — rAF throttle
    ═══════════════════════════════════════════ */
    let ticking = false;
    window.addEventListener('scroll', () => {
        if (!ticking) {
            window.requestAnimationFrame(() => {
                updateProgressBar();
                updateHeroParallax();
                updateBannerParallax();
                ticking = false;
            });
            ticking = true;
        }
    }, { passive: true });

    updateProgressBar();
    updateHeroParallax();
    updateBannerParallax();

});