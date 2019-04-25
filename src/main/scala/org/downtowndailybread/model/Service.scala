package org.downtowndailybread.model

import org.downtowndailybread.model.ServiceType.ServiceType

case class Service(
                    name: String,
                    serviceType: ServiceType,
                    schedule: Schedule)
