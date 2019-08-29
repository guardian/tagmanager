import { PandaReqwest } from "./tagManagerApi";

export const createSpreadsheet = filters => {
  return PandaReqwest({
    url: "/api/spreadsheet",
    data: JSON.stringify({ filters }),
    contentType: "application/json",
    method: "post"
  });
};
