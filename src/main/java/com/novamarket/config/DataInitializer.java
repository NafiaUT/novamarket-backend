package com.novamarket.config;

import com.novamarket.model.*;
import com.novamarket.repository.CategoryRepository;
import com.novamarket.repository.ProductRepository;
import com.novamarket.repository.RoleRepository;
import com.novamarket.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            log.info("--> Initialisation des roles...");
            Role roleCustomer = roleRepository.save(new Role(RoleType.ROLE_CUSTOMER));
            Role roleAdmin = roleRepository.save(new Role(RoleType.ROLE_ADMIN));

            log.info("--> Initialisation des utilisateurs de demonstration avec BCrypt...");
            User admin = new User("admin@novamarket.com", passwordEncoder.encode("Admin123!"), "Alexandre", "Dupont");
            admin.setRoles(Set.of(roleCustomer, roleAdmin));
            userRepository.save(admin);

            User customer = new User("client@novamarket.com", passwordEncoder.encode("Client123!"), "Sophie", "Martin");
            customer.setRoles(Set.of(roleCustomer));
            userRepository.save(customer);

            log.info("--> Initialisation des categories de demonstration...");
            Category laptops = categoryRepository.save(new Category(
                    "Ordinateurs Portables",
                    "ordinateurs-portables",
                    "Ultra-portables performants et stations de travail",
                    "https://images.unsplash.com/photo-1517336714731-489689fd1ca8"
            ));

            Category smartphones = categoryRepository.save(new Category(
                    "Smartphones & Accessoires",
                    "smartphones",
                    "Derniers smartphones haut de gamme",
                    "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9"
            ));

            Category audio = categoryRepository.save(new Category(
                    "Audio & Casques",
                    "audio",
                    "Casques a reduction de bruit et enceintes connectees",
                    "https://images.unsplash.com/photo-1505740420928-5e560c06d30e"
            ));

            log.info("--> Initialisation du catalogue de produits de demonstration...");
            productRepository.saveAll(List.of(
                    new Product("LAP-MAC-M3", "MacBook Pro 16\" M3 Max", "Puce Apple M3 Max, 36 Go de memoire unifiee, SSD 1 To, Ecran Liquid Retina XDR", new BigDecimal("3499.00"), 15, "https://images.unsplash.com/photo-1517336714731-489689fd1ca8", laptops),
                    new Product("LAP-DELL-XPS15", "Dell XPS 15 OLED", "Intel Core i9-13900H, 32 Go RAM, SSD 1 To, Ecran 3.5K OLED Tactile", new BigDecimal("2399.00"), 20, "https://images.unsplash.com/photo-1593642632823-8f785ba67e45", laptops),
                    new Product("PHN-IPHONE15P", "iPhone 15 Pro Max 256 Go", "Design en titane, puce A17 Pro, bouton Action personnalisable, teleobjectif 5x", new BigDecimal("1479.00"), 40, "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9", smartphones),
                    new Product("PHN-PIXEL8P", "Google Pixel 8 Pro", "Google Tensor G3, ecran Super Actua 120Hz, photo IA professionnelle", new BigDecimal("1099.00"), 25, "https://images.unsplash.com/photo-1598327105666-5b89351aff97", smartphones),
                    new Product("AUD-SONY-XM5", "Sony WH-1000XM5 Noir", "Reduction de bruit active leader du marche, son Hi-Res Audio, 30h d'autonomie", new BigDecimal("379.00"), 50, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e", audio),
                    new Product("AUD-AIRPODS-P2", "Apple AirPods Pro (2e generation)", "Puce H2, reduction active du bruit 2x plus efficace, boitier MagSafe USB-C", new BigDecimal("279.00"), 65, "https://images.unsplash.com/photo-1600294037681-c80b4cb5b434", audio)
            ));

            log.info("--> NovaMarket : Catalogue et utilisateurs initialises avec succes !");
        }
    }
}