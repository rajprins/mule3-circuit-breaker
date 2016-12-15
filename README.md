mule-circuit-breaker
====================

Implementation of the [Circuit Breaker design pattern](http://en.wikipedia.org/wiki/Circuit_breaker_design_pattern) as a Mule DevKit component.
Heavily based on [John D'Emic's article](http://blogs.mulesoft.org/implementing-a-circuit-breaker-with-devkit/) on that topic.

**Installation**
- Check out the source code
- Build the project using maven: `mvn clean compile package`
- Install the Circuit Breaker component via the generated update site folder in your workspace


**Use**
1. Install the Circuit Breaker component (see section 'Installation')
2. Add the Circuit Breaker component to your flow. Select the "Filter" operation.
3. Add the Circuit breaker component to your exception handling strategy. Select the "trip" operation.

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
In this example the flow will stop executing the Java component 'ConnectionComponent' after three failed attempts.
After 30 seconds the situation is reset and the flow will start executing the Java component again.