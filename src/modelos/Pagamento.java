package modelos;

import java.io.Serializable;

public class Pagamento implements Serializable {

    private double salarioReceptor, aliquotaUtilizada, impostoCobrado, pagamentoPratico;

    public Pagamento (double aliquota, double salario) {

        salarioReceptor = salario;

        aliquotaUtilizada = aliquota;

        double impostoCalculado = salario * aliquota;

        impostoCobrado = impostoCalculado;

        pagamentoPratico = salario - impostoCalculado;
    }

    public double getSalario () {

        return salarioReceptor;
    }

    public double getAliquota () {

        return aliquotaUtilizada;
    }

    public double getImposto () {

        return impostoCobrado;
    }

    public double getPagamentoPratico () {

        return pagamentoPratico;
    }

    public Object get (int coluna) {

        switch (coluna) {

            case 7 -> {
                return salarioReceptor;
            }

            case 8 -> {
                return aliquotaUtilizada;
            }

            case 9 -> {
                return impostoCobrado;
            }

            case 10 -> {
                return pagamentoPratico;
            }

            default -> {
                return "Nossa tabela não tem " + coluna + " colunas.";
            }
        }
    }
}