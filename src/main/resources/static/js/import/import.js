
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