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

        try {
            String serverIP = System.getenv("RMI_SERVER_HOST");

            String numeroServicoFuncionario = System.getenv("RMI_NUMERO_SERVICO_FUNCIONARIO");

            String numeroServicoPagamento = System.getenv("RMI_NUMERO_SERVICO_PAGAMENTO");

            var port = Integer.parseInt(System.getenv("RMI_SERVER_PORT"));

            Registry conexao = LocateRegistry.getRegistry(serverIP, 1500);

            servicoFuncionario = (InterfaceFuncionario) conexao.lookup(numeroServicoFuncionario);

            servicoPagamento = (InterfacePagamento) conexao.lookup(numeroServicoPagamento);
        }

        catch (NullPointerException variaveisAmbienteNaoDeclaradas) {

            try {
                Registry conexao = LocateRegistry.getRegistry("172.16.0.19", 1500);

                servicoFuncionario = (InterfaceFuncionario) conexao.lookup("numeroServicoFuncionario");

                servicoPagamento = (InterfacePagamento) conexao.lookup("numeroServicoPagamento");
            }

            catch (RemoteException e) {

                throw new RuntimeException("Erro de conexão. - " + e.getMessage());
            }

            catch (NotBoundException e) {

                throw new RuntimeException("Erro na chama do serviço. - " + e.getMessage());
            }
        }

        catch (RemoteException e) {

            throw new RuntimeException("Erro de conexão. - " + e.getMessage());
        }

        catch (NotBoundException e) {

            throw new RuntimeException("Erro na chama do serviço. - " + e.getMessage());
        }
    }
}
