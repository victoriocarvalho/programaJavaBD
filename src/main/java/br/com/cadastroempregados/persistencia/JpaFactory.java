package br.com.cadastroempregados.persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public class JpaFactory {
    private static final String PERSISTENCE_UNIT = "cadastro-empregados";

    private final EntityManagerFactory entityManagerFactory;

    public JpaFactory(ConfiguracaoBanco configuracaoBanco) {
        Map<String, String> propriedades = new HashMap<>();
        propriedades.put("jakarta.persistence.jdbc.url", configuracaoBanco.getUrl());
        propriedades.put("jakarta.persistence.jdbc.user", configuracaoBanco.getUsuario());
        propriedades.put("jakarta.persistence.jdbc.password", configuracaoBanco.getSenha());
        propriedades.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");

        this.entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, propriedades);
    }

    public EntityManager criarEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    public void fechar() {
        entityManagerFactory.close();
    }
}
