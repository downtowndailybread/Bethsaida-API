package org.downtowndailybread.integration

import org.downtowndailybread.bethsaida.Migrate
import org.downtowndailybread.integration.base.{BethsaidaSupport, TestDatabaseSource}
import org.scalatest.BeforeAndAfterAll

class IntegrationMain extends BethsaidaSupport with BeforeAndAfterAll
  with AuthenticationTest
  with ClientTest
  with UserTest {

  override def beforeAll() = {
    TestDatabaseSource.dropAndAddSchema()
    Migrate.migrate(settings.ds)
  }

  override def afterAll() = {

  }
}
