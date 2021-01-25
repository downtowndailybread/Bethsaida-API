package org.downtowndailybread.bethsaida.model

case class ServiceStats(
                                serviceName: String,
                                year: Int,
                                month: Int,
                                day: Int,
                                numClients: Int,
                                dayShelterVisits: Int,
                                nightShelterVisits: Int,
                                showerVisits: Int,
                                totalVisits: Int,
                                numEvents: Int,
                                numMale: Int,
                                numFemale: Int
                              )
