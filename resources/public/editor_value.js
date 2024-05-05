const marker = Math.random().toString();

const getEditorMovements = () => {
    const editor = $('#editor').cloneNode(true);
    const movements = {}
    let reference = editor.querySelector('.reference');
    while (reference) {

        if (!reference.innerHTML.trim()) {
            reference.parentNode.removeChild(reference);
            reference = editor.querySelector('.reference');
        } else {
            const oldOffset = reference.dataset.offset;
            reference.innerHTML = marker;
            const newOffset = editor.innerText.indexOf(marker);
            movements[oldOffset] = newOffset;

            reference.parentNode.removeChild(reference);
            reference = editor.querySelector('.reference');
        }
    }
    return movements;
}

const saveEditor = () => {
    const values = {
        command: 'text',
        text: $('#editor').innerText,
        movements: getEditorMovements(),
    };
    htmx.ajax('POST', 'editor', {values});
};
