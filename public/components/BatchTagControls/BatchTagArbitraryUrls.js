import React, {useState} from "react";

export const BatchTagArbitraryUrls = ({addPathsToSelection}) => {

  const [isModalDisplayed, setIsModalDisplayed] = useState(false);

  const [input, setInput] = useState("") // string
  const [cleaned, setCleaned] = useState(); // string[]

  const close = () => {
    setInput("");
    setCleaned(null);
    setIsModalDisplayed(false);
  }
  const clean = () => setCleaned([...new Set(
    input.split("\n").map(url => {
      if(url.startsWith("http")){
        return url.trim().split("/").slice(3).join("/");
      }
      return url.trim(); // already just the path
    }).filter(_ => !!_) // remove empty
  )]);

  const complete = () => {
    addPathsToSelection(cleaned);
    close();
  }

  return (
    <React.Fragment>
      {isModalDisplayed && (
        <div className="batch-tag__arbitrary_modal_background">
          <div className="batch-tag__arbitrary_modal_content">
            <div><strong>{cleaned
              ? "Please check the paths extracted from the web URLs you entered"
              : "Enter web URLs (one per line)"
            }</strong></div>
            <textarea
              className="batch-tag__arbitrary_modal_input"
              value={cleaned ? cleaned.join("\n") : input}
              onChange={({target}) => setInput(target.value)}
              disabled={!!cleaned}
            />
            <div className="batch-tag__arbitrary_modal_button_bar">
              <button className="batch-tag__arbitrary_button" onClick={close}>Cancel</button>
              {cleaned ? (
                <div>
                  <button className="batch-tag__arbitrary_button--red" onClick={() => setCleaned(null)}>
                    Back
                  </button>
                  {" "}
                  <button className="batch-tag__arbitrary_button--green" disabled={!input} onClick={complete}>
                    Add {cleaned.length} unique rows
                  </button>
                </div>
              ) : (
                <button className="batch-tag__arbitrary_button--green" disabled={!input} onClick={clean}>
                  Next
                </button>
              )}
            </div>
          </div>
        </div>
      )}
      <button className="batch-tag__arbitrary_button" onClick={() => setIsModalDisplayed(true)}>Add arbitrary URLs</button>
    </React.Fragment>
  );
}
