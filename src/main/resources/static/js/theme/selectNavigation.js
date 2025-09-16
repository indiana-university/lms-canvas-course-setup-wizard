$(document).ready(function() {
    // monitor the radio buttons for changes and displays additional fields if needed
    $('input[name=includeNavigation]').on('change', function() {
        if ( this.value == 'true') {
            $("#navigation-wrapper").removeClass('rvt-display-none');
            $('#sr-msg').html('Select navigation options below.')
        } else {
            $("#navigation-wrapper").addClass('rvt-display-none');
            $('input[type="radio"][name=navigationOption]:checked').prop('checked', false);
        }
    });

    // set the initial state of the additional fields on page load
    if ($('#include-navigation-yes').is(':checked')) {
        $("#navigation-wrapper").removeClass('rvt-display-none');
    } else {
        $("#navigation-wrapper").addClass('rvt-display-none');
        $('input[type="radio"][name=navigationOption]:checked').prop('checked', false);
        $("#sr-msg").html(""); // reset the sr messaging
    }
});

function selectNavigationSubmit(button) {
    let yesNavigation = document.querySelector('#include-navigation-yes:checked');

    if (yesNavigation != null) {
        let checkedRadio = document.querySelector('input[type=radio][name=navigationOption]:checked');
        if (checkedRadio == null) {
            let allRadio = document.getElementsByName("navigationOption");
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
