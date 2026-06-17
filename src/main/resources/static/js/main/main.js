document.addEventListener('DOMContentLoaded', function () {

    /* ═══════════════════════════════════════════
       0. 히어로 슬라이드 동적 빌드 (1~4번 순서대로 매핑)
    ═══════════════════════════════════════════ */
    const heroSlider = document.getElementById('heroSlider');
    
    if (heroSlider) {
        // 1. 이미지와 문구를 1:1로 매칭한 순차 데이터셋 (총 4개)
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

        // 기존 HTML에 하드코딩된 임시 슬라이드 비우기
        heroSlider.innerHTML = '';

        // 배열 순서대로(1번~4번) 슬라이드 DOM 생성 및 주입
        slideData.forEach((slide, index) => {
            const slideDiv = document.createElement('div');
            // 첫 번째 슬라이드에만 active 클래스를 부여하여 시작 시 노출시킴
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

    /* ═══════════════════════════════════════════
       0-1. 스크롤 진행 바 — 동적 삽입
    ═══════════════════════════════════════════ */
    const progressBar = document.createElement('div');
    progressBar.id = 'scroll-progress-bar';
    document.body.prepend(progressBar);

    function updateProgressBar() {
        const scrollTop = window.scrollY;
        const docHeight = document.documentElement.scrollHeight - window.innerHeight;
        const pct = docHeight > 0 ? (scrollTop / docHeight) * 100 : 0;
        progressBar.style.width = pct + '%';
    }

    /* ═══════════════════════════════════════════
       1. 히어로 패럴랙스 + 텍스트 페이드아웃
    ═══════════════════════════════════════════ */
    const heroSection = document.querySelector('.hero-section');
    const heroSlides  = document.querySelectorAll('.hero-slider .slide');
    const slideContents = document.querySelectorAll('.slide-content');

    function updateHeroParallax() {
        if (!heroSection) return;
        const scrollY = window.scrollY;
        const heroH   = heroSection.offsetHeight;

        // 배경 패럴랙스 — 스크롤의 40% 속도로 이동
        heroSlides.forEach(slide => {
            slide.style.backgroundPositionY = `calc(50% + ${scrollY * 0.4}px)`;
        });

        // 텍스트 페이드아웃 — 히어로 높이 50% 지점에서 완전히 사라짐
        const fadeRatio = Math.min(scrollY / (heroH * 0.5), 1);
        slideContents.forEach(content => {
            content.style.opacity   = String(1 - fadeRatio);
            content.style.transform = `translateY(${fadeRatio * -30}px)`;
        });
    }

    /* ═══════════════════════════════════════════
       2. Stagger 등장 클래스 부여
    ═══════════════════════════════════════════ */
    document.querySelectorAll('.link-card').forEach((el, i) => {
        el.classList.add('stagger-child');
        el.style.transitionDelay = `${i * 70}ms`;
    });

    document.querySelectorAll('.notice-item').forEach((el, i) => {
        el.classList.add('stagger-child');
        el.style.transitionDelay = `${i * 80}ms`;
    });

    document.querySelectorAll('.gallery-item').forEach((el, i) => {
        el.classList.add('stagger-child');
        el.style.transitionDelay = `${i * 60}ms`;
    });

    document.querySelectorAll('.program-card').forEach((el, i) => {
        el.classList.add('stagger-child');
        el.style.transitionDelay = `${i * 100}ms`;
    });

    const programWrapper = document.querySelector('.program-wrapper');
    const noticeWrapper  = document.querySelector('.notice-wrapper');
    if (programWrapper) programWrapper.classList.add('slide-in-left');
    if (noticeWrapper)  noticeWrapper.classList.add('slide-in-right');

    /* ═══════════════════════════════════════════
       3. IntersectionObserver — stagger / slide-in
    ═══════════════════════════════════════════ */
    const staggerObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('active');
                staggerObserver.unobserve(entry.target);
            }
        });
    }, { threshold: 0.1, rootMargin: '0px 0px -30px 0px' });

    document.querySelectorAll(
        '.stagger-child, .slide-in-left, .slide-in-right'
    ).forEach(el => staggerObserver.observe(el));

    /* ═══════════════════════════════════════════
       4. IntersectionObserver — 섹션 단위 Reveal
    ═══════════════════════════════════════════ */
    const revealObserver = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('active');
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.1, rootMargin: '0px 0px -50px 0px' });

    document.querySelectorAll('.reveal').forEach(el => {
        if (el.classList.contains('board-section')) {
            el.style.opacity   = '1';
            el.style.transform = 'none';
            el.style.transition = 'none';
        } else {
            revealObserver.observe(el);
        }
    });

    /* ═══════════════════════════════════════════
       5. 스크롤 이벤트 — rAF로 묶어 성능 최적화
    ═══════════════════════════════════════════ */
    let ticking = false;

    window.addEventListener('scroll', () => {
        if (!ticking) {
            window.requestAnimationFrame(() => {
                updateProgressBar();
                updateHeroParallax();
                ticking = false;
            });
            ticking = true;
        }
    }, { passive: true });

    updateProgressBar();
    updateHeroParallax();

    /* ═══════════════════════════════════════════
       6. 히어로 슬라이드 자동 롤링 (순차 주입된 노드로 추적)
    ═══════════════════════════════════════════ */
    const dynamicSlides = document.querySelectorAll('.hero-slider .slide');
    const heroPrev      = document.getElementById('hero-prev');
    const heroNext      = document.getElementById('hero-next');
    let currentSlide    = 0;
    let slideInterval;

    function showSlide(index) {
        dynamicSlides.forEach(slide => slide.classList.remove('active'));
        dynamicSlides[index].classList.add('active');
    }

    function nextSlide() {
        currentSlide = (currentSlide + 1) % dynamicSlides.length;
        showSlide(currentSlide);
    }

    function prevSlide() {
        currentSlide = (currentSlide - 1 + dynamicSlides.length) % dynamicSlides.length;
        showSlide(currentSlide);
    }

    function resetInterval() {
        clearInterval(slideInterval);
        slideInterval = setInterval(nextSlide, 5000);
    }

    if (dynamicSlides.length > 0) {
        slideInterval = setInterval(nextSlide, 5000);
        if (heroNext) heroNext.addEventListener('click', () => { nextSlide(); resetInterval(); });
        if (heroPrev) heroPrev.addEventListener('click', () => { prevSlide(); resetInterval(); });
    }

    /* ═══════════════════════════════════════════
       7. 프로그램 가로 슬라이더
    ═══════════════════════════════════════════ */
    const programTrack = document.getElementById('programTrack');
    const progPrev     = document.getElementById('prog-prev');
    const progNext     = document.getElementById('prog-next');
    const scrollStep   = 300;

    if (programTrack && progPrev && progNext) {
        progNext.addEventListener('click', () => {
            programTrack.scrollBy({ left: scrollStep, behavior: 'smooth' });
        });
        progPrev.addEventListener('click', () => {
            programTrack.scrollBy({ left: -scrollStep, behavior: 'smooth' });
        });
    }

    /* ═══════════════════════════════════════════
       8. 모달 팝업
    ═══════════════════════════════════════════ */
    const modals = document.querySelectorAll('.popup-modal');

    modals.forEach(modal => {
        const id       = modal.id.replace('popup-modal-', '');
        const hideKey  = 'popupHideUntil_' + id;
        const hideUntil = localStorage.getItem(hideKey);
        const now      = new Date();

        if (!hideUntil || new Date(hideUntil) <= now) {
            modal.style.display = 'flex';
            setTimeout(() => modal.classList.add('active'), 10);
        }

        const closeBtn = document.getElementById('close-popup-btn-' + id);
        const checkbox = document.getElementById('dont-show-' + id);

        if (closeBtn) {
            closeBtn.addEventListener('click', () => {
                if (checkbox && checkbox.checked) {
                    const tomorrow = new Date();
                    tomorrow.setDate(tomorrow.getDate() + 1);
                    localStorage.setItem(hideKey, tomorrow.toISOString());
                }
                modal.classList.remove('active');
                setTimeout(() => { modal.style.display = 'none'; }, 300);
            });
        }

        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.classList.remove('active');
                setTimeout(() => { modal.style.display = 'none'; }, 300);
            }
        });
    });

    /* ═══════════════════════════════════════════
       9. 관리자 공지 순서 수정 버튼
    ═══════════════════════════════════════════ */
    const editOrderBtn = document.getElementById('edit-order-btn');
    if (editOrderBtn) {
        editOrderBtn.addEventListener('click', () => {
            alert('순서 수정 모드');
        });
    }

});