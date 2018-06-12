package models

object TypeCarburant extends Enumeration {
  type TypeCarburant = Value
  val Essence, Diesel, Hybride, Electrique = Value


  def getSeq(): Seq[(String, String)] = {
    return Seq("Essence"-> Essence.toString,"Diesel"->Diesel.toString,"Hybride"->Hybride.toString, "Electrique"-> Electrique.toString)
  }

  def withNameOpt(s: String): Option[Value] = values.find(_.toString == s)

}
