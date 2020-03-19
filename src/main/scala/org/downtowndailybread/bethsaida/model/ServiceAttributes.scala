package org.downtowndailybread.bethsaida.model

case class ServiceAttributes(
                             name: String,
                             serviceType: ServiceType,
                             defaultCapacity: Option[Int]
                           )