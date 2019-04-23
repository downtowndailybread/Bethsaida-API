package org.downtowndailybread.exceptions

abstract class DDBException(message: String) extends Exception(message) {
  val errorType = this.getClass.getSimpleName.replace("Exception", "")
}
