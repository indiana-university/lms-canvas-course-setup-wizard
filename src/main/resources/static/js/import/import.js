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

function selectContentSubmit(button) {
    var checkedRadio = document.querySelector('input[name="importContentOption"]:checked');
    if (checkedRadio == null) {
        var allRadio = document.getElementsByName("importContentOption");
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

function selectCourseSubmit(button) {
    //Select control
    var inputControl = document.querySelector('input[id="course-select-input"]');
    //Hidden input with the actual value
    var inputValue = document.querySelector('input[name="selectedCourseId"]');
    if (inputValue.value == "") {
        inputControl.setAttribute("aria-invalid", "true");
        inputControl.setAttribute("aria-describedby", "option-message");
        alertMessage = document.getElementById('select-alert');
        alertMessage.classList.remove("rvt-display-none");
        inputControl.focus();
        return false;
    }

    applyLoadingButton(button);
}

function configureDates1Submit(button) {
    var checkedRadio = document.querySelector('input[name="dateOption"]:checked');
    if (checkedRadio == null) {
        var allRadio = document.getElementsByName("dateOption");
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
