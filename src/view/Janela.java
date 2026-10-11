package view;

import componentes.Departamentos;
import componentes.NomeCargo;
import java.awt.*;
import java.rmi.ConnectException;
import java.rmi.ConnectIOException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.text.Collator;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import modelos.Cargo;
import modelos.Departamento;

public class Janela extends JFrame {

    private final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final DateTimeFormatter MES_ANO_ENVIADO = DateTimeFormatter.ofPattern("MM/yyyy");

    private final DateTimeFormatter MES_ANO_EXIBIDO = DateTimeFormatter.ofPattern("MMMM/yyyy", PT_BR);

    private final int FUNCIONARIO_NAO_ENCONTRADO = -1;// O que o servidor devolve em buscarIdFuncionario() quando não há um ID para dar.

    private final int FUNCIONARIO_NOME_REPETIDO = -2;

    private final Cliente SERVICO_CLIENTE;
    
    private int cargaAtual = 0;// Numera as cargas da lista de funcionários: se uma resposta antiga chegar depois de uma mais nova, é descartada.

    private final JLabel ROTULO_DE_ESTADO;



    // Componentes da aba Funcionários

    private JTextField txtNomeFuncionario, txtCpfFuncionario;

    private SeletorOpcoes<NomeCargo> seletorCargoFuncionario;

    private JButton btnSalvarFuncionario;

    private DefaultTableModel modeloTabelaFuncionarios;



    // Componentes da aba Departamentos

    private JTextField txtNomeDepartamento;

    private JButton btnSalvarDepartamento;

    private JTextArea areaDepartamento;



    // Componentes da Aba Cargos

    private JTextField txtNomeCargo, txtSalarioCargo;

    private SeletorOpcoes<Departamentos> seletorDepartamentoCargo;

    private JButton btnSalvarCargo;



    // Componentes da Aba Pagamentos

    private SeletorOpcoes<String> seletorFuncionarioPagamento;

    private SeletorOpcoes<YearMonth> seletorMesPagamento;

    private JButton btnProcessar;

    private JTextArea areaPagamento;



    private JTabbedPane abas () {

        JTabbedPane retorno = new JTabbedPane();

        retorno.add("Funcionários", criarPainelFuncionarios());

        retorno.add("Departamentos", criarPainelDepartamentos());

        retorno.add("Cargos", criarPainelCargos());

        retorno.add("Pagamentos", criarPainelPagamentos());

        // Ao trocar de aba: atualiza a lista de funcionários (quem foi cadastrado agora, ou por outra pessoa,
        // já aparece) e deixa o cursor no primeiro campo, poupando um clique.
        retorno.addChangeListener(e -> {

            carregarFuncionarios();

            Component primeiroCampo = switch (retorno.getSelectedIndex()) {

                case 0 -> txtNomeFuncionario;

                case 1 -> txtNomeDepartamento;

                case 2 -> txtNomeCargo;

                default -> seletorFuncionarioPagamento;
            };

            SwingUtilities.invokeLater(primeiroCampo::requestFocusInWindow);
        });

        System.out.println("\nAbas do painel criadas.");

        return retorno;
    }



    // --- PAINEL DE FUNCIONÁRIOS --- //

    private JPanel criarPainelFormularioCadastro () {

        JPanel form = criarFormulario("Cadastrar Funcionário");

        // Funcionário novo: o nome é dado novo, então continua sendo digitado.
        txtNomeFuncionario = new JTextField();

        // O usuário digita só os números; pontos e traço entram sozinhos.
        txtCpfFuncionario = new JTextField();

        ((AbstractDocument) txtCpfFuncionario.getDocument()).setDocumentFilter(new FiltroCPF());

        // cargo vem da lista (enum NomeCargo)
        seletorCargoFuncionario = new SeletorOpcoes<>("Escolha o cargo...", "Nenhum cargo disponível", NomeCargo::toString);

        seletorCargoFuncionario.atualizar(List.of(NomeCargo.values()));

        linha(form, "Nome:", txtNomeFuncionario);

        linha(form, "CPF:", txtCpfFuncionario);

        linha(form, "Cargo:", seletorCargoFuncionario);

        btnSalvarFuncionario = new JButton("Salvar Funcionário");

        btnSalvarFuncionario.addActionListener(e -> salvarFuncionario());

        JButton btnAtualizarFuncionario = new JButton("Atualizar Lista");

        btnAtualizarFuncionario.addActionListener(e -> carregarFuncionarios());

        botoes(form, btnSalvarFuncionario, btnAtualizarFuncionario);

        // Enter passa para o campo seguinte.
        txtNomeFuncionario.addActionListener(e -> txtCpfFuncionario.requestFocusInWindow());

        txtCpfFuncionario.addActionListener(e -> seletorCargoFuncionario.requestFocusInWindow());

        return form;
    }



    private void salvarFuncionario () {

        String nome = txtNomeFuncionario.getText().trim();

        String cpf = txtCpfFuncionario.getText();

        NomeCargo cargo = seletorCargoFuncionario.getSelecionado();

        if (nome.isEmpty()) { avisar("Informe o nome do funcionário.", txtNomeFuncionario); return; }

        if (cpf.length() != FiltroCPF.TAMANHO_FORMATADO) { avisar("Informe o CPF completo (11 números).", txtCpfFuncionario); return; }

        if (cargo == null) { avisar("Escolha o cargo do funcionário.", seletorCargoFuncionario); return; }

        int cargoID = idDoCargo(cargo);

        if (cargoID <= 0) { mostrarErro("O cargo " + cargo + " não tem ID definido no switch de Cargo.java."); return; }

        executar(btnSalvarFuncionario, () -> SERVICO_CLIENTE.servicoFuncionario.cadastrarFuncionario(nome, cpf, cargo), cadastrou -> {

            if (!cadastrou) {

                mostrarErro("Erro relacionado a banco de dados...");

                return;
            }

            JOptionPane.showMessageDialog(this, "Funcionário cadastrado com sucesso.");

            txtNomeFuncionario.setText("");

            txtCpfFuncionario.setText("");

            txtNomeFuncionario.requestFocusInWindow();

            carregarFuncionarios();
        });
    }



    private JPanel criarPainelFuncionarios () {

        JPanel painel = new JPanel(new BorderLayout());

        painel.add(criarPainelFormularioCadastro(), BorderLayout.NORTH);

        modeloTabelaFuncionarios = tabelaSomenteLeitura("Funcionário");

        painel.add(new JScrollPane(new JTable(modeloTabelaFuncionarios)), BorderLayout.CENTER);

        return painel;
    }



    /**
     * Busca os nomes no servidor (sem travar a tela) e atualiza a tabela e a lista da aba Pagamentos.
     */
    private void carregarFuncionarios () {

        int numeroDestaCarga = ++cargaAtual;

        executar(null, () -> SERVICO_CLIENTE.servicoFuncionario.listarFuncionarios(),

        nomes -> {

            if (numeroDestaCarga != cargaAtual) return; // já há uma carga mais nova em andamento

            modeloTabelaFuncionarios.setRowCount(0);

            for (String nome: nomes) modeloTabelaFuncionarios.addRow(new String[]{nome});

            List<String> emOrdemAlfabetica = new ArrayList<>(nomes);

            emOrdemAlfabetica.sort(Collator.getInstance(PT_BR));

            seletorFuncionarioPagamento.atualizar(emOrdemAlfabetica);

            avisarNaBarra("", false);
        },

        erro -> {

            if (numeroDestaCarga != cargaAtual) return;

            String conhecido = traduzirSeConhecido(erro);

            avisarNaBarra(conhecido != null ? conhecido : "Não foi possível atualizar a lista de funcionários: " + traduzir(erro), true);
        });
    }



    // --- PAINEL DE DEPARTAMENTOS --- //

    private JPanel criarFormularioDepartamentos () {

        JPanel painel = new JPanel(new BorderLayout());

        JPanel form = new JPanel(new FlowLayout());

        form.add(new JLabel("Nome do departamento:"));

        txtNomeDepartamento = new JTextField(15);

        form.add(txtNomeDepartamento);

        btnSalvarDepartamento = new JButton("Cadastrar departamento");

        btnSalvarDepartamento.addActionListener(e -> inserirDepartamento());

        form.add(btnSalvarDepartamento);

        // É o único campo da aba: Enter já cadastra.
        txtNomeDepartamento.addActionListener(e -> inserirDepartamento());

        painel.add(form, BorderLayout.NORTH);

        return painel;
    }



    private void inserirDepartamento () {

        String nome = txtNomeDepartamento.getText().trim();

        if (nome.isEmpty()) {
            
            avisar("Informe o nome do departamento.", txtNomeDepartamento);
            
            return;
        }

        executar(btnSalvarDepartamento, () ->
                
                SERVICO_CLIENTE.servicoFuncionario.inserirDepartamento(nome), inseriu -> {

                    if (!inseriu) {
        
                        mostrarErro("O departamento não foi cadastrado.");
        
                        return;
                    }
        
                    JOptionPane.showMessageDialog(this, "Departamento cadastrado.");
        
                    txtNomeDepartamento.setText("");
                });
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

        JPanel form = criarFormulario("Cadastrar cargo");

        txtNomeCargo = new JTextField();

        txtSalarioCargo = new JTextField();

        txtSalarioCargo.setToolTipText("Ex.: 3660,50 ou 3.660,50");

        seletorDepartamentoCargo = new SeletorOpcoes<>("Escolha o departamento...", "Nenhum departamento disponível", Departamentos::toString);

        seletorDepartamentoCargo.atualizar(List.of(Departamentos.values()));

        linha(form, "Nome do cargo:", txtNomeCargo);

        linha(form, "Salário base (R$):", txtSalarioCargo);

        linha(form, "Departamento:", seletorDepartamentoCargo);

        btnSalvarCargo = new JButton("Salvar cargo");

        btnSalvarCargo.addActionListener(e -> cadastrarCargo());

        botoes(form, btnSalvarCargo);

        // Enter passa para o campo seguinte.
        txtNomeCargo.addActionListener(e -> txtSalarioCargo.requestFocusInWindow());

        txtSalarioCargo.addActionListener(e -> seletorDepartamentoCargo.requestFocusInWindow());

        painel.add(form, BorderLayout.NORTH);

        return painel;
    }



    private void cadastrarCargo () {

        String nome = txtNomeCargo.getText().trim();

        if (nome.isEmpty()) { avisar("Informe o nome do cargo.", txtNomeCargo); return; }

        double salario;

        try { salario = lerSalario(txtSalarioCargo.getText()); }

        catch (IllegalArgumentException ex) { avisar(ex.getMessage(), txtSalarioCargo); return; }

        Departamentos departamento = seletorDepartamentoCargo.getSelecionado();

        if (departamento == null) { avisar("Escolha o departamento do cargo.", seletorDepartamentoCargo); return; }

        int departamentoID = idDoDepartamento(departamento);

        if (departamentoID <= 0) { mostrarErro("O departamento " + departamento + " não tem ID definido no switch de Departamento.java."); return; }

        double salarioFinal = salario;

        executar(btnSalvarCargo, () -> SERVICO_CLIENTE.servicoFuncionario.inserirCargo(nome, salarioFinal, departamentoID), inseriu -> {

            if (!inseriu) {

                mostrarErro("Erro no cadastro de um cargo...");

                return;
            }

            JOptionPane.showMessageDialog(this, "Cargo cadastrado com sucesso.");

            // O departamento fica escolhido: cargos do mesmo departamento em sequência não exigem escolher de novo.
            txtNomeCargo.setText("");

            txtSalarioCargo.setText("");

            txtNomeCargo.requestFocusInWindow();
        });
    }



    private JPanel criarPainelCargos () {

        var painel = criarFormularioCargos();

        JTextArea areaCargo = new JTextArea();

        painel.add(new JScrollPane(areaCargo), BorderLayout.CENTER);

        return painel;
    }



    // --- PAINEL DE PAGAMENTOS --- //

    private JPanel criarFormularioPagamentos () {

        JPanel painel = new JPanel(new BorderLayout());

        JPanel form = criarFormulario("Gerar Folha de Pagamento");

        // Os nomes vêm de listarFuncionarios(), que devolve só o nome.
        seletorFuncionarioPagamento = new SeletorOpcoes<>("Escolha o funcionário...", "Nenhum funcionário cadastrado", nome -> nome);

        // Já vem com o mês atual escolhido: no uso normal, não precisa clicar aqui.
        seletorMesPagamento = new SeletorOpcoes<>("Escolha o mês...", "", mes -> primeiraMaiuscula(MES_ANO_EXIBIDO.format(mes)));

        seletorMesPagamento.atualizar(ultimosMeses());

        seletorMesPagamento.selecionarPrimeiro();

        linha(form, "Funcionário:", seletorFuncionarioPagamento);

        linha(form, "Mês/Ano:", seletorMesPagamento);

        btnProcessar = new JButton("Processar Pagamento");

        btnProcessar.addActionListener(e -> processarPagamento());

        botoes(form, btnProcessar);

        painel.add(form, BorderLayout.NORTH);

        return painel;
    }



    /**
     * ID sai do NOME: só o servidor sabe qual ID cada nome tem.
     * Pagar a pessoa errada é pior do que não pagar: com nome repetido, o cliente recusa.
     */
    private void processarPagamento () {

        String nomeFuncionario = seletorFuncionarioPagamento.getSelecionado();

        YearMonth mes = seletorMesPagamento.getSelecionado();

        if (nomeFuncionario == null) { avisar("Escolha o funcionário.", seletorFuncionarioPagamento); return; }

        if (mes == null) { avisar("Escolha o mês.", seletorMesPagamento); return; }

        String mesAno = mes.format(MES_ANO_ENVIADO); // o servidor recebe "10/2026", como antes

        executar(btnProcessar, () -> {

            int funcionarioID = SERVICO_CLIENTE.servicoFuncionario.buscarIdFuncionario(nomeFuncionario);

            if (funcionarioID == FUNCIONARIO_NAO_ENCONTRADO)

                throw new IllegalStateException("Não existe funcionário chamado \"" + nomeFuncionario + "\". Atualize a lista e tente de novo.");

            if (funcionarioID == FUNCIONARIO_NOME_REPETIDO)

                throw new IllegalStateException("Há mais de um funcionário chamado \"" + nomeFuncionario + "\": não dá para saber qual deles pagar. Nada foi processado.");

            SERVICO_CLIENTE.servicoPagamento.calcularEfetuarPagamento(funcionarioID, mesAno);

            return null;
        },

        ok -> {

            JOptionPane.showMessageDialog(this, "Pagamento gerado com sucesso.");

            areaPagamento.append("Pagamento processado para " + nomeFuncionario + " referente a " + mesAno + "\n");
        });
    }



    private JPanel criarPainelPagamentos () {

        var painel = criarFormularioPagamentos();

        areaPagamento = new JTextArea();

        areaPagamento.setEditable(false);

        painel.add(new JScrollPane(areaPagamento), BorderLayout.CENTER);

        return painel;
    }



    private int idDoDepartamento (Departamentos departamento) { return new Departamento(departamento).getId(); }

    private int idDoCargo (NomeCargo cargo) { return new Cargo(cargo).getId(); }



    /**
     * Chama o servidor sem travar a tela e, quando ele responde, executa "aoConcluir" na thread da tela.
     * O botão (se houver) fica desligado durante a chamada: clique duplo não cadastra duas vezes.
     */
    private <T> void executar (JButton botao, Callable<T> chamada, Consumer<T> aoConcluir) {

        executar(botao, chamada, aoConcluir, erro -> mostrarErro(traduzir(erro)));
    }



    private <T> void executar (JButton botao, Callable<T> chamada, Consumer<T> aoConcluir, Consumer<Throwable> aoFalhar) {

        if (botao != null) botao.setEnabled(false);

        new SwingWorker<T, Void>() {

            @Override protected T doInBackground () throws Exception { return chamada.call(); }

            @Override protected void done () {

                if (botao != null) botao.setEnabled(true);

                try { aoConcluir.accept(get()); }

                catch (InterruptedException e) { Thread.currentThread().interrupt(); }

                catch (ExecutionException e) { aoFalhar.accept(e.getCause()); }
            }
        }.execute();
    }



    private void avisar (String mensagem, Component campoComProblema) {

        JOptionPane.showMessageDialog(this, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);

        campoComProblema.requestFocusInWindow();
    }



    private void mostrarErro (String mensagem) {

        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }



    /**
     * Mensagem discreta no rodapé, para falhas de atualização automática.
     * Um pop-up a cada troca de aba incomodaria.
     */
    private void avisarNaBarra (String mensagem, boolean erro) {

        ROTULO_DE_ESTADO.setForeground(erro ? new Color(0xB00020) : Color.DARK_GRAY);

        ROTULO_DE_ESTADO.setText(mensagem.isEmpty() ? " " : mensagem.replace('\n', ' '));

        ROTULO_DE_ESTADO.setToolTipText(mensagem.isEmpty() ? null : mensagem);
    }



    /**
     * Como RMI costuma embrulhar o erro de verdade dentro de outro (ex.: ServerException), olhamos a cadeia toda.
     * Devolve null se não for um erro que a gente saiba explicar melhor.
     */ 
    private String traduzirSeConhecido (Throwable erro) {

        for (Throwable causa = erro; causa != null; causa = causa.getCause()) {

            // O servidor responde "método desconhecido" quando ainda não tem o método novo.
            if (causa instanceof UnmarshalException && String.valueOf(causa.getMessage()).contains("method hash")) {

                return "Servidor desatualizado: falta o método buscarIdFuncionario (veja PATCH_SERVIDOR.md).";
            }

            if (causa instanceof ConnectException || causa instanceof ConnectIOException || causa instanceof NoSuchObjectException) {

                return "Sem conexão com o servidor. Verifique se ele está no ar.";
            }
        }

        return null;
    }



    private String traduzir (Throwable erro) {

        String conhecido = traduzirSeConhecido(erro);

        if (conhecido != null) return conhecido;

        return "Erro inesperado: " + erro.getMessage();
    }



    /**
     * Formulário em duas colunas.
     * Rótulo ocupa só o que precisa e o campo fica com o resto da largura,
     * para os textos das listas clicáveis não ficarem cortados.
     */
    private JPanel criarFormulario (String titulo) {

        JPanel form = new JPanel(new GridBagLayout());

        form.setBorder(BorderFactory.createTitledBorder(titulo));

        return form;
    }



    private void linha (JPanel form, String rotulo, JComponent campo) {

        GridBagConstraints doRotulo = new GridBagConstraints();

        doRotulo.anchor = GridBagConstraints.WEST;

        doRotulo.insets = new Insets(3, 5, 3, 5);

        form.add(new JLabel(rotulo), doRotulo);

        GridBagConstraints doCampo = new GridBagConstraints();

        doCampo.gridwidth = GridBagConstraints.REMAINDER; // termina a linha

        doCampo.weightx = 1.0;

        doCampo.fill = GridBagConstraints.HORIZONTAL;

        doCampo.insets = new Insets(3, 5, 3, 5);

        form.add(campo, doCampo);
    }



    /**
     * Faixa de botões, lado a lado, ocupando a largura toda do formulário.
     */
    private void botoes (JPanel form, JButton... botoes) {

        JPanel faixa = new JPanel(new GridLayout(1, botoes.length, 5, 5));

        for (JButton botao: botoes) faixa.add(botao);

        GridBagConstraints c = new GridBagConstraints();

        c.gridwidth = GridBagConstraints.REMAINDER;

        c.fill = GridBagConstraints.HORIZONTAL;

        c.insets = new Insets(3, 5, 3, 5);

        form.add(faixa, c);
    }



    private DefaultTableModel tabelaSomenteLeitura (String... colunas) {

        return new DefaultTableModel(colunas, 0) {

            @Override public boolean isCellEditable (int linha, int coluna) { return false; }
        };
    }



    private String primeiraMaiuscula (String texto) {

        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }



    private List<YearMonth> ultimosMeses () {

        List<YearMonth> meses = new ArrayList<>();

        YearMonth atual = YearMonth.now();

        int quantosMesesAparecemNaListaDePagamentos = 24;

        for (int i = 0; i < quantosMesesAparecemNaListaDePagamentos; i++) meses.add(atual.minusMonths(i));

        return meses;
    }



    /**
     * Aceita o jeito brasileiro de escrever: 3660, 3660,50 ou 3.660,50.
     * Recusa 3660.50, de propósito.
     * O ponto poderia ser lido como milhar e o salário sairia 100 vezes maior.
     */
    private double lerSalario (String texto) {

        String limpo = texto.replace("R$", "").trim();

        if (!limpo.matches("\\d{1,3}(\\.\\d{3})+(,\\d{1,2})?|\\d+(,\\d{1,2})?"))

            throw new IllegalArgumentException("Salário inválido. Use o formato 3660,50 ou 3.660,50 ou 3660,00.");

        double salario = Double.parseDouble(limpo.replace(".", "").replace(",", "."));

        if (salario < 1621) throw new IllegalArgumentException("O salário precisa ser maior ou igual a R$ 1.621,00.");

        return salario;
    }



    public Janela () throws RemoteException {

        SERVICO_CLIENTE = new Cliente();

        setTitle("Sistema de Folha de Pagamento - Grupo 1");

        setSize(700, 500);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLocationRelativeTo(null);

        ROTULO_DE_ESTADO = new JLabel(" ");

        ROTULO_DE_ESTADO.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

        add(abas(), BorderLayout.CENTER);

        add(ROTULO_DE_ESTADO, BorderLayout.SOUTH);

        carregarFuncionarios();
    }



    // --- COLOCAR A TELA NA TELA --- //

    public static void main (String[] args) {

        SwingUtilities.invokeLater(() -> {

            try { new Janela().setVisible(true); }

            catch (Exception e) {

                JOptionPane.showMessageDialog(new JOptionPane(), "Erro: " + e.getMessage());
            }
        });
    }
}
