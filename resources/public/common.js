hxPost = (h, v) => {
    const [href, command] = h.split(':');
    const values = {...v, command};
    return htmx.ajax('POST', href, {values});
}
