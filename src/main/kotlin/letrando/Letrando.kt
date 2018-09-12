package letrando

import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Alert
import javafx.scene.paint.Color
import tornadofx.*
import com.google.gson.*
import khttp.*;

data class Player(var name :String?= null, var date : String?=null, var time : Float ?= null)

class Letrando:App(MyView::class)

var allPlayersList = mutableListOf<Player>()  //lista contendo todos os players que estão no banco (definida globalmente para a tableview)

class dbController(){
    init {}
    fun getRecords(){
        val resp = get("http://localhost:8080/records")
        var allPlayersJson = resp.text
        val parser = JsonParser()
        var jsonElem = parser.parse(allPlayersJson)
        var allPlayersJsonArray = jsonElem.asJsonArray   //utilizo como JsonArray
        allPlayersList.clear()
        for (i in 0 until allPlayersJsonArray.size()){     //obtenho cada um dos elementos do JsonArray e adiciono na lista (AllPlayersList)
            var playerAux = Player()
            playerAux.name = allPlayersJsonArray.get(i).asJsonObject.get("name").asString
            playerAux.time = allPlayersJsonArray.get(i).asJsonObject.get("time").asFloat
            playerAux.date = allPlayersJsonArray.get(i).asJsonObject.get("date").asString
            allPlayersList.add(playerAux)
        }
    }
}

class Records:View(){       //view para mostrar as pontuações
    override val root = vbox()
    init {
        with(root){
            tableview(allPlayersList.observable()){
                readonlyColumn("Nome",Player::name)
                readonlyColumn("Tempo",Player::time)
                readonlyColumn("Data",Player::date)
                columnResizePolicy = SmartResize.POLICY
            }
            hbox {
                button ("Ok") {
                    action {
                        close()
                    }
                }
                button("Atualizar") {
                    action {
                        var controller = dbController()
                        controller.getRecords()
                    }
                }
            }
        }
    }
    }

class MyView:View(){        //view inicial do jogo
    override val root = vbox()  //linha padrão do tornadofx, vbox(preenche com componentes verticalmente) pode ser substituido por outras views
    init {
        with(root){
            setPrefSize(800.0,600.0)    //seta o tamanho da janela
            style{
                backgroundColor += c("#88ff88")
            }
            label ("Letrando"){
                paddingLeft = 150
                paddingTop = 100
                textFill = Color.RED    //cor do texto
                val custom = loadFont("/font/madpakkeDEMO.otf",size = 140)
                font = custom
            }
            hbox {//horizontal box, preenche com componentes horizontalmente
                paddingTop = 30
                paddingLeft = 110
                label("Digite seu nome:") {
                    style {
                        fontSize = 24.px
                    }
                }
                val playerToVerify = object : ViewModel(){
                    val name = bind { SimpleStringProperty()}
                }
                textfield(playerToVerify.name){}
                button ("Jogar"){
                    action {
                        playerToVerify.commit{
                            //verificação se o nome já existe aqui
                        }
                        //jogo aqui
                    }
                }
                button("Mostrar pontuações") {
                    action {
                        var controller = dbController()
                        controller.getRecords()
                        openInternalWindow<Records>()
                    }
                }
            }
        }
    }
}


fun main(args: Array<String>) {

}