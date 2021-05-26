package helpers

import org.joda.time.DateTime
import play.api.libs.json.Format

object JodaDateTimeFormat {

  implicit val jodaDateTimeFormat: Format[DateTime] = Format(
    play.api.libs.json.JodaReads.DefaultJodaDateTimeReads,
    play.api.libs.json.JodaWrites.JodaDateTimeNumberWrites
  )

}
