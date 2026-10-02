package implementadores;

import RMI.Conexao;
import interfaces.InterfacePagamento;
import modelos.Pagamento;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ServicoPagamento extends UnicastRemoteObject implements InterfacePagamento {

    public ServicoPagamento () throws RemoteException {}

    private final Conexao LINK = new Conexao();

    private final ServicoFuncionario SERVICO_FUNCIONARIO = new ServicoFuncionario();

    private final String SQL_ACHAR_PAGAMENTO_FUNCIONARIO = "select pagamento from Funcionario where Funcionario.id = ?";

    private final String SQL_ACHAR_SALARIO = "select c.salario from Funcionario f join Cargo c on f.cargoID = c.id where f.id = ?";

    private final String SQL_PAGAMENTO_JA_EFETUADO = "select count(*) from Pagamento where funcionario_id = ? and mes_ano = ?";

    private final String SQL_EFETUAR_PAGAMENTO = "insert into Pagamento (funcionario_id, mes_ano, salario_receptor, aliquota_utilizada, imposto_cobrado, pagamento_pratico) values (?, ?, ?, ?, ?, ?)";

    private static final DateTimeFormatter FORMATO_MES_ANO = DateTimeFormatter.ofPattern("MM/uuuu");



    @Override public double consultarPagamento (int funcionarioID) throws RemoteException {

        double pagamento;

        try {
            LINK.conectar();

            PreparedStatement sentenca = LINK.link.prepareStatement(SQL_ACHAR_PAGAMENTO_FUNCIONARIO);

            sentenca.setInt(1, funcionarioID);

            var resultado = sentenca.getResultSet();

            pagamento = resultado.getDouble(1);

            LINK.link.close();
        }

        catch (Exception e) {

            System.out.println("\nErro ao demitir funcionário: " + e.getMessage());

            pagamento = -5;
        }

        return pagamento;
    }



    /**
     * Alíquota (fração, ex.: 0.09 = 9%) aplicada ao salário.
     * Faixas ilustrativas, ajuste conforme a regra exigida no trabalho.
     */
    private double definirAliquota (double salario) {

        if (salario <= 1518.00) return 0.075;

        if (salario <= 2793.88) return 0.09;

        if (salario <= 4190.83) return 0.12;

        return 0.14;
    }



    @Override public void calcularEfetuarPagamento (int funcionarioID, String mesAno) throws RemoteException {

        // 1. Valida o mês/ano (formato MM/aaaa).
        try {
            YearMonth.parse(mesAno, FORMATO_MES_ANO);
        }

        catch (DateTimeParseException | NullPointerException e) {

            throw new IllegalArgumentException("Mês/ano inválido: use o formato MM/aaaa (ex.: 03/2026).");
        }

        try {
            LINK.conectar();

            Connection conexao = LINK.link;

            if (conexao == null) throw new IllegalStateException("Sem conexão com o banco de dados.");

            try (conexao) {

                // 2. Impede pagar duas vezes o mesmo funcionário no mesmo mês.
                try (PreparedStatement verificacao = conexao.prepareStatement(SQL_PAGAMENTO_JA_EFETUADO)) {

                    verificacao.setInt(1, funcionarioID);

                    verificacao.setString(2, mesAno);

                    try (ResultSet resultado = verificacao.executeQuery()) {

                        resultado.next();

                        if (resultado.getInt(1) > 0)
                            throw new IllegalStateException("O funcionário " + funcionarioID + " já foi pago em " + mesAno + ".");
                    }
                }

                // 3. Busca o salário do cargo do funcionário.
                double salario;

                try (PreparedStatement busca = conexao.prepareStatement(SQL_ACHAR_SALARIO)) {

                    busca.setInt(1, funcionarioID);

                    try (ResultSet resultado = busca.executeQuery()) {

                        if (!resultado.next())
                            throw new IllegalArgumentException("Funcionário " + funcionarioID + " não encontrado.");

                        salario = resultado.getDouble(1);
                    }
                }

                // 4. Calcula (alíquota, imposto e valor líquido ficam dentro de Pagamento).
                Pagamento pagamento = new Pagamento(definirAliquota(salario), salario);

                // 5. Efetua: registra o pagamento no banco.
                try (PreparedStatement insercao = conexao.prepareStatement(SQL_EFETUAR_PAGAMENTO)) {

                    insercao.setInt(1, funcionarioID);

                    insercao.setString(2, mesAno);

                    insercao.setDouble(3, pagamento.getSalario());

                    insercao.setDouble(4, pagamento.getAliquota());

                    insercao.setDouble(5, pagamento.getImposto());

                    insercao.setDouble(6, pagamento.getPagamentoPratico());

                    insercao.executeUpdate();
                }
            }
        }

        catch (IllegalArgumentException | IllegalStateException e) {

            throw e;
        }

        catch (Exception e) {

            throw new RuntimeException("Erro ao calcular/efetuar o pagamento: " + e.getMessage());
        }
    }
}