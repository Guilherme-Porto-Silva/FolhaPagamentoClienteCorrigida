package view;

import interfaces.InterfaceFuncionario;
import interfaces.InterfacePagamento;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Cliente {

    InterfaceFuncionario servicoFuncionario;

    InterfacePagamento servicoPagamento;

    public Cliente () {

        String serverIP = "172.16.0.19";

        try{
            Registry conexao = LocateRegistry.getRegistry(serverIP,1500);

            InterfaceFuncionario servicoFuncionario = (InterfaceFuncionario) conexao.lookup("numeroServicoFuncionario");

            InterfacePagamento servicoPagamento = (InterfacePagamento) conexao.lookup("numeroServicoPagamento");
        }

        catch (RemoteException e) {

            throw new RuntimeException("Erro de conexão. - " + e.getMessage());
        }

        catch (NotBoundException e) {

            throw new RuntimeException("Erro na chama do serviço. - " + e.getMessage());
        }
    }
}
