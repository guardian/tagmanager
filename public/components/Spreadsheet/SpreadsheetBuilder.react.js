import React from "react";
import _startCase from "lodash.startcase";
import { createSpreadsheet } from "../../util/spreadSheetApi";
import debounce from "lodash.debounce";

const DEFAULT_FILTER = {
  type: "internalName",
  value: ""
};

const FILTER_TYPES = ["internalName", "externalName", "path", "type", "hasFields"];

const DEFAULT_COLUMN = "internalName";
const AVAILABLE_COLUMNS = [
  "id",
  "internalName",
  "externalName",
  "description",
  "slug",
  "section",
  "path",
  "type",
  "hyperlink"
];

const MAX_PREVIEW_ROWS = 20;

export default class SpreadsheetBuilder extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      columns: ["internalName", "hyperlink"],
      filters: [Object.assign({}, DEFAULT_FILTER)],
      previewRows: []
    };
    this.addColumn = this.addColumn.bind(this);
    this.updateColumnType = this.updateColumnType.bind(this);
    this.deleteColumn = this.deleteColumn.bind(this);
    this.moveColumn = this.moveColumn.bind(this);

    this.addFilter = this.addFilter.bind(this);
    this.updateFilterType = this.updateFilterType.bind(this);
    this.updateFilterValue = this.updateFilterValue.bind(this);

    this.fetchRows = this.fetchRows.bind(this);
    this.debounceFetchRows = debounce(this.fetchRows, 500);
    this.copySpreadSheet = this.copySpreadSheet.bind(this);

    this.renderFilter = this.renderFilter.bind(this);
    this.renderColumnHeader = this.renderColumnHeader.bind(this);
  }

  componentDidMount() {
    this.fetchRows();
  }
  componentDidUpdate(prevProps, prevState) {
    if (this.state.filters !== prevState.filters) {
      this.debounceFetchRows();
    }
  }

  addColumn() {
    this.setState({
      columns: [...this.state.columns, DEFAULT_COLUMN]
    });
  }

  addFilter() {
    this.setState({
      filters: [...this.state.filters, Object.assign({}, DEFAULT_FILTER)]
    });
  }

  updateFilterType(index, type) {
    const clone = [...this.state.filters];
    clone[index].type = type;
    this.setState({
      filters: clone
    });
  }

  updateFilterValue(index, value) {
    const clone = [...this.state.filters];
    clone[index].value = value;
    this.setState({
      filters: clone
    });
  }

  deleteFilter(index) {
    const { filters } = this.state;
    this.setState({
      filters: [...filters.slice(0, index), ...filters.slice(index + 1)]
    });
  }

  deleteColumn(index) {
    const { columns } = this.state;
    this.setState({
      columns: [...columns.slice(0, index), ...columns.slice(index + 1)]
    });
  }

  updateColumnType(index, column) {
    const clone = [...this.state.columns];
    clone[index] = column;
    this.setState({
      columns: clone
    });
  }

  moveColumn(index, offset) {
    const clone = [...this.state.columns];
    const tmp = clone[index + offset];
    clone[index + offset] = clone[index];
    clone[index] = tmp;
    this.setState({
      columns: clone
    });
  }

  fetchRows() {
    createSpreadsheet(this.state.filters, 10).then(paginatedResponse => {
      this.setState({
        previewRows: paginatedResponse.tags
      });
    });
  }

  copyStringToClipboard(str) {
    var el = document.createElement("textarea");
    el.value = str;
    el.setAttribute("readonly", "");
    el.style = { position: "absolute", left: "-9999px" };
    document.body.appendChild(el);
    el.select();
    document.execCommand("copy");
    document.body.removeChild(el);
  }

  copySpreadSheet() {
    let rows = this.state.previewRows.map(tag => {
      return this.state.columns
        .reduce((acc, column) => {
          if (column === "hyperlink") {
            return [
              ...acc,
              `=HYPERLINK("https://${window.location.host}/tag/${tag.id}", "Open In Tag Manager")`
            ];
          } else {
            return [...acc, tag[column]];
          }
        }, [])
        .join("\t");
    });

    const entireSheet =
      this.state.columns.map(s => _startCase(s)).join("\t") +
      "\n" +
      rows.join("\n");

    this.copyStringToClipboard(entireSheet);
    alert("Copied to clipboard...");
  }

  renderFilter(filter, index) {
    return (
      <div key={filter.type + index} className="spreadsheet-builder__filter">
        <select
          value={filter.type}
          onChange={e => this.updateFilterType(index, e.target.value)}
        >
          {FILTER_TYPES.map(filter => (
            <option key={filter} value={filter}>
              {_startCase(filter)}
            </option>
          ))}
        </select>
        <input
          value={filter.value}
          onChange={e => this.updateFilterValue(index, e.target.value)}
        />
        <i className="i-delete" onClick={() => this.deleteFilter(index)} />
      </div>
    );
  }

  renderColumnHeader(column, index) {
    return (
      <th key={column + index}>
        <div className="spreadsheet-builder__table-header">
          {index > 0 ? (
            <div onClick={() => this.moveColumn(index, -1)}>⬅️</div>
          ) : (
            <div />
          )}
          <div className="spreadsheet-builder__table-header-picker">
            <select
              onChange={e => this.updateColumnType(index, e.target.value)}
              value={column}
            >
              {AVAILABLE_COLUMNS.map(c => (
                <option key={c} value={c}>
                  {_startCase(c)}
                </option>
              ))}
            </select>
            <div onClick={() => this.deleteColumn(index)}>🗑️</div>
          </div>
          {index < this.state.columns.length - 1 ? (
            <div onClick={() => this.moveColumn(index, 1)}>➡️️</div>
          ) : (
            <div />
          )}
        </div>
      </th>
    );
  }

  render() {
    return (
      <div>
        <div className="spreadsheet-builder__panel">
          <div>
            <h1>Filters</h1>
            {this.state.filters.map(this.renderFilter)}
            <button onClick={() => this.addFilter()}>New Filter</button>
            <button onClick={() => this.copySpreadSheet()}>
              Copy sheet to clipboard
            </button>
          </div>
          <h1>Table Preview</h1>
        </div>

        <div className="spreadsheet-builder__table">
          <table>
            <thead>
              <tr>
                {this.state.columns.map(this.renderColumnHeader)}
                <th className="spreadsheet-builder__table-new-column"></th>
              </tr>
            </thead>
            <tbody>
              {this.state.previewRows
                .slice(0, MAX_PREVIEW_ROWS)
                .map((row, i, previews) => {
                  return (
                    <tr>
                      {this.state.columns.map(column => (
                        <td>
                          {column === "hyperlink" ? (
                            <a
                              href={`/tag/${row.id}`}
                              rel="noopener noreferrer"
                              target="_blank"
                            >
                              Open in Tag Manager
                            </a>
                          ) : row[column] ? (
                            row[column]
                          ) : (
                            "Unknown field: " + column
                          )}
                        </td>
                      ))}
                      <td className="spreadsheet-builder__table-new-column-button">
                        {Math.ceil(previews.length / 2) === i ? (
                          <button onClick={this.addColumn}>Add Column</button>
                        ) : null}
                      </td>
                    </tr>
                  );
                })}
              {this.state.previewRows.length > MAX_PREVIEW_ROWS ? (
                <tr>
                  <td
                    className="spreadsheet-builder__table-hidden-fields"
                    colSpan={this.state.columns.length}
                  >
                    More rows hidden...
                  </td>
                </tr>
              ) : null}
            </tbody>
          </table>
        </div>
      </div>
    );
  }
}
