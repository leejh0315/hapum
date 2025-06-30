$(function() {
	// ────────────────────────────────────────────
	// 1) 공지사항: 순서 수정 & 상단고정 토글
	// ────────────────────────────────────────────
	var $list = $('#notification-list'),
		$editBtn = $('#edit-order-btn'),
		editing = false;

	$editBtn.on('click', function() {
		editing = !editing;
		$list.toggleClass('editing', editing);
		$editBtn.find('span').text(editing ? '수정완료' : '수정');

		if (!editing) {
			var payload = [];
			$list.find('input[name=orderNum]').each(function() {
				var $inp = $(this),
					id = $inp.data('id'),
					val = parseInt($inp.val(), 10) || 0;
				payload.push({ id: id, orderNum: val });
			});
			$.ajax({
				url: '/admin/notification/order',
				type: 'POST',
				contentType: 'application/json',
				data: JSON.stringify(payload),
				success: function() { location.reload(); },
				error: function() { alert('순서 저장 실패'); }
			});
		}
	});

	$list.on('click', '.toggle-top-btn', function() {
		var $btn = $(this),
			id = $btn.data('id'),
			isTo = String($btn.data('istro')),
			newTo = (isTo === '1' ? '0' : '1'),
			payload = { id: id, isTop: newTo };

		$.ajax({
			url: '/admin/notification/toggleTop',
			type: 'POST',
			contentType: 'application/json',
			data: JSON.stringify(payload),
			success: function() { location.reload(); },
			error: function() { alert('고정 토글 실패'); }
		});
	});


	// ────────────────────────────────────────────
	// 2) 갤러리 슬라이더 (한 장씩 이동, 반응형)
	// ────────────────────────────────────────────
	var $container = $('.gallery-container'),
		$track = $('.gallery-track'),
		$items = $('.gallery-item'),
		itemCount = $items.length,
		currentIndex = 0,
		itemWidth,
		visibleCount,
		maxIndex;

	function recalc() {
		itemWidth = $items.outerWidth(true);
		visibleCount = Math.floor($container.width() / itemWidth);
		maxIndex = Math.max(0, itemCount - visibleCount);
		currentIndex = Math.min(currentIndex, maxIndex);
		slide();
	}

	function slide() {
		$track.css('transform', 'translateX(' + (-currentIndex * itemWidth) + 'px)');
		$('#gallery-prev').prop('disabled', currentIndex === 0);
		$('#gallery-next').prop('disabled', currentIndex === maxIndex);
	}

	$('#gallery-prev').on('click', function() {
		if (currentIndex > 0) {
			currentIndex--;
			slide();
		}
	});

	$('#gallery-next').on('click', function() {
		if (currentIndex < maxIndex) {
			currentIndex++;
			slide();
		}
	});

	$(window).on('resize', recalc);
	recalc();
});
$(function() {
	var $container = $('.gallery-container'),
		$track = $('.gallery-track'),
		$items = $('.gallery-item'),
		currentIndex = 0,
		itemWidth, visibleCount, maxIndex;

	// 슬라이더 상태 계산 & 렌더링
	function update() {
		itemWidth = $items.outerWidth(true);
		visibleCount = Math.floor($container.width() / itemWidth);
		maxIndex = Math.max(0, $items.length - visibleCount);
		currentIndex = Math.min(Math.max(currentIndex, 0), maxIndex);

		$track.css('transform', 'translateX(' + (-currentIndex * itemWidth) + 'px)');
		$('#gallery-prev').prop('disabled', currentIndex === 0);
		$('#gallery-next').prop('disabled', currentIndex === maxIndex);
	}

	// 오른쪽(다음) 버튼
	$('#gallery-next').on('click', function() {
		if (currentIndex < maxIndex) {
			currentIndex++;
			update();
		}
	});

	// 왼쪽(이전) 버튼
	$('#gallery-prev').on('click', function() {
		if (currentIndex > 0) {
			currentIndex--;
			update();
		}
	});

	// 윈도우 리사이즈 시 재계산
	$(window).on('resize', update);

	// 초기 실행
	update();
});