package br.com.cadastroempregados.persistencia;

import br.com.cadastroempregados.modelo.Empregado;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;

import java.util.List;

public class EmpregadoRepositoryHibernate implements EmpregadoRepository {
    private final JpaFactory jpaFactory;

    public EmpregadoRepositoryHibernate(JpaFactory jpaFactory) {
        this.jpaFactory = jpaFactory;
    }

    @Override
    public void inserir(Empregado empregado) {
        EntityManager entityManager = jpaFactory.criarEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            entityManager.persist(empregado);
            transaction.commit();
        } catch (PersistenceException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new IllegalStateException("Nao foi possivel inserir o empregado.", e);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<Empregado> listar() {
        try (EntityManager entityManager = jpaFactory.criarEntityManager()) {
            return entityManager
                    .createQuery("select e from Empregado e order by e.nome", Empregado.class)
                    .getResultList();
        } catch (PersistenceException e) {
            throw new IllegalStateException("Nao foi possivel listar os empregados.", e);
        }
    }

    @Override
    public boolean existeCpf(String cpf) {
        try (EntityManager entityManager = jpaFactory.criarEntityManager()) {
            Long quantidade = entityManager
                    .createQuery("select count(e) from Empregado e where e.cpf = :cpf", Long.class)
                    .setParameter("cpf", cpf)
                    .getSingleResult();

            return quantidade > 0;
        } catch (PersistenceException e) {
            throw new IllegalStateException("Nao foi possivel consultar o CPF.", e);
        }
    }
}
