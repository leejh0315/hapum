$(document).ready(function () {
  const $slides = $('#slideshow img');
  let currentIndex = 0;
  const slideCount = $slides.length;
  let transitioning = false;

  const containerWidth = $('#slideshow-container').width();

  // 초기 슬라이드 위치 설정
  $slides.each(function (index) {
    const $img = $(this);
    const id = $img.data('program-id');

    $img.css({
      left: index === 0 ? '0px' : containerWidth + 'px',
      display: index === 0 ? 'block' : 'none',
      position: 'absolute',
      'view-transition-name': `program-thumbnail-${id}`
    });
  });

  function showSlide(newIndex, direction) {
    if (transitioning || newIndex === currentIndex) return;

    transitioning = true;
    const containerWidth = $('#slideshow-container').width();

    const $currentSlide = $slides.eq(currentIndex);
    const $nextSlide = $slides.eq(newIndex);

    const startLeft = direction === 1 ? containerWidth : -containerWidth;
    const endCurrentLeft = direction === 1 ? -containerWidth : containerWidth;

    $nextSlide.css({ left: startLeft, display: 'block' });

    let animationsDone = 0;
    const onAnimationComplete = () => {
      animationsDone++;
      if (animationsDone === 2) {
        transitioning = false;
      }
    };

    $currentSlide.animate({ left: endCurrentLeft }, 500, function () {
      $currentSlide.css({ display: 'none' });
      onAnimationComplete();
    });

    $nextSlide.animate({ left: 0 }, 500, onAnimationComplete);

    currentIndex = newIndex;
  }

  $('#next-btn').click(function () {
    const nextIndex = (currentIndex + 1) % slideCount;
    showSlide(nextIndex, 1);
  });

  $('#prev-btn').click(function () {
    const prevIndex = (currentIndex - 1 + slideCount) % slideCount;
    showSlide(prevIndex, -1);
  });

  // 자동 슬라이드 (4초 간격)
  setInterval(function () {
    const nextIndex = (currentIndex + 1) % slideCount;
    showSlide(nextIndex, 1);
  }, 4000);

  // View Transition 클릭 이벤트
  $slides.each(function () {
    const $img = $(this);
    const id = $img.data('program-id');

    $img.parent().css('cursor', 'pointer').on('click', function () {
      const url = `/main/program/detail/${id}`;
      if (document.startViewTransition) {
        document.startViewTransition(() => {
          window.location.href = url;
        });
      } else {
        window.location.href = url;
      }
    });
  });
});
