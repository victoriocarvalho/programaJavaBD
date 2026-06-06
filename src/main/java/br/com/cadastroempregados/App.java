package br.com.cadastroempregados;

import br.com.cadastroempregados.aplicacao.EmpregadoService;
import br.com.cadastroempregados.persistencia.EmpregadoRepositoryMemoria;
import br.com.cadastroempregados.ui.EmpregadoFrame;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EmpregadoRepositoryMemoria repository = new EmpregadoRepositoryMemoria();
            EmpregadoService service = new EmpregadoService(repository);
            EmpregadoFrame frame = new EmpregadoFrame(service);
            frame.setVisible(true);
        });
    }
}
