package pl.edu.agh.to.kolektyw_glazurniczy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import pl.edu.agh.to.kolektyw_glazurniczy.dto.UserRegistrationDTO;
import pl.edu.agh.to.kolektyw_glazurniczy.gui.controller.ApplicationStateManager;
import pl.edu.agh.to.kolektyw_glazurniczy.gui.controller.LoginController;
import pl.edu.agh.to.kolektyw_glazurniczy.model.*;
import pl.edu.agh.to.kolektyw_glazurniczy.repository.ReviewRepository;
import pl.edu.agh.to.kolektyw_glazurniczy.repository.UserRepository;
import pl.edu.agh.to.kolektyw_glazurniczy.service.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.IntStream;

@ConditionalOnProperty(name = "load.test.data", havingValue = "true")
@Component
public class DataLoader implements ApplicationRunner {

    private final BookService bookService;
    private final AuthService authService;
    private final BorrowingService borrowingService;
    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final ApplicationStateManager applicationStateManager;
    private final Logger logger;


    public DataLoader(BookService bookService,
                      AuthService authService,
                      BorrowingService borrowingService,
                      ReviewRepository reviewRepository,
                      UserService userService,
                      NotificationService notificationService,
                      UserRepository userRepository,
                      ApplicationStateManager applicationStateManager) {
        this.bookService = bookService;
        this.authService = authService;
        this.borrowingService = borrowingService;
        this.reviewRepository = reviewRepository;
        this.userService = userService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.applicationStateManager = applicationStateManager;
        this.logger = LoggerFactory.getLogger(DataLoader.class);
    }


    public void run(ApplicationArguments args) throws IOException {

        Map<String, Author> authorMap = new HashMap<>();
        List<Book> bookList = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        File jsonFile = new File("src/main/resources/data/books.json");
        JsonNode booksNode = objectMapper.readTree(jsonFile).get("books");

        for (JsonNode bookNode : booksNode) {
            String title = bookNode.get("title").asText();
            boolean bookExists = bookList.stream().anyMatch(book -> book.getTitle().equals(title));
            if (bookExists) {
                continue;
            }

            String isbn = bookNode.get("isbn").asText();
            String category = bookNode.get("category").asText();
            int pagesCount = bookNode.get("pagesCount").asInt();
            String language = bookNode.get("language").asText();
            String description = bookNode.get("description").asText();
            String imageUrl = bookNode.get("imageUrl").asText();

            // Extract author details directly since there is only one author
            String authorName = bookNode.get("authors").get(0).get("name").asText();
            LocalDate birthDate = LocalDate.parse(bookNode.get("authors").get(0).get("birthDate").asText());

            Author author;
            Author existingAuthor = authorMap.get(authorName);
            if (existingAuthor != null && existingAuthor.getBirthDate().equals(birthDate)) {
                author = existingAuthor;
            } else {
                author = new Author(authorName, birthDate);
                authorMap.put(authorName, author);
            }

            Book book = new Book(
                    title,
                    author,
                    BookCategory.valueOf(category),
                    pagesCount,
                    Language.valueOf(language),
                    isbn,
                    description,
                    imageUrl
            );

            bookList.add(book);
        }

        logger.info("Saving books to a database");
        Random random = new Random(2137420);

        for (Book book : bookList) {
            try {
                int total = random.nextInt(2, 20);
                BookStatistics bookStatistics = new BookStatistics(book, LocalDate.now(), total, random.nextInt(0, total), 0);
                book.setBookStatistics(bookStatistics);
                bookService.save(book);
                // bookStatisticsRepository.save(bookStatistics);

            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage());
            }
        }
        logger.info("Creating new users");
        User admin = authService.register(new UserRegistrationDTO("Admin", "admin@gmail.com", "12345678"));
        admin.setUserRole(UserRole.ADMIN);
        admin = userRepository.save(admin);

        List<User> users = List.of(
                authService.register(new UserRegistrationDTO("Adam", "adam@gmail.com", "12345678")),
                authService.register(new UserRegistrationDTO("Pawel", "pawel@gmail.com", "12345678")),
                authService.register(new UserRegistrationDTO("Grzegorz", "grzegorz@gmail.com", "12345678")),
                authService.register(new UserRegistrationDTO("Ania", "ania@gmail.com", "12345678")),
                authService.register(new UserRegistrationDTO("Karolina", "karolina@gmail.com", "12345678")),
                authService.register(new UserRegistrationDTO("Wojtek", "wojtek@gmail.com", "12345678"))
        );
        IntStream.range(0, 10).forEach(i -> authService.register(new UserRegistrationDTO("Wojtek" + i, "wojtek" + i + "@gmail.com", "12345678")));
        List<User> workers = List.of(
                authService.register(new UserRegistrationDTO("Kamil Bibliotekarz", "kamil@gmail.com", "12345678")),
                authService.register(new UserRegistrationDTO("Martyna Bibliotekarka", "martyna@gmail.com", "12345678"))
        );
        for (User worker : workers) {
            worker.setUserRole(UserRole.WORKER);
            userRepository.save(worker);
        }
        logger.info("Creating borrowings");
        for (User user : users) {
            int borrowCount = random.nextInt(1, 7);
            int returnCount = random.nextInt(0, borrowCount);
            List<Borrowing> borrowings = new ArrayList<>();
            for (int i = 0; i < borrowCount; i++) {
                Book randomBook = bookList.get(random.nextInt(bookList.size()));
                borrowings.add(borrowingService.borrowBook(randomBook, user));
            }
            for (int i = 0; i < returnCount; i++) {
                if (borrowings.get(i) != null)
                    borrowingService.returnBook(borrowings.get(i));
            }
        }


        User tomek = authService.register(new UserRegistrationDTO("Tomek", "tomek@gmail.com", "12345678"));

        Review review = new Review(tomek, bookList.get(0), "Szczera opinia", "Wracam po wielu latach do tych opowiadań. Nic się nie zestarzały. Nadal są rewelacyjne i stanowią jedne z perełek literatury fantasy.", (short) 4);


        reviewRepository.save(review);

        logger.info("Creating borrowings for Tomek");

        authService.login("tomek@gmail.com", "12345678");

        borrowingService.borrowBook(bookList.get(0), tomek);
        borrowingService.borrowBook(bookList.get(1), tomek);
        borrowingService.borrowBook(bookList.get(2), tomek);


        authService.login("admin@gmail.com", "12345678");

        applicationStateManager.userProperty().set(admin);

        createNotifications();

    }


    private void createNotifications() {
        List<User> allUsers = userService.getAllUsers();

        String libraryClosedMessage = """
                Szanownik czytelnicy,
                                
                z przykrością informujemy że w dniu 12.04 biblioteka będzie zamknięta.
                Powodem jest coroczny konkurs czytania po ciemku który się w tym dniu odbędzie.
                                
                                
                Przepraszamy za niedogodności
                                
                Administracja biblioteki
                """;

        for (User user : allUsers) {
            Notification notification = new Notification("Biblioteka Zamknięta", libraryClosedMessage,
                    Notification.NotificationDestination.IN_APP, Notification.NotificationType.IMPORTANT);
            notificationService.sendSystemNotification(notification, user);
        }

        String newBookMessage = """
                Szanownik czytelnicy,
                                
                W dniu 12.04 w bibliotece pojawi się nowa książka Fortnite autorstwa Tencent.
                Jest to światowy besteller
                                
                Zapraszamy
                                
                Administracja biblioteki
                """;

        for (User user : allUsers) {
            Notification notification = new Notification("Nowa książka - Fortnite", newBookMessage,
                    Notification.NotificationDestination.IN_APP, Notification.NotificationType.NEW_BOOK);
            notificationService.sendSystemNotification(notification, user);
        }

        String message = """
                Szanowny Panie,
                książka #%d nie oddana a termin minął 2 lata temu
                 
                  
                Proszę o kontakt
                
                Biblioteka
                """;


        User finalAdmin = applicationStateManager.getUser();


        IntStream.range(0, 10)
                .mapToObj(i -> new Notification("Proszę oddać książki!!!" + i, message.formatted(i),
                        Notification.NotificationDestination.IN_APP, Notification.NotificationType.BOOK_OVERDUE))
                .forEach(notification -> notificationService.sendNotification(notification, finalAdmin, finalAdmin));

    }
}