package letrando

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.control.Alert
import javafx.scene.control.TextField
import javafx.scene.paint.Color
import tornadofx.*
import khttp.*;
import java.util.*

data class Player(var name :String?= null, var date : String?=null, var score : Int ?= null)
data class Words(var numeroJogo :Int ?= null, var words :JsonArray ?= null)

class Letrando:App(MyView::class)

var allPlayersListSorted = listOf<Player>()  //lista contendo todos os players que estão no banco (definida globalmente para a tableview)
var allPlayersList = mutableListOf<Player>()  //lista contendo todos os players que estão no banco (definida globalmente para a tableview)
var allGameWords = mutableListOf<Words>()

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
    fun getWords(){
        val resp = get("http://localhost:8080/words")
        val allWords = resp.text
        val parser = JsonParser()
        val allWordsJson = parser.parse(allWords)
        var allWordsJsonArray = allWordsJson.asJsonArray
        allGameWords.clear()
        for (i in 0 until allWordsJsonArray.size()){
            var wordAux = Words()
            wordAux.numeroJogo = allWordsJsonArray.get(i).asJsonObject.get("numeroJogo").asInt
            wordAux.words = allWordsJsonArray.get(i).asJsonObject.get("words").asJsonArray
            allGameWords.add(wordAux)

        }
        allPlayersListSorted = allPlayersList.sortedBy { it.score }.reversed()
    }
}

class Records:View(){       //view para mostrar as pontuações
    override val root = vbox()
    init {
        with(root){
            tableview(allPlayersListSorted.observable()){
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
        val audio = AudioClip(MyView::class.java.getResource("/medias/bgmusic.wav").toExternalForm())
        with(root){
            audio.play()
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

private fun removeUltimoChar(str: String): String {
    return str.substring(0, str.length - 1)
}

object Stopwatch {
    inline fun elapse(callback: () -> Unit): Long {
        var start = System.currentTimeMillis()
        callback()
        return System.currentTimeMillis() - start
    }

    inline fun elapseNano(callback: () -> Unit): Long {
        var start = System.nanoTime()
        callback()
        return System.nanoTime() - start
    }
}

class Game:View(){       //view do jogo
    override val root = vbox()

    init {
        with(root){

            style{
                backgroundColor += c("#88ff88")
            }
            var words: MutableList<String> = mutableListOf<String>("hospital",
                    "ilhotas",
                    "palitos",
                    "sol",
                    "tio",
                    "sal")

            //println(allGameWords)
            var acertos = FXCollections.observableArrayList("Palavras:")
            var palavraDoJogo = words.first().toCharArray().toMutableList().shuffled()
            var tentativa = ""

            label {
                children.bind(acertos){
                    label(it){
                        style {
                            fontSize = 25.px
                        }
                    }
                }
            }

            val campoTexto = label(){
                paddingLeft = 300
                paddingTop = 200
                style {
                    fontSize = 50.px
                }
            }

            hbox {
                paddingLeft = 160
                paddingTop = 30

                for (letra in palavraDoJogo) {
                    togglebutton(letra.toString().capitalize()) {
                        paddingAll = 20
                        action {
                            if (!isSelected) {
                                tentativa += letra.toString()
                                campoTexto.setText(tentativa)
                                println(tentativa)
                            } else {
                                tentativa = removeUltimoChar(tentativa)
                                campoTexto.setText(tentativa)
                                println(tentativa)
                            }
                        }
                    }
                }

                button("Enviar") {
                    paddingAll = 20
                    action {
                        campoTexto.getText()

                        val iterator = words.iterator()

                        while (iterator.hasNext()) {
                            val item = iterator.next()
                            if (item == campoTexto.getText()) {
                                acertos.add(item)
                                iterator.remove()
                                println("Acertou")
                                if(words.isEmpty()){
                                    alert(Alert.AlertType.INFORMATION, "Fim de jogo!")
                                    replaceWith<MyView>()
                                }
                                break
                            } else {
                                if (!iterator.hasNext())
                                    println("Errou")
                            }
                        }

                    }
                }
            }

        }
    }
}


fun main(args: Array<String>) {

}