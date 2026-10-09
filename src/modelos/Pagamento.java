package modelos;

import java.io.Serializable;

public class Pagamento implements Serializable {

    private double salarioReceptor, aliquotaUtilizada, impostoCobrado, pagamentoPratico;



    public Pagamento (double aliquota, double salario) {

        salarioReceptor = salario;

        aliquotaUtilizada = aliquota;

        double impostoCalculado = salario / aliquota;// Vai que ele pega uma versão antiga do valor, né?

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
}