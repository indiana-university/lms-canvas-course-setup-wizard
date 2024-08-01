function selectThemeSubmit(button) {
    let checkedRadio = document.querySelector('input[name="themeId"]:checked');
    if (checkedRadio == null) {
        let allRadio = document.getElementsByName("themeId");
        allRadio.forEach(btn => {
            btn.setAttribute("aria-describedby", "option-message");
        });
        alertMessage = document.getElementById('select-alert');
        alertMessage.classList.remove("rvt-display-none");
        allRadio[0].focus();
        return false;
    }

    applyLoadingButton(button);
}