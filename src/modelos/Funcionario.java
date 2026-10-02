package modelos;

import java.io.Serializable;

public class Funcionario implements Serializable {

    private int id;

    private String nome;



    public Funcionario (String nomeFuncionario) {

        nome = nomeFuncionario;
    }

    @Override public String toString () {

        return "Funcionario " + nome;
    }

    public int getId() {

        return id;
    }

    public String getNome() {

        return nome;
    }



    public Object get (int coluna) {

        switch (coluna) {

            case 1 -> {
                return id;
            }

            case 2 -> {
                return nome;
            }

            default -> {
                return "O índice da coluna precisa ser maior que 0.";
            }
        }
    }
}