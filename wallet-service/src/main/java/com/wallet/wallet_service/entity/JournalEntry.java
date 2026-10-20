package com.wallet.wallet_service.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "journal_entries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    private JournalType type;

    @Column(updatable = false, length = 255)
    private String description;

    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.PERSIST)
    private List<LedgerEntry> entries = new ArrayList<>();

    public static JournalEntry create(JournalType type, String description, UUID createdBy) {
        JournalEntry journal = new JournalEntry();
        journal.type = type;
        journal.description = description;
        journal.createdBy = createdBy;
        return journal;
    }
    public static JournalEntry move(JournalType type, String description, UUID createdBy,
                                    Wallet from, Wallet to, BigDecimal amount) {
        return create(type, description, createdBy)
                .debit(from, amount)
                .credit(to, amount);
    }

    public JournalEntry debit(Wallet wallet, BigDecimal amount) {
        entries.add(new LedgerEntry(this, wallet, EntryType.DEBIT, amount));
        return this;
    }

    public JournalEntry credit(Wallet wallet, BigDecimal amount) {
        entries.add(new LedgerEntry(this, wallet, EntryType.CREDIT, amount));
        return this;
    }

    public void assertBalanced() {
        if (entries.size() < 2) {
            throw new IllegalStateException("A journal entry needs at least two lines");
        }
        BigDecimal debits = sum(EntryType.DEBIT);
        BigDecimal credits = sum(EntryType.CREDIT);
        if (debits.compareTo(credits) != 0) {
            throw new IllegalStateException("Unbalanced journal: debits=" + debits + " credits=" + credits);
        }
    }

    public List<LedgerEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    private BigDecimal sum(EntryType type) {
        return entries.stream()
                .filter(e -> e.getEntryType() == type)
                .map(LedgerEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
