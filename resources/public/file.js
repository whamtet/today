updateFile = (filenameDisp, click_id) => {
    if (confirm(`This will completely replace ${filenameDisp}.  Proceed?`)) {
        document.getElementById(click_id).click();
    }
};
