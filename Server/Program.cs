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
using MongoDB.Bson.Serialization;
using Newtonsoft.Json;


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

        [BsonElement("date")]
        [BsonRequired()]
        public String date { get; set; }

        public string getName(){
            return name;
        }
        public float getTime(){
            return time;
        }
        public String getDate(){
            return date;
        }
        
    }

    class OperationRequest{ //classe pra identificar os requests
            public int records;
            public int newGame;

        public OperationRequest(){
            this.records = 1;
            this.newGame = 2;
        }
    }
    class Serv
    {
        MongoClient mongo;
        IMongoDatabase db;
        IMongoCollection<Player> playersCollection;
        public Serv(){
            this.mongo = new MongoClient("mongodb://localhost:27017");
            this.db = mongo.GetDatabase("Letrando");
            this.playersCollection = db.GetCollection<Player>("player");
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
        public void ClientHandler(NetworkStream stream, TcpClient clientSocket){
            while(true){
                while(stream.DataAvailable){
                    Byte[] code = new Byte[1];
                    stream.Read(code,0,1);
                    String message = System.Text.Encoding.ASCII.GetString(code);
                    if(Int32.Parse(message) == (new OperationRequest().records)){
                        var allPlayersList = playersCollection.Find<Player>(_ => true).ToList();
                        var allPlayersJson = JsonConvert.SerializeObject(allPlayersList);
                        String mes = Convert.ToChar(allPlayersJson.Length) + allPlayersJson;
                        Console.WriteLine(mes);
                        Byte[] sendBytes = Encoding.UTF8.GetBytes(mes);
                        stream.Write(sendBytes,0,sendBytes.Length);
                        stream.Flush();
                    }
                    else if(Int32.Parse(message) == (new OperationRequest().newGame)){
                            //implementar o inicio do jogo aqui
                    }
                }
            }
        }
        static void Main(){
            Serv s = new Serv();
        }
    }

}