# Folha de Pagamento – Cliente RMI

Aplicação **Java** com interface gráfica (Swing) para um sistema de **folha de pagamento**, que utiliza **RMI (Remote Method Invocation)** para a comunicação entre cliente e servidor. Este repositório contém o lado do **cliente** (`FolhaPagamentoCliente`).

Trabalho I – RMI da disciplina de **Sistemas Distribuídos**, Universidade do Estado de Minas Gerais (UEMG).

**Grupo 1:** Giovana Silva Manço e Guilherme Porto e Silva
**Tema:** Folha de Pagamento
**Entidades:** Funcionário, Cargo, Departamento e Pagamento

---

## Funcionalidades

A janela principal (`ClienteJanela`) é organizada em quatro abas:

| Aba | O que faz |
|---|---|
| **Funcionários** | Cadastra funcionário (nome, CPF, ID do departamento e ID do cargo) e lista os funcionários cadastrados |
| **Departamentos** | Cadastra departamentos |
| **Cargos** | Cadastra cargos com nome e salário base |
| **Pagamentos** | Processa o pagamento de um funcionário (ID + mês/ano, ex.: `10/2026`) |

Além disso, os serviços remotos expõem operações de demissão de funcionário (com justificativa registrada na tabela `Demissoes`) e consulta de pagamento.

## Arquitetura

```
┌────────────────────┐      RMI (porta 1500)      ┌────────────────────┐      JDBC      ┌─────────┐
│  Cliente (Swing)   │ ─────────────────────────▶ │  Servidor (RMI)    │ ─────────────▶ │  MySQL  │
│  view/             │ ◀───────────────────────── │  implementadores/  │                │         │
└────────────────────┘                            └────────────────────┘                └─────────┘
```

- As **interfaces remotas** (`interfaces/`) definem o contrato entre cliente e servidor.
- Os **implementadores** (`implementadores/`) estendem `UnicastRemoteObject` e executam as operações no banco de dados via JDBC.
- Os **modelos** (`modelos/`) são `Serializable`, para poderem trafegar entre as máquinas.

### Interfaces remotas

**`InterfaceFuncionario`**

| Método | Descrição |
|---|---|
| `cadastrarFuncionario(nome, cpf, cargoID)` | Insere um funcionário |
| `demitirFuncionario(funcionarioID, justificativa)` | Registra a justificativa em `Demissoes` e remove o funcionário |
| `listarFuncionarios()` | Retorna a lista de nomes dos funcionários |
| `inserirCargo(nome, salario, departamentoID)` | Insere um cargo |
| `inserirDepartamento(nome)` | Insere um departamento |

**`InterfacePagamento`**

| Método | Descrição |
|---|---|
| `consultarPagamento(funcionarioID)` | Consulta o pagamento de um funcionário |
| `calcularEfetuarPagamento(funcionarioID, mesAno)` | Calcula e efetua o pagamento do mês/ano informado |

## Estrutura do projeto

```
FolhaPagamentoCliente/
├── src/
│   ├── RMI/
│   │   └── Conexao.java               # Conexão JDBC com o MySQL
│   ├── interfaces/
│   │   ├── InterfaceFuncionario.java  # Contrato remoto de funcionários/cargos/departamentos
│   │   └── InterfacePagamento.java    # Contrato remoto de pagamentos
│   ├── implementadores/
│   │   ├── ServicoFuncionario.java    # Implementação (acesso ao banco)
│   │   └── ServicoPagamento.java
│   ├── modelos/
│   │   ├── Funcionario.java
│   │   └── Pagamento.java
│   └── view/
│       ├── Cliente.java               # Localiza o registro RMI e faz o lookup dos serviços
│       └── ClienteJanela.java         # Interface gráfica (ponto de entrada)
└── cliente.iml
```

## Tecnologias

- Java (projeto configurado no IntelliJ IDEA com JDK 24+)
- Java RMI
- Java Swing
- JDBC + MySQL (driver `com.mysql.cj.jdbc.Driver`, MySQL Connector/J)

## Configuração

### Banco de dados

O banco esperado se chama `rmi_guigui_chan` (MySQL). As consultas do código utilizam as tabelas abaixo; um esquema compatível seria:

```sql
CREATE DATABASE IF NOT EXISTS rmi_guigui_chan;
USE rmi_guigui_chan;

CREATE TABLE Departamento (
    id   INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(25) NOT NULL
);

CREATE TABLE Cargo (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    nome         VARCHAR(100) NOT NULL,
    salario      float NOT NULL,
    departamento INT,
    FOREIGN KEY (departamento) REFERENCES Departamento(id)
);

CREATE TABLE Funcionario (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    nome    VARCHAR(150) NOT NULL,
    cpf     VARCHAR(14)  NOT NULL,
    cargoID INT,
    FOREIGN KEY (cargoID) REFERENCES Cargo(id)
);

CREATE TABLE Demissoes (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    funcionario_demitido INT NOT NULL,
    razao_demissao      VARCHAR(255)
);
```

> Este esquema foi deduzido a partir das consultas SQL do código. Ajuste-o ao script oficial do projeto, se houver diferenças.

### Credenciais

A classe `Conexao` lê as credenciais das seguintes **variáveis de ambiente**:

| Variável | Fallback |
|---|---|
| `RMI_URL` | `jdbc:mysql://localhost/rmi_guigui_chan` |
| `DB_USER` | `root` |
| `DB_PASSWORD` | `123456` |

Se as variáveis não estiverem definidas, ela usa um valor padrão local (`jdbc:mysql://localhost/rmi_guigui_chan`, usuário `root`). Recomenda-se sempre definir as variáveis em vez de depender do padrão.

### Endereço do servidor RMI

Em `src/view/Cliente.java`, ajuste o IP do servidor conforme a máquina em que ele estiver rodando no laboratório:

```java
String serverIP = "172.16.0.20";   // IP da máquina servidora
Registry conexao = LocateRegistry.getRegistry(serverIP, 1500);   // porta 1500
```

Os serviços são buscados no registro pelos nomes `"chave"` (funcionários) e `"produto"` (pagamentos), que devem ser os mesmos usados no `bind`/`rebind` do servidor.

## Como executar

1. Suba o **MySQL** e crie o banco/tabelas (seção acima).
2. Inicie o **servidor RMI** na máquina servidora, registrando os serviços na porta `1500`.
3. Na máquina cliente, abra o projeto no IntelliJ IDEA, adicione o **MySQL Connector/J** às bibliotecas do projeto e confirme o IP em `Cliente.java`.
4. Execute a classe `view.ClienteJanela`.

Para as duas máquinas se enxergarem, verifique que estão na mesma rede e que a porta `1500` está liberada no firewall do servidor.

## Cálculo do pagamento

A classe `Pagamento` recebe a **alíquota** e o **salário** e calcula:

- `imposto = salário / alíquota`
- `pagamento líquido = salário − imposto`

---

## 🔗 Repositório do Servidor

O servidor desta interface gráfica pode ser encontrado no repositório:  
👉 [[#]](https://github.com/Guilherme-Porto-Silva/FolhaPagamentoCliente/tree/main)

---

## 👥 Autores

- **Fillip Will de Oliveira Amaral**
- **Giovana Silva Manço**
- **Guilherme Porto e Silva**
