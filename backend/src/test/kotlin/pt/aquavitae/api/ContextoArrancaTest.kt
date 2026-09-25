package pt.aquavitae.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import pt.aquavitae.api.cave.CaveController
import pt.aquavitae.api.cave.CaveRepository
import pt.aquavitae.api.produtor.ProdutorController
import pt.aquavitae.api.review.ReviewController
import kotlin.test.Test
import kotlin.test.assertNotNull

// O contexto Spring completo carrega SEM base de dados: valida a injeção de dependências, o mapeamento das
// entidades e a resolução dos nomes das queries derivadas (ex.: findByUtilizador_IdOrderByDataCriacaoAscIdAsc),
// que só o arranque da API costumava apanhar. A ligação ao Oracle nunca chega a ser aberta (URL fictício,
// pool preguiçoso e sem consulta de metadados). NÃO valida o schema (`validate`) nem executa SQL — para isso
// é preciso a BD (ver PLANO.md, "Retomar aqui").
@SpringBootTest(
    properties = [
        "spring.datasource.url=jdbc:oracle:thin:@//localhost:1/sem-bd",
        "spring.datasource.hikari.initialization-fail-timeout=-1",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false",
        "aquavitae.links.verificacao.ativo=false",
    ],
)
class ContextoArrancaTest {

    @Autowired
    lateinit var contexto: ApplicationContext

    @Test
    fun `o contexto carrega e os controllers e repositorios estao ligados`() {
        listOf(ProdutorController::class.java, CaveController::class.java, ReviewController::class.java, CaveRepository::class.java)
            .forEach { assertNotNull(contexto.getBean(it), "bean em falta: ${it.simpleName}") }
    }
}
