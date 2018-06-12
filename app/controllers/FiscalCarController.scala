package controllers

import javax.inject.Inject
import models.{FiscalCar, TypeCarburant}
import play.api.Logger
import play.api.mvc._
import controllers.FiscalCarForm._
import play.api.data.Form

/**
 * The FiscalCarController using MessagesAbstractController.
 *
 * Instead of MessagesAbstractController, you can use the I18nSupport trait,
 * which provides implicits that create a Messages instance from a request
 * using implicit conversion.
 *
 * See https://www.playframework.com/documentation/2.6.x/ScalaForms#passing-messagesprovider-to-form-helpers
 * for details.
 */
class FiscalCarController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

  val logger: Logger = Logger(this.getClass())

  // The URL to the Car.  You can call this directly from the template, but it
  // can be more convenient to leave the template completely stateless i.e. all
  // of the "FiscalCarController" references are inside the .scala file.
  private val postUrl = routes.FiscalCarController.createCar()

  def index = Action {
    Redirect(routes.FiscalCarController.listCars())
  }

  def listCars = Action { implicit request: MessagesRequest[AnyContent] =>
    // Pass an unpopulated form to the template
    val fcars = FiscalCar.findAll
    Ok(views.html.listCars(fcars, fCarForm, postUrl))
  }

  //TODO: https://alvinalexander.com/scala/how-to-format-numbers-commas-international-currency-in-scala
  def showCar(id: Int) = Action { implicit request: MessagesRequest[AnyContent] =>
    val putUrl = routes.FiscalCarController.editCar(id)
    FiscalCar.findById(id).map { client =>
      Ok(views.html.car(client, fCarForm, putUrl))
    }.getOrElse(NotFound)
  }



  // This will be the action that handles our form post
  def createCar = Action { implicit request: MessagesRequest[AnyContent] =>
    Logger.warn(">In CreateCar")

    val fcars = FiscalCar.findAll
    val errorFunction = { formWithErrors: Form[FiscalCarForm] =>
      // This is the bad case, where the form had validation errors.
      // Let's show the user the form again, with the errors highlighted.
      // Note how we pass the form with errors to the template.
      Logger.debug("Form submission error:" + formWithErrors.errors.toString())
      BadRequest(views.html.listCars(fcars, formWithErrors, postUrl))
    }

    val successFunction = { fCarForm: FiscalCarForm =>
      // This is the good case, where the form was successfully parsed as a Data object.
      val fcar = FiscalCar(FiscalCar.getNextId, name = fCarForm.name, description = fCarForm.description, prixCatalogueHTVA = fCarForm.prixCatalogueHTVA, typeCarburant = TypeCarburant.withNameOpt(fCarForm.typeCarburant).get, remise = fCarForm.remise, tauxCO2 = fCarForm.tauxCO2)
      FiscalCar.addCar(fcar)
      Redirect(routes.FiscalCarController.listCars()).flashing("success" -> "Car added!")
    }

    val formValidationResult = fCarForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }


  def editCar(id: Int) = {
    Action(updateCar(id))
  }

  def editCarForm(id: Int) = {
    Action(updateCar(id))
  }


  def updateCar(id: Int) = { implicit request: MessagesRequest[AnyContent] =>
    logger.warn("Dans updateCar")
    logger.warn("methode="+request.method)
    logger.warn("params="+request.queryString.map { case (k,v) => k -> v.mkString })
    logger.warn("params="+request.body.asFormUrlEncoded.get.get("prixCatalogueHTVA").head)

    val errorFunction = { formWithErrors: Form[FiscalCarForm] =>
      val fcars = FiscalCar.findAll
      // This is the bad case, where the form had validation errors.
      // Let's show the user the form again, with the errors highlighted.
      // Note how we pass the form with errors to the template.
      Logger.debug("Form submission error:" + formWithErrors.errors.toString())
      BadRequest(views.html.listCars(fcars, formWithErrors, postUrl))
    }

    val successFunction = { fCarForm: FiscalCarForm =>
      // This is the good case, where the form was successfully parsed as a Data object.
      val modfcar = FiscalCar(id = fCarForm.id, name = fCarForm.name, description = fCarForm.description, prixCatalogueHTVA = fCarForm.prixCatalogueHTVA, typeCarburant = TypeCarburant.withNameOpt(fCarForm.typeCarburant).get, remise = fCarForm.remise,  tauxCO2 = fCarForm.tauxCO2)
      FiscalCar.updateCar(modfcar)
      Redirect(routes.FiscalCarController.listCars()).flashing("success" -> "Car modified!")
    }

    val formValidationResult = fCarForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }



  def removeCarForm(id: Int) = {
    Action(removeCar(id))
  }

  def deleteCar(id: Int) = {
    Action(removeCar(id))
  }


  def removeCar(id: Int) = { implicit request: MessagesRequest[AnyContent] =>
    logger.warn("Dans removeCar")
      FiscalCar.deleteCar(id)
      Redirect(routes.FiscalCarController.listCars()).flashing("success" -> "Car removed!")
  }





}
