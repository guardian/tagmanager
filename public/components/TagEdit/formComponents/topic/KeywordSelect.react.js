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
        <span style="font-weight: normal;">Proper names of people, including fictional people, first names, last names, individual or family names, and unique nicknames. Individual artist names should be included, while band names should be organisation.</span></p>
        <br/>
        <p><strong>Organisation</strong><br/>
        <span style="font-weight: normal;">Names of companies, government agencies, educational institutions, sports teams, political organisations, musical groups/bands, museums and galleries etc.</span></p>
        <br/>
        <p><strong>Event</strong><br/>
        <span style="font-weight: normal;">Named wars, natural disasters, political events, conferences, summits, sporting or cultural events etc.</span></p>
        <br/>
        <p><strong>Work of art or product</strong><br/>
        <span style="font-weight: normal;">Titles of books, songs, films, TV programs and other creations. Also includes names of any product such as foods, vehicles, tech products, weapons etc.</span></p>
        <br/>
        <p><strong>Place</strong><br/>
        <span style="font-weight: normal;">Names of geopolitical entities (e.g. countries, cities, states, provinces etc.) and names of geographical locations (mountain ranges, coasts, planets, bodies of water, named regions (e.g. the Middle East)). Also includes names of human-made structures and buildings etc.</span></p>
        <br/>
        <p><strong>Other</strong><br/>
        <span style="font-weight: normal;">Keyword tags that do not fit into any of the other categories. Includes more conceptual topics (e.g. education, healthcare), general subjects (e.g. human rights, diseases), and named entities outside the main categories (e.g. named animals).</span></p>
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
