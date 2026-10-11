package view;

import java.awt.Color;
import java.awt.Component;
import java.util.List;
import java.util.function.Function;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;

/**
 * Lista suspensa de opções clicáveis (JComboBox), no mesmo espírito do combo de cargos que já existia.
 * O usuário vê só o texto (rótulo); o objeto de verdade fica guardado por trás e é pego com
 * {@link #getSelecionado()}. Assim ninguém precisa digitar ID nenhum: o ID sai do objeto escolhido.
 * Comportamentos pensados para o usuário fazer o mínimo de escolhas:
 * - ao trocar as opções, a que já estava escolhida continua escolhida;
 * - se só existe UMA opção, ela já vem escolhida;
 * - sem escolha feita, mostra um texto-guia em cinza ("Escolha o cargo...").
 */
public class SeletorOpcoes<T> extends JComboBox<SeletorOpcoes.Opcao<T>> {

    /** Item da lista: o objeto + o texto que o usuário enxerga. */
    public record Opcao<T> (T valor, String rotulo) {

        // O JComboBox usa toString() para escolher item pela primeira letra digitada.
        @Override public String toString () { return rotulo; }
    }



    private static final Color COR_TEXTO_GUIA = new Color(0x6B6B6B);

    private final Function<T, String> rotulador;

    private final String textoEscolher, textoSemOpcoes;

    // Enquanto a lista não chegou do servidor, não dá para dizer que "não há opção".
    private boolean carregado = false;



    public SeletorOpcoes (String textoEscolher, String textoSemOpcoes, Function<T, String> rotulador) {

        this.textoEscolher = textoEscolher;

        this.textoSemOpcoes = textoSemOpcoes;

        this.rotulador = rotulador;

        setMaximumRowCount(12);

        setRenderer(new DefaultListCellRenderer() {

            private boolean textoGuia;

            @Override public Component getListCellRendererComponent (JList<?> lista, Object valor, int indice, boolean selecionado, boolean foco) {

                textoGuia = valor == null;// valor == null: nada escolhido, então, mostramos o texto-guia.

                super.getListCellRendererComponent(lista, valor, indice, selecionado, foco);

                if (textoGuia) {

                    if (!carregado) setText("(lista ainda não carregada)");

                    else setText(getItemCount() == 0 ? SeletorOpcoes.this.textoSemOpcoes : SeletorOpcoes.this.textoEscolher);
                }

                return this;
            }

//          O visual do Swing repinta a cor do texto DEPOIS do renderer.
//          Por isso, a cor do texto-guia é imposta aqui.
            @Override public void setForeground (Color cor) { super.setForeground(textoGuia ? COR_TEXTO_GUIA : cor); }
        });

        setSelectedIndex(-1);
    }



    /** Troca as opções, mantendo a escolha atual quando ela ainda existir (comparando com equals). */
    public void atualizar (List<T> itens) {

        carregado = true;

        T anterior = getSelecionado();

        DefaultComboBoxModel<Opcao<T>> modelo = new DefaultComboBoxModel<>();

        for (T item: itens) modelo.addElement(new Opcao<>(item, rotulador.apply(item)));

        setModel(modelo);

        setSelectedIndex(-1); // o modelo escolhe sozinho o 1º item; aqui desfazemos isso

        if (anterior != null) {

            for (int i = 0; i < itens.size(); i++) {

                if (anterior.equals(itens.get(i))) setSelectedIndex(i);
            }
        }

        if (getSelectedIndex() < 0 && itens.size() == 1) setSelectedIndex(0);
    }



    /** Objeto escolhido (ou null, se o usuário ainda não tiver escolhido). */
    @SuppressWarnings("unchecked")
    public T getSelecionado () {

        Opcao<T> escolhida = (Opcao<T>) getSelectedItem();

        return escolhida == null ? null : escolhida.valor();
    }



    public void selecionarPrimeiro () {

        if (getItemCount() > 0) setSelectedIndex(0);
    }
}
