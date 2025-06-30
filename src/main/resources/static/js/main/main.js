$(function() {
  // ──────────────────────────────────────────────────────
  // 1) 공지사항: 순서 수정 & 상단 고정 토글
  // ──────────────────────────────────────────────────────
  var $list    = $('#notification-list'),
      $editBtn = $('#edit-order-btn'),
      editing  = false;

  // 수정/수정완료 토글
  $editBtn.on('click', function() {
    editing = !editing;
    $list.toggleClass('editing', editing);
    $editBtn.find('span').text(editing ? '수정완료' : '수정');

    if (!editing) {
      // 수정완료 시 입력된 순서값 수집 & 전송
      var payload = [];
      $list.find('input[name=orderNum]').each(function() {
        var $inp = $(this),
            id   = $inp.data('id'),
            val  = parseInt($inp.val(), 10) || 0;
        payload.push({ id: id, orderNum: val });
      });

      $.ajax({
        url: '/admin/notification/order',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(payload),
        success: function() {
          location.reload();
        },
        error: function() {
          alert('순서 저장에 실패했습니다.');
        }
      });
    }
  });

  // 상단 고정 토글
  $list.on('click', '.toggle-top-btn', function() {
    var $btn    = $(this),
        id      = $btn.data('id'),
        isTo    = String($btn.data('istro')),       // '0' or '1'
        newTo   = (isTo === '1' ? '0' : '1'),
        payload = { id: id, isTop: newTo };

    $.ajax({
      url: '/admin/notification/toggleTop',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(payload),
      success: function() {
        location.reload();
      },
      error: function() {
        alert('고정 토글에 실패했습니다.');
      }
    });
  });


  // ──────────────────────────────────────────────────────
  // 2) 갤러리 슬라이더 (4개씩 묶음, 자동·수동 네비게이션)
  // ──────────────────────────────────────────────────────
  var $track        = $('.gallery-track'),
      itemsPerPage  = 4,
      itemCount     = $track.children().length,
      pageCount     = Math.ceil(itemCount / itemsPerPage),
      currentPage   = 0,
      autoTimer;

  // 버튼 활성/비활성
  function updateButtons() {
    $('#gallery-prev').prop('disabled', currentPage === 0);
    $('#gallery-next').prop('disabled', currentPage === pageCount - 1);
  }

  // 실제 슬라이드 이동
  function slide() {
    var shift = - currentPage * 100;
    $track.css('transform', 'translateX(' + shift + '%)');
    updateButtons();
  }

  // 수동 네비게이션
  $('#gallery-prev').on('click', function() {
    if (currentPage > 0) {
      currentPage--;
      slide();
    }
  });
  $('#gallery-next').on('click', function() {
    if (currentPage < pageCount - 1) {
      currentPage++;
      slide();
    }
  });

  // 자동 재생 시작/정지
  function startAuto() {
    autoTimer = setInterval(function() {
      currentPage = (currentPage + 1) % pageCount;
      slide();
    }, 3000);
  }
  function stopAuto() {
    clearInterval(autoTimer);
  }

  // 호버 시 자동 재생 멈추고, 떠나면 재시작
  $('.gallery-wrapper')
    .on('mouseenter', stopAuto)
    .on('mouseleave', startAuto);

  // 초기화
  slide();
  startAuto();
});
