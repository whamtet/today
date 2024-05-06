const marker = Math.random().toString();

const innerText = e => {
    let s = e.innerText.trim();
    while (s.indexOf('\n\n\n') >= 0) {
        s = s.replaceAll('\n\n\n', '\n\n');
    }
    return s;
}

const getEditorMovements = () => {
    const editor = $('#editor').cloneNode(true);
    editor.id = undefined;
    document.body.appendChild(editor);
    const movements = {}
    let reference = editor.querySelector('.reference');
    while (reference) {

        if (!reference.innerHTML.trim()) {
            reference.parentNode.removeChild(reference);
            reference = editor.querySelector('.reference');
        } else {
            const oldOffset = reference.dataset.offset;
            reference.innerHTML = marker;
            const newOffset = innerText(editor).indexOf(marker);
            movements[oldOffset] = newOffset;

            reference.remove();
            reference = editor.querySelector('.reference');
        }
    }
    const text = innerText(editor);
    editor.remove();
    return ({movements, text});
}

const saveEditor = () => {
    const values = {
        command: 'text',
        ...getEditorMovements(),
    };
    htmx.ajax('POST', 'editor', {values});
};
