package org.downtowndailybread.bethsaida.model

case class ClientAttributeTypeAttribute(
                                         displayName: String,
                                         dataType: String,
                                         required: Boolean,
                                         requiredForOnboarding: Boolean,
                                         ordering: Int
                                       )
