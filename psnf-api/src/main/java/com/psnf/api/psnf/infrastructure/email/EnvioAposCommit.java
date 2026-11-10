package com.psnf.api.psnf.infrastructure.email;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Executa o envio em uma virtual thread, depois do commit da transação atual (se houver).
 * <ul>
 *   <li>depois do commit: se a transação falhar, nenhum e-mail sai com um link para um token que não existe;</li>
 *   <li>em segundo plano: a resposta não espera o SMTP, e "esqueci a senha" responde no mesmo tempo para
 *       e-mails cadastrados ou não (o tempo de resposta não revela quem tem conta).</li>
 * </ul>
 */
final class EnvioAposCommit {

    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private EnvioAposCommit() {
    }

    static void executar(Runnable envio) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    EXECUTOR.submit(envio);
                }
            });
        } else {
            EXECUTOR.submit(envio);
        }
    }
}
