package com.wallet.wallet_service.repository;

import com.wallet.wallet_service.entity.EntryType;
import com.wallet.wallet_service.entity.LedgerEntry;
import com.wallet.wallet_service.repository.projection.TransactionRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {


    @Query("""
            select sum(case when e.entryType = :credit then e.amount else -e.amount end)
            from LedgerEntry e
            where e.wallet.id = :walletId
            """)
    BigDecimal sumSigned(@Param("walletId") UUID walletId, @Param("credit") EntryType credit);

    default BigDecimal balanceOf(UUID walletId) {
        BigDecimal sum = sumSigned(walletId, EntryType.CREDIT);
        return sum == null ? BigDecimal.ZERO.setScale(2) : sum;
    }


    @Query(value = """
            select new com.wallet.wallet_service.repository.projection.TransactionRow(
                mine.id, j.type, mine.entryType, mine.amount, j.description,
                otherWallet.type, otherWallet.userId, mine.createdAt)
            from LedgerEntry mine
            join mine.journalEntry j
            join LedgerEntry other
                 on other.journalEntry = mine.journalEntry
                and other.entryType <> mine.entryType
            join other.wallet otherWallet
            where mine.wallet.id = :walletId
            order by mine.createdAt desc, mine.id desc
            """,
            countQuery = "select count(e) from LedgerEntry e where e.wallet.id = :walletId")
    Page<TransactionRow> findHistory(@Param("walletId") UUID walletId, Pageable pageable);

    @Query("""
            select new com.wallet.wallet_service.repository.projection.TransactionRow(
                mine.id, j.type, mine.entryType, mine.amount, j.description,
                otherWallet.type, otherWallet.userId, mine.createdAt)
            from LedgerEntry mine
            join mine.journalEntry j
            join LedgerEntry other
                 on other.journalEntry = mine.journalEntry
                and other.entryType <> mine.entryType
            join other.wallet otherWallet
            where mine.id = :id
              and mine.wallet.id = :walletId
            """)
    Optional<TransactionRow> findTransaction(@Param("id") UUID id, @Param("walletId") UUID walletId);
    @EntityGraph(attributePaths = "journalEntry")
    Optional<LedgerEntry> findByIdAndWalletId(UUID id, UUID walletId);
}
