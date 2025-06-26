// ✅ flatpickr 초기화
flatpickr("#startDate", {
	enableTime: true,
	dateFormat: "Y-m-d H:i",
	minDate: "today",
	locale: "ko",
});

flatpickr("#endDate", {
	enableTime: true,
	dateFormat: "Y-m-d H:i",
	minDate: "today",
	locale: "ko",
});

function validateForm() {
	const title = document.getElementById("title").value.trim();
	const startDate = document.getElementById("startDate").value;
	const endDate = document.getElementById("endDate").value;
	const location = document.getElementById("location").value.trim();
	const target = document.getElementById("target").value.trim();
	const capacity = document.getElementById("capacity").value;
	const content = document.getElementById("content").value.trim();
	const freeChecked = document.getElementById("freeCheck").checked;
	const expenseInput = document.getElementById("expense");
const subject = document.getElementById("subject").value.trim();  // 추가
	let expense = expenseInput.value.trim().replace(/,/g, '');

	if (!title || !content || !startDate ||  !endDate || !location || !target || !capacity || !subject ) {
		alert("모든 항목을 입력해주세요.");
		return false;
	}

	const start = new Date(startDate);
	const end = new Date(endDate);
	const now = new Date();

	if (start < now) {
		alert("시작일시는 현재 이후로 설정해주세요.");
		return false;
	}

	if (end <= start) {
		alert("종료일시는 시작일시 이후로 설정해주세요.");
		return false;
	}

	if (freeChecked) {
		expense = "0";
		expenseInput.value = "0";
	} else {
		if (!expense || isNaN(expense) || Number(expense) < 0) {
			alert("비용을 올바르게 입력해주세요.");
			return false;
		}
	}

	return confirm("프로그램을 개설하시겠습니까?");
}

document.addEventListener("DOMContentLoaded", () => {
	// ✅ 이미지 처리
	const imageInput = document.getElementById("image");
	const previewContainer = document.getElementById("image-preview-container");
	let selectedImageFile = null;

	imageInput.addEventListener("change", (event) => {
		const file = event.target.files[0];

		previewContainer.innerHTML = "";
		previewContainer.classList.add("empty");
		selectedImageFile = null;

		if (!file) return;

		if (!file.type.startsWith("image/")) {
			alert("이미지 파일만 첨부 가능합니다.");
			imageInput.value = "";
			return;
		}

		const reader = new FileReader();
		reader.onload = function (e) {
			const img = document.createElement("img");
			img.src = e.target.result;

			const removeBtn = document.createElement("button");
			removeBtn.type = "button";
			removeBtn.className = "remove-image-btn";
			removeBtn.textContent = "×";

			removeBtn.addEventListener("click", () => {
				imageInput.value = "";
				previewContainer.innerHTML = "";
				previewContainer.classList.add("empty");
				selectedImageFile = null;
			});

			previewContainer.appendChild(img);
			previewContainer.appendChild(removeBtn);
			previewContainer.classList.remove("empty");
			selectedImageFile = file;
		};
		reader.readAsDataURL(file);
	});

	// ✅ 무료 체크박스 + 비용 입력 처리
	const expenseInput = document.getElementById("expense");
	const freeCheck = document.getElementById("freeCheck");

	// 자동 쉼표 붙이기
	expenseInput.addEventListener("input", (e) => {
		let value = e.target.value.replace(/,/g, '');
		if (!/^\d*$/.test(value)) return;
		e.target.value = Number(value).toLocaleString();
	});

	freeCheck.addEventListener("change", () => {
		if (freeCheck.checked) {
			expenseInput.value = "0";
			expenseInput.disabled = true;
		} else {
			expenseInput.disabled = false;
			expenseInput.value = "";
		}
	});
});
