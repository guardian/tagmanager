import React from "react";
import {keywordTagTypes} from "../../../../constants/keywordTagTypes";
import ReactTooltip from "react-tooltip";

export const KeywordTypeSelect = ({updateTag, tag}) => {
    const setKeywordType = (e) => {
        updateTag(
            Object.assign({}, tag, {
                keywordType: e.target.value
            })
        );
    }

    const keywordTypeTooltipHtml = `
        <p><strong>Person</strong><br/>
        <span style="font-weight: normal;">Names of people: real names or stage names</span></p>
        <br/>
        <p><strong>Organisation</strong><br/>
        <span style="font-weight: normal;">Organisations such as companies, institutions, political parties, sports teams, and bands</span></p>
        <br/>
        <p><strong>Event</strong><br/>
        <span style="font-weight: normal;">Specific events, such as named natural disasters, wars, sporting events, political events</span></p>
        <br/>
        <p><strong>Work of art or product</strong><br/>
        <span style="font-weight: normal;">Works of art (films, books etc.) or named products (e.g. iPhone, ChatGPT)</span></p>
        <br/>
        <p><strong>Place</strong><br/>
        <span style="font-weight: normal;">Named places: geopolitical and geographical locations, including structures and buildings</span></p>
        <br/>
        <p><strong>Other</strong><br/>
        <span style="font-weight: normal;">General topics and concepts, or those that don't fit elsewhere (including named animals)</span></p>
        `;

    return (
        <div className="tag-edit__input-group">
            <label className="tag-edit__input-group__header">
                Keyword Type
                <span
                    data-tip={keywordTypeTooltipHtml}
                    data-html={true}
                    data-place="right"
                >
                    <i className="i-info-grey"/>
                </span>
                <ReactTooltip html={true} />
            </label>
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
                </select>
            </div>
        </div>
    );
}
