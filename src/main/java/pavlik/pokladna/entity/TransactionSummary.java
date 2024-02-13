package pavlik.pokladna.entity;

/**
 * Třída TransactionSummary představuje souhrn transakce,
 * včetně ID transakce a zůstatku po provedení transakce.
 * Pro predani dat pro vytvoreni grafu v chartAreaBalanceBefore.js
 */
public class TransactionSummary {

    private int idTransaction;
    private int balanceAfter;

    public TransactionSummary() {

    }

    public int getIdTransaction() {
        return idTransaction;
    }

    public void setIdTransaction(int idTransaction) {
        this.idTransaction = idTransaction;
    }

    public int getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(int balanceAfter) {
        this.balanceAfter = balanceAfter;
    }
}
