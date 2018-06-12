package controllers

import models.{FiscalCar, TypeCarburant}
import play.api.data.Form
import play.api.data.Forms._

/**
  * A form processing DTO that maps to the form below.
  *
  * Using a class specifically for form binding reduces the chances
  * of a parameter tampering attack and makes code clearer.
  */
case class FiscalCarForm(id: Int, name: String, description: String, prixCatalogueHTVA: Int = 0, remise: Int = 0, typeCarburant: String = TypeCarburant.Diesel.toString, tauxCO2: Int)

object FiscalCarForm {

  /**
    * The form definition for the "create a Car" form.
    * It specifies the form fields and their types,
    * as well as how to convert from a Data to form data and vice versa.
    */

  val fCarForm: Form[FiscalCarForm] = Form {
    mapping(
      "id" -> number,
      "name" -> nonEmptyText,
      "description" -> text,
      "prixCatalogueHTVA" -> number(min = 0),
      "remise" -> number(min = 0),
      "typeCarburant" -> nonEmptyText,
      "tauxCO2" -> number(min = 0)
    )(FiscalCarForm.apply)(FiscalCarForm.unapply)
  }
}
