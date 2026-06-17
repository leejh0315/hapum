// ────────────────────────────────────────────────────────
// 1. PC 버전 내비게이션 드롭다운 제어 (변경 없음)
// ────────────────────────────────────────────────────────
const navItems = document.querySelectorAll('.nav-item');
const dropdownCols = document.querySelectorAll('.dropdown-col');
const dropdownPanel = document.querySelector('.dropdown-panel');
const navWrapper = document.querySelector('.nav-wrapper');

if (navItems && navWrapper && dropdownPanel) {
  navItems.forEach((item, idx) => {
    item.addEventListener('mouseenter', () => {
      const col = document.querySelector(`.dropdown-col[data-nav="${idx}"]`);
      const hasItems = col && col.children.length > 0;

      dropdownCols.forEach(c => c.classList.remove('active'));

      if (hasItems) {
        col.classList.add('active');
        dropdownPanel.classList.add('active');
      } else {
        dropdownPanel.classList.remove('active');
      }
    });
  });

  navWrapper.addEventListener('mouseleave', () => {
    dropdownPanel.classList.remove('active');
    dropdownCols.forEach(c => c.classList.remove('active'));
  });
}

// ────────────────────────────────────────────────────────
// 2. 모바일 사이드바 제어
// ────────────────────────────────────────────────────────
const sidebar = document.getElementById("rightSidebar");
const backdrop = document.getElementById("sidebarBackdrop");
const hamburgerIcon = document.getElementById("hamburgerIcon");

/**
 * 스크롤바 너비를 계산하여 body padding 보정 (오른쪽 밀림 방지)
 */
function getScrollbarWidth() {
  return window.innerWidth - document.documentElement.clientWidth;
}

function toggleSidebar() {
  if (!sidebar) return;
  sidebar.classList.contains("active") ? closeSidebar() : openSidebar();
}

function openSidebar() {
  if (!sidebar || !backdrop) return;

  // 스크롤바 너비 보정값 CSS 변수로 주입 → body padding-right 와 일치
  const scrollbarWidth = getScrollbarWidth();
  document.documentElement.style.setProperty('--scrollbar-width', scrollbarWidth + 'px');

  // 열기
  sidebar.classList.add("active");
  backdrop.classList.add("active");
  document.body.classList.add("sidebar-open");

  // 햄버거 → X 모핑
  if (hamburgerIcon) hamburgerIcon.classList.add("open");
}

function closeSidebar() {
  if (!sidebar || !backdrop) return;

  sidebar.classList.remove("active");
  backdrop.classList.remove("active");
  document.body.classList.remove("sidebar-open");
  document.documentElement.style.removeProperty('--scrollbar-width');

  // X → 햄버거 복원
  if (hamburgerIcon) hamburgerIcon.classList.remove("open");

  // 닫힐 때 모든 열린 아코디언 초기화
  document.querySelectorAll('.sidebar-nav-item.open').forEach(el => {
    el.classList.remove('open');
    const sub = el.querySelector('.mobile-sub-menu');
    if (sub) sub.style.maxHeight = '0px';
  });
}

// 서브메뉴 아코디언 토글
function toggleSubMenu(linkEl) {
  if (!linkEl) return;
  const parentLi = linkEl.parentElement;
  const subMenu = parentLi.querySelector('.mobile-sub-menu');
  if (!subMenu) return;

  const isOpen = parentLi.classList.contains('open');

  // 다른 열린 항목 닫기
  document.querySelectorAll('.sidebar-nav-item.open').forEach(el => {
    if (el !== parentLi) {
      el.classList.remove('open');
      const other = el.querySelector('.mobile-sub-menu');
      if (other) other.style.maxHeight = '0px';
    }
  });

  if (!isOpen) {
    parentLi.classList.add('open');
    subMenu.style.maxHeight = subMenu.scrollHeight + "px";
  } else {
    parentLi.classList.remove('open');
    subMenu.style.maxHeight = '0px';
  }
}

// 백드롭 클릭으로 닫기
if (backdrop) {
  backdrop.addEventListener("click", closeSidebar);
}

// ESC 키로 닫기
document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape' && sidebar && sidebar.classList.contains('active')) {
    closeSidebar();
  }
});

// ────────────────────────────────────────────────────────
// 3. 비동기 로그아웃 및 인증 경로 바인딩
// ────────────────────────────────────────────────────────
function goLogin() { window.location.href = "/auth/login"; }
function goSignin() { window.location.href = "/auth/signin"; }

function doLogout() {
  const currentPath = window.location.pathname;
  $.ajax({
    type: "POST",
    url: "/auth/doLogout",
    contentType: 'application/json',
    data: JSON.stringify({ link: currentPath }),
    success: function(response) {
      if (response) {
        window.location.href = response;
      } else {
        alert("로그아웃 실패: 리다이렉트 주소 없음");
      }
    },
    error: function(error) {
      console.error("로그아웃 오류:", error);
    }
  });
}