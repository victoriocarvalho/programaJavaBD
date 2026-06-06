# Cadastro de Empregados com Java

Este tutorial conduz a criacao de um programa Java em camadas. A primeira versao usa uma lista em memoria. Depois, a camada de persistencia sera substituida por acesso a banco de dados com JDBC.

## Objetivo da primeira versao

Nesta etapa, o sistema cadastra empregados em memoria. Para cada empregado, guardamos:

- nome;
- CPF;
- salario.

A interface permite apenas:

- inserir empregado;
- listar empregados cadastrados.

Ainda nao teremos edicao, exclusao ou banco de dados.

## Camadas do projeto

O programa foi separado em quatro partes:

- `modelo`: representa os dados do sistema.
- `persistencia`: guarda e recupera os dados.
- `aplicacao`: contem as regras do cadastro.
- `ui`: contem a interface grafica em Swing.

Essa divisao prepara o projeto para trocar a persistencia em memoria por JDBC sem reescrever a interface.

## Classe de modelo

A classe `Empregado` representa um empregado:

```java
public class Empregado {
    private String nome;
    private String cpf;
    private BigDecimal salario;
}
```

O salario usa `BigDecimal`, que e uma escolha melhor para valores monetarios do que `double`.

## Persistencia em memoria

A interface `EmpregadoRepository` define o que a aplicacao precisa da persistencia:

```java
void inserir(Empregado empregado);
List<Empregado> listar();
boolean existeCpf(String cpf);
```

A classe `EmpregadoRepositoryMemoria` implementa essas operacoes usando um `ArrayList`.

## Regras da aplicacao

A classe `EmpregadoService` recebe os dados vindos da interface e aplica as regras:

- nome, CPF e salario sao obrigatorios;
- salario nao pode ser negativo;
- CPF nao pode ser duplicado;
- salario precisa ser numerico.

Somente depois dessas validacoes o empregado e enviado para a persistencia.

## Interface Swing

A classe `EmpregadoFrame` monta uma tela simples com:

- campos para nome, CPF e salario;
- botao Salvar;
- tabela com todos os empregados cadastrados.

A interface nao manipula diretamente o `ArrayList`. Ela chama a classe de aplicacao, que chama a persistencia.

## Execucao

Para obter exatamente esta versao do projeto, mesmo depois que o repositorio tiver novas versoes, use:

```bash
git clone https://github.com/victoriocarvalho/programaJavaBD.git
cd programaJavaBD
git checkout v1-arraylist
```

Compile e execute a classe principal:

```bash
javac -d target/classes src/main/java/br/com/cadastroempregados/App.java src/main/java/br/com/cadastroempregados/modelo/Empregado.java src/main/java/br/com/cadastroempregados/persistencia/EmpregadoRepository.java src/main/java/br/com/cadastroempregados/persistencia/EmpregadoRepositoryMemoria.java src/main/java/br/com/cadastroempregados/aplicacao/EmpregadoService.java src/main/java/br/com/cadastroempregados/ui/EmpregadoFrame.java
java -cp target/classes br.com.cadastroempregados.App
```

No VS Code, tambem e possivel executar pelo menu `Terminal > Run Task...` e selecionar a tarefa `Executar`.

## Proximo passo

Na proxima etapa, poderemos melhorar a organizacao do tutorial e preparar a camada de persistencia para ser substituida por uma implementacao JDBC usando o banco de dados no Aiven.
