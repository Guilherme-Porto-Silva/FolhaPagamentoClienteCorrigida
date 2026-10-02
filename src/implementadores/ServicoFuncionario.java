package implementadores;

import RMI.Conexao;
import interfaces.InterfaceFuncionario;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.PreparedStatement;
import java.util.LinkedList;
import java.util.List;
import modelos.Funcionario;

public class ServicoFuncionario extends UnicastRemoteObject implements InterfaceFuncionario {

    public ServicoFuncionario () throws RemoteException {}

    private final Conexao LINK = new Conexao();

    private final String SQL_CADASTRO_FUNCIONARIO = "insert into Funcionario (nome, cpf, cargoID) values (?, ?, ?)";

    private final String SQL_DEMICAO_FUNCIONARIO = "delete from Funcionario where Funcionario.id = ?";

    private final String SQL_CADASTRO_CARGO = "insert into Cargo (nome, salario, departamento) values (?, ?, ?)";

    private final String SQL_CADASTRO_DEPARTAMENTO = "insert into Departamento (nome) values (?)";

    private final String SQL_DEMICAO_JUSTIFICATIVA = "insert into Demissoes (funcionario_demitido, razao_demissao) values (?, ?)";

    private final String SQL_ACHAR_FUNCIONARIO = "select * from Funcionario where Funcionario.id = ?";

    private final String SQL_LISTAR_FUNCIONARIOS = "select nome from Funcionario";



    @Override public boolean cadastrarFuncionario (String nome, String cpf, int cargoID) throws RemoteException {

        boolean deuCerto;

        try{
            LINK.conectar();

            PreparedStatement sentencaFuncionario = LINK.link.prepareStatement(SQL_CADASTRO_FUNCIONARIO);

            sentencaFuncionario.setString(1, nome);

            sentencaFuncionario.setString(2, cpf);

            sentencaFuncionario.setInt(3, cargoID);

            deuCerto = !sentencaFuncionario.execute();

            LINK.link.close();
        }

        catch (Exception e) {

            System.out.println("\nDeu a seguinte merda: " + e.getMessage());

            deuCerto = false;
        }

        return deuCerto;
    }



    @Override public boolean demitirFuncionario (int funcionarioID, String justificativa) throws RemoteException {

        boolean deuCerto;

        try {
            LINK.conectar();

            PreparedStatement sentencaJustificativa = LINK.link.prepareStatement(SQL_DEMICAO_JUSTIFICATIVA);

            sentencaJustificativa.setInt(1, funcionarioID);

            sentencaJustificativa.setString(2, justificativa);

            int linhasJustificativa = sentencaJustificativa.executeUpdate();

            PreparedStatement sentencaDemissao = LINK.link.prepareStatement(SQL_DEMICAO_FUNCIONARIO);

            sentencaDemissao.setInt(1, funcionarioID);

            int linhasDemissao = sentencaDemissao.executeUpdate();

            deuCerto = (linhasJustificativa > 0 && linhasDemissao > 0);

            LINK.link.close();
        }

        catch (Exception e) {

            System.out.println("\nErro ao demitir funcionário: " + e.getMessage());

            deuCerto = false;
        }

        return deuCerto;
    }



    public Funcionario acharFuncionario (int funcionarioID) throws RemoteException {

        Funcionario procurado;

        try {
            LINK.conectar();

            PreparedStatement sentenca = LINK.link.prepareStatement(SQL_ACHAR_FUNCIONARIO);

            var resultado = sentenca.executeQuery();

            procurado = resultado.getObject(0, Funcionario.class);

            LINK.link.close();
        }

        catch (Exception e) {

            throw new RuntimeException("\nErro ao listar funcionários: " + e.getMessage());
        }

        return procurado;
    }



    @Override public List<String> listarFuncionarios () throws RemoteException {

        List<String> lista = new LinkedList<>();

        try {
            LINK.conectar();

            PreparedStatement sentenca = LINK.link.prepareStatement(SQL_LISTAR_FUNCIONARIOS);

            var resultado = sentenca.executeQuery();

            var tamanhoResultado = resultado.getFetchSize();

            for (var i = 0; i < tamanhoResultado; i++) lista.add(resultado.getString(i));

            LINK.link.close();
        }

        catch (Exception e) {

            System.out.println("\nErro ao listar funcionários: " + e.getMessage());
        }

        return lista;
    }



    @Override public boolean inserirCargo (String nome, double salario, int departamentoID) throws RemoteException {

        boolean deuCerto;

        try {
            LINK.conectar();

            PreparedStatement sentenca = LINK.link.prepareStatement(SQL_CADASTRO_CARGO);

            sentenca.setString(1, nome);

            sentenca.setDouble(2, salario);

            sentenca.setString(3, nome);

            deuCerto = !sentenca.execute();

            LINK.link.close();
        }

        catch (Exception e) {

            System.out.println("\nProblema no cadastro de um cargo: " + e.getMessage());

            deuCerto = false;
        }

        return deuCerto;
    }



    @Override public boolean inserirDepartamento (String nome) throws RemoteException {

        boolean deuCerto;

        try {
            LINK.conectar();

            PreparedStatement sentenca = LINK.link.prepareStatement(SQL_CADASTRO_DEPARTAMENTO);

            sentenca.setString(1, nome);

            deuCerto = !sentenca.execute();

            LINK.link.close();
        }

        catch (Exception e) {

            System.out.println("\nProblema no cadastro de um cargo: " + e.getMessage());

            deuCerto = false;
        }

        return deuCerto;
    }
}