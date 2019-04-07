package org.downtowndailybread.exception.client

import org.downtowndailybread.exception.DDBException

class MissingRequiredClientAttributeException(attribs: Seq[String])
  extends DDBException({
    s"Cannot create or modify a client without all required fields. missing field${
      if (attribs.isEmpty) { "s" } else { "" }}: " +
      s"${attribs.mkString(", ")}"
  })