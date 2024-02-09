package pl.edu.agh.to.kolektyw_glazurniczy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Author;
import pl.edu.agh.to.kolektyw_glazurniczy.repository.AuthorRepository;

import java.util.List;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;

    Logger logger = LoggerFactory.getLogger(AuthorService.class);

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }


    public Author saveAuthor(Author author) {
        try {
            logger.info("saving new author {}", author);
            return authorRepository.save(author);
        } catch (DataIntegrityViolationException exception) {
            logger.error(exception.getMessage());
            throw exception;
        }
    }

    public List<Author> getAllAuthors() {
        logger.debug("getAllAuthors()");
        return authorRepository.findAll();
    }
}
