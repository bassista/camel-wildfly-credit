/**
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.camel.examples;

import java.util.Date;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.processor.validation.PredicateValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@ContextName("camel-context")
public class CamelConfiguration extends RouteBuilder {

  private static final Logger log = LoggerFactory.getLogger(CamelConfiguration.class);

  @Inject
  @ContextName("camel-context")
  private CamelContext camelContext;
  
  @Produces
  @Named("currentDate")
  @Dependent
  public Date currentDate() {
    return new Date();
  }
  
  @Override
  public void configure() throws Exception {
    
    from("direct:getScore")
      .log("Fetching credit score for [${body}]")
      .to("sql:select * from credit_score where ssn = :#${body} order by version desc")
      .filter().simple("${body} == ${null} || ${body.size()} == 0")
        .setBody().groovy("javax.ws.rs.core.Response.status(404).entity(null).build()")
        .stop()
      .end()
      .setBody().groovy("javax.ws.rs.core.Response.status(200).entity(['creditScore':request.body[0]?.get('score')]).build()")
    ;
    
    from("direct:postScores")
      .onException(PredicateValidationException.class)
        .handled(true)
        .log("Error processing credit scores: [${exception.message}]")
        .setBody().groovy("javax.ws.rs.core.Response.status(422).entity(null).build()")
        .markRollbackOnly()
      .end()
      .onException(Exception.class)
        .handled(true)
        .log("Error processing credit scores: [${exception.message}]")
        .setBody().groovy("javax.ws.rs.core.Response.status(500).entity(null).build()")
        .markRollbackOnly()
      .end()
      .log("Processing credit scores: [${body}]")
      .transacted("PROPAGATION_REQUIRED")
      .split(simple("${body[creditScores]}"))
        .shareUnitOfWork()
        .parallelProcessing(false)
        .stopOnException()
        .validate().simple("${body[ssn]} regex '^\\d{3}-\\d{2}-\\d{4}$'").end()
        .validate().simple("${body[score]} >= 300 && ${body[score]} <= 850").end()
        .to("sql:insert into credit_score values (:#${body[ssn]}, :#${body[score]}, :#${ref:currentDate})")
      .end()
      .setBody().groovy("javax.ws.rs.core.Response.status(200).entity(null).build()")
    ;
  }
}
