package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface InterfaceFuncionario extends Remote {

    boolean cadastrarFuncionario(String nome, String cpf, int cargoID) throws RemoteException;

    boolean demitirFuncionario(int funcionarioID, String justificativa) throws RemoteException;

    List<String> listarFuncionarios() throws RemoteException;

    boolean inserirCargo(String nome, double salario, int departamentoID) throws RemoteException;

    boolean inserirDepartamento(String nome) throws RemoteException;
}