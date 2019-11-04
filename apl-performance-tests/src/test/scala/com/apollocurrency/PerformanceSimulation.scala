package com.apollocurrency

import com.typesafe.config.ConfigFactory
import collection.JavaConverters._
import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.util.Random
import scalaj.http._
import java.util.UUID.randomUUID

class PerformanceSimulation extends Simulation {

	val env: String = System.getProperty("test.env")
	val users = System.getProperty("users").toDouble
	val duration = System.getProperty("duration").toDouble

	var peers = ConfigFactory.load("application.conf").getStringList(env).asScala.toList
	val random = new Random
	val httpProtocol = http.baseUrls(peers)


	before {
		println("Stop/Start forging!")
		for (peer <- peers) {
			try {
			println(peer)
			val response = Http(peer+"/apl")
				.postForm
				.param("requestType","stopForging")
  			.param("adminPassword","1").asString
		   	 println(response.body)
			} catch { case e: Exception =>
				println(e.getMessage)
			}
		 }

		for( i <- 1 to 200) {
				try {
					val peer = peers(
						random.nextInt(peers.length)
					)
					println(peer)
					val response = Http(peer+"/apl")
						.postForm
						.param("requestType","startForging")
						.param("secretPhrase",i.toString).asString
					println(response.body)
					} catch { case e: Exception =>
						println(e.getMessage)
					}
				}
	}


	val scn = scenario("Send Money")
		.exec(http("Get Account Id")
		.post("/apl?requestType=getAccountId&secretPhrase="+random.nextInt(200).toString)
			.check(status.is(200))
			.check(jsonPath("$.accountRS").find.saveAs("accountRS")))
		.pause(1)
		.exec(session => {
			val transaction = session("accountRS").asOption[String]
			session
		})
		.exec(http("Send Money")
		.post("/apl?" +
			"requestType=sendMoney&" +
			"feeATM=3000000000&" +
			"deadline=1440&" +
			"amountATM="+random.nextInt(2000).toString+"00000000&" +
			"recipient=${accountRS}&secretPhrase="+random.nextInt(200).toString))
		.exec { session =>
			println(session)
			session
		}

	val scn_1 = scenario("Shuffling")
		.exec(http("Shuffling Create")
			.post("/apl?" +
				"requestType=shufflingCreate&" +
				"amount=100000000000&" +
				"registrationPeriod=4000&" +
				"holdingType=0&" +
				"participantCount=3&" +
				"secretPhrase="+random.nextInt(50).toString+"&" +
				"feeATM=3000000000&deadline=1440")
			.check(status.is(200))
			.check(jsonPath("$.fullHash").find.saveAs("fullHash")))
		.pause(30)
		.exec(session => {
			val fullHash = session("fullHash").asOption[String]
			session
		})
  	.repeat(5) {
			exec(http("Shuffling Join")
				.post("/apl?requestType=startShuffler&" +
					"shufflingFullHash=${fullHash}&" +
					"recipientSecretPhrase=" + randomUUID().toString + "&" +
					"secretPhrase=" + random.nextInt(200).toString + "&" +
					"createNoneTransactionMethod=true&" +
					"feeATM=3000000000&deadline=1440"))
		}
		.exec { session =>
			println(session)
			session
		}



	val inject = 	constantUsersPerSec(users) during (duration minutes)
	val inject_low = 	rampUsers(25) during (duration seconds)
	setUp(
		scn.inject(inject),
	  scn_1.inject(inject_low)
	).protocols(httpProtocol)
}