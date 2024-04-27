package projetobancario;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SistemaBancario {
    public static void main(String[] args) {
        Banco banco = new Banco();
        Loja loja1 = new Loja(banco);
        Loja loja2 = new Loja(banco);

        for (int i = 0; i < 2; i++) {
            loja1.adicionarFuncionario(new Funcionario(loja1));
            loja2.adicionarFuncionario(new Funcionario(loja2));
        }

        for (int i = 0; i < 5; i++) {
            new Cliente(banco).start();
        }

        loja1.start();
        loja2.start();
    }
}

class Banco {
    private final Loja loja1;
    private final Loja loja2;

    public Banco() {
        this.loja1 = new Loja(this);
        this.loja2 = new Loja(this);
    }

    public Loja getLoja1() {
        return loja1;
    }

    public Loja getLoja2() {
        return loja2;
    }

    public synchronized void transferir(Conta origem, Conta destino, double valor) {
        origem.debitar(valor);
        destino.creditar(valor);
        System.out.println("Transferência de " + valor + " da conta " + origem.getId() + " para a conta " + destino.getId());
    }
}

class Conta {
    private final int id;
    private double saldo;

    public Conta(int id, double saldoInicial) {
        this.id = id;
        this.saldo = saldoInicial;
    }

    public synchronized void creditar(double valor) {
        saldo += valor;
    }

    public synchronized void debitar(double valor) {
        saldo -= valor;
    }

    public synchronized double getSaldo() {
        return saldo;
    }

    public int getId() {
        return id;
    }
}

class Cliente extends Thread {
    private static final double[] COMPRAS = {100.0, 200.0};
    private final Banco banco;
    private final Conta conta;

    public Cliente(Banco banco) {
        this.banco = banco;
        this.conta = new Conta(ThreadLocalRandom.current().nextInt(1000, 9999), 1000.0);
    }

    @Override
    public void run() {
        while (conta.getSaldo() > 0) {
            double valorCompra = COMPRAS[ThreadLocalRandom.current().nextInt(0, COMPRAS.length)];
            Loja loja = ThreadLocalRandom.current().nextBoolean() ? banco.getLoja1() : banco.getLoja2();
            banco.transferir(conta, loja.getConta(), valorCompra);
        }
        System.out.println("Cliente " + conta.getId() + " terminou suas compras.");
    }
}

class Loja extends Thread {
    private final Banco banco;
    private final Conta conta;
    private final List<Funcionario> funcionarios;

    public Loja(Banco banco) {
        this.banco = banco;
        this.conta = new Conta(ThreadLocalRandom.current().nextInt(10000, 99999), 0);
        this.funcionarios = new ArrayList<>();
    }

    public void adicionarFuncionario(Funcionario funcionario) {
        funcionarios.add(funcionario);
    }

    public Conta getConta() {
        return conta;
    }

    @Override
    public void run() {
        while (true) {
            double totalSalario = funcionarios.size() * 1400.0;
            if (conta.getSaldo() >= totalSalario) {
                for (Funcionario funcionario : funcionarios) {
                    banco.transferir(conta, funcionario.getContaSalario(), 1400.0);
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Funcionario extends Thread {
    private final Loja loja;
    private final Conta contaSalario;
    private final Conta contaInvestimento;

    public Funcionario(Loja loja) {
        this.loja = loja;
        this.contaSalario = new Conta(ThreadLocalRandom.current().nextInt(100000, 999999), 0);
        this.contaInvestimento = new Conta(ThreadLocalRandom.current().nextInt(1000000, 9999999), 0);
    }

    public Conta getContaSalario() {
        return contaSalario;
    }

    @Override
    public void run() {
        while (true) {
            while (contaSalario.getSaldo() == 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            double valorInvestimento = contaSalario.getSaldo() * 0.2;
            loja.getConta().debitar(valorInvestimento);
            contaInvestimento.creditar(valorInvestimento);
            System.out.println("Funcionário recebeu o salário e investiu " + valorInvestimento);
        }
    }
}
