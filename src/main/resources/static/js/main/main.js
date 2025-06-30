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
  var $track    = $('.gallery-track'),
      $items    = $('.gallery-item'),
      total     = $items.length,
      visible   = 5,                          // 한번에 보일 아이템 수
      index     = 0,                          // 현재 슬라이드 offset
      itemW     = $items.outerWidth(true),    // 150px + 10px 마진
      maxIndex  = Math.max(0, total - visible);

  function update() {
    // transform으로 X축 이동시켜 슬라이드
    $track.css('transform', 'translateX(' + (-index * itemW) + 'px)');
    // 버튼 활성/비활성 토글
    $('#gallery-prev').prop('disabled', index >= maxIndex);
    $('#gallery-next').prop('disabled', index <= 0);
  }

  // 다음(→) 버튼 클릭: [1,2,3,4,5]→[2,3,4,5,6]
  $('#gallery-prev').on('click', function() {
    if (index < maxIndex) {
      index++;
      update();
    }
  });

  // 이전(←) 버튼 클릭: [2,3,4,5,6]→[1,2,3,4,5]
  $('#gallery-next').on('click', function() {
    if (index > 0) {
      index--;
      update();
    }
  });

  // 윈도우 리사이즈 시 재계산
  $(window).on('resize', function() {
    itemW    = $('.gallery-item').outerWidth(true);
    maxIndex = Math.max(0, $('.gallery-item').length - visible);
    index    = Math.min(index, maxIndex);
    update();
  });

  // 초기 렌더링 (최초에는 index=0 이므로 1~5번만 보임)
  update();
});