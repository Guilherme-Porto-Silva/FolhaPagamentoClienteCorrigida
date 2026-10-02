package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfacePagamento extends Remote {

    double consultarPagamento (int funcionarioID) throws RemoteException;

    void calcularEfetuarPagamento(int funcionarioID, String mesAno) throws RemoteException;
}