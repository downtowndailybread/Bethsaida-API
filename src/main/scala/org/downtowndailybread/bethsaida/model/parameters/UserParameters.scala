package org.downtowndailybread.bethsaida.model.parameters

case class UserParameters(
                               firstName: String,
                               lastName: String,
                               loginParameters: LoginParameters,
                               admin: Option[Boolean]
                               )
