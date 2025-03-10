package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class LoadTest extends Simulation {
  
  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val userScenario = scenario("User API Load Test")
    .exec(
      http("Create User Session")
        .post("/api/sessions")
        .body(StringBody("""{"userId": "test-user", "ipAddress": "127.0.0.1", "userAgent": "Gatling-Test"}"""))
        .check(status.is(200))
        .check(jsonPath("$.sessionToken").saveAs("sessionToken"))
    )
    .pause(1)
    .exec(
      http("Log User Activity")
        .post("/api/activities")
        .body(StringBody("""{"userId": "test-user", "action": "TEST", "details": "Load test activity"}"""))
        .check(status.is(200))
    )
    .pause(1)
    .exec(
      http("Get User Activities")
        .get("/api/activities/test-user")
        .check(status.is(200))
    )
    .pause(1)
    .exec(
      http("Log Analytics Event")
        .post("/api/analytics")
        .body(StringBody("""{"eventType": "LOAD_TEST", "userId": "test-user", "metadata": {"test": true}}"""))
        .check(status.is(200))
    )
    .pause(1)
    .exec(
      http("Get Analytics Events")
        .get("/api/analytics/LOAD_TEST")
        .check(status.is(200))
    )

  val auditScenario = scenario("Audit API Load Test")
    .exec(
      http("Create Audit Log")
        .post("/api/audit")
        .body(StringBody(
          """{
            "userId": "test-user",
            "action": "TEST",
            "resourceType": "LOAD_TEST",
            "resourceId": "123",
            "changes": {"test": true},
            "status": "SUCCESS",
            "details": "Load test audit"
          }"""
        ))
        .check(status.is(200))
    )
    .pause(1)
    .exec(
      http("Get Audit Logs")
        .get("/api/audit/test-user")
        .check(status.is(200))
    )

  setUp(
    userScenario.inject(
      rampUsers(50).during(30.seconds),
      constantUsersPerSec(5).during(1.minute)
    ),
    auditScenario.inject(
      rampUsers(30).during(30.seconds),
      constantUsersPerSec(3).during(1.minute)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lt(5000),    // Max response time should be less than 5 seconds
      global.successfulRequests.percent.gt(95)  // Success rate should be above 95%
    )
} 