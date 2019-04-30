package org.downtowndailybread.model

case class Name(
               familyName: String,
               firstName: String,
               middleNames: List[String],
               nickNames: List[String]
               )
