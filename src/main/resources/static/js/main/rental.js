document.addEventListener("DOMContentLoaded", function() {

	// --- 기본 헬퍼 함수들 ---
	function safeQuerySelectorAll(selector) {
		const nodeList = document.querySelectorAll(selector);
		return nodeList ? Array.from(nodeList) : [];
	}
	function timeStringToMinutes(s) {
		const [hours, minutes] = s.split(':').map(Number);
		return hours * 60 + minutes;
	}
	function formatPrice(num) {
		return new Intl.NumberFormat('ko-KR').format(num);
	}
	function getWeekOfMonth(date) {
		const day = date.getDate();
		return Math.floor((day - 1) / 7) + 1;
	}
	function formatDate(d) {
		let mm = d.getMonth() + 1;
		let dd = d.getDate();
		return d.getFullYear() + '-' + (mm < 10 ? '0' + mm : mm) + '-' + (dd < 10 ? '0' + dd : dd);
	}

	// --- 전역 변수 ---
	const today = new Date();
	const todayFormatted = formatDate(today);
	const endDate = new Date(today);
	endDate.setDate(endDate.getDate() + 30);
	const timeSlots = [
		'09:00', '09:30', '10:00', '10:30',
		'11:00', '11:30', '12:00', '12:30',
		'13:00', '13:30', '14:00', '14:30',
		'15:00', '15:30', '16:00', '16:30',
		'17:00', '17:30', '18:00', '18:30',
		'19:00', '19:30', '20:00', '20:30',
		'21:00', '21:30', '22:00'
	];
	let selectedRoom = null;
	let selectedDate = null;
	let selectedSlots = [];
	let fullDayOptionSelected = false;
	let fullDayOptionAvailable = false;

	// --- DOM 요소 ---
	const dateRangeText = document.getElementById('date-range-text');
	const datePicker = document.getElementById('datePicker');
	const submitBtn = document.getElementById('submit-btn');
	const selectedTimeDisplay = document.getElementById('selected-time-display');
	const timetableContainer = document.getElementById('timetable-container');

	dateRangeText.textContent = `${formatDate(today)} ~ ${formatDate(endDate)}`;
	datePicker.min = formatDate(today);
	datePicker.value = formatDate(today);

	// --- conflict 검사 함수: full day 예약 가능 여부 ---
	function checkFullDayBooking(effectiveDate) {
		const selDateObj = new Date(effectiveDate);
		const weekday = selDateObj.getDay();
		const weekOfMonth = getWeekOfMonth(selDateObj);
		const blockedMap = new Map();
		// blockedDays 적용
		const blockedTimes = blockedDays
			.filter(bd => bd.weekOfMonth === weekOfMonth && bd.dayOfWeek === weekday)
			.flatMap(() => timeSlots.map(t => ({ time: t })));
		blockedTimes.forEach(({ time }) => blockedMap.set(time, true));
		// fixedReservations 적용
		const fixedTimes = getFixedReservationTimesForDate(effectiveDate);
		fixedTimes.forEach(({ time }) => blockedMap.set(time, true));
		// rentals (강당과 연습실 conflict 적용)
		rentals.forEach(r => {
			if (isConflictingLocation(r.location.trim(), selectedRoom) &&
				r.reservationDate === effectiveDate) {
				const startTime = r.startTime.slice(0, 5);
				const endTime = r.endTime.slice(0, 5);
				const startIdx = timeSlots.indexOf(startTime);
				const endIdx = timeSlots.indexOf(endTime);
				if (startIdx !== -1 && endIdx !== -1) {
					for (let i = startIdx; i < endIdx; i++) {
						blockedMap.set(timeSlots[i], true);
					}
				}
			}
		});
		// ── 여기 바로 아래에 추가 ──
		// programs 일정도 conflict 처리
		const programTimes = getProgramTimesForDate(effectiveDate);
		programTimes.forEach(({ time }) => blockedMap.set(time, true));
		// ────────────────────────────
		const startIndex = timeSlots.indexOf("09:00");
		const endIndex = timeSlots.indexOf("22:00");
		for (let i = startIndex; i <= endIndex; i++) {
			if (blockedMap.has(timeSlots[i])) {
				return false;
			}
		}
		return true;
	}

	// --- fixedReservations에 따른 시간대 가져오기 ---
	function getFixedReservationTimesForDate(dateStr) {
		const date = new Date(dateStr);
		const weekday = date.getDay();
		const weekOfMonth = getWeekOfMonth(date);
		const blocked = [];
		for (const fr of fixedReservations) {
			const frWeekday = fr.weekday !== null ? Number(fr.weekday) : null;
			const frWeekOfMonth = fr.weekOfMonth !== null ? Number(fr.weekOfMonth) : null;
			const frDayOfWeek = fr.dayOfWeek !== null ? Number(fr.dayOfWeek) : null;
			let match = false;
			if (fr.type === 'WEEKLY' && frWeekday !== null) {
				match = frWeekday === weekday;
			} else if (fr.type === 'MONTHLY' && frWeekOfMonth !== null && frDayOfWeek !== null) {
				match = (frWeekOfMonth === weekOfMonth) && (frDayOfWeek === weekday);
			}
			if (match) {
				const startTime = fr.startTime.slice(0, 5);
				const endTime = fr.endTime.slice(0, 5);
				const startIdx = timeSlots.indexOf(startTime);
				const endIdx = timeSlots.indexOf(endTime);
				if (startIdx !== -1 && endIdx !== -1) {
					for (let i = startIdx; i < endIdx; i++) {
						blocked.push({ time: timeSlots[i] });
					}
				}
			}
		}
		return blocked;
	}
	// 1) 프로그램 시간대 추출 함수 추가 (rental.js 상단 가까이)
	function getProgramTimesForDate(dateStr) {
		const slots = [];
		const day = dateStr.slice(0, 10);  // "YYYY-MM-DD"

		programs.forEach(p => {
			const startDay = p.startDate.slice(0, 10);
			const endDay = p.endDate.slice(0, 10);
			const startTime = p.startDate.slice(11, 16);  // "HH:MM"
			const endTime = p.endDate.slice(11, 16);

			const startIdx = timeSlots.indexOf(startTime);
			const endIdx = timeSlots.indexOf(endTime);

			if (startDay === endDay && day === startDay) {
				// 1일짜리 프로그램: 시작→종료
				for (let i = startIdx; i < endIdx; i++) {
					slots.push({ time: timeSlots[i] });
				}

			} else if (day === startDay) {
				// 시작일: 시작시간→마지막(22:00)
				for (let i = startIdx; i < timeSlots.length; i++) {
					slots.push({ time: timeSlots[i] });
				}

			} else if (day === endDay) {
				// 종료일: 첫슬롯(09:00)→종료시간
				for (let i = 0; i < endIdx; i++) {
					slots.push({ time: timeSlots[i] });
				}

			}
			// else { // 중간 날짜는 block 없음 }
		});

		return slots;
	}

	// --- 시설 간 conflict 검사 ---
	function isConflictingLocation(rentalLoc, selectedRoom) {
		if (selectedRoom === "강당" || selectedRoom === "연습실") {
			return (rentalLoc === "강당" || rentalLoc === "연습실");
		} else {
			return rentalLoc === selectedRoom;
		}
	}

	// --- 타임슬롯 초기화 및 재생성 ---
	function updateTimeSlots() {
		const timeSlotsRow = document.getElementById('time-slots-row');
		timeSlotsRow.innerHTML = '<div class="label-cell">예약</div>';
		for (let t of timeSlots) {
			const slotDiv = document.createElement('div');
			slotDiv.className = 'slot';
			slotDiv.dataset.time = t;
			slotDiv.textContent = t;
			if (fullDayOptionSelected) {
				slotDiv.style.pointerEvents = 'none';
				slotDiv.classList.add('disabled');
			}
			timeSlotsRow.appendChild(slotDiv);
		}
		datePicker.dispatchEvent(new Event('change'));
	}

	// --- 시설 선택 처리 ---
	safeQuerySelectorAll('.room-btn').forEach(btn => {
		btn.addEventListener('click', () => {
			// active 클래스 갱신: 모든 버튼에서 제거 후 현재 버튼에 추가
			safeQuerySelectorAll('.room-btn').forEach(b => b.classList.remove('active'));
			btn.classList.add('active');

			selectedRoom = btn.getAttribute('data-room');
			selectedDate = null;
			selectedSlots = [];
			fullDayOptionSelected = false;
			fullDayOptionAvailable = false;
			submitBtn.disabled = true;
			selectedTimeDisplay.textContent = "";
			timetableContainer.style.display = 'none';

			const roomInfo = roomData[selectedRoom];
			if (!roomInfo) return;
			document.getElementById('room-title').textContent = selectedRoom;
			document.getElementById('floor-title').textContent = `${roomInfo.floor}층`;
			const contentDiv = document.getElementById('room-content');
			contentDiv.innerHTML = `
              <img class="roomInfoImg" src="${roomInfo.img}" style="max-width: 350px; max-height: 350px; border-radius: 10px;" />
              <div class="roomInfoText">${roomInfo.html}</div>
            `;
			if (selectedRoom === "강당") {
				let tomorrow = new Date();
				tomorrow.setDate(tomorrow.getDate() + 1);
				datePicker.min = formatDate(tomorrow);
				datePicker.value = formatDate(tomorrow);
				dateRangeText.textContent = `${formatDate(tomorrow)} ~ ${formatDate(endDate)}`;

				const bookingChoiceDiv = document.createElement('div');
				bookingChoiceDiv.className = 'booking-choice';
				bookingChoiceDiv.id = 'booking-choice';
				bookingChoiceDiv.innerHTML = `
				
                <label>
                  <input type="radio" name="bookingType" value="4시간" checked> 4시간 예약 (5만원)
                </label>
                <label>
                  <input type="radio" name="bookingType" value="하루종일"> 하루종일 예약 (10만원)
                </label>
              `;
				contentDiv.appendChild(bookingChoiceDiv);
				bookingChoiceDiv.addEventListener('change', function() {
					selectedSlots = [];
					const bookingChoice = document.querySelector('input[name="bookingType"]:checked');
					const effectiveDate = selectedDate || datePicker.value;
					if (bookingChoice.value === "하루종일") {
						fullDayOptionSelected = true;
						if (checkFullDayBooking(effectiveDate)) {
							fullDayOptionAvailable = true;
							selectedTimeDisplay.textContent = `시설: ${selectedRoom} / 날짜: ${effectiveDate} / 예약: 하루종일 (09:00 ~ 22:00)`;
							submitBtn.disabled = false;
						} else {
							fullDayOptionAvailable = false;
							selectedTimeDisplay.textContent = "해당 날짜에는 하루종일 예약이 불가능합니다.";
							submitBtn.disabled = true;
						}
					} else {
						fullDayOptionSelected = false;
						fullDayOptionAvailable = true;
						selectedTimeDisplay.textContent = "";
						submitBtn.disabled = true;
					}
					updateTimeSlots();
				});
			} else {
				datePicker.min = formatDate(today);
				datePicker.value = formatDate(today);
				dateRangeText.textContent = `${formatDate(today)} ~ ${formatDate(endDate)}`;
			}

			const timeButton = document.createElement('button');
			timeButton.id = 'show-timetable-btn';
			timeButton.textContent = '시간 선택하기';
			timeButton.style.marginTop = '20px';
			timeButton.style.width = '150px';
			timeButton.style.padding = '10px 20px';
			timeButton.style.borderRadius = '5px';
			timeButton.style.background = '#444';
			timeButton.style.color = 'white';
			timeButton.style.cursor = 'pointer';

			if (!user) {
				timeButton.disabled = true;
				timeButton.style.cursor = 'not-allowed';
				contentDiv.appendChild(timeButton);
				const loginWarning = document.createElement('p');
				loginWarning.id = "login-warning";
				loginWarning.textContent = "로그인 한 사용자만 대관신청 할 수 있습니다.";
				loginWarning.style.color = "red";
				loginWarning.style.marginTop = "10px";
				contentDiv.appendChild(loginWarning);
			} else {
				contentDiv.appendChild(timeButton);
				timeButton.addEventListener('click', () => {
					if (!selectedRoom) {
						alert('시설을 먼저 선택해주세요.');
						return;
					}
					let bookingType = 'normal';
					if (selectedRoom === "강당") {
						const bookingChoice = document.querySelector('input[name="bookingType"]:checked');
						bookingType = bookingChoice ? bookingChoice.value : '4시간';
					}
					timetableContainer.style.display = 'block';
					updateTimeSlots();
					selectedDate = datePicker.value || formatDate(today);
					selectedSlots = [];
					if (selectedRoom === "강당") {
						const bookingChoice = document.querySelector('input[name="bookingType"]:checked');
						if (bookingChoice.value === "하루종일") {
							updateTimeSlots();
						} else {
							fullDayOptionSelected = false;
							submitBtn.disabled = true;
						}
					} else {
						submitBtn.disabled = true;
					}
					datePicker.dispatchEvent(new Event('change'));
				});
			}
			document.getElementById('room-info').style.display = 'block';
		});
	});

	// --- 날짜 변경 이벤트 ---
	datePicker.addEventListener('change', () => {
		if (!selectedRoom) {
			alert('시설을 먼저 선택해주세요.');
			datePicker.value = formatDate(today);
			return;
		}
		selectedDate = datePicker.value;
		selectedSlots = [];
		if (!(selectedRoom === "강당" && fullDayOptionSelected)) {
			submitBtn.disabled = true;
			selectedTimeDisplay.textContent = "";
		}
		safeQuerySelectorAll('#time-slots-row .slot').forEach(slot => {
			slot.classList.remove('selected', 'blocked-slot', 'past-slot');
			slot.style.pointerEvents = 'auto';
			// 클론을 통해 기존 상태 제거
			const newSlot = slot.cloneNode(true);
			slot.parentNode.replaceChild(newSlot, slot);
		});

		const selDate = new Date(selectedDate);
		const weekday = selDate.getDay();
		const weekOfMonth = getWeekOfMonth(selDate);
		const blockedTimes = blockedDays
			.filter(bd => bd.weekOfMonth === weekOfMonth && bd.dayOfWeek === weekday)
			.flatMap(() => timeSlots.map(t => ({ time: t })));
		const fixedTimes = getFixedReservationTimesForDate(selectedDate);
		const blockedMap = new Map();
		blockedTimes.forEach(({ time }) => blockedMap.set(time, true));
		fixedTimes.forEach(({ time }) => blockedMap.set(time, true));
		rentals.forEach(rental => {
			if (isConflictingLocation(rental.location.trim(), selectedRoom) &&
				rental.reservationDate === selectedDate) {
				const startTime = rental.startTime.slice(0, 5);
				const endTime = rental.endTime.slice(0, 5);
				const startIdx = timeSlots.indexOf(startTime);
				const endIdx = timeSlots.indexOf(endTime);
				if (startIdx !== -1 && endIdx !== -1) {
					for (let i = startIdx; i < endIdx; i++) {
						blockedMap.set(timeSlots[i], true);
					}
				}
			}
		});

		const progTimes = getProgramTimesForDate(selectedDate);
		progTimes.forEach(({ time }) => blockedMap.set(time, true));


		safeQuerySelectorAll('#time-slots-row .slot').forEach(slot => {
			if (blockedMap.has(slot.dataset.time)) {
				slot.classList.add('blocked-slot');
				slot.style.pointerEvents = 'none';
			}
		});

		// 오늘 날짜일 경우: 먼저 todayFormatted 변수를 사용하여 비교
		if (selectedDate === todayFormatted) {
			const now = new Date();
			const nowMinutes = now.getHours() * 60 + now.getMinutes();
			safeQuerySelectorAll('#time-slots-row .slot').forEach(slot => {
				if (!slot.classList.contains('blocked-slot')) {
					const slotMinutes = timeStringToMinutes(slot.dataset.time);
					if (slotMinutes < nowMinutes) {
						slot.classList.add('past-slot');
						slot.style.pointerEvents = 'none';
					}
				}
			});
		}

		if (selectedRoom === "강당") {
			const bookingChoice = document.querySelector('input[name="bookingType"]:checked');
			if (bookingChoice && bookingChoice.value === "하루종일") {
				const effectiveDate = selectedDate;
				if (checkFullDayBooking(effectiveDate)) {
					fullDayOptionAvailable = true;
					selectedTimeDisplay.textContent = `시설: ${selectedRoom} / 날짜: ${effectiveDate} / 예약: 하루종일 (09:00 ~ 22:00)`;
					submitBtn.disabled = false;
				} else {
					fullDayOptionAvailable = false;
					selectedTimeDisplay.textContent = "해당 날짜에는 하루종일 예약이 불가능합니다.";
					submitBtn.disabled = true;
				}
				updateTimeSlots();
			}
		}
	});

	// --- 슬롯 클릭 처리 ---
	document.getElementById('time-slots-row').addEventListener('click', (e) => {
		if (!selectedDate || !selectedRoom || fullDayOptionSelected) {
			return;
		}
		const slot = e.target.closest('.slot');
		if (!slot || slot.classList.contains('blocked-slot') || slot.classList.contains('past-slot')) return;
		let requiredSlots = 4; // 기본 2시간 예약 (4슬롯)
		if (selectedRoom === "강당") {
			const bookingChoice = document.querySelector('input[name="bookingType"]:checked');
			let bookingType = bookingChoice ? bookingChoice.value : '4시간';
			if (bookingType === "4시간") {
				requiredSlots = 8;
			}
		}
		const startIndex = timeSlots.indexOf(slot.dataset.time);
		if (startIndex === -1 || startIndex + requiredSlots > timeSlots.length) {
			safeQuerySelectorAll('#time-slots-row .slot.selected').forEach(s => s.classList.remove('selected'));
			selectedSlots = [];
			selectedTimeDisplay.textContent = '';
			submitBtn.disabled = true;
			return;
		}
		for (let i = startIndex; i < startIndex + requiredSlots; i++) {
			const t = timeSlots[i];
			const nextSlot = document.querySelector(`#time-slots-row .slot[data-time="${t}"]`);
			if (nextSlot && (nextSlot.classList.contains('blocked-slot') || nextSlot.classList.contains('past-slot'))) {
				safeQuerySelectorAll('#time-slots-row .slot.selected').forEach(s => s.classList.remove('selected'));
				selectedSlots = [];
				selectedTimeDisplay.textContent = '';
				submitBtn.disabled = true;
				return;
			}
		}
		safeQuerySelectorAll('#time-slots-row .slot.selected').forEach(s => s.classList.remove('selected'));
		selectedSlots = [];
		for (let i = startIndex; i < startIndex + requiredSlots; i++) {
			const t = timeSlots[i];
			const s = document.querySelector(`#time-slots-row .slot[data-time="${t}"]`);
			if (s) {
				s.classList.add('selected');
				selectedSlots.push(t);
			}
		}
		const start = selectedSlots[0];
		const end = timeSlots[startIndex + requiredSlots];
		if (!end) {
			safeQuerySelectorAll('#time-slots-row .slot.selected').forEach(s => s.classList.remove('selected'));
			selectedSlots = [];
			selectedTimeDisplay.textContent = '';
			submitBtn.disabled = true;
			return;
		}
		if (selectedRoom === "강당") {
			const bookingChoice = document.querySelector('input[name="bookingType"]:checked');
			const bookingType = bookingChoice ? bookingChoice.value : '4시간';
			if (bookingType === "4시간") {
				selectedTimeDisplay.textContent = `시설: ${selectedRoom} / 날짜: ${selectedDate} / 시간: ${start} ~ ${end} (4시간 예약: 5만원)`;
			}
		} else {
			selectedTimeDisplay.textContent = `시설: ${selectedRoom} / 날짜: ${selectedDate} / 시간: ${start} ~ ${end} (2시간 예약: 1만원)`;
		}
		submitBtn.disabled = false;
	});
	$('#cancelApplyBtn').on('click', function() {
		$('#rentalModalOverlay').removeClass('show');
	});

	$('#rentalUseGuide').on('click', function(e) {
		if (e.target.id === 'rentalUseGuide') {
			$(this).removeClass('show');
		}
	});

	$('#rentalModalOverlay').on('click', function(e) {
		if (e.target.id === 'rentalModalOverlay') {
			$(this).removeClass('show');
		}
	});


	$('#rentalDetailModal').on('click', function(e) {
		if (e.target.id === 'rentalDetailModal') {
			$(this).removeClass('show');
		}
	});
	// --- 예약 제출 ---
	window.submitRental = function() {
		document.getElementById('rentalUseGuide').classList.add('show');

		$('#useGuideBtn').on('click', function() {
			if (!$('#useGuideCheckBox').is(":checked")) {
				alert("이용 안내 확인 체크를 해주세요.");
				return;
			} else {
				$('#rentalUseGuide').removeClass('show');
				document.getElementById('rentalDetailModal').classList.add('show');
			}
		})
	}


	$('#detailConfirmBtn').on('click', function(e) {
		if (!$('#purpose').val() || !$('#groupName').val()) {
			alert("항목들을 작성해주세요.");
			return;
		}
		const headCount = $('#headCount').val();
		if (!headCount || isNaN(headCount) || Number(headCount) <= 0 || Number(headCount) > 100) {
			alert("사용 인원을 확인해주세요.");
			return;
		}

		rental();
	});

	function rental() {
		$('#rentalDetailModal').removeClass('show');
		document.getElementById('rentalModalOverlay').classList.add('show');
		if (!selectedRoom || !selectedDate) {
			alert('시설과 날짜를 먼저 선택해주세요.');
			return;
		}
		let equipmentList = [];

		if ($('#needBeam').is(':checked')) {
			equipmentList.push('빔프로젝터');
		}
		if ($('#needSound').is(':checked')) {
			equipmentList.push('음향장비');
		}
		const headCount = $('#headCount').val();
		const date = document.querySelector('#datePicker').value;
		const times = document.querySelector('#selected-time-display').innerText;
		const room = document.querySelector('.room-btn.active').dataset.room;
		document.getElementById('modalDate').innerText = date;
		document.getElementById('modalTime').innerText = times;
		document.getElementById('modalRoom').innerText = room;

		document.getElementById('rentalModalOverlay').classList.add('show');

		let payload = {
			id: null,
			location: selectedRoom,
			reservationDate: selectedDate,
			startTime: "",
			endTime: "",
			price: 0,
			userId: null,
			createdAt: null,
			isApp: null,
			purpose: $('#purpose').val(),
			groupName: $('#groupName').val(),
			equipment: equipmentList.join(', '),
			headCount: headCount
		};
		if (selectedRoom === "강당") {
			const bookingChoice = document.querySelector('input[name="bookingType"]:checked');
			let bookingType = bookingChoice ? bookingChoice.value : '4시간';
			if (bookingType === "하루종일") {
				payload.startTime = "09:00";
				payload.endTime = "22:00";
				payload.price = 100000;
			} else if (bookingChoice && bookingChoice.value === "4시간") {
				if (selectedSlots.length !== 8) {
					alert('4시간 예약을 위해 4시간 연속 시간대를 선택해주세요.');
					return;
				}
				const start = selectedSlots[0];
				const startIdx = timeSlots.indexOf(start);
				const end = timeSlots[startIdx + 8];
				if (!end) {
					alert('선택한 시작 시간은 4시간 예약이 불가능한 시간입니다.');
					return;
				}
				payload.startTime = start;
				payload.endTime = end;
				payload.price = 50000;
			}
		} else {
			if (selectedSlots.length !== 4) {
				alert('2시간 예약을 위해 2시간 연속 시간대를 선택해주세요.');
				return;
			}
			const start = selectedSlots[0];
			const startIdx = timeSlots.indexOf(start);
			const end = timeSlots[startIdx + 4];
			if (!end) {
				alert('선택한 시작 시간은 2시간 예약이 불가능한 시간입니다.');
				return;
			}
			payload.startTime = start;
			payload.endTime = end;
			payload.price = 10000;
		}
		const rawPrice = payload.price;

		$('#confirmApplyBtn').off('click').on('click', function() {
			if (!$('#privacyConsentCheckbox').is(':checked')) {
				alert('개인정보 수집·이용·제공에 동의하셔야 신청이 가능합니다.');
				return;
			}
			submitBtn.disabled = true;
			payload.price = formatPrice(rawPrice);
			payload.purpose = $('#purpose').val(),
				payload.groupName = $('#groupName').val();
			$('#programApplyModalOverlay').removeClass('show');
			$('#spinnerOverlay').show();
			$.ajax({
				url: '/main/submitRental',
				type: 'POST',
				data: JSON.stringify(payload),
				contentType: 'application/json',
				success: function(response) {
					if (response == "success") {
						alert(`대관 신청 완료되었습니다!\n이메일을 확인해주세요.\n시설: ${selectedRoom}\n날짜: ${selectedDate}\n시간: ${selectedSlots[0]} ~ ${selectedSlots[3]}`);
						window.location.href = "/main/rental"
					}
				},
				error: function(xhr, status, error) {
					alert('대관 신청 실패. 다시 시도해주세요.');
					$('#spinnerOverlay').hide();
					submitBtn.disabled = false;
				}
			});
		});
	};


	document.querySelector('.room-btn[data-room="강의실1"]')?.click();
});


