package org.downtowndailybread.bethsaida.model

case class ServiceDailyStats(
                                serviceName: String,
                                year: Int,
                                month: Int,
                                day: Int,
                                numClients: Int,
                                totalVisits: Int,
                                numEvents: Int,
                                numMale: Int,
                                numFemale: Int
                              )
