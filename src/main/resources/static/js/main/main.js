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







});


$(function() {
  const $track   = $('.gallery-track');
  const $items   = $('.gallery-item');
  let total      = $items.length;
  let visible    = 5;                            // 한 번에 보일 개수
  let index      = 0;
  let itemW      = $items.outerWidth(true);      // 아이템 너비 + 마진
  let maxIndex   = Math.max(0, total - visible); // 이동 가능 최대 index

  function update() {
    $track.css('transform', 'translateX(' + (-index * itemW) + 'px)');
    $('#gallery-prev').prop('disabled', index <= 0);
    $('#gallery-next').prop('disabled', index >= maxIndex);
  }

  // ✅ 오른쪽 버튼: 다음 이미지로 이동 (index 증가)
  $('#gallery-next').on('click', function() {
    if (index < maxIndex) {
      index++;
      update();
    }
  });

  // ✅ 왼쪽 버튼: 이전 이미지로 이동 (index 감소)
  $('#gallery-prev').on('click', function() {
    if (index > 0) {
      index--;
      update();
    }
  });

  // ✅ 리사이즈 시 다시 계산
  $(window).on('resize', function() {
    itemW    = $('.gallery-item').outerWidth(true);
    total    = $('.gallery-item').length;
    maxIndex = Math.max(0, total - visible);
    index    = Math.min(index, maxIndex);
    update();
  });

  update();
});