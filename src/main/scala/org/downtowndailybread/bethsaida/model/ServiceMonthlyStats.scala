package org.downtowndailybread.bethsaida.model

case class ServiceMonthlyStats(
                                serviceName: String,
                                month: Int,
                                year: Int,
                                numClients: Int,
                                totalVisits: Int,
                                numEvents: Int,
                                numMale: Int,
                                numFemale: Int,
                                numCurrentMonthIntake: Int
                              )
