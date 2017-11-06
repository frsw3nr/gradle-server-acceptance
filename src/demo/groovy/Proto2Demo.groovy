import groovyx.javafx.*

import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList

import javafx.concurrent.*
import javafx.scene.control.*
import javafx.stage.Stage

import org.controlsfx.control.GridView
import org.controlsfx.control.cell.ColorGridCell
import org.controlsfx.control.CheckComboBox
import org.controlsfx.control.PropertySheet
import org.controlsfx.control.PropertySheet.Item
import org.controlsfx.control.Rating
import org.controlsfx.property.*

// class ControlFXTest implements Runnable {
//   private Stage primary
//   protected PropertySheet propSheet
//   protected CheckComboBox checkComboBox

//   protected ObservableList ratingList, stringList
//   protected ObservableList<Color> gridViewList
//   protected ObservableList<PropertySheet.Item> propSheetList

//   // Platform2(){
//   //   stringList = FXCollections.observableArrayList(
//   //                   ["node1","node2","node3","node4","node5"] )

//   //   gridViewList = FXCollections.<Color>observableArrayList(
//   //                                                     [Color.CORNFLOWERBLUE,
//   //                                                      Color.DARKCYAN,
//   //                                                      Color.DARKGREEN,
//   //                                                      Color.DARKSEAGREEN] )

//   //   propSheetList = BeanPropertyUtils.getProperties(new TestBean())
//   // }

//  @Override
//   public void run(){
//     GroovyFX.start {
//       primary = getPrimaryStage()
//       registControlsFxControls(delegate)

//       stage(title: "GroovyFX Demo2", visible: true){
//         scene(width: 1024d, height: 700d){
//           borderPane() {
//             top(){
//                menuBar{
//                    menu(text:'File'){
//                       menuItem(text:'Exit', onAction: {
//                           getPrimaryStage().close()
//                       })
//                    }
//                }
//             }
//             center(){
//                 tabPane{
//                     tab(text: "Controls", closable: false){
//                      vbox(spacing: 5){
//                         checkComboBox = checkComboBox(prefWidth: 250d)

//                         hbox{
//                           label(text: "view2-3", prefHeight:25)
//                           rating(rating: 0, max: 6, prefHeight:25)
//                         }

//                         propSheet = propertySheet(prefWidth: 250d, prefHeight:450d)

//                         double width = 450; double height = 700
//                         scrollPane(prefWidth: width, prefHeight: height){
//                           gridView(prefWidth: width, prefHeight: height,
//                             cellFactory: gridViewFactory, items: gridViewList)
//                         }
//                       }
//                     }
//                     tab(text: 'Grid View', closable: false){
//                       gridView(cellFactory: gridViewFactory, items: gridViewList)
//                     }
//                 }
//             }
//             bottom(){
//                 label(text: "This is a bottom")
//             }
//           }
//         }
//       }
//     }

//     Platform.runLater{
//       propSheet.getItems().setAll(propSheetList)
//       checkComboBox.getItems().setAll(stringList)
//     }
//   }

//   def gridViewFactory = { param ->
//     return new ColorGridCell()
//   }

//   def registControlsFxControls = { SceneGraphBuilder builder ->
//     builder.registerBeanFactory("gridView", GridView)
//     builder.registerBeanFactory("checkComboBox", CheckComboBox)
//     builder.registerBeanFactory("propertySheet", PropertySheet)
//     builder.registerBeanFactory("rating", Rating)
//   }

//   class TestBean{
//     String name, property
//     Integer index
//     Boolean flag
//   }
// }
