/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.examples;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.sql.DataSource;
import org.apache.camel.component.sql.SqlComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class SqlComponentConfiguration {

  private static final Logger log = LoggerFactory.getLogger(SqlComponentConfiguration.class);

  @Resource(lookup = "java:jboss/datasources/CreditDS")
  private DataSource creditDS;
  
  @Produces
  @Named("sql")
  public SqlComponent sqlComponent() {
    SqlComponent component = new SqlComponent();
    component.setDataSource(creditDS);
    return component;
  }
}
