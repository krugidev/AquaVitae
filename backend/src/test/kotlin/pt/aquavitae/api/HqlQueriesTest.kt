package pt.aquavitae.api

import jakarta.persistence.Entity
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.core.type.filter.AssignableTypeFilter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import kotlin.test.Test
import kotlin.test.assertTrue

// Valida a sintaxe e a semântica (propriedades, joins, parâmetros) de TODAS as @Query dos repositórios contra o
// mapeamento das entidades, sem base de dados: um erro de HQL apanha-se aqui em segundos, em vez de só ao arrancar
// a API contra o Oracle. Não executa SQL, por isso não substitui testar com dados reais.
class HqlQueriesTest {

    private val sessionFactory by lazy {
        val entidades = ClassPathScanningCandidateComponentProvider(false)
            .apply { addIncludeFilter(AnnotationTypeFilter(Entity::class.java)) }
            .findCandidateComponents("pt.aquavitae.api")
            .mapNotNull { it.beanClassName }
            .map { Class.forName(it) }

        val registry = StandardServiceRegistryBuilder()
            .applySetting("hibernate.dialect", "org.hibernate.dialect.OracleDialect")
            .applySetting("hibernate.boot.allow_jdbc_metadata_access", "false")
            .applySetting("hibernate.hbm2ddl.auto", "none")
            .build()
        MetadataSources(registry).apply { entidades.forEach { addAnnotatedClass(it) } }.buildMetadata().buildSessionFactory()
    }

    // Interfaces (o scanner por omissão só devolve classes concretas) que estendem JpaRepository.
    private fun repositorios(): List<Class<*>> =
        object : ClassPathScanningCandidateComponentProvider(false) {
            override fun isCandidateComponent(beanDefinition: AnnotatedBeanDefinition) = beanDefinition.metadata.isInterface
        }.apply { addIncludeFilter(AssignableTypeFilter(JpaRepository::class.java)) }
            .findCandidateComponents("pt.aquavitae.api")
            .mapNotNull { it.beanClassName }
            .map { Class.forName(it) }

    @Test
    fun `todas as queries HQL dos repositorios sao validas`() {
        val falhas = mutableListOf<String>()
        var validadas = 0
        sessionFactory.openSession().use { session ->
            repositorios().forEach { repositorio ->
                repositorio.declaredMethods.mapNotNull { metodo -> metodo.getAnnotation(Query::class.java)?.let { metodo to it } }
                    .filterNot { (_, query) -> query.nativeQuery }
                    .forEach { (metodo, query) ->
                        validadas++
                        try {
                            session.createQuery(query.value.trimIndent())
                        } catch (e: Exception) {
                            falhas += "${repositorio.simpleName}.${metodo.name}: ${e.message?.lineSequence()?.firstOrNull()}"
                        }
                    }
            }
        }
        assertTrue(validadas > 0, "não encontrou nenhuma @Query para validar — o scanner de repositórios falhou?")
        assertTrue(falhas.isEmpty(), "HQL inválido em $validadas queries:\n" + falhas.joinToString("\n"))
    }

    // Controlo negativo: sem isto, um scanner/validador que aceita tudo passava despercebido.
    @Test
    fun `uma query com propriedade inexistente e rejeitada`() {
        val rejeitou = try {
            sessionFactory.openSession().use { it.createQuery("SELECT b FROM Bebida b WHERE b.propriedadeQueNaoExiste = 1") }
            false
        } catch (e: Exception) {
            true
        }
        assertTrue(rejeitou)
    }
}
