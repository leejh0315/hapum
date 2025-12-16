// 예약 가능 날짜 설정 (오늘 ~ 30일 후)
const today = new Date();
const startDate = new Date(today);
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

const dateRangeText = document.getElementById('date-range-text');
const datePicker = document.getElementById('datePicker');
const submitBtn = document.getElementById('submit-btn');
const selectedTimeDisplay = document.getElementById('selected-time-display');
const timetableContainer = document.getElementById('timetable-container');

function formatDate(d) {
	let mm = d.getMonth() + 1;
	let dd = d.getDate();
	return d.getFullYear() + '-' + (mm < 10 ? '0' + mm : mm) + '-' + (dd < 10 ? '0' + dd : dd);
}

// 오늘 날짜 기본값 설정 및 min, max 설정
dateRangeText.textContent = `${formatDate(startDate)} ~ ${formatDate(endDate)}`;
datePicker.min = formatDate(startDate);
datePicker.max = formatDate(endDate);
datePicker.value = formatDate(today);  // 오늘 날짜 기본 선택

// 요일/주차 계산 유틸
function getWeekOfMonth(date) {
	const day = date.getDate();
	return Math.floor((day - 1) / 7) + 1;
}

// fixedReservations로부터 차단 시간 가져오기
function getFixedReservationTimesForDate(dateStr) {
	const date = new Date(dateStr);
	const weekday = date.getDay(); // 0=Sun ~ 6=Sat
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
					blocked.push({ time: timeSlots[i], reason: fr.description });
				}
			}
		}
	}
	return blocked;
}

// room 버튼 클릭
document.querySelectorAll('.room-btn').forEach(btn => {
	btn.addEventListener('click', () => {
		selectedRoom = btn.getAttribute('data-room');
		selectedDate = null;
		selectedSlots = [];
		submitBtn.disabled = true;
		selectedTimeDisplay.textContent = '';
		timetableContainer.style.display = 'none';

		const roomInfo = roomData[selectedRoom];
		if (!roomInfo) return;

		document.getElementById('room-title').textContent = selectedRoom;
		document.getElementById('floor-title').textContent = `${roomInfo.floor}층`;

		const contentDiv = document.getElementById('room-content');
		contentDiv.innerHTML = `
			<img src="${roomInfo.img}" style="max-width: 350px; max-height: 350px; border-radius: 10px;" />
			<div>${roomInfo.html}</div>
		`;

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

		timeButton.addEventListener('click', () => {
			if (!selectedRoom) {
				alert('시설을 먼저 선택해주세요.');
				return;
			}
			timetableContainer.style.display = 'block';

			const timeSlotsRow = document.getElementById('time-slots-row');
			timeSlotsRow.innerHTML = '<div class="label-cell">예약</div>';
			for (let t of timeSlots) {
				const slotDiv = document.createElement('div');
				slotDiv.className = 'slot';
				slotDiv.dataset.time = t;
				slotDiv.textContent = t;
				timeSlotsRow.appendChild(slotDiv);
			}

			// 오늘 날짜로 초기값 설정 및 선택 초기화
			selectedDate = datePicker.value || formatDate(today);
			selectedSlots = [];
			selectedTimeDisplay.textContent = '';
			submitBtn.disabled = true;
		});

		contentDiv.appendChild(timeButton);
		document.getElementById('room-info').style.display = 'block';
	});
});

// 날짜 선택 시
datePicker.addEventListener('change', () => {
	if (!selectedRoom) {
		alert('시설을 먼저 선택해주세요.');
		datePicker.value = formatDate(today);
		return;
	}
	selectedDate = datePicker.value;
	selectedSlots = [];
	submitBtn.disabled = true;
	selectedTimeDisplay.textContent = '';

	// 초기화 및 기존 툴팁 삭제 + 이벤트 초기화
	const slots = document.querySelectorAll('#time-slots-row .slot');
	slots.forEach(slot => {
		slot.classList.remove('selected', 'blocked-slot');
		slot.style.pointerEvents = 'auto';

		const oldTooltip = slot.querySelector('.tooltip');
		if (oldTooltip) oldTooltip.remove();

		// 이벤트 리스너 중복 방지 위해 클론 노드로 교체
		const newSlot = slot.cloneNode(true);
		slot.parentNode.replaceChild(newSlot, slot);
	});

	const selected = new Date(selectedDate);
	const weekday = selected.getDay();
	const weekOfMonth = getWeekOfMonth(selected);

	const blockedTimesWithReason = blockedDays
		.filter(bd => bd.weekOfMonth === weekOfMonth && bd.dayOfWeek === weekday)
		.flatMap(() => timeSlots.map(t => ({ time: t, reason: '대관 불가' })));

	const fixedTimesWithReason = getFixedReservationTimesForDate(selectedDate);

	const blockedMap = new Map();
	blockedTimesWithReason.forEach(({ time, reason }) => blockedMap.set(time, reason));
	fixedTimesWithReason.forEach(({ time, reason }) => blockedMap.set(time, reason));

	// 툴팁 다시 생성 + 이벤트 연결
	document.querySelectorAll('#time-slots-row .slot').forEach(slot => {
		const reason = blockedMap.get(slot.dataset.time);
		if (reason) {
			slot.classList.add('blocked-slot');
			slot.style.pointerEvents = 'none';

			const tooltip = document.createElement('div');
			tooltip.className = 'tooltip';
			tooltip.textContent = reason;
			slot.appendChild(tooltip);

			slot.addEventListener('mouseenter', () => tooltip.classList.add('visible'));
			slot.addEventListener('mouseleave', () => tooltip.classList.remove('visible'));
		}
	});
});

// 시간 슬롯 클릭 - 2시간 연속 (총 4 슬롯 선택)
document.getElementById('time-slots-row').addEventListener('click', (e) => {
	if (!selectedDate || !selectedRoom) {
		alert('시설과 날짜를 먼저 선택해주세요.');
		return;
	}
	const slot = e.target.closest('.slot');
	if (!slot || slot.classList.contains('blocked-slot')) return;

	const idx = timeSlots.indexOf(slot.dataset.time);
	if (idx < 0 || idx + 3 >= timeSlots.length) {
		alert('2시간 연속 예약은 마지막 시간 이후로 선택할 수 없습니다.');
		return;
	}

	// 연속 4개 슬롯 예약 가능한지 확인
	for (let i = idx; i <= idx + 3; i++) {
		const t = timeSlots[i];
		const s = document.querySelector(`#time-slots-row .slot[data-time="${t}"]`);
		if (!s || s.classList.contains('blocked-slot')) {
			alert('선택한 2시간 구간 중 예약 불가능한 시간이 포함되어 있습니다.');
			return;
		}
	}

	// 초기화 후 선택
	document.querySelectorAll('#time-slots-row .slot.selected').forEach(s => s.classList.remove('selected'));

	selectedSlots = [];
	for (let i = idx; i <= idx + 3; i++) {
		const t = timeSlots[i];
		const s = document.querySelector(`#time-slots-row .slot[data-time="${t}"]`);
		if (s) {
			s.classList.add('selected');
			selectedSlots.push(t);
		}
	}

	const start = selectedSlots[0];
	const startIdx = timeSlots.indexOf(start);
	const end = timeSlots[startIdx + 4];
	if (!end) {
		alert('선택한 시작 시간은 2시간 예약이 불가능한 시간입니다.');

		// 선택된 슬롯 초기화
		document.querySelectorAll('#time-slots-row .slot.selected').forEach(s => s.classList.remove('selected'));
		selectedSlots = [];
		selectedTimeDisplay.textContent = '';
		submitBtn.disabled = true;

		return;
	}

	// 시설, 날짜, 시간 표시
	selectedTimeDisplay.textContent = `시설: ${selectedRoom} / 날짜: ${selectedDate} / 시간: ${start} ~ ${end}`;

	submitBtn.disabled = false;
});

// 제출
function submitRental() {
	if (!selectedRoom || !selectedDate || selectedSlots.length !== 4) {
		alert('시설, 날짜 및 2시간 연속 시간을 정확히 선택해주세요.');
		return;
	}
	const date = document.querySelector('#datePicker').value;
      const times = document.querySelector('#selected-time-display').innerText;
      const room = document.querySelector('.room-btn.active').dataset.room;
      document.getElementById('modalDate').innerText = date;
      document.getElementById('modalTime').innerText = times;
      document.getElementById('modalRoom').innerText = room;
	 document.getElementById('rentalModalOverlay').classList.add('show');

	//alert(`대관 신청 완료되었습니다!\n시설: ${selectedRoom}\n날짜: ${selectedDate}\n시간: ${selectedSlots[0]} ~ ${selectedSlots[3]}`);
}
