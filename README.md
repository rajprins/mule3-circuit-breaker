mule-circuit-breaker
====================

Implementation of the [Circuit Breaker design pattern](http://en.wikipedia.org/wiki/Circuit_breaker_design_pattern) as a Mule DevKit component.
Heavily based on [John D'Emic's article](http://blogs.mulesoft.org/implementing-a-circuit-breaker-with-devkit/) on that topic.

**Installation**
- Check out the source code
- Build the project using maven: `mvn clean compile package`
- Install the Circuit Breaker component via the generated update site folder in your workspace


**Use**
- Install the Circuit Breaker component (see section 'Installation')
- Add the Circuit Breaker component to your flow. Select the "Filter" operation.
- Add the Circuit breaker component to your exception handling strategy and select the "trip" operation.
- Configure the exception (by classname) to be observed by the Circuit Breaker.

**Example**
``` XML
<circuitbreaker:config name="Circuit_Breaker__config" tripThreshold="3" tripTimeout="30000" breakerName="Circuit_Breaker" doc:name="Circuit Breaker: config"/>

<flow name="circuitbreaker-testFlow">
   <http:listener config-ref="HTTP_Listener_Configuration" path="/cbtest" allowedMethods="GET" doc:name="HTTP"/>
   <circuitbreaker:filter config-ref="Circuit_Breaker__config" doc:name="Circuit Breaker Filter"/>
   <component class="com.mulesoft.training.demo.components.ConnectionComponent" doc:name="ConnectionComponent"/>
   <catch-exception-strategy doc:name="Catch Exception Strategy">
      <circuitbreaker:trip config-ref="Circuit_Breaker__config" tripOnException="com.mulesoft.training.demo.exceptions.ConnectionNotAvailbleException" doc:name="Circuit Breaker"/>
      <logger message="Tripped on exception 'ConnectionNotAvailbleException'" level="INFO" doc:name="Logger"/>
      <set-payload value="#[groovy:message.getExceptionPayload().getRootException().getMessage()]" doc:name="Set Payload"/>
   </catch-exception-strategy>
</flow>
```
In this example a custom Java component throws an exception to simulate the unavailability of an external resource.
The Java component throws an exception named 'ConnectionNotAvailbleException'.  

The circuit breaker component is used to prevent executing the Java component in case of too many failures (exceptions). The circuit breaker is configured to trip on three catches of the ConnectionNotAvailbleException exception.
After 30 seconds (configured as 30000 milliseconds), the failure count is reset and the Java component will be executed again.

