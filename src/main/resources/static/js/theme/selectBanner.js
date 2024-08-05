/*-
 * #%L
 * course-setup-wizard
 * %%
 * Copyright (C) 2022 - 2024 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

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
