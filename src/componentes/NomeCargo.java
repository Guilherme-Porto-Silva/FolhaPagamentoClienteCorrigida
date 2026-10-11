package componentes;

public enum NomeCargo {

    Gerente,
    Coordenador,
    Professora;

    public static NomeCargo parse (String digitada) {

        String digitadaMinuscula = digitada.toLowerCase();

        if (digitadaMinuscula.contains("geren")) return Gerente;

        if (digitadaMinuscula.contains("coordena")) return Coordenador;

        if (digitadaMinuscula.contains("prof")) return Professora;

        throw new IllegalArgumentException('"' + digitada.strip() + "\" não é um dos cargos que o sistema se preparou para representar.");
    }
}