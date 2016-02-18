export function cleanTag(tag) {
    var cleaned = Object.assign({}, tag);
    removeEmptyStringsFromObject(cleaned);
    return cleaned;
}

function removeEmptyStringsFromObject(obj) {
    var fields = Object.keys(obj);
    fields.forEach(f => {
        if (obj[f] === "") {
            delete obj[f];
        } else if (Array.isArray(obj[f])) {
            removeEmptyStringsFromArray(obj[f]);
        } else if (typeof obj[f] === "object" && obj[f] !== null) {
            removeEmptyStringsFromObject(obj[f]);
        }
    });
}

function removeEmptyStringsFromArray(array) {
    var idx = array.indexOf("");
    while (idx !== -1) {
        array.splice(idx, 1);
        idx = array.indexOf("");
    }
}
