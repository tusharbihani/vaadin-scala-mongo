package vaadin.scala.example.mongo

import vaadin.scala._
import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.global._
import scala.util.Random
import scala.reflect.BeanProperty

class MongoExampleMinimal extends Application("Mongo & Vaadin, tied together with Scala") {

  val registrations: MongoCollection = MongoConnection()("vaadin-scala-mongo-example")("registrations")

  def mapRegistrations: List[MinRegistration] = registrations.map(grater[MinRegistration].asObject(_)).toList

  override val main: ComponentContainer = new HorizontalLayout {
    sizeFull()
    styleName = Reindeer.LAYOUT_WHITE

    val tableLayout = new VerticalLayout {
      size(50 pct, 50 pct)
      spacing = true

      val table = new Table {
        sizeFull()
        styleNames += (Reindeer.TABLE_BORDERLESS, Reindeer.TABLE_STRONG)
        container = new BeanItemContainer[MinRegistration](registrations.map(grater[MinRegistration].asObject(_)).toList)
        visibleColumns = Seq("username", "realName")
      }

      val addButton: Button = Button("Register", showForm)

      components += (table, addButton)
    }

    val form = new Form {
      size(50 pct, 50 pct)
      caption = "Registration"
      formFieldFactory = createFormFieldFactory
    }

    form.footer = new HorizontalLayout {
      components += Button("Save", showList)
    }

    components += tableLayout

    alignment(tableLayout -> Alignment.MiddleCenter)

    def showForm(): Unit = {
      form.item = new BeanItem[MinRegistration](MinRegistration())
      form.visibleItemProperties = Seq("realName", "username", "password")
      replaceComponent(tableLayout, form)
      alignment(form -> Alignment.MiddleCenter)
    }

    def showList(): Unit = {
      form.commit
      val bean = form.item.get.asInstanceOf[BeanItem[MinRegistration]].bean
      registrations.save(grater[MinRegistration].asDBObject(bean))
      tableLayout.table.container = new BeanItemContainer[MinRegistration](mapRegistrations)
      tableLayout.table.visibleColumns = Seq("username", "realName")
      replaceComponent(form, tableLayout)
      alignment(tableLayout -> Alignment.MiddleCenter)
      mainWindow.showNotification("User %s registered".format(bean.username))
    }
  }

  def createFormFieldFactory = FormFieldFactory(_ match {
    case FormFieldIngredients(_, "password", _) =>
      Some(new PasswordField {
        caption = DefaultFieldFactory.createCaptionByPropertyId("password")
        required = true
      })

    case otherIngredient => {
      val field = DefaultFieldFactory.createField(otherIngredient)
      field.foreach(_.required = true)
      field
    }
  })
}

case class MinRegistration(
  @BeanProperty var username: String = "username" + Random.nextInt,
  @BeanProperty var password: String = "",
  @BeanProperty var realName: String = "Joe Tester")