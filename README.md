mule-circuit-breaker
====================

Implementation of the [Circuit Breaker design pattern](http://en.wikipedia.org/wiki/Circuit_breaker_design_pattern) as a Mule DevKit component.
Heavily based on [John D'Emic's article](http://blogs.mulesoft.org/implementing-a-circuit-breaker-with-devkit/) on that topic.
In lieu of any additional documentation, please refer to that article.
The most important functional change compared to what is documented there is the following:

- The `tripOnException` attribute may also name a super-class of the actually thrown exception. This requires that class to be loadable by `Class.forName()`.
