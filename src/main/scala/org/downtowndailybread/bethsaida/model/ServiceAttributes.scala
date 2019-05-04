package org.downtowndailybread.bethsaida.model

import org.downtowndailybread.bethsaida.model.ServiceType.ServiceType

case class ServiceAttributes(
                             name: String,
                             serviceType: ServiceType,
                           )