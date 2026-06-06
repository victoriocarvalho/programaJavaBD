package br.com.cadastroempregados.persistencia;

import br.com.cadastroempregados.modelo.Empregado;

import java.util.ArrayList;
import java.util.List;

public class EmpregadoRepositoryMemoria implements EmpregadoRepository {
    private final List<Empregado> empregados = new ArrayList<>();

    @Override
    public void inserir(Empregado empregado) {
        empregados.add(empregado);
    }

    @Override
    public List<Empregado> listar() {
        return new ArrayList<>(empregados);
    }

    @Override
    public boolean existeCpf(String cpf) {
        for (Empregado empregado : empregados) {
            if (empregado.getCpf().equals(cpf)) {
                return true;
            }
        }

        return false;
    }
}
