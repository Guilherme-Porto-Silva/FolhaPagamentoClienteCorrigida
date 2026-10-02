package view;

import interfaces.InterfaceFuncionario;
import interfaces.InterfacePagamento;
import java.awt.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ClienteJanela extends JFrame {

    

    private Cliente SERVICO_CLIENTE = new Cliente();



    // Componentes da aba Funcionários
    
    private JTextField txtNomeFuncionario, txtCpfFuncionario, txtIdDepartamentoFuncionario, txtIdCargoFuncionario;
    
    private JTable tabelaFuncionarios;
    
    private DefaultTableModel modeloTabelaFuncionarios;



    // Componentes da aba Departamentos
    
    private JTextField txtNomeDepartamento;
    
    private JTextArea areaDepartamento;



    // Componentes da Aba Cargos

    private JTextField txtNomeCargo, txtSalarioCargo;
    
    private JTextArea areaCargo;



    // Componentes da Aba Pagamentos

    private JTextField txtFuncionarioIDPag, txtMesAnoPag;
    
    private JTextArea areaPagamento;



    /*private void conectarRMI () {

        try {
            SERVICO_CLIENTE.conectarRMI();

            System.out.println("\nRMI conectado.");
        }

        catch (Exception e) {

            JOptionPane.showMessageDialog(this, "Erro ao conectar ao Servidor RMI: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }*/



    private JTabbedPane abas () {

        JTabbedPane retorno = new JTabbedPane();

        retorno.add("Funcionários", criarPainelFuncionarios());

        retorno.add("Departamentos", criarPainelDepartamentos());

        retorno.add("Cargos", criarPainelCargos());

        retorno.add("Pagamentos", criarPainelPagamentos());

        System.out.println("\nAbas do painel criadas.");

        return retorno;
    }



    public ClienteJanela () throws RemoteException {   

        setTitle("Sistema de Folha de Pagamento - Grupo 1");

        setSize(700, 500);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLocationRelativeTo(null);

        add(abas());
    }



    // --- PAINEL DE FUNCIONÁRIOS --- //

    private JPanel criarPainelFormularioCadastro () {

        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));

        form.setBorder(BorderFactory.createTitledBorder("Cadastrar Funcionário"));

        form.add(new JLabel("Nome:"));

        txtNomeFuncionario = new JTextField();

        form.add(txtNomeFuncionario);

        form.add(new JLabel("CPF:"));

        txtCpfFuncionario = new JTextField();

        form.add(txtCpfFuncionario);

        form.add(new JLabel("ID Departamento:"));

        txtIdDepartamentoFuncionario = new JTextField();

        form.add(txtIdDepartamentoFuncionario);

        form.add(new JLabel("ID Cargo:"));

        txtIdCargoFuncionario = new JTextField();

        form.add(txtIdCargoFuncionario);

        JButton btnSalvarFuncionario = new JButton("Salvar Funcionário");

        btnSalvarFuncionario.addActionListener(e -> { salvarFuncionario(); });

        form.add(btnSalvarFuncionario);

        JButton btnAtualizarFuncionario = new JButton("Atualizar Lista");

        btnAtualizarFuncionario.addActionListener(e -> carregarFuncionarios());

        form.add(btnAtualizarFuncionario);

        return form;
    }



    private void salvarFuncionario () {

        String serverIP = "172.16.0.19";

        try{
            Registry conexao = LocateRegistry.getRegistry(serverIP,1500);

            InterfaceFuncionario servicoFuncionario = (InterfaceFuncionario) conexao.lookup("numeroServicoFuncionario");

           String nome = txtNomeFuncionario.getText();

            String cpf = txtCpfFuncionario.getText();

            

            var cargoID = Integer.parseInt(txtIdCargoFuncionario.getText());

            if (servicoFuncionario.cadastrarFuncionario(nome, cpf, cargoID)) {

                JOptionPane.showMessageDialog(this, "Funcionário cadastrado com sucesso.");

                carregarFuncionarios();
            }

            else JOptionPane.showMessageDialog(this, "Erro relacionado a banco de dados...", "Erro", JOptionPane.ERROR_MESSAGE);
        }

        catch (RemoteException e) {

            throw new RuntimeException("Erro de conexão. - " + e.getMessage());
        }

        catch (NotBoundException e) {

            throw new RuntimeException("Erro na chama do serviço. - " + e.getMessage());
        }
        
    }



    private JPanel criarPainelFuncionarios() {

        JPanel painel = new JPanel(new BorderLayout());

        painel.add(criarPainelFormularioCadastro(), BorderLayout.NORTH);

        // Tabela de Listagem

        modeloTabelaFuncionarios = new DefaultTableModel(new String[]{"ID", "Dados do Funcionário"}, 0);

        tabelaFuncionarios = new JTable(modeloTabelaFuncionarios);

        painel.add(new JScrollPane(tabelaFuncionarios), BorderLayout.CENTER);

        carregarFuncionarios();

        return painel;
    }



    private void carregarFuncionarios() {

         String serverIP = "172.16.0.19";

        try{
            Registry conexao = LocateRegistry.getRegistry(serverIP,1500);

            InterfaceFuncionario servicoFuncionario = (InterfaceFuncionario) conexao.lookup("numeroServicoFuncionario");

            modeloTabelaFuncionarios.setRowCount(0);

            List<String> lista = servicoFuncionario.listarFuncionarios();

            for (String item: lista) modeloTabelaFuncionarios.addRow(new String[]{item});
        }

        catch (RemoteException e) {

            throw new RuntimeException("Erro de conexão. - " + e.getMessage());
        }

        catch (NotBoundException e) {

            throw new RuntimeException("Erro na chama do serviço. - " + e.getMessage());
        }
     
    }



    // --- PAINEL DE DEPARTAMENTOS --- //

    private JPanel criarFormularioDepartamentos () {

        JPanel painel = new JPanel(new BorderLayout());

        JPanel form = new JPanel(new FlowLayout());

        form.add(new JLabel("Nome do departamento:"));

        txtNomeDepartamento = new JTextField(15);

        form.add(txtNomeDepartamento);

        JButton btnSalvarDepartamento = new JButton("Cadastrar departamento");

        btnSalvarDepartamento.addActionListener(e -> { inserirDepartamento(); });

        form.add(btnSalvarDepartamento);

        painel.add(form, BorderLayout.NORTH);

        return painel;
    }



    private void inserirDepartamento () {

        String serverIP = "172.16.0.19";

        try{
            Registry conexao = LocateRegistry.getRegistry(serverIP,1500);

            InterfaceFuncionario servicoFuncionario = (InterfaceFuncionario) conexao.lookup("numeroServicoFuncionario");

            servicoFuncionario.inserirDepartamento(txtNomeDepartamento.getText());

            JOptionPane.showMessageDialog(this, "Departamento cadastrado.");

            txtNomeDepartamento.setText("");
        }

        catch (Exception ex) {

            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }



    private JPanel criarPainelDepartamentos () {

        var painel = criarFormularioDepartamentos();

        areaDepartamento = new JTextArea();

        painel.add(new JScrollPane(areaDepartamento), BorderLayout.CENTER);

        return painel;
    }



    // --- PAINEL DE CARGOS --- //

    private JPanel criarFormularioCargos () {

        JPanel painel = new JPanel(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));

        form.setBorder(BorderFactory.createTitledBorder("Cadastrar cargo"));

        form.add(new JLabel("Nome do cargo:"));

        txtNomeCargo = new JTextField();

        form.add(txtNomeCargo);

        form.add(new JLabel("Salário base (R$):"));

        txtSalarioCargo = new JTextField();

        form.add(txtSalarioCargo);

        JButton btnSalvarCargo = new JButton("Salvar cargo");

        btnSalvarCargo.addActionListener(e -> {
            
            try{ cadastrarCargo(); }

            catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Erro no cadastro do cargo: " + ex.getMessage());
            }
        });

        form.add(btnSalvarCargo);

        painel.add(form, BorderLayout.NORTH);

        return painel;
    }



    private void cadastrarCargo () throws HeadlessException, RemoteException {

        String nome = txtNomeCargo.getText();

        var departamentoID = Integer.parseInt(txtIdDepartamentoFuncionario.getText());

        double salario = Double.parseDouble(txtSalarioCargo.getText());

        String serverIP = "172.16.0.19";

        try{
            Registry conexao = LocateRegistry.getRegistry(serverIP,1500);

            InterfaceFuncionario servicoFuncionario = (InterfaceFuncionario) conexao.lookup("numeroServicoFuncionario");

        if (servicoFuncionario.inserirCargo(nome, salario, departamentoID)) {

            JOptionPane.showMessageDialog(this, "Cargo cadastrado com sucesso.");

            txtNomeCargo.setText("");

            txtSalarioCargo.setText("");
        }

        else JOptionPane.showMessageDialog(this, "Erro no cadastro de um cargo...");
    }

        catch (Exception ex) {

            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }


    
    private JPanel criarPainelCargos () {
        
        var painel = criarFormularioCargos();
        
        areaCargo = new JTextArea();
        
        painel.add(new JScrollPane(areaCargo), BorderLayout.CENTER);

        return painel;
    }



    // --- PAINEL DE PAGAMENTOS --- //

    private JPanel criarFormularioPagamentos () {

        JPanel painel = new JPanel(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));

        form.setBorder(BorderFactory.createTitledBorder("Gerar Folha de Pagamento"));

        form.add(new JLabel("ID do funcionário:"));

        txtFuncionarioIDPag = new JTextField();

        form.add(txtFuncionarioIDPag);

        form.add(new JLabel("Mês/Ano (Ex: 10/2026):"));

        txtMesAnoPag = new JTextField();

        form.add(txtMesAnoPag);

        JButton btnProcessar = new JButton("Processar Pagamento");

        btnProcessar.addActionListener(e -> { processarPagamento(); });

        form.add(btnProcessar);

        painel.add(form, BorderLayout.NORTH);

        return painel;
    }



    private void processarPagamento () {

        String serverIP = "172.16.0.19";

        try{
            Registry conexao = LocateRegistry.getRegistry(serverIP,1500);

            InterfacePagamento servicoPagamento = (InterfacePagamento) conexao.lookup("numeroServicoFuncionario");

            var funcionarioID = Integer.parseInt(txtFuncionarioIDPag.getText());

            String mesAno = txtMesAnoPag.getText();

            servicoPagamento.calcularEfetuarPagamento(funcionarioID, mesAno);

            JOptionPane.showMessageDialog(this, "Pagamento gerado com sucesso.");

            areaPagamento.append("Pagamento processado para o funcionário ID " + funcionarioID + " referente a " + mesAno + "\n");
        }

        catch (Exception ex) {

            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }


    
    private JPanel criarPainelPagamentos () {

        var painel = criarFormularioPagamentos();

        areaPagamento = new JTextArea();

        painel.add(new JScrollPane(areaPagamento), BorderLayout.CENTER);

        return painel;
    }



    // --- COLOCAR A TELA NA TELA --- //

    public static void main (String[] args) {

        SwingUtilities.invokeLater(() -> {

            try { new ClienteJanela().setVisible(true); }

            catch (RemoteException e) {

                System.out.println("\nDeu a seguinte merda: " + e.getMessage());

                System.out.println("\nEla foi causada por: " + e.getCause().toString());
            }
        });
    }
}