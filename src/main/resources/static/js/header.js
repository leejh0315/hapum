//let login = document.getElementById("login");
//let signin = document.getElementById("signin");



const sidebar  = document.getElementById("rightSidebar");
const backdrop = document.getElementById("sidebarBackdrop");
const content  = document.querySelector(".right-side-content");
const menuBtn  = document.querySelector(".mobile-menu");

// 사이드바 열기/닫기
function toggleSidebar() {
	console.log("clicked");
  const isOpen = sidebar.classList.contains("active");
  if (isOpen) {
    closeSidebar();
  } else {
    sidebar.classList.add("active");
    backdrop.classList.add("active");
  }
}

// 사이드바 닫기 공통 로직
function closeSidebar() {
  sidebar.classList.remove("active");
  backdrop.classList.remove("active");
}

// 백드롭 클릭 시 닫기
backdrop.addEventListener("click", closeSidebar);

// 사이드바 내부 클릭은 전파 차단 (닫히지 않도록)
content.addEventListener("click", e => e.stopPropagation());

// 모바일 햄버거 버튼에 연결




function toggleMenu() {
	$("#nav1 ul").slideToggle();
}
function goLogin() {
	window.location.href = "/auth/login";
}
function goSignin() {
	window.location.href = "/auth/signin";
}

function doLogout() {
	let currentPath = window.location.pathname;

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
			console.error("로그아웃 중 오류 발생:", error);
			alert("오류가 발생했습니다.");
		}
	});
}

/*
	login.addEventListener("click", ()=>{
		window.location.href = "/auth/login";
	})
	signin.addEventListener("click", ()=>{
		window.location.href = "/auth/signin";
	})
*/

