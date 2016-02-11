// This tool compares an array of elasticsearch records to another array of elasticsearch records
// Finds:
// * record level differences old to new and new to old
// * records missing from old in new and new in old
//
// Note: Requires ramda!
//
// Run like so: `node sourceDiff.js > differences.json`

var R = require("ramda");
var oldTags = require("./tag.json");
var newTags = require("./tagmanagerTag.json");

function diffArray(a, b, fieldName, differingFields) {
    var same = 0;
    a[fieldName].forEach(function (e) {
        same += R.contains(e, b[fieldName])
    });
    if (same !== a[fieldName].length) {
        differingFields.push(fieldName);
    }
}

function diffObject(a, b, fieldName, differingFields) {
    var aSubfields = Object.keys(a[fieldName]);

    var shouldContinue = true;
    aSubfields.forEach(function(subfield) {
        if (shouldContinue) {
            return;
        }
        if (!compare(a[fieldName][subfield], b[fieldName[subfield]])) {
            differingFields.push(fieldName);
            shouldContinue = false;
        }
    });
}

function compare(a, b) {
    if (a && typeof a === 'string' && b) {
        var aClean = a.replace("http://", "https://");
        var bClean = b.replace("http://", "https://");
        return aClean === bClean;
    }

    return a === b;
}

function getDifferentSources(a, b) {
    var differingFields = [];

    var aFields = Object.keys(a);

    aFields.forEach(function (f) {
        if (Array.isArray(a[f])) {
            diffArray(a, b, f, differingFields);
        } else if ((typeof a[f] === "object") && (a[f] !== null)) {
            diffObject(a, b, f, differingFields);

        } else {
            if (!compare(a[f], b[f])) {
                differingFields.push(f);
            }
        }
    });

    return differingFields;
}

function testAToB(A, B) {
    var bAsMap = {};
    B.forEach(function (b) {
        var bSource = b._source;
        bAsMap[bSource.id] = bSource;
    });

    var problems = {
        missing: [],
        different: []
    };

    A.forEach(function (a) {
        var aSource = a._source;
        var bSource = bAsMap[aSource.id];

        if (bSource) {
            var differentFields = getDifferentSources(aSource, bSource);
            if (differentFields.length > 0) {
                var badTag = {
                    "id": aSource.id,
                    "fields": {}
                };
                differentFields.forEach(function (d) {
                    if (Array.isArray(aSource[d])) {
                        aSource[d].sort();
                        bSource[d].sort();
                    }
                    badTag.fields[d] = {
                        "a": aSource[d],
                        "b": bSource[d]
                    }
                });
                problems.different.push(badTag);
            }
        } else {
            problems.missing.push(aSource);
        }
    })
    return problems;
}

var all = {
    "newDiffOld" : testAToB(newTags, oldTags),
    "oldDiffNew" : testAToB(oldTags, newTags)
}

console.log(JSON.stringify(all, undefined, 4));

