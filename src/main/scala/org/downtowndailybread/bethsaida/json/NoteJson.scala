package org.downtowndailybread.bethsaida.json

import org.downtowndailybread.bethsaida.model.Note
import org.downtowndailybread.bethsaida.providers.SettingsProvider
import spray.json.DefaultJsonProtocol._

trait NoteJson extends BaseSupport {
    this: SettingsProvider =>

  implicit val noteJson = jsonFormat1(Note)
}
