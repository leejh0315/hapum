
		$(document).ready(function () {


			// 모든 input 요소에서 Enter 키 눌렀을 때 form submit 방지
			$('form input').on('keydown', function (e) {
				if (e.key === 'Enter') {
					e.preventDefault();
					return false;
				}
			});

			if ($('input[name="emailChecked"]').val() === "true") {
				$('#submit-btn').css('display', 'inline-block');
				$('input[name="email"]').prop('readonly', true);
				$('#checkDuplicateBtn').hide();
			}

			// 중복확인 버튼 클릭 시 처리
			$('#checkDuplicateBtn').on('click', function () {
				const email = $('input[name="email"]').val();

				if (!email) {
					alert('이메일을 입력해주세요.');
					return;
				}

				$.ajax({
					type: 'POST',
					url: '/auth/emailCheck', // <-- 여기서 endpoint도 변경
					contentType: 'application/json',
					data: JSON.stringify({email: email}),
					success: function (res) {
						if (res === 2) {
							alert('이메일 형식을 확인해주세요.');
							return;
						}

						if (res === 1) {
							alert('이미 존재하는 이메일입니다.');
							return;
						}

						// 사용 가능한 이메일 - 사용자 확인
						const useEmail = confirm('사용 가능한 이메일입니다. 이 이메일을 사용하시겠습니까?');

						if (useEmail) {
							$('input[name="email"]').prop('readonly', true);
							$('#checkDuplicateBtn').hide();
							$('input[name="emailChecked"]').val(true);

							// 이메일 인증 버튼 표시
							$('#sendVerificationBtn').show();

							// 회원가입 버튼은 숨긴 상태 유지
							$('#submit-btn').hide();
						} else {
							$('input[name="email"]').val('').focus();
						}
					},
					error: function () {
						alert('서버 오류가 발생했습니다. 다시 시도해주세요.');
					}
				});
			});

			let timerInterval;

			// 타이머 함수
			function startTimer(durationInSeconds) {
				clearInterval(timerInterval); // 기존 타이머 제거
				let remaining = durationInSeconds;

				function updateTimer() {
					const minutes = Math.floor(remaining / 60);
					const seconds = remaining % 60;
					$('#timer').text(`${minutes}:${seconds < 180 ? seconds : seconds}`);
					remaining--;

					if (remaining < 0) {
						clearInterval(timerInterval);
						$('timerP').hide();
						$('#timer').text('인증 시간이 만료되었습니다.');

						// 인증번호 입력창과 인증확인 버튼 숨기기
						$('#verificationCodeInput').hide();
						$('#verifyCodeBtn').hide();

						// 재발송 버튼만 보이기
						$('#resendBtn').show();
					}
				}

				updateTimer();
				timerInterval = setInterval(updateTimer, 1000);
			}

			$('#sendVerificationBtn').on('click', function () {
				const email = $('input[name="email"]').val();

				if (!email) {
					alert('이메일을 입력해주세요.');
					return;
				}

				// 인증 UI 먼저 보여주기
				$('#verificationSection').show();  // 입력창, 버튼 보이기
				$('#sendVerificationBtn').hide();  // 요청 버튼 숨김

				// 먼저 alert 띄우고, alert이 닫힌 후에 타이머 시작
				alert('인증 메일이 발송되었습니다.\n10~30초 소요될 수 있습니다.\n메일을 확인하고 인증번호를 입력해주세요.');


				// 타이머 시작 (alert 이후에 실행)
				startTimer(180);  // 1분 타이머 시작

				// 서버로 인증 메일 발송 요청
				$.ajax({
					type: 'POST',
					url: '/auth/authEmail',
					contentType: 'application/json',
					data: JSON.stringify({email: email}),
					success: function () {
						console.log('이메일 전송 성공');
					},
					error: function () {
						alert('이메일 인증 요청 중 오류가 발생했습니다.');
					}
				});
			});

			// 재발송 버튼 클릭 시 처리
			$('#resendBtn').on('click', function () {
				const email = $('input[name="email"]').val();
				alert('인증 메일이 재발송되었습니다. 다시 메일을 확인해주세요.');

				// 인증 UI를 복원 (인증번호 입력창, 인증 확인 버튼 다시 보이게)
				$('#verificationCodeInput').val('').show(); // 입력창 초기화 후 다시 표시
				$('#verifyCodeBtn').show();                 // 인증 확인 버튼 다시 표시
				$('#resendBtn').hide();                     // 재발송 버튼 숨김

				// 타이머 재시작
				startTimer(180); // 3분 타이머 재시작

				// 인증 메일 재발송 요청
				$.ajax({
					type: 'POST',
					url: '/auth/authEmail',
					contentType: 'application/json',
					data: JSON.stringify({email: email}),
					success: function () {
						console.log('이메일 재발송 성공');
					},
					error: function () {
						alert('이메일 재발송 중 오류가 발생했습니다.');
					}
				});
			});

			$('#verifyCodeBtn').on('click', function () {
				const email = $('input[name="email"]').val();
				const number = $('#verificationCodeInput').val();

				if (!number) {
					alert('인증번호를 입력해주세요.');
					return;
				}

				$.ajax({
					type: 'POST',
					url: '/auth/numberCheck',
					contentType: 'application/json',
					data: JSON.stringify({email: email, number: number}),
					success: function (res) {
						if (res === '1') {
							alert('인증에 성공했습니다!');
							$('#submit-btn').show(); // 회원가입 버튼 표시
							$('#verifyCodeBtn').hide(); // 인증 확인 버튼 숨김
							$('#verificationCodeInput').hide(); // 인증번호 입력창 숨기기
							clearInterval(timerInterval); // 타이머 정지
							$('#timer').hide(); // 타이머 숨김
							$('#timerP').hide();
							$('input[name="emailVerificationPassed"]').val(true);
						} else if (res === '2') {
							alert('인증 시간이 만료되었습니다. 재발송해주세요.');
						} else {
							alert('인증번호가 일치하지 않습니다.');
						}
					},
					error: function () {
						alert('서버 오류가 발생했습니다. 다시 시도해주세요.');
					}
				});
			});

			$('#submit-btn').on('click', function () {
				$('#signupModalOverlay').fadeIn(200);
			});

			$('#confirmSubmit').on('click', function () {
				if (!$('#privacyConsentCheckbox').is(':checked')) {
					alert('개인정보 수집·이용에 동의하셔야 회원가입이 가능합니다.');
					return;
				}
				$('#signupModalOverlay').fadeOut(200);
				$('form').submit();
			});

			// 모달 취소 버튼
			$('#cancelSubmit').on('click', function () {
				$('#signupModalOverlay').fadeOut(200);
			});

			// 오버레이 클릭 시 모달 닫기 (모달 외부 클릭 시)
			$('#signupModalOverlay').on('click', function (e) {
				if (e.target.id === 'signupModalOverlay') {
					$(this).fadeOut(200);
				}
			});
		});