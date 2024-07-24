
function selectBannerSubmit(button) {
    let yesBannerImage = document.querySelector('#include-banner-image-radio-yes:checked');

    if (yesBannerImage != null) {
        let checkedRadio = document.querySelector('input[type=radio][name=banner-image-radio-group]:checked');
        if (checkedRadio != null) {
            let bannerImageId = checkedRadio.value;
            $('#banner-image-id').val(bannerImageId);
        } else {
            let allRadio = document.getElementsByName("banner-image-radio-group");
            allRadio.forEach(btn => {
                btn.setAttribute("aria-describedby", "option-message");
            });
            alertMessage = document.getElementById('select-alert');
            alertMessage.classList.remove("rvt-display-none");
            return false;
        }
    }

    applyLoadingButton(button);
}

function loadBannerImagesForSelectedCategory() {
    let categoryId = $('#select-input-categories').val();
    let categoryName = $('#select-input-categories option[value="' + categoryId + '"]').text();
    $('#banner-image-category-legend').text('Image options for ' + categoryName);
    $('#image-ul').load('/app/theme/' + courseId + '/bannerimage/' + categoryId ,  function() {
        let bannerImageId = $('#banner-image-id').val();

        if (bannerImageId != '') {
            let radioThing = $('input[type=radio][name=banner-image-radio-group]');
            radioThing.each(function() {
                if (bannerImageId == $(this).val()) {
                    $(this).prop('checked', true);
                }
            });
        }
    });
}


$(document).ready(function() {
    // determine if additional fields display or not
    $('input[name=includeBannerImage]').on('change', function() {
        if ( this.value == 'true') {
            $("#banner-wrapper").removeClass('rvt-display-none');
        } else {
            $("#banner-wrapper").addClass('rvt-display-none');
            $('#banner-image-id').val('');
            $('input[type="radio"][name=banner-image-radio-group]:checked').prop('checked', false);
        }
    });

    $('#select-input-categories').change(function() {
        loadBannerImagesForSelectedCategory();
    });

    if ($('#include-banner-image-radio-yes').is(':checked')) {
        $("#banner-wrapper").removeClass('rvt-display-none');
    } else {
        $("#banner-wrapper").addClass('rvt-display-none');
        $('#banner-image-id').val('');
        $('input[type="radio"][name=banner-image-radio-group]:checked').prop('checked', false);
    }

    loadBannerImagesForSelectedCategory();
});