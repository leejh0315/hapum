let logoutBtn = document.getElementById("logoutBtn");
	
	logoutBtn.addEventListener("click", () => {
    // 현재 URL 경로만 추출 (예: /main/program)
    let currentPath = window.location.pathname;

    fetch("/doLogout", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: "link=" + encodeURIComponent(currentPath)
    })
    .then(response => {
        if (response.redirected) {
            // Spring이 redirect:/...로 보냈을 때
            window.location.href = response.url;
        } else {
            alert("로그아웃 실패");
        }
    })
    .catch(err => {
        console.error("로그아웃 중 오류 발생:", err);
        alert("오류가 발생했습니다.");
    });
});