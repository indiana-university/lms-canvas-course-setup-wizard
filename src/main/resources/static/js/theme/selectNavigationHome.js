$(document).ready(function() {
    $('input[name=navigationHomeNumber]').on('change', function() {
        let selectedValue = $('input[name="navigationHomeNumber"]:checked').val();
        updateTableRows(selectedValue);
    });

    // Initial state
    let selectedValue = $('input[name="navigationHomeNumber"]:checked').val();
    updateTableRows(selectedValue);
});

function updateTableRows(numberOfRows) {
    for (let i = 1; i <= 6; i++) {
        const row = $('#button-row-' + i);
        if (i <= numberOfRows) {
            row.removeClass('rvt-display-none');
        } else {
            row.addClass('rvt-display-none');
            $('#home-button' + i).val("");
        }
    }
}

function selectNavigationHomeSubmit(button) {
    const form = document.getElementById('wizard-theme-form-submit');
    let selectedValue = $('input[name="navigationHomeNumber"]:checked').val();
    let hasEmpty = false;
    let values = [];

    for (let i = 1; i <= selectedValue; i++) {
        const select = document.getElementById('home-button' + i);
        if (select.value === "") {
            hasEmpty = true;
        }
        values.push(select.value);
    }

    alertMessage = document.getElementById('select-alert');
    alertMessage2 = document.getElementById('select-alert2');

    if (hasEmpty) {
        let allSelects = form.querySelectorAll('select[id^="home-button"]');
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
        let allSelects = form.querySelectorAll('select[id^="home-button"]');
        allSelects.forEach(btn => {
            btn.setAttribute("aria-describedby", "option-message2");
        });
        alertMessage2.classList.remove("rvt-display-none");
        alertMessage.classList.add("rvt-display-none");
        return false;
    }

    applyLoadingButton(button);
}
