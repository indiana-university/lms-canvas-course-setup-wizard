/*-
 * #%L
 * course-setup-wizard
 * %%
 * Copyright (C) 2022 - 2025 Indiana University
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
$(document).ready(function() {
    let isCopyHomepageChange = false;

    $('#copy-homepage-checkbox').on('change', function() {
        if (this.checked && themeModel) {
            // set this to true to not trigger the other change listeners to uncheck the box
            isCopyHomepageChange = true;
            $('input[name="navigationSyllabusNumber"][value="' + themeModel.navigationHomeNumber + '"]').prop('checked', true).trigger('change');
            for (let i = 1; i <= themeModel.navigationHomeNumber; i++) {
                $('#syllabus-button' + i).val(themeModel.navigationHomeButtonLabels[i - 1]);
            }
            isCopyHomepageChange = false;
        }
    });

    $('input[name=navigationSyllabusNumber]').on('change', function() {
        if (!isCopyHomepageChange) {
            $('#copy-homepage-checkbox').prop('checked', false);
        }
        let selectedValue = $('input[name="navigationSyllabusNumber"]:checked').val();
        updateTableRows(selectedValue);
    });

    $('select[id^="syllabus-button"]').on('change', function() {
        if (!isCopyHomepageChange) {
            $('#copy-homepage-checkbox').prop('checked', false);
        }
    });

    // Initial state
    let selectedValue = $('input[name="navigationSyllabusNumber"]:checked').val();
    updateTableRows(selectedValue);
});

function updateTableRows(numberOfRows) {
    for (let i = 1; i <= 6; i++) {
        const row = $('#button-row-' + i);
        if (i <= numberOfRows) {
            row.removeClass('rvt-display-none');
        } else {
            row.addClass('rvt-display-none');
            $('#syllabus-button' + i).val("");
        }
    }
}

function selectNavigationSyllabusSubmit(button) {
    const form = document.getElementById('wizard-theme-form-submit');
    let selectedValue = $('input[name="navigationSyllabusNumber"]:checked').val();
    let hasEmpty = false;
    let values = [];

    for (let i = 1; i <= selectedValue; i++) {
        const select = document.getElementById('syllabus-button' + i);
        if (select.value === "") {
            hasEmpty = true;
        }
        values.push(select.value);
    }

    alertMessage = document.getElementById('select-alert');
    alertMessage2 = document.getElementById('select-alert2');

    if (hasEmpty) {
        let allSelects = form.querySelectorAll('select[id^="syllabus-button"]');
        allSelects.forEach(btn => {
            btn.setAttribute("aria-describedby", "option-message");
        });
        alertMessage.classList.remove("rvt-display-none");
        alertMessage2.classList.add("rvt-display-none");
        return false;
    }

    // Check for uniqueness among non-empty values
    let nonEmptyValues = values.filter(v => v !== "");
    let uniqueValues = new Set(nonEmptyValues);

    if (uniqueValues.size !== nonEmptyValues.length) {
        let allSelects = form.querySelectorAll('select[id^="syllabus-button"]');
        allSelects.forEach(btn => {
            btn.setAttribute("aria-describedby", "option-message2");
        });
        alertMessage2.classList.remove("rvt-display-none");
        alertMessage.classList.add("rvt-display-none");
        return false;
    }

    applyLoadingButton(button);
}
