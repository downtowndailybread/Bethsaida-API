package org.downtowndailybread.model

case class ClientAttributeTypeAttribute(
                                         displayName: String,
                                         dataType: String,
                                         required: Boolean,
                                         requiredForOnboarding: Boolean,
                                         ordering: Int
                                       )
