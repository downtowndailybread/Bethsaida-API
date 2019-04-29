package org.downtowndailybread.bethsaida.model

import org.downtowndailybread.bethsaida.model.ServiceType.ServiceType

case class Service(
                    name: String,
                    serviceType: ServiceType,
                    schedule: Schedule)
