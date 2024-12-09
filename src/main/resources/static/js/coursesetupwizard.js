/*-
 * #%L
 * course-setup-wizard
 * %%
 * Copyright (C) 2022 Indiana University
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

jQuery(document).ready(function($) {

    $("#exit-wizard").click(function(event) {
        $("#dialog-exit-title").focus();
      });


    var invalidInputs = $("input[aria-invalid='true']");
    if (invalidInputs.length > 0) {
        invalidInputs.first().focus();
    } else {
        $('#csw-header').focus();
    }

    // trim extra spaces in the date inputs. Voice control sometimes adds a leading space to the date
    $('input.date-input').blur(function() {
        $(this).val($(this).val().trim());
    });

    $(".radio-card--clickable input").change(function() {
        $("#image-ul>li.radio-card--clickable--checked").removeClass("radio-card--clickable--checked");
        if ($(this).is(":checked")) {
            $(this).parent().parent().addClass("radio-card--clickable--checked");
        }
    });

    // If there is no validation on your button, add the loading-btn class to make it a loader.
    // Otherwise, you need to manually call applyLoadingButton after your validation
    $(".loading-btn").click(function() {
        let loadingBtn = $(this).get(0);
        applyLoadingButton(loadingBtn);
    });

});

function applyLoadingButton(loadingBtn) {
    loadingBtn.setAttribute("aria-busy", true);

    // disable all buttons on the page
    let buttonsToDisable = document.getElementsByTagName('button');
    for(var i = 0; i < buttonsToDisable.length; i++) {
        buttonsToDisable[i].disabled = true;
    }

    loadingBtn.classList.add("rvt-button--loading");

    let spinners = loadingBtn.getElementsByClassName('rvt-loader');
    if (spinners && spinners.length > 0) {
        spinners[0].classList.remove("rvt-display-none");
    }

    let srText = loadingBtn.getElementsByClassName('sr-loader-text');
    if (srText && srText.length > 0) {
        srText[0].classList.remove("rvt-display-none");
    }

    let btnForm = loadingBtn.form;
    if (btnForm && !loadingBtn.classList.contains('no-submit')) {
        let btnValue = loadingBtn.value;

        if (btnValue) {
            // The value of disabled buttons is not submitted with the form, so we need to create
            // a hidden input to add the value to the form
            var hiddenInput = document.createElement('input');
            hiddenInput.setAttribute('name', 'action');
            hiddenInput.setAttribute('value', btnValue);
            hiddenInput.setAttribute('type', 'hidden');

            btnForm.appendChild(hiddenInput); //append the input to the form
        }

        btnForm.submit();
    }

}

function resetLoading(loadingBtn) {
    loadingBtn.removeAttribute("aria-busy");

    // enable all buttons on the page
    let allButtons = document.getElementsByTagName('button');
    for(var i = 0; i < allButtons.length; i++) {
        allButtons[i].removeAttribute("disabled");
    }

    loadingBtn.classList.remove("rvt-button--loading");

    let spinners = loadingBtn.getElementsByClassName('rvt-loader');
    if (spinners && spinners.length > 0) {
        spinners[0].classList.add("rvt-display-none");
    }

    let srText = loadingBtn.getElementsByClassName('sr-loader-text');
    if (srText && srText.length > 0) {
        srText[0].classList.add("rvt-display-none");
    }
}

function doSubmitOptionChoice(button) {
    let checkedRadio = document.querySelector('input[name="menuChoice"]:checked');
    if (checkedRadio == null) {
        let allRadio = document.getElementsByName("menuChoice");
        allRadio.forEach(btn => {
            btn.setAttribute("aria-describedby", "option-message");
        })
        alertMessage = document.getElementById('select-alert');
        alertMessage.classList.remove("rvt-display-none");
        allRadio[0].focus();
        return false;
    }

    applyLoadingButton(button);
}



