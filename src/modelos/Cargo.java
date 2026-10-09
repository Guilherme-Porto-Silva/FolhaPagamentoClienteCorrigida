package modelos;

import componentes.Departamentos;
import componentes.NomeCargo;
import java.io.Serializable;

public class Cargo implements Serializable {

    private int id;
    
    private double salario;
    
    private Departamento departamento;
    
    private String horarioChegada, horarioSaida, nome;



    public Cargo (int id, String nome) {

        this.id = id;

        this.nome = nome;
    }



    public Cargo (NomeCargo nomeCargo) {

        nome = nomeCargo.toString();

        switch (nomeCargo) {

            case NomeCargo.Gerente -> {

                id = 1;

                salario = 3660.00;

                horarioChegada = "10:00";

                horarioSaida = "18:00";

                departamento = new Departamento(Departamentos.Administrativo);
            }

            case NomeCargo.Coordenador -> {

                id = 2;

                salario = 1830.00;

                horarioChegada = "09:00";

                horarioSaida = "18:00";

                departamento = new Departamento(Departamentos.Administrativo);
            }

            case NomeCargo.Professora -> {

                id = 3;

                salario = 1189.50;

                horarioChegada = "07:00";

                horarioSaida = "18:00";

                departamento = new Departamento(Departamentos.Operacional);
            }
        }
    }



    protected double getSalario() {
        
        return salario;
    }

    protected Departamento getDepartamento() {
        
        return departamento;
    }

    protected String getHorarioChegada() {
        
        return horarioChegada;
    }

    protected String getHorarioSaida() {
        
        return horarioSaida;
    }

    public String getNome() {
        return nome;
    }
}