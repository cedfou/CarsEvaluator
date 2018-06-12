package models

import controllers.FiscalCarForm
import models.TypeCarburant._
import play.api.libs.json.Json
import play.api.Logger
import scala.math.min

/**
 * Presentation object used for displaying data in a template.
 *
 * Note that it's a good practice to keep the presentation DTO,
 * which are used for reads, distinct from the form processing DTO,
 * which are used for writes.
 */

//prixCatalogueHTVA = prix obtenu (avec remise) et HTVA
//remise = remise sur prix catalogue obtenu aupèrs vendeur
//Attention: la déductibilité TVA et l'amortissement se calculent sur le prix réél; alors que l'ATN se calcule sur le prix catalogue!!!
case class FiscalCar(id: Int, name: String, description: String, prixCatalogueHTVA: Int = 0, remise: Int = 0, typeCarburant: Value = TypeCarburant.Diesel, tauxCO2: Int, prixFiscalNet: Int = 0) {

  val valRefCO2Diesel = 86
  val valRefCO2EssenceGaz = 105
  var fraisCarburantEmployeur = true


  //TODO: hardcodé pour l'instant. 1) Pouvoir changer (modèle?)
  val TVApourcentage:Double = 21.0
  private val coefficientTVA: Double = TVApourcentage/100.0
  private val coefficientAjoutTVA: Double = 1+(TVApourcentage/100.0)

  //TODO: hardcodé pour l'instant. 1) Pouvoir changer (modèle?) 2) mettre en session 3) DB??
  val TVApourcentageDéductionTrajet:Double = 50/100.0 // 50% pour l'instant = meilleur taux

  //TODO: hardcodé pour l'instant. 1) Pouvoir changer (modèle?) 2) mettre en session 3) DB??
  val benefComptableAnnuelAttendu:Double = 50000.00

  //TODO: hardcodé pour l'instant. 1) Pouvoir changer (modèle?) 2) mettre en session 3) DB??
  val anneesAmortissement = 5

  //TODO: rajouter possibilité de gérer ceci (1er et 3 eme remplis; 2ème calculé sur base KW?!)
  /*
  Carburant/Electricité
  Taxe de Circulation
  Entretien Auto
  */
  def getFrais: (Int, Int, Int) = {
    val(carburant, taxeCirculation, entretienAuto) =
      typeCarburant match {
        case Electrique => (600, 80, 1000)
        case Diesel =>  (1400, 800, 1400)
        case _ => (1600, 950, 1400)
      }
    def frais= (carburant, taxeCirculation, entretienAuto)
    return frais
  }

  def getFraisTotal= {
    getFrais._1+getFrais._2+getFrais._3
  }

  //Prix Catalogue (hor remise)
  def getPrixCatalogueHTVA = { prixCatalogueHTVA }
  def getPrixCatalogueTVA =  { prixCatalogueHTVA * coefficientTVA }
  def getPrixCatalogueTVAC = { prixCatalogueHTVA * coefficientAjoutTVA }
  //Prix d'achat réel (Prix Catalogue - remise)
  def getPrix_achatreel_HTVA = { getPrixCatalogueHTVA - remise }
  def getPrix_achatreel_TVA =  {
      getPrix_achatreel_HTVA * coefficientTVA  }
  def getPrix_achatreel_TVAC =  { getPrix_achatreel_HTVA * coefficientAjoutTVA }

  //Valeur_Catalogue avec options (HTVA) +TVA sur prix achat réel! == base de calcul ATN !!!
  def Valeur_Catalogue_BaseATN = {
    prixCatalogueHTVA + getPrix_achatreel_TVA
  }

  // TODO: implémenter les 3 formules de déductibilité TVA pour estimations (cf https://www.bdo.be/fr-be/actualites/2016/deduction-tva-voitures-de-societe-trois-methodes)
  // Pour l'instant, méthode 2 = semi-forfait avec taux 50%
  // => = TVA achat réél * coefficient déduc TVA
  def deductibiliteTVA = {
    getPrix_achatreel_TVA * TVApourcentageDéductionTrajet
  }

  def getBaseAmortissement ={
    getPrix_achatreel_TVAC - deductibiliteTVA
  }

  def getAmortissementAnnuel ={
    getBaseAmortissement / anneesAmortissement
  }

  //cf référence: https://www.groups.be/1_88026.htm
  /*A la suite de la publication de l’arrêté royal du 13 décembre 2017 au Moniteur belge du 19 décembre 2017, la formule de calcul de l’avantage imposable voiture de société, pour 2018, est :
    véhicules essence, LPG et gaz naturel : valeur catalogue x [5,5 + ((taux émission CO2 - 105) x 0,1)] % x 6/7
    véhicules diesel : valeur catalogue x [5,5 + ((taux émission CO2 - 86) x 0,1)] % x 6/7
    véhicules électriques : valeur catalogue x 4 % x 6/7
*/
  def getATN: Double = {
    var tauxCO2ATN = typeCarburant match {
      case Electrique =>  4.0
      case Diesel => 5.5 + ((tauxCO2 - valRefCO2Diesel) * 0.1)
      case _ => 5.5 + ((tauxCO2 - valRefCO2EssenceGaz) * 0.1)
    }
    //Le pourcentage de base (5,5) est donc augmenté ou diminué en fonction du taux d'émission de CO2 de la voiture.
    //Attention: Ce pourcentage ne pourra jamais dépasser 18 % (limite supérieure) et descendre en dessous de 4 % (limite inférieure)!!!
    if (tauxCO2ATN < 4) {tauxCO2ATN =4}
    if (tauxCO2ATN > 18) {tauxCO2ATN =18}

    Valeur_Catalogue_BaseATN * tauxCO2ATN / 100 * 6 / 7
  }

  //TODO: BigDecimal(1.23456789).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
  def getATNmensuel = { getATN/12 }

  def getFraisTotalDeductibles: Double = {
    getAmortissementAnnuel + getFraisTotal + getATN
  }




  def getDeductibilitePercent = {
    min((120-(0.5*1*tauxCO2)),100)
  }
  def depencesNonAdmisesPercent = {
    100 - getDeductibilitePercent
  }

  def depencesNonAdmisesAmortissemeent = {
    depencesNonAdmisesPercent * getAmortissementAnnuel /100
  }

  def depencesNonAdmisesFrais = {
    depencesNonAdmisesPercent * getFraisTotal /100
  }


  //A partir du 1er janvier 2017, la DNA de 17% des ATN voiture sera portée à 40% lorsque l’employeur supporte, en partie ou en totalité, les frais de carburant du véhicule.
  //source: http://www.ddbcf.be/voitures-de-societe-nouvelle-taxation-pour-lemployeur-a-partir-du-1er-janvier-2017/
  def depencesNonAdmisesATN = {
    if (fraisCarburantEmployeur) {
      getATN *0.40
    } else {
      getATN *0.17
    }
  }

  def tauxNonAdmissATN = {
    if (fraisCarburantEmployeur) {
      40
    } else {
      17
    }
  }


  def getDNATotal: Double = {
    depencesNonAdmisesAmortissemeent + depencesNonAdmisesFrais + depencesNonAdmisesATN
  }

  def getBeneficeComptableCalcule: Double = {
     benefComptableAnnuelAttendu - getFraisTotalDeductibles
  }

  def getBeneficeFiscal: Double = {
    getBeneficeComptableCalcule + getDNATotal
  }

  def getIsocAttendu = {
    benefComptableAnnuelAttendu * 0.25
  }

  def getIsocCalculeBenefFiscal = {
    getBeneficeFiscal * 0.25
  }

  def getGainISOC = {
    getIsocAttendu - getIsocCalculeBenefFiscal
  }



  def getCoutAnnuelNet() = {
    getFraisTotalDeductibles-getGainISOC
  }

}






object FiscalCar {

  val logger: Logger = Logger(this.getClass())

  //TODO: convertir en récupération DB à terme...
  //préremplissage manuel
  var fcars = scala.collection.mutable.ListBuffer(
    FiscalCar(1, "BMW i3", "BMW as usual...", 45500, 1500, TypeCarburant.Electrique, 0),
    FiscalCar(2, "Jaguar i-pace", "Cool electric car", 70250, 4000, TypeCarburant.Electrique, 0),
    FiscalCar(3, "Jaguar i-pace FULL", "Cool electric car with first edition Full options", 86800, 4000, TypeCarburant.Electrique, 0),
    FiscalCar(4, "Volvo hybride T8", "Best hybrid on Earth", 65300, 3000, TypeCarburant.Hybride, 50),
    FiscalCar(5, "Peugeot 3008", "Good medium diesel", 36000, 0, TypeCarburant.Diesel, 108)
  )

  logger.warn("Sample data set initialized!")

  def findAll = fcars.toList.sortBy(_.id)

  def findById(id: Int) = fcars.find(_.id == id)

  def addCar(car: FiscalCar) = {
    logger.debug(">>>>> addCar ID=" + car.id)
    fcars += car
  }

  def updateCar(car: FiscalCar) = {
    logger.warn(">>>>> updateCar ID="+car.id)
    logger.warn(">>>>> updateCar name="+car.name)
    logger.warn(">>>>> updateCar description="+car.description)
    logger.warn(">>>>> updateCar prixCatalogueHTVA="+car.prixCatalogueHTVA)
    logger.warn(">>>>> updateCar remise="+car.remise)
    logger.warn(">>>>> updateCar typeCarburant="+car.typeCarburant)

    var index = fcars.indexWhere(car.id == _.id)
    logger.warn(">>>>> updateCar index !!!! ="+index)
    fcars.update(index, car)
  }

  def deleteCar(car: FiscalCar) = {
    logger.warn(">>>>> deleteCar ID="+car.id)
    var index = fcars.indexOf(car)
    var fcar = fcars.remove(index)
    logger.warn(">>>>>index of removed car ="+fcar.id)
  }

  def deleteCar(id: Int) = {
    var car = findById(id).get
    logger.warn(">>>>> deleteCar ID="+car.id)
    var index = fcars.indexOf(car)
    var fcar = fcars.remove(index)
    logger.warn(">>>>>index of removed car ="+fcar.id)
  }

  def getNextId = fcars.length+1



  //TODO: attention: mapping à partir de FiscalCarForm et non FiscalCar...
  implicit val fiscalCarFormat = Json.format[FiscalCarForm]
}
