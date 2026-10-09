package modelos;

import componentes.Departamentos;
import java.io.Serializable;

public class Departamento implements Serializable {

    private int id;

    private String nome;

    public Departamento (Departamentos departamento) {

        nome = departamento.toString();

        switch (departamento) {

            case Departamentos.Administrativo -> {

                id = 1;
            }

            case Departamentos.Operacional -> {

                id = 3;
            }
        }
    }
}