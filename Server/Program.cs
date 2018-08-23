using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net.Sockets;
using System.Net;
using System.Threading;
using MongoDB.Driver;
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace Server
{
    public class Player{
        [BsonId()]
        public ObjectId Id { get; set; }

        [BsonElement("name")]
        [BsonRequired()]
        public string name { get; set; }

        [BsonElement("time")]
        [BsonRequired()]
        public float time { get; set; }

        public string getName(){
            return name;
        }
        public float getTime(){
            return time;
        }
        
    }
    class Program
    {
        static void Main(string[] args)
        {
            TcpListener serverSocket = new TcpListener(IPAddress.Parse("127.0.0.1"),8080);
            serverSocket.Start();
            Console.WriteLine(" >> Server Started");
            while (true){
                TcpClient clientSocket = serverSocket.AcceptTcpClient();
                Console.WriteLine(" >> Accept connection from client");
                NetworkStream stream = clientSocket.GetStream();
                Thread clientThread = new Thread(()=>ClientHandler(stream,clientSocket));
                clientThread.IsBackground = false;
                clientThread.Start();
            }
        }
        static void ClientHandler(NetworkStream stream, TcpClient clientSocket){
            var mongo = new MongoClient("mongodb://localhost:27017");
            var db = mongo.GetDatabase("Letrando");
            var playersCollection = db.GetCollection<Player>("player");
            while(true){
                while(stream.DataAvailable){
                    Byte[] bytes = new Byte[clientSocket.Available];
                    stream.Read(bytes,0,bytes.Length);
                    String message = System.Text.Encoding.ASCII.GetString(bytes);
                    Console.WriteLine(message);
                    if(Int32.Parse(message) == 1){
                        var allPlayersList = playersCollection.Find(_ => true).ToList();
                        foreach(var p in allPlayersList){
                            Console.WriteLine(p.getName());
                            Console.WriteLine(p.getTime());
                        }

                        // var allPlayersJson = allPlayersList.ToJson();
                        // Console.WriteLine(allPlayersJson);
                        // String mes = Convert.ToChar(allPlayersJson.Length) + allPlayersJson;
                        // Console.WriteLine(mes);
                        // Byte[] sendBytes = Encoding.UTF8.GetBytes(mes);
                        // stream.Write(sendBytes,0,sendBytes.Length);
                        // stream.Flush();
                    }
                }
            }
        }
    }
}