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

## Segunda VersÃ£o: persistencia com PostgreSQL no Aiven

Agora estamos iniciando a segunda parte do projeto. O objetivo desta etapa e substituir a persistencia em memoria, feita com `ArrayList`, por persistencia em banco de dados PostgreSQL no Aiven.

A interface, o modelo e a classe de aplicacao continuam com a mesma responsabilidade. A mudanca principal acontece na camada de persistencia: em vez de guardar os empregados em uma lista, vamos inserir e consultar os dados na tabela `empregado` usando JDBC.

## Biblioteca necessaria

Como o projeto nao usa Maven nem Gradle, precisamos acrescentar manualmente apenas a biblioteca estritamente necessaria:

- driver JDBC do PostgreSQL.

Crie uma pasta chamada `lib` na raiz do projeto e coloque nela o arquivo `.jar` do driver PostgreSQL. Nesta versao, usamos este arquivo:

```text
https://jdbc.postgresql.org/download/postgresql-42.7.11.jar
```

Com isso, os comandos de compilacao e execucao passam a incluir `lib/*` no classpath.

## Configuracao do acesso ao banco

Nao vamos gravar URL, usuario ou senha diretamente no codigo. Essas informacoes devem ficar em um arquivo local chamado `.env`.

O projeto possui um arquivo modelo chamado `.env.example`:

```text
APP_DB_URL=jdbc:postgresql://HOST:PORTA/NOME_DO_BANCO?sslmode=require
APP_DB_USUARIO=USUARIO
APP_DB_SENHA=SENHA
```

Cada aluno deve copiar esse arquivo para um novo arquivo chamado `.env` e trocar os placeholders pelos dados reais do seu banco.

No Aiven, o service URI costuma vir neste formato:

```text
postgres://USUARIO:SENHA@HOST:PORTA/NOME_DO_BANCO?sslmode=require
```

Para usar com JDBC, separamos as informacoes e montamos a URL neste formato:

```text
APP_DB_URL=jdbc:postgresql://HOST:PORTA/NOME_DO_BANCO?sslmode=require
APP_DB_USUARIO=USUARIO
APP_DB_SENHA=SENHA
```

Exemplo generico do arquivo `.env`:

```text
APP_DB_URL=jdbc:postgresql://HOST:PORTA/NOME_DO_BANCO?sslmode=require
APP_DB_USUARIO=USUARIO
APP_DB_SENHA=SENHA
```

O arquivo `.env` possui dados sensiveis e nao deve ser enviado para o Git. Por isso ele fica listado no `.gitignore`. Somente o `.env.example`, com dados ficticios, deve ser versionado.

## Estabelecendo a conexao

A classe `ConexaoFactory`, na camada de persistencia, fica responsavel por ler o arquivo `.env` e abrir a conexao:

```java
public Connection conectar() {
    String url = lerConfiguracaoObrigatoria("APP_DB_URL");
    String usuario = lerConfiguracaoObrigatoria("APP_DB_USUARIO");
    String senha = lerConfiguracaoObrigatoria("APP_DB_SENHA");

    Class.forName("org.postgresql.Driver");
    return DriverManager.getConnection(url, usuario, senha);
}
```

Antes disso, a propria `ConexaoFactory` carrega o `.env`, lendo linhas no formato `CHAVE=VALOR`. Assim, as demais classes nao precisam saber de onde vieram a URL, o usuario e a senha.

## Listando empregados pelo banco

Na nova classe `EmpregadoRepositoryPostgres`, o metodo `listar` consulta a tabela `empregado`:

```java
String sql = "select nome, cpf, salario from empregado order by nome";

try (Connection conexao = conexaoFactory.conectar();
     PreparedStatement comando = conexao.prepareStatement(sql);
     ResultSet resultado = comando.executeQuery()) {
    while (resultado.next()) {
        Empregado empregado = new Empregado(
                resultado.getString("nome"),
                resultado.getString("cpf"),
                resultado.getBigDecimal("salario")
        );
        empregados.add(empregado);
    }
}
```

O resultado do banco e transformado em objetos `Empregado`, que sao devolvidos para a aplicacao.

## Inserindo empregados no banco

O metodo `inserir` usa `PreparedStatement` para enviar os dados ao PostgreSQL:

```java
String sql = "insert into empregado (nome, cpf, salario) values (?, ?, ?)";

try (Connection conexao = conexaoFactory.conectar();
     PreparedStatement comando = conexao.prepareStatement(sql)) {
    comando.setString(1, empregado.getNome());
    comando.setString(2, empregado.getCpf());
    comando.setBigDecimal(3, empregado.getSalario());
    comando.executeUpdate();
}
```

O uso de `PreparedStatement` evita montar SQL por concatenacao de texto e deixa o codigo mais seguro e organizado.

## Compilando e executando a segunda versÃ£o

Para obter exatamente esta segunda versao do projeto, mesmo depois que o repositorio tiver novas versoes, use:

```bash
git clone https://github.com/victoriocarvalho/programaJavaBD.git
cd programaJavaBD
git checkout v2-persistenciaBD
```

Depois de criar o arquivo `.env` a partir do `.env.example`, ajustando os valores das variaveis, compile e execute:

```powershell
javac -d target/classes -cp "lib/*" src/main/java/br/com/cadastroempregados/App.java src/main/java/br/com/cadastroempregados/modelo/Empregado.java src/main/java/br/com/cadastroempregados/persistencia/EmpregadoRepository.java src/main/java/br/com/cadastroempregados/persistencia/ConexaoFactory.java src/main/java/br/com/cadastroempregados/persistencia/EmpregadoRepositoryPostgres.java src/main/java/br/com/cadastroempregados/aplicacao/EmpregadoService.java src/main/java/br/com/cadastroempregados/ui/EmpregadoFrame.java

java -cp "target/classes;lib/*" br.com.cadastroempregados.App
```

No VS Code, tambem e possivel executar pelo menu `Terminal > Run Task...` e selecionar a tarefa `Executar`.

## Terceira Versao: persistencia com Hibernate

Na terceira versao, continuamos usando PostgreSQL no Aiven, mas a camada de persistencia deixa de escrever SQL manualmente com JDBC na maior parte do codigo. Agora usamos Hibernate, uma ferramenta ORM.

ORM significa mapeamento objeto-relacional. Na pratica, isso permite mapear uma classe Java para uma tabela do banco de dados. Assim, a aplicacao trabalha com objetos `Empregado`, e o Hibernate se encarrega de transformar essas operacoes em comandos SQL.

A divisao em camadas continua a mesma:

- `modelo`: representa os dados e agora tambem contem as anotacoes de mapeamento.
- `persistencia`: cria o `EntityManager` e implementa as operacoes usando Hibernate.
- `aplicacao`: continua validando os dados antes de salvar.
- `ui`: continua apenas chamando a camada de aplicacao.

Essa e a principal vantagem de ter criado a interface `EmpregadoRepository`: a interface grafica e a classe `EmpregadoService` nao precisam saber se os dados estao sendo salvos com `ArrayList`, JDBC ou Hibernate.

## Por que usar Maven nesta versao

Nas versoes anteriores, o projeto era compilado diretamente com `javac`. Para JDBC isso ainda era simples, porque bastava adicionar o driver do PostgreSQL em `lib`.

Com Hibernate, isso muda. O Hibernate depende de varias outras bibliotecas. Baixar cada `.jar` manualmente deixaria o projeto dificil de montar e facil de quebrar. Por isso, nesta etapa o projeto passa a usar Maven.

O arquivo `pom.xml` declara as dependencias:

```xml
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>7.4.1.Final</version>
</dependency>

<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.11</version>
</dependency>
```

O Maven baixa essas bibliotecas automaticamente durante a compilacao.

## Entidade Empregado

A classe `Empregado` agora e uma entidade JPA:

```java
@Entity
@Table(name = "empregado")
public class Empregado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 20)
    private String cpf;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salario;

    protected Empregado() {
    }
}
```

As principais anotacoes sao:

- `@Entity`: informa que a classe sera persistida no banco.
- `@Table`: define o nome da tabela.
- `@Id`: indica a chave primaria.
- `@GeneratedValue`: informa que o banco gera o valor do `id`.
- `@Column`: configura detalhes das colunas.

O construtor vazio `protected` e necessario para o Hibernate criar objetos ao consultar o banco.

## Configuracao do Hibernate

O arquivo `src/main/resources/META-INF/persistence.xml` define a unidade de persistencia:

```xml
<persistence-unit name="cadastro-empregados" transaction-type="RESOURCE_LOCAL">
    <class>br.com.cadastroempregados.modelo.Empregado</class>
    <properties>
        <property name="hibernate.hbm2ddl.auto" value="update"/>
        <property name="hibernate.show_sql" value="true"/>
        <property name="hibernate.format_sql" value="true"/>
    </properties>
</persistence-unit>
```

A propriedade `hibernate.hbm2ddl.auto` com valor `update` permite que o Hibernate crie ou atualize a tabela conforme o mapeamento da entidade.

As credenciais do banco continuam fora do codigo. O arquivo `.env` permanece igual:

```text
APP_DB_URL=jdbc:postgresql://HOST:PORTA/NOME_DO_BANCO?sslmode=require
APP_DB_USUARIO=USUARIO
APP_DB_SENHA=SENHA
```

A classe `ConfiguracaoBanco` le esse arquivo. A classe `JpaFactory` usa esses valores para criar o `EntityManagerFactory`:

```java
Map<String, String> propriedades = new HashMap<>();
propriedades.put("jakarta.persistence.jdbc.url", configuracaoBanco.getUrl());
propriedades.put("jakarta.persistence.jdbc.user", configuracaoBanco.getUsuario());
propriedades.put("jakarta.persistence.jdbc.password", configuracaoBanco.getSenha());
propriedades.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");

this.entityManagerFactory = Persistence.createEntityManagerFactory(
        "cadastro-empregados",
        propriedades
);
```

## Repositorio com Hibernate

A nova classe `EmpregadoRepositoryHibernate` implementa a mesma interface `EmpregadoRepository`.

Para inserir:

```java
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
```

O metodo `persist` substitui o `insert into empregado (...) values (...)` escrito manualmente na versao JDBC.

Para listar:

```java
return entityManager
        .createQuery("select e from Empregado e order by e.nome", Empregado.class)
        .getResultList();
```

Essa consulta usa JPQL. Ela parece SQL, mas consulta entidades e atributos Java. Por isso usamos `Empregado` e `e.nome`, e nao diretamente o nome da tabela e das colunas.

Para verificar CPF duplicado:

```java
Long quantidade = entityManager
        .createQuery("select count(e) from Empregado e where e.cpf = :cpf", Long.class)
        .setParameter("cpf", cpf)
        .getSingleResult();
```

## Inicializacao da aplicacao

Na classe `App`, a implementacao usada agora e a do Hibernate:

```java
JpaFactory jpaFactory = new JpaFactory(new ConfiguracaoBanco());
Runtime.getRuntime().addShutdownHook(new Thread(jpaFactory::fechar));

EmpregadoRepositoryHibernate repository = new EmpregadoRepositoryHibernate(jpaFactory);
EmpregadoService service = new EmpregadoService(repository);
EmpregadoFrame frame = new EmpregadoFrame(service);
frame.setVisible(true);
```

O `shutdownHook` fecha o `EntityManagerFactory` quando a aplicacao termina.

## Compilando e executando a terceira versao

Para obter exatamente esta terceira versao do projeto, mesmo depois que o repositorio tiver novas versoes, use:

```bash
git clone https://github.com/victoriocarvalho/programaJavaBD.git
cd programaJavaBD
git checkout v3-persistenciaJPA
```

Para esta versao, instale o Maven e confira se ele esta disponivel no terminal:

```powershell
mvn -version
```

Depois de criar o arquivo `.env` a partir do `.env.example`, compile:

```powershell
mvn compile
```

Execute:

```powershell
mvn exec:java
```

No VS Code, tambem e possivel executar pelo menu `Terminal > Run Task...` e selecionar a tarefa `Executar`.

## Comparacao entre JDBC e Hibernate

Na versao JDBC, a classe `EmpregadoRepositoryPostgres` precisava abrir conexao, criar `PreparedStatement`, escrever SQL e transformar cada linha do `ResultSet` em um objeto.

Na versao Hibernate, a classe `EmpregadoRepositoryHibernate` trabalha diretamente com entidades:

- `persist` salva um objeto.
- JPQL consulta objetos.
- transacoes continuam existindo, mas o codigo de mapeamento fica menor.

O JDBC ainda aparece indiretamente, porque o Hibernate usa o driver PostgreSQL por baixo. A diferenca e que agora a aplicacao nao precisa escrever manualmente a maior parte do codigo repetitivo de acesso ao banco.
