const $ = x => document.querySelector(x);
const $$ = x => Array.from(document.querySelectorAll(x));

const isDev = location.host.startsWith('localhost');
const viewerHref = isDev ? 'http://localhost:8888/web/viewer.html' : 'https://doc-index.simpleui.io/pdf.js/web/viewer.html';

const isText = el => el.nodeType === el.TEXT_NODE;
const editorHas = x => $('#editor').contains(x);
const referenceHas = x => $$('.reference').some(ref => ref.contains(x));
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
    const index = innerText($('#editor')).indexOf(markerText);
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

if ($('#add-reference')) {
    document.onselectionchange = () => {
        const selection = getSelection();
        if (selection) {
            const {anchorNode, focusNode} = selection;
            $('#add-reference').disabled = (
                !editorHas(anchorNode) || !editorHas(focusNode)
                || referenceHas(anchorNode) || referenceHas(focusNode)
            );
        }
    }
}

const addReference = () => {
    const selection = getSelection();
    if (selection) {
        saveEditor();
        // next open window
        const values = {offset: getIndex(trailingNode(selection))};
        const q = selection.toString().trim();
        if (q) {
            values.q = q;
        }
        htmx.ajax('POST', 'reference-modal', {values, target: '#modal'});
    }
}

// for established attribute
const openPage = (file_id, page) => {
    if (page) {
        window.open(viewerHref + '?' + new URLSearchParams({file_id, page}));
    } else {
        window.open(`/api/file/${file_id}`);
    }
}
