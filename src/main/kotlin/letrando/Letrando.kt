package letrando

import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Alert
import javafx.scene.paint.Color
import tornadofx.*
import java.time.LocalDate
import java.net.Socket
import Requests.allPlayers;
import java.io.*


data class Player(val name:String, val time:Double = 0.0,val date : LocalDate = LocalDate.now())

class Letrando:App(MyView::class)

val client = Socket("127.0.0.1",8080)
var out = PrintWriter(client.getOutputStream(),true)
var input = InputStreamReader(client.getInputStream())
var allPlayersList : String?=null;

class Records:View(){
    override val root = vbox()
    init {
        with(root){
//            tableview(allPlayersList) {
//                readonlyColumn("Nome",Player::name)
//                readonlyColumn("Tempo",Player::time)
//                readonlyColumn("Data",Player::date)
//            }
            hbox {
                button ("Ok") {
                    action {
                        close()
                    }
                }
                button("Atualizar") {
                    action {

                        }
                    }
                }
            }
        }
    }

class MyView:View(){
    override val root = vbox()
    init {
        with(root){
            setPrefSize(800.0,600.0)
            style{
                backgroundColor += c("#88ff88")
            }
            label ("Letrando"){
                paddingLeft = 150
                paddingTop = 100
                textFill = Color.RED
                val custom = loadFont("/font/madpakkeDEMO.otf",size = 140);
                font = custom
            }
            hbox {
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
//                            val exists = players.findOne(Player::name eq playerToVerify.name.value)
//                            if (exists == null){
//                                players.insertOne(Player(playerToVerify.name.value))
//                            }
//                            else{
//                                alert(Alert.AlertType.ERROR, "Nome já utilizado", "Insira outro nome")
//                            }
                        }
                    }
                }
                button("Mostrar pontuações") {
                    action {
                        out.write(allPlayers);
                        out.flush();
                        val size =  input.read()
                        var buffer = CharArray(size)
                        input.read(buffer);
                        var allPlayersss = String(buffer);
                        println(allPlayersss)
                        openInternalWindow<Records>()
                    }
                }
            }
        }
    }
}


fun main(args: Array<String>) {

}