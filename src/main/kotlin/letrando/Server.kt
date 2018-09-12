package letrando

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.sun.net.httpserver.HttpServer
import java.io.PrintWriter
import java.net.InetSocketAddress
import org.litote.kmongo.*;
import letrando.Player

/**
 * Minimal embedded HTTP server in Kotlin using Java built in HttpServer
 */
fun main(args: Array<String>) {
    val client = KMongo.createClient() //get com.mongodb.MongoClient new instance
    val database = client.getDatabase("Letrando") //normal java driver usage
    val players = database.getCollection<Player>("player")

    HttpServer.create(InetSocketAddress(8080), 0).apply {
        createContext("/records") { http -> //rota do get
            http.responseHeaders.add("Content-type", "text/plain")
            val parser = JsonParser()
            http.sendResponseHeaders(200, 0)
            val playersIterable = players.find()
            var playersList = JsonArray()
            for(item in playersIterable){
                playersList.add(parser.parse(item.json))
            }
            PrintWriter(http.responseBody).use { out ->
                out.println(playersList)
            }
        }
        createContext("/verify") { http -> //rota do post
            http.responseHeaders.add("Content-type", "text/plain")
            val resp = http.requestBody.readAllBytes()
            val respString = String(resp)
            val parser = JsonParser()
            var respJson =  parser.parse(respString).asJsonObject
            var exists = players.findOne(Player::name eq respJson.get("name").asString)
            if (exists == null){
//                players.insertOne(Player(respJson.get("name").asString, LocalDate.now().toString(),0))
                http.sendResponseHeaders(200, 0)
                PrintWriter(http.responseBody).use { out ->
                    out.println("Avaible")
                }
            }
            else{
                http.sendResponseHeaders(400, 0)
                PrintWriter(http.responseBody).use { out ->
                    out.println("Name already exists")
                }
            }
        }

        start()
    }
}
