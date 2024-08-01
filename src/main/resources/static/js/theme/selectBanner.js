
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
            allRadio[0].focus();
            return false;
        }
    }

    applyLoadingButton(button);
}

function loadBannerImagesForSelectedCategory(focusOnFirst) {
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

        // we need to do this at the end of the load
        $('#sr-msg').html("Now displaying image options for the " + categoryName + " category.");
        if (focusOnFirst) {
            // move focus to first image
            $('input[type="radio"][name=banner-image-radio-group]').first().focus();
        }
    });


}


$(document).ready(function() {
    // determine if additional fields display or not
    $('input[name=includeBannerImage]').on('change', function() {
        if ( this.value == 'true') {
            $("#banner-wrapper").removeClass('rvt-display-none');
            $('#sr-msg').html('Select your banner image below.')
        } else {
            $("#banner-wrapper").addClass('rvt-display-none');
            $('#banner-image-id').val('');
            $('input[type="radio"][name=banner-image-radio-group]:checked').prop('checked', false);
        }
    });

    $('#select-input-categories').change(function() {
        // reset validation
        $("#select-alert").addClass("rvt-display-none");

        loadBannerImagesForSelectedCategory(true);

    });

    if ($('#include-banner-image-radio-yes').is(':checked')) {
        $("#banner-wrapper").removeClass('rvt-display-none');
    } else {
        $("#banner-wrapper").addClass('rvt-display-none');
        $('#banner-image-id').val('');
        $('input[type="radio"][name=banner-image-radio-group]:checked').prop('checked', false);
        $("#sr-msg").html(""); // reset the sr messaging
    }

    loadBannerImagesForSelectedCategory(false);
});