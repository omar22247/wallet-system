package com.wallet.wallet_service.bootstrap;

import com.wallet.wallet_service.entity.Wallet;
import com.wallet.wallet_service.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;


@Component
public class SystemWalletInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SystemWalletInitializer.class);

    private final WalletRepository walletRepository;
    private final String currency;

    public SystemWalletInitializer(WalletRepository walletRepository,
                                   @Value("${wallet.currency}") String currency) {
        this.walletRepository = walletRepository;
        this.currency = currency;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (walletRepository.findBySystemCode(SystemWallets.FUNDING).isPresent()) {
            return;
        }
        try {
            walletRepository.saveAndFlush(Wallet.system(SystemWallets.FUNDING, currency));
            log.info("Created system wallet {}", SystemWallets.FUNDING);
        } catch (DataIntegrityViolationException alreadyCreated) {
            log.info("System wallet {} was created by another instance", SystemWallets.FUNDING);
        }
    }
}
