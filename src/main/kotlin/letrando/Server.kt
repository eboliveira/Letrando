package letrando

import com.sun.net.httpserver.HttpServer
import org.bson.Document
import java.io.PrintWriter
import java.net.InetSocketAddress
import org.litote.kmongo.*;
import javax.json.Json


/**
 * Minimal embedded HTTP server in Kotlin using Java built in HttpServer
 */
fun main(args: Array<String>) {
    val client = KMongo.createClient() //get com.mongodb.MongoClient new instance
    val database = client.getDatabase("Letrando") //normal java driver usage
    val players = database.getCollection("player")

    HttpServer.create(InetSocketAddress(8080), 0).apply {
        createContext("/records") { http -> //rota do get
            http.responseHeaders.add("Content-type", "text/plain")
            http.sendResponseHeaders(200, 0)
            val playersIterable = players.find()
            var playersList = arrayListOf<String>()
            for(item in playersIterable){
                item.remove("_id")
                playersList.add(item.toJson())
            }
            PrintWriter(http.responseBody).use { out ->
                out.println(playersList)
            }
        }

        start()
    }
}
