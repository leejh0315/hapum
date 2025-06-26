$(document).ready(function() {


	var $slides = $('#slideshow img');
	var currentIndex = 0;
	var slideCount = $slides.length;
	var transitioning = false;

	// 초기 설정: 첫 이미지 보이도록, 나머지는 오른쪽에 배치
	var containerWidth = $('#slideshow-container').width();
	$slides.each(function(index) {
		if (index === 0) {
			$(this).css({ left: '0px', display: 'block' });
		} else {
			$(this).css({ left: containerWidth + 'px', display: 'none' });
		}
	});

	// 슬라이드 전환 함수
	// newIndex: 이동할 슬라이드 인덱스, direction: 1(다음 → 왼쪽 이동), -1(이전 → 오른쪽 이동)
	function showSlide(newIndex, direction) {
		if (transitioning || newIndex === currentIndex) return;
		transitioning = true;
		containerWidth = $('#slideshow-container').width();

		var $currentSlide = $slides.eq(currentIndex);
		var $nextSlide = $slides.eq(newIndex);
		var startLeft = (direction === 1) ? containerWidth : -containerWidth;
		var endCurrentLeft = (direction === 1) ? -containerWidth : containerWidth;

		// 다음 슬라이드를 위치시키고 표시
		$nextSlide.css({ left: startLeft, display: 'block' });

		var animationsCompleted = 0;
		function animationComplete() {
			animationsCompleted++;
			if (animationsCompleted === 2) {
				transitioning = false;
			}
		}

		// 현재 슬라이드는 바깥으로, 다음 슬라이드는 중앙으로 이동
		$currentSlide.animate({ left: endCurrentLeft }, 500, function() {
			$currentSlide.css({ display: 'none' });
			animationComplete();
		});
		$nextSlide.animate({ left: 0 }, 500, animationComplete);

		currentIndex = newIndex;
	}

	// 다음 버튼 이벤트
	$('#next-btn').click(function() {
		var nextIndex = (currentIndex + 1) % slideCount;
		showSlide(nextIndex, 1);
	});

	// 이전 버튼 이벤트
	$('#prev-btn').click(function() {
		var prevIndex = (currentIndex - 1 + slideCount) % slideCount;
		showSlide(prevIndex, -1);
	});

	// 자동 슬라이드: 2초마다 자동 전환
	setInterval(function() {
		var nextIndex = (currentIndex + 1) % slideCount;
		showSlide(nextIndex, 1);
	}, 2000);
});