const $ = x => document.querySelector(x);
const $$ = x => document.querySelectorAll(x);

const isText = el => el.nodeType === el.TEXT_NODE;
const isSubeditor = el => isText(el) ? el.parentNode.id === 'editor' : el.id === 'editor';

const marker = () => document.createTextNode('fuck');

const insertMarker = (el, offset) => {
    if (isText(el)) {
        const after = el.splitText(offset);
        el.parentNode.insertBefore(marker(), after);
    } else if (offset === el.childNodes.length) {
        el.appendChild(marker());
    } else {
        el.insertBefore(marker(), el.childNodes[offset]);
    }
}

const insertRange = () => {
    const selection = getSelection();
    if (selection) {
        insertMarker(selection.focusNode, selection.focusOffset);
    }
}

document.onselectionchange = () => {
    const selection = getSelection();
    if (selection) {
        $('#add-reference').disabled = !isSubeditor(selection.anchorNode) || !isSubeditor(selection.focusNode);
    }
}

const addReference = () => {
    console.log('add reference');
}
