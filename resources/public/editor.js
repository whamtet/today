const $ = x => document.querySelector(x);
const $$ = x => document.querySelectorAll(x);

const isDev = location.host.startsWith('localhost');
const viewerHref = isDev ? 'http://localhost:8888/web/index.html' : 'https://app.simplifydd.com/pdf.js/web/';

const isText = el => el.nodeType === el.TEXT_NODE;
const editorHas = x => $('#editor').contains(x);
// const isSubeditor = el => isText(el) ? el.parentNode.id === 'editor' : el.id === 'editor';

const markerText = Math.random().toString();
const newMarker = () => document.createTextNode(markerText);

const insertMarker = ([el, offset]) => {
    const marker = newMarker();
    if (isText(el)) {
        const after = el.splitText(offset);
        el.parentNode.insertBefore(marker, after);
    } else if (offset === el.childNodes.length) {
        el.appendChild(marker);
    } else {
        el.insertBefore(marker, el.childNodes[offset]);
    }
    return marker;
}

const getIndex = (offsets) => {
    const marker = insertMarker(offsets);
    const index = $('#editor').innerText.indexOf(markerText);
    marker.remove();
    return index;
}

const preceding = (a, b) => (a.compareDocumentPosition(b) & 4) === 4
const trailingNode = ({anchorNode, focusNode, anchorOffset, focusOffset}) => {
    if (anchorNode === focusNode) {
        return [anchorNode, Math.max(anchorOffset, focusOffset)];
    } else if (preceding(anchorNode, focusNode)) {
        return [focusNode, focusOffset];
    } else {
        return [anchorNode, anchorOffset];
    }
};

document.onselectionchange = () => {
    const selection = getSelection();
    if (selection) {
        const {anchorNode, focusNode} = selection;
        $('#add-reference').disabled = !editorHas(anchorNode) || !editorHas(focusNode);
    }
}

const addReference = () => {
    const selection = getSelection();
    if (selection) {
        // first save the text
        const values = {
            command: 'text',
            text: $('#editor').innerText
        };
        htmx.ajax('POST', 'editor', {values});
        // next open window
        const offset = getIndex(trailingNode(selection));
        const params = new URLSearchParams({offset, question_id});
        const q = selection.toString().trim();
        if (q) {
            params.append('q', q);
        }
        window.open(viewerHref + '?' + params.toString());
    }
}

// refresh for references
window.onmessage = ({data}) => {
    if (data === 'refresh') {
        location.reload();
    }
}
