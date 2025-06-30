//let login = document.getElementById("login");
//let signin = document.getElementById("signin");
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

