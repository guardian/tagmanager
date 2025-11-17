import React from "react";
import {keywordTagTypes} from "../../../../constants/keywordTagTypes";

export const KeywordSelect = ({updateTag, tag}) => {
    const setKeywordType = (e) => {
        updateTag(
            Object.assign({}, tag, {
                keywordType: e.target.value
            })
        );
    }

    return (
        <div className="tag-edit__input-group">
            <label className="tag-edit__input-group__header">Keyword Type</label>
            <div className="tag-edit__field">
                <select onChange={setKeywordType} value={tag.keywordType ?? ""}>
                    <option></option>
                    {keywordTagTypes.map(keyword => {
                        const optionValue = keyword.value
                        return (
                            <option key={optionValue} value={optionValue}>
                                {keyword.label}
                            </option>
                        );
                    })}
                    )
                </select>
            </div>
        </div>
    );
}
