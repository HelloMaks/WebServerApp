package ru.altmanea.webapp.repo

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection
import ru.altmanea.webapp.common.Payload

val client: MongoClient = KMongo.createClient("mongodb://127.0.0.1:27017")
val mongoDB: MongoDatabase = client.getDatabase("db") // база данных MongoDB

val parsingData =
    mongoDB.getCollection<Payload>().apply { drop() } // коллекция MongoDB