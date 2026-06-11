package br.com.cadastroempregados;

import br.com.cadastroempregados.aplicacao.EmpregadoService;
import br.com.cadastroempregados.persistencia.ConfiguracaoBanco;
import br.com.cadastroempregados.persistencia.EmpregadoRepositoryHibernate;
import br.com.cadastroempregados.persistencia.JpaFactory;
import br.com.cadastroempregados.ui.EmpregadoFrame;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JpaFactory jpaFactory = new JpaFactory(new ConfiguracaoBanco());
            Runtime.getRuntime().addShutdownHook(new Thread(jpaFactory::fechar));

            EmpregadoRepositoryHibernate repository = new EmpregadoRepositoryHibernate(jpaFactory);
            EmpregadoService service = new EmpregadoService(repository);
            EmpregadoFrame frame = new EmpregadoFrame(service);
            frame.setVisible(true);
        });
    }
}
