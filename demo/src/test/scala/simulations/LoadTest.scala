package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class LoadTest extends Simulation {
  
  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val scn = scenario("Basic Load Test")
    .exec(
      http("Get All Users")
        .get("/api/v1/users")
        .check(status.is(200))
    )
    .pause(1)
    .exec(
      http("Health Check")
        .get("/actuator/health")
        .check(status.is(200))
    )

  setUp(
    scn.inject(
      rampUsers(50).during(30.seconds),
      constantUsersPerSec(2).during(2.minutes)
    )
  ).protocols(httpProtocol)
} 