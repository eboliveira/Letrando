package letrando

import com.google.gson.JsonParser
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Alert
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.scene.paint.Color
import tornadofx.*
import khttp.*;
import java.io.File

data class Player(var name :String?= null, var date : String?=null, var score : Int ?= null)

class Letrando:App(MyView::class)

var allPlayersList = mutableListOf<Player>()  //lista contendo todos os players que estão no banco (definida globalmente para a tableview)

class dbController(){
    init {}
    fun getRecords(){
        val resp = get("http://localhost:8080/records")
        val allPlayers = resp.text
        val parser = JsonParser()
        val allPlayersJson = parser.parse(allPlayers)
        var allPlayersJsonArray = allPlayersJson.asJsonArray   //utilizo como JsonArray
        allPlayersList.clear()
        for (i in 0 until allPlayersJsonArray.size()){     //obtenho cada um dos elementos do JsonArray e adiciono na lista (AllPlayersList)
            var playerAux = Player()
            playerAux.name = allPlayersJsonArray.get(i).asJsonObject.get("name").asString
            playerAux.score = allPlayersJsonArray.get(i).asJsonObject.get("score").asInt
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
                readonlyColumn("Score",Player::score)
                readonlyColumn("Data",Player::date)
                columnResizePolicy = SmartResize.POLICY
            }
            hbox {
                button ("Ok") {
                    action {
                        close()
                    }
                }
            }
        }
    }
    }

class MyView:View(){        //view inicial do jogo
    override val root = vbox()  //linha padrão do tornadofx, vbox(preenche com componentes verticalmente) pode ser substituido por outras views
    init {
        val media = Media(File("/medias/teste.mp3").toURI().toString())
        val mediaPlayer = MediaPlayer(media)
        mediaPlayer.play()
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
                            var p = org.bson.Document()
                            p.append("name", playerToVerify.name.value)
                            var playerJson = p.toJson()
                            val post = post("http://localhost:8080/verify",data = playerJson)
                            if(post.statusCode == 400){
                                alert(Alert.AlertType.ERROR, "Nome já utilizado", "Por favor, insira outro nome")
                            }
                            //verificação se o nome já existe aqui
                            else{
                                //jogo aqui
                            }
                        }
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