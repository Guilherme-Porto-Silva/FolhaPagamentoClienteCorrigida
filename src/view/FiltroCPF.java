package view;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * Deixa o usuário digitar (ou colar) apenas os números do CPF: a pontuação entra sozinha.
 * Digitar 12345678901 resulta em 123.456.789-01. Colar "123.456.789-01" ou "12345678901" dá o mesmo resultado.
 * Letras e símbolos são ignorados e passando de 11 dígitos o excesso é descartado.
 */
public class FiltroCPF extends DocumentFilter {

    public static final int TAMANHO_FORMATADO = 14;



    @Override public void insertString (FilterBypass fb, int posicao, String texto, AttributeSet atributos) throws BadLocationException {

        replace(fb, posicao, 0, texto, atributos);
    }



    @Override public void remove (FilterBypass fb, int posicao, int tamanho) throws BadLocationException {

        replace(fb, posicao, tamanho, "", null);
    }



    @Override public void replace (FilterBypass fb, int posicao, int tamanho, String texto, AttributeSet atributos) throws BadLocationException {

        int tamanhoAtual = fb.getDocument().getLength();

        String atual = fb.getDocument().getText(0, tamanhoAtual);

        String resultado = new StringBuilder(atual).replace(posicao, posicao + tamanho, texto == null ? "" : texto).toString();

        fb.replace(0, tamanhoAtual, formatar(resultado), atributos);
    }



    /** "12345678901" (ou qualquer texto com esses dígitos) vira "123.456.789-01"; parcial fica parcial. */
    public static String formatar (String texto) {

        String digitos = texto.replaceAll("\\D", "");

        if (digitos.length() > 11) digitos = digitos.substring(0, 11);

        StringBuilder formatado = new StringBuilder();

        for (int i = 0; i < digitos.length(); i++) {

            if (i == 3 || i == 6) formatado.append('.');

            if (i == 9) formatado.append('-');

            formatado.append(digitos.charAt(i));
        }

        return formatado.toString();
    }
}
