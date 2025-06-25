	$(function() {
		var $list = $('#notification-list'),
			$btn = $('#edit-order-btn'),
			editing = false;

		$btn.on('click', function() {
			editing = !editing;
			$list.toggleClass('editing', editing);
			$btn.find('span').text(editing ? '수정완료' : '수정');

			if (!editing) {
				// 수정완료: order 값 수집 & AJAX 전송
				var payload = [];
				$list.find('input[name=orderNum]').each(function() {
					var $inp = $(this);
					var id = $inp.data('id');
					var val = parseInt($inp.val(), 10) || 0;
					payload.push({ id: id, orderNum: val });
				});
				console.log(payload)
				$.ajax({
					url: '/admin/notification/order',
					type: 'POST',
					contentType: 'application/json',
					data: JSON.stringify(payload),
					success: function(res) {
						// 필요에 따라 알림 or 새로고침
						location.reload();
					},
					error: function() {
						alert('순서 저장에 실패했습니다.');
					}
				});
			}
		});

		$('#notification-list').on('click', '.toggle-top-btn', function() {
			var $btn = $(this),
				id = $btn.data('id'),
				isTo = $btn.data('istro') + '',       // 기존 값 '0' or '1'
				newTo = (isTo === '1' ? '0' : '1'),
				payload = { id: id, isTop: newTo };

			$.ajax({
				url: '/admin/notification/toggleTop',
				type: 'POST',
				contentType: 'application/json',
				data: JSON.stringify(payload),
				success: function() {
					alert("성공");
					location.reload();
					/*
					// data-istro 업데이트
					$btn.data('istro', newTo);
					// 버튼 아이콘 토글
					$btn.find('i.fa-solid')
						.toggleClass('fa-arrow-turn-up fa-arrow-turn-down');
					// 제목 볼드/핀 아이콘 토글
					var $span = $btn.siblings('a').find('span'),
						$pin = $btn.siblings('a').find('.pin-icon');

					if (newTo === '1') {
						$span.addClass('top-bold');
						if (!$pin.length) {
							$btn.siblings('a')
								.append('<i class="fa-solid fa-thumbtack pin-icon"></i>');
						}
					} else {
						$span.removeClass('top-bold');
						$pin.remove();
					}
					*/
				},
				error: function() {
					alert('변환에 실패했습니다.');
				}
			});
		});


	});
