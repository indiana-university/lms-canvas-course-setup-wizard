function selectThemeSubmit(button) {
    var checkedRadio = document.querySelector('input[name="themeId"]:checked');
    if (checkedRadio == null) {
        var allRadio = document.getElementsByName("themeId");
        allRadio.forEach(btn => {
            btn.setAttribute("aria-describedby", "option-message");
        });
        alertMessage = document.getElementById('select-alert');
        alertMessage.classList.remove("rvt-display-none");
        return false;
    }

    applyLoadingButton(button);
}