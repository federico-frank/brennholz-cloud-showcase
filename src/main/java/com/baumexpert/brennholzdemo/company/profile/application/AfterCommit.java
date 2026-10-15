package com.baumexpert.brennholzdemo.company.profile.application;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Führt Seiteneffekte (Nachrichten, Löschen im Speicher) erst nach dem Commit aus –
 * sonst könnte ein Empfänger den alten Stand lesen oder ein Rollback Dateien verwaisen lassen.
 * Ohne laufende Transaktion (z. B. in Unit-Tests) sofort.
 */
final class AfterCommit {

    private AfterCommit() {
    }

    static void run(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}
