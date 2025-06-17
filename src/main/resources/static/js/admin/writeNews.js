let selectedImages = [];
let thumbnailIndex = 0;

document.getElementById('images').addEventListener('change', function (event) {
  const files = Array.from(event.target.files);
  const previewContainer = document.getElementById('preview-container');

  if (selectedImages.length + files.length > 5) {
    alert("사진은 최대 5장까지 첨부할 수 있습니다.");
    return;
  }

  files.forEach(file => {
    if (!file.type.startsWith('image/')) return;

    const reader = new FileReader();
    reader.onload = function (e) {
      const previewDiv = document.createElement('div');
      previewDiv.className = 'preview-item';

      const img = document.createElement('img');
      img.src = e.target.result;
      img.className = 'preview-image';

      const delBtn = document.createElement('button');
      delBtn.innerText = 'X';
      delBtn.className = 'delete-button';
      delBtn.onclick = function (event) {
        event.stopPropagation();
        const removeIndex = selectedImages.indexOf(file);
        if (removeIndex !== -1) {
          selectedImages.splice(removeIndex, 1);

          if (thumbnailIndex === removeIndex) {
            thumbnailIndex = selectedImages.length > 0 ? 0 : -1;
          } else if (thumbnailIndex > removeIndex) {
            thumbnailIndex -= 1;
          }

          previewDiv.remove();
          updateHiddenImageInputs();
          updateThumbnailBorders();
        }
      };

      previewDiv.onclick = function () {
        thumbnailIndex = selectedImages.indexOf(file);
        updateHiddenImageInputs();
        updateThumbnailBorders();
      };

      previewDiv.appendChild(img);
      previewDiv.appendChild(delBtn);
      previewContainer.appendChild(previewDiv);

      selectedImages.push(file);

      updateHiddenImageInputs();
      updateThumbnailBorders();
    };
    reader.readAsDataURL(file);
  });

  event.target.value = '';
});

function updateHiddenImageInputs() {
  const container = document.getElementById('hidden-image-inputs');
  container.innerHTML = '';

  selectedImages.forEach((file) => {
    const data = new DataTransfer();
    data.items.add(file);

    const input = document.createElement('input');
    input.type = 'file';
    input.name = 'images';
    input.files = data.files;
    container.appendChild(input);
  });

  const thumbIndexInput = document.createElement('input');
  thumbIndexInput.type = 'hidden';
  thumbIndexInput.name = 'thumbnailIndex';
  thumbIndexInput.value = thumbnailIndex >= 0 ? thumbnailIndex : 0;
  container.appendChild(thumbIndexInput);
}

function updateThumbnailBorders() {
  const previewItems = document.querySelectorAll('.preview-item');
  previewItems.forEach((item, index) => {
    item.classList.toggle('thumbnail-selected', index === thumbnailIndex);
  });
}

function validateNewsForm() {
  const title = document.getElementById("title").value.trim();
  const content = document.getElementById("content").value.trim();

  if (!title || !content) {
    alert("제목과 내용을 모두 입력해주세요.");
    return false;
  }

  if (selectedImages.length > 5) {
    alert("사진은 최대 5장까지 첨부할 수 있습니다.");
    return false;
  }

  if (thumbnailIndex < 0 && selectedImages.length > 0) {
    thumbnailIndex = 0;
    updateHiddenImageInputs();
  }

  return confirm("하품소식을 등록하시겠습니까?");
}
