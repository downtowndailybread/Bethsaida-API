package org.downtowndailybread.bethsaida.exception

abstract class DDBException(message: String) extends Exception(message) {
  val errorType = this.getClass.getSimpleName.replace("Exception", "")
}
